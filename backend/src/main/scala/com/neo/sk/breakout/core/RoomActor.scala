package com.neo.sk.breakout.core

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.{Behaviors, StashBuffer, TimerScheduler}
import com.neo.sk.breakout.common.AppSettings
import com.neo.sk.breakout.core.control.GameContainerServerImpl
import com.neo.sk.breakout.shared.protocol.BreakoutGameEvent
import org.seekloud.byteobject.MiddleBufferInJvm
import com.neo.sk.breakout.Boot.userManager
import com.neo.sk.breakout.shared.model.Point

import scala.language.implicitConversions
import org.seekloud.byteobject.ByteObject._
import org.slf4j.LoggerFactory

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

  final case class ChildDead[U](name:String,childRef:ActorRef[U]) extends Command

  case object GameLoopKey

  def create(nameA:String,nameB:String,playerMap:mutable.HashMap[String,ActorRef[UserActor.Command]]) = {
    Behaviors.setup[Command]{
      ctx =>
        implicit val stashBuffer = StashBuffer[Command](Int.MaxValue)
        Behaviors.withTimers[Command]{implicit timer =>
          implicit val sendBuffer = new MiddleBufferInJvm(81920)
          val gameContainer = GameContainerServerImpl(AppSettings.breakoutGameConfig,log, dispatch(playerMap), dispatchTo(playerMap))
//          val map = mutable.HashMap[String,UserActor.Command]()
          timer.startPeriodicTimer(GameLoopKey, GameLoop, (gameContainer.config.frameDuration / gameContainer.config.playRate).millis)
          idle(nameA,nameB,playerMap,gameContainer,0)
//          Behaviors.same

        }

    }
}

  private def idle(
                    nameA:String,
                    nameB:String,
                    subscribesMap:mutable.HashMap[String,ActorRef[UserActor.Command]],
                    gameContainer:GameContainerServerImpl,
                    tickCount:Long)(
    implicit stashBuffer:StashBuffer[Command],
    sendBuffer:MiddleBufferInJvm,
    timer:TimerScheduler[Command]
  ):Behavior[Command] ={
    Behaviors.receive[Command]{(ctx,msg) =>
      msg match{
        case BeginGame =>
          gameContainer.generateRacketAndBall(nameA,nameB)
          val state = gameContainer.getGameContainerState()
          dispatch(subscribesMap)(BreakoutGameEvent.SyncGameAllState(state))
          Behaviors.same

        case GameLoop =>
          gameContainer.update()
          val state = gameContainer.getGameContainerState()
          if(tickCount % classify == 0){
            dispatch(subscribesMap)(BreakoutGameEvent.SyncGameAllState(state))
          }
          idle(nameA,nameB,subscribesMap,gameContainer,tickCount + 1)

        case ChildDead(name,childRef) =>
          ctx.unwatch(childRef)
          Behaviors.same

        case unknownMsg =>
          log.debug(s"${ctx.self.path} recv an unknow msg=${msg}")
          Behaviors.same
      }
    }
  }

  import scala.language.implicitConversions
  import org.seekloud.byteobject.ByteObject._

  def dispatch(subscribes:mutable.HashMap[String,ActorRef[UserActor.Command]])(msg:BreakoutGameEvent.WsMsgServer)(implicit sendBuffer:MiddleBufferInJvm) = {
    subscribes.values.foreach(_ ! UserActor.DispatchMsg(BreakoutGameEvent.Wrap(msg.asInstanceOf[BreakoutGameEvent.WsMsgServer].fillMiddleBuffer(sendBuffer).result(),msg.isInstanceOf[BreakoutGameEvent.GameOver])))
  }

  def dispatchTo(subscribes:mutable.HashMap[String,ActorRef[UserActor.Command]])(name:String,msg:BreakoutGameEvent.WsMsgServer)(implicit sendBuffer:MiddleBufferInJvm) = {
    subscribes.get(name).foreach(_ ! UserActor.DispatchMsg(BreakoutGameEvent.Wrap(msg.asInstanceOf[BreakoutGameEvent.WsMsgServer].fillMiddleBuffer(sendBuffer).result(),false)))
  }










}
