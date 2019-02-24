package com.neo.sk.breakout.core

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.{ActorContext, Behaviors, StashBuffer, TimerScheduler}
import com.neo.sk.breakout.common.AppSettings
import com.neo.sk.breakout.core.control.GameContainerServerImpl
import com.neo.sk.breakout.shared.protocol.BreakoutGameEvent
import org.seekloud.byteobject.MiddleBufferInJvm
import com.neo.sk.breakout.Boot.userManager
import com.neo.sk.breakout.models.DAO.AccountDAO
import com.neo.sk.breakout.models.SlickTables
import com.neo.sk.breakout.shared.model.{Point, Score}
import com.neo.sk.breakout.Boot.roomManager

import scala.language.implicitConversions
import org.seekloud.byteobject.ByteObject._
import org.slf4j.LoggerFactory
import com.neo.sk.breakout.Boot.{executor, scheduler, timeout, userManager}
import com.neo.sk.breakout.core.RoomManager.GameBattleRecord
import com.neo.sk.breakout.shared.`object`.Racket
import com.neo.sk.breakout.shared.model
import com.neo.sk.breakout.shared.ptcl.GameHallProtocol.GameModelReq

import concurrent.duration._
import scala.collection.mutable

/**
  * created by benyafang on 2019/2/3 15:52
  * A3
  * 1.开始游戏
  * 2.websocket消息
  * 3.离开房间
  * 4.游戏结束
  * 5.游戏循环
  * 6.广播&定向分发
  * */
object RoomActor {
  private val log = LoggerFactory.getLogger(this.getClass)

  private final val classify= 50 // 5s同步一次状态

  trait Command

  case object BeginGame extends Command

  case object GameLoop extends Command
  case class WebSocketMsg(uid: String, tankId: Int, req: BreakoutGameEvent.UserActionEvent) extends Command with RoomManager.Command

//  case class GameBattleRecord(scores:List[Score]) extends Command with RoomManager.Command

  final case class ChildDead[U](name:String,childRef:ActorRef[U]) extends Command

  case class GameOver(uid:Long) extends Command

  case object GameLoopKey

  case object ActorStop extends Command

  private case object BehaviorChangeKey extends Command

  case class TimeOut(str: String) extends Command

//  private case class UpdateUserInfo(r:model.Score,ls:List[model.Score],isStop:Boolean) extends Command with RoomManager.Command


  final case class SwitchBehavior(
                                   name: String,
                                   behavior: Behavior[Command],
                                   durationOpt: Option[FiniteDuration] = None,
                                   timeOut: TimeOut = TimeOut("busy time error")
                                 ) extends Command

  private[this] def switchBehavior(ctx: ActorContext[Command],
                                   behaviorName: String, behavior: Behavior[Command], durationOpt: Option[FiniteDuration] = None,timeOut: TimeOut  = TimeOut("busy time error"))
                                  (implicit stashBuffer: StashBuffer[Command],
                                   timer:TimerScheduler[Command]) = {
    log.debug(s"${ctx.self.path} becomes $behaviorName behavior.")
    timer.cancel(BehaviorChangeKey)
    durationOpt.foreach(timer.startSingleTimer(BehaviorChangeKey,timeOut,_))
    stashBuffer.unstashAll(ctx,behavior)
  }


  def create(roomId:Int,nameA:GameModelReq,nameB:GameModelReq,playerMap:mutable.HashMap[Long,ActorRef[UserActor.Command]]) = {
    Behaviors.setup[Command]{
      ctx =>
        log.debug(s"RoomActor-${roomId} start...")
        implicit val stashBuffer = StashBuffer[Command](Int.MaxValue)
        Behaviors.withTimers[Command]{implicit timer =>
          implicit val sendBuffer = new MiddleBufferInJvm(81920)
          val gameContainer = GameContainerServerImpl(
            AppSettings.breakoutGameConfig,
            log,
            timer,
            roomId,
            List(nameA,nameB),
            ctx.self,
            dispatch(playerMap),
            dispatchTo(playerMap)
          )
//          val map = mutable.HashMap[String,UserActor.Command]()
          timer.startPeriodicTimer(GameLoopKey, GameLoop, (gameContainer.config.frameDuration / gameContainer.config.playRate).millis)
          idle(roomId,nameA,nameB,playerMap,gameContainer,0)
//          Behaviors.same

        }

    }
}

