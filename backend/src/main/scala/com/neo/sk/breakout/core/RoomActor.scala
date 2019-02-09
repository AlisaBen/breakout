package com.neo.sk.breakout.core

import akka.actor.typed.ActorRef
import akka.actor.typed.scaladsl.{Behaviors, StashBuffer, TimerScheduler}
import com.neo.sk.breakout.core.control.GameContainerServerImpl
import com.neo.sk.breakout.shared.protocol.BreakoutGameEvent
import org.seekloud.byteobject.MiddleBufferInJvm

import scala.language.implicitConversions
import org.seekloud.byteobject.ByteObject._

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

  trait Command

  case object BeginGame extends Command

  case object GameLoop extends Command

  def create(nameA:String,nameB:String) = {
    Behaviors.setup[Command]{
      ctx =>
        implicit val stashBuffer = StashBuffer[Command](Int.MaxValue)
        Behaviors.withTimers[Command]{implicit timer =>
//          val map = mutable.HashMap[String,UserActor.Command]()
//          idle(nameA,nameB)
          Behaviors.same

        }

    }
}

  private def idle(
                    nameA:String,
                    nameB:String,
                    gameContainer:GameContainerServerImpl)(
    implicit stashBuffer:StashBuffer[Command],
    sendBuffer:MiddleBufferInJvm,
    timer:TimerScheduler[Command]
  ) ={
    Behaviors.receive[Command]{(ctx,msg) =>
      msg match{
        case BeginGame =>

          Behaviors.same

        case GameLoop =>
          Behaviors.same

        case unknownMsg =>
          Behaviors.same
      }
    }
  }

  def dispatch(subscribes:mutable.HashMap[String,ActorRef[UserActor.Command]])(msg:BreakoutGameEvent.WsMsgServer)(implicit sendBuffer:MiddleBufferInJvm) = {
    subscribes.values.foreach(_ ! UserActor.DispatchMsg(BreakoutGameEvent.Wrap(msg.asInstanceOf[BreakoutGameEvent.WsMsgServer].fillMiddleBuffer(sendBuffer).result(),msg.isInstanceOf[BreakoutGameEvent.GameOver])))
  }

  def dispatchTo(subscribes:mutable.HashMap[String,ActorRef[UserActor.Command]])(name:String,msg:BreakoutGameEvent.WsMsgServer)(implicit sendBuffer:MiddleBufferInJvm) = {
    subscribes.get(name).foreach(_ ! UserActor.DispatchMsg(BreakoutGameEvent.Wrap(msg.asInstanceOf[BreakoutGameEvent.WsMsgServer].fillMiddleBuffer(sendBuffer).result(),false)))
  }










}
