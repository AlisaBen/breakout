package com.neo.sk.breakout.core

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.{Behaviors, StashBuffer, TimerScheduler}
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

  private final val classify= 100 // 10s同步一次状态

  trait Command

  case object BeginGame extends Command

  case object GameLoop extends Command
  case class WebSocketMsg(uid: String, tankId: Int, req: BreakoutGameEvent.UserActionEvent) extends Command with RoomManager.Command

  case class GameBattleRecord(scores:List[Score]) extends Command

  final case class ChildDead[U](name:String,childRef:ActorRef[U]) extends Command

  case object GameLoopKey

  case object ActorStop extends Command


  def create(roomId:Int,nameA:(String,Long),nameB:(String,Long),playerMap:mutable.HashMap[Long,ActorRef[UserActor.Command]]) = {
    Behaviors.setup[Command]{
      ctx =>
        implicit val stashBuffer = StashBuffer[Command](Int.MaxValue)
        Behaviors.withTimers[Command]{implicit timer =>
          implicit val sendBuffer = new MiddleBufferInJvm(81920)
          val gameContainer = GameContainerServerImpl(
            AppSettings.breakoutGameConfig,
            log,
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
                    nameA:(String,Long),
                    nameB:(String,Long),
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
          gameContainer.generateRacketAndBall(nameA._1,nameB._1,nameA._2,nameB._2,subscribesMap)
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

        case GameBattleRecord(ls) =>
//          log.debug(s"${ctx.self.path} 插入战绩")
          roomManager !RoomManager.GameOver(roomId)
          ls.map{r =>
            AccountDAO.updateUserInfo(r.n,r.score > ls.map(_.score).max).map{t =>
//              log.debug(s"${ctx.self.path}更新用户信息")
            }.recover{
              case e:Exception =>
                log.debug(s"${ctx.self.path} 更新用户信息失败：${e}")
            }
          }
          AccountDAO.insertBattleRecord(
            SlickTables.rBattleRecord(-1l,System.currentTimeMillis(),ls(0).n,ls(1).n,ls(0).score,ls(1).score))
            .map{r =>
              log.debug(s"${ctx.self.path} 插入战绩")

            }.recover{
            case e:Exception =>
              log.debug(s"${ctx.self.path}插入战绩失败：${e}")
          }
          timer.startSingleTimer("ActorStop",ActorStop,3.minute)
          Behaviors.same

        case ChildDead(name,childRef) =>
          ctx.unwatch(childRef)
          Behaviors.same

        case ActorStop =>

          Behaviors.stopped

        case unknownMsg =>
          log.debug(s"${ctx.self.path} recv an unknow msg=${msg}")
          Behaviors.same
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