  private def idle(
                  roomId:Int,
                    nameA:GameModelReq,
                    nameB:GameModelReq,
                    subscribesMap:mutable.HashMap[Long,ActorRef[UserActor.Command]],
                    gameContainer:GameContainerServerImpl,
                    tickCount:Long)(
    implicit stashBuffer:StashBuffer[Command],
    sendBuffer:MiddleBufferInJvm,
    timer:TimerScheduler[Command]
  ):Behavior[Command] ={
    Behaviors.receive[Command]{(ctx,msg) =>
      msg match{
        case BeginGame =>
//          subscribesMap.values.foreach(actor => actor !UserActor.JoinRoomSuccess(racketB,config.getGameConfigImpl(),roomActorRef))
//          playerMap(nameB) ! UserActor.JoinRoomSuccess(racketB,config.getGameConfigImpl(),roomActorRef)
          gameContainer.generateRacketAndBall(nameA,nameB,subscribesMap)
          Behaviors.same

        case GameLoop =>
          gameContainer.update()
          val state = gameContainer.getGameContainerState()
          if(tickCount % classify == 0){
            dispatch(subscribesMap)(BreakoutGameEvent.SyncGameState(state))
          }
          idle(roomId,nameA,nameB,subscribesMap,gameContainer,tickCount + 1)

        case WebSocketMsg(uid, tankId, req) =>
          gameContainer.receiveUserAction(req)
          Behaviors.same

        case GameOver(uid) =>
          val gameOverEvent = BreakoutGameEvent.GameOver(gameContainer.racketMap.values.map(t => Score(t.racketId,t.name,t.damageStatistics)).toList)
          timer.cancel(RoomActor.GameLoopKey)
          val userId = if(nameA.uid == uid) nameB.uid else nameA.uid
          dispatchTo(subscribesMap)(userId,gameOverEvent)
          roomManager !RoomManager.GameOver(roomId)
          roomManager ! GameBattleRecord(gameContainer.racketMap.values.map(t => Score(t.racketId,t.name,t.damageStatistics)).toList)
          ctx.self ! ActorStop
          Behaviors.same

        case ChildDead(name,childRef) =>
          ctx.unwatch(childRef)
          Behaviors.same

        case ActorStop =>
          log.debug(s"${ctx.self.path} is over")
          Behaviors.stopped

        case unknownMsg =>
          log.debug(s"${ctx.self.path} recv an unknow msg=${msg}")
          Behaviors.same
      }
    }
  }

  private def busy(roomId:Int,
                   nameA:GameModelReq,
                   nameB:GameModelReq,
                   subscribesMap:mutable.HashMap[Long,ActorRef[UserActor.Command]],
                   gameContainer:GameContainerServerImpl,
                   tickCount:Long)
                  (
                    implicit stashBuffer:StashBuffer[Command],
                    sendBuffer:MiddleBufferInJvm,
                    timer:TimerScheduler[Command]
                  ): Behavior[Command] = {
    Behaviors.receive[Command] { (ctx, msg) =>
      msg match {
        case SwitchBehavior(name, behavior, durationOpt, timeOut) =>
          log.debug(s"${ctx.self.path} recv a SwitchBehavior Msg=${name}")
          switchBehavior(ctx, name, behavior, durationOpt, timeOut)

        case TimeOut(m) =>
          log.debug(s"${ctx.self.path} is time out when busy,msg=${m}")
          switchBehavior(ctx, "idle", idle(roomId,nameA,nameB,subscribesMap,gameContainer:GameContainerServerImpl,tickCount))

        case unknownMsg =>
          stashBuffer.stash(unknownMsg)
          Behavior.same
      }
    }
  }

  import scala.language.implicitConversions
  import org.seekloud.byteobject.ByteObject._

  def dispatch(subscribes:mutable.HashMap[Long,ActorRef[UserActor.Command]])(msg:BreakoutGameEvent.WsMsgServer)(implicit sendBuffer:MiddleBufferInJvm) = {
    subscribes.values.foreach(_ ! UserActor.DispatchMsg(BreakoutGameEvent.Wrap(msg.asInstanceOf[BreakoutGameEvent.WsMsgServer].fillMiddleBuffer(sendBuffer).result(),msg.isInstanceOf[BreakoutGameEvent.GameOver])))
  }

  def dispatchTo(subscribes:mutable.HashMap[Long,ActorRef[UserActor.Command]])(uid:Long,msg:BreakoutGameEvent.WsMsgServer)(implicit sendBuffer:MiddleBufferInJvm) = {
    subscribes.get(uid).foreach(_ ! UserActor.DispatchMsg(BreakoutGameEvent.Wrap(msg.asInstanceOf[BreakoutGameEvent.WsMsgServer].fillMiddleBuffer(sendBuffer).result(),msg.isInstanceOf[BreakoutGameEvent.GameOver])))
  }










}
