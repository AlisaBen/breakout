package com.neo.sk.breakout.core

import java.util.concurrent.atomic.AtomicLong

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.{ActorContext, Behaviors, StashBuffer, TimerScheduler}
import akka.stream.OverflowStrategy
import akka.stream.scaladsl.Flow
import akka.stream.typed.scaladsl.ActorSource
import akka.stream.typed.scaladsl.ActorSink
import com.neo.sk.breakout.shared.`object`.Racket
import com.neo.sk.breakout.shared.config.{GameConfig, GameConfigImpl}
import com.neo.sk.breakout.shared.protocol.BreakoutGameEvent
import com.neo.sk.breakout.shared.protocol.BreakoutGameEvent.TankGameSnapshot
import org.seekloud.byteobject.MiddleBufferInJvm
import org.slf4j.LoggerFactory
import scala.language.implicitConversions
import org.seekloud.byteobject.ByteObject._
import scala.concurrent.duration.FiniteDuration

/**
  * created by benyafang on 2019/2/3 15:52
  *
  * */
object UserActor {

  private val log = LoggerFactory.getLogger(this.getClass)

  trait Command

  case object BehaviorChangeKey extends Command

  case class WebSocketMsg(reqOpt:Option[BreakoutGameEvent.WsMsgFront]) extends Command
  case object CompleteMsgFront extends Command
  case class FailMsgFront(ex:Throwable) extends Command
  case class UserFrontActor(actor:ActorRef[BreakoutGameEvent.WsMsgSource]) extends Command

  case class DispatchMsg(msg:BreakoutGameEvent.WsMsgSource) extends Command

  case class JoinRoomSuccess(racket:Racket,config:GameConfigImpl,roomActor:ActorRef[RoomActor.Command]) extends Command
  case class UserLeft[U](actorRef:ActorRef[U]) extends Command

  final case class SwitchBehavior(
                                   name: String,
                                   behavior: Behavior[Command],
                                   durationOpt: Option[FiniteDuration] = None,
                                   timeOut: TimeOut = TimeOut("busy time error")
                                 ) extends Command

  case class TimeOut(msg:String) extends Command

  private[this] def switchBehavior(ctx: ActorContext[Command],
                                   behaviorName: String, behavior: Behavior[Command], durationOpt: Option[FiniteDuration] = None,timeOut: TimeOut  = TimeOut("busy time error"))
                                  (implicit stashBuffer: StashBuffer[Command],
                                   timer:TimerScheduler[Command]) = {
    log.debug(s"${ctx.self.path} becomes $behaviorName behavior.")
    timer.cancel(BehaviorChangeKey)
    durationOpt.foreach(timer.startSingleTimer(BehaviorChangeKey,timeOut,_))
    stashBuffer.unstashAll(ctx,behavior)
  }


  def flow(actor:ActorRef[UserActor.Command]):Flow[WebSocketMsg,BreakoutGameEvent.WsMsgSource,Any] = {
    val in = Flow[WebSocketMsg].to(
      ActorSink.actorRef[Command](actor,CompleteMsgFront,FailMsgFront.apply)
    )
    val out = ActorSource.actorRef[BreakoutGameEvent.WsMsgSource](
      completionMatcher = {
        case BreakoutGameEvent.CompleteMsgServer =>
      },
      failureMatcher = {
        case BreakoutGameEvent.FailMsgServer(e) => e
      },
      bufferSize = 128,
      overflowStrategy = OverflowStrategy.dropHead
    )
      .mapMaterializedValue(outActor => actor ! UserFrontActor(outActor))
    Flow.fromSinkAndSource(in,out)
  }

  def create(name:String): Behavior[Command] = {
    log.debug(s"UserManager start...")
    Behaviors.setup[Command] {
      ctx =>
        Behaviors.withTimers[Command] {
          implicit timer =>
            implicit val stashBuffer = StashBuffer[Command](Int.MaxValue)
            implicit val sendBuffer = new MiddleBufferInJvm(8192)
//            val uidGenerator = new AtomicLong(1L)
            idle(name)
//            Behaviors.same
        }
    }
  }

  def idle(name:String,frontActor:Option[ActorRef[BreakoutGameEvent.WsMsgSource]] = None)(
    implicit stashBuffer:StashBuffer[Command],
    sendBuffer:MiddleBufferInJvm,
    timer:TimerScheduler[Command]
  ):Behavior[Command] = {
    Behaviors.receive[Command]{(ctx,msg) =>
      msg match {
        case UserFrontActor(frontActor) =>
          ctx.watchWith(frontActor,UserLeft(frontActor))
          idle(name,Some(frontActor))
//          switchBehavior(ctx,"idle",idle(uId, userInfo,System.currentTimeMillis(), frontActor))
        case UserLeft(actor) =>
          ctx.unwatch(actor)
          Behaviors.stopped

        case JoinRoomSuccess(racket,config,roomActor) =>
          frontActor.foreach(t => t ! BreakoutGameEvent.Wrap(BreakoutGameEvent.YourInfo(BreakoutGameEvent.PlayerInfo(racket.racketId,racket.name),config).asInstanceOf[BreakoutGameEvent.WsMsgServer].fillMiddleBuffer(sendBuffer).result()))
          switchBehavior(ctx,"play",play(racket.name,racket,frontActor.get,roomActor))


        case unknowMsg =>
          Behaviors.same

      }
    }
  }

  def play(name:String,racket:Racket,frontActor: ActorRef[BreakoutGameEvent.WsMsgSource],roomActor: ActorRef[RoomActor.Command])(
    implicit stashBuffer:StashBuffer[Command],
    timer:TimerScheduler[Command],
    sendBuffer:MiddleBufferInJvm
  ):Behavior[Command] = {
    Behaviors.receive[Command]{(ctx,msg) =>
      msg match{
        case WebSocketMsg(reqOpt) =>
          if(reqOpt.nonEmpty){
            reqOpt.get match{
              case t:BreakoutGameEvent.UserActionEvent =>
                roomActor ! RoomActor.WebSocketMsg(name, racket.racketId, t)
                Behaviors.same

              case t: BreakoutGameEvent.PingPackage =>
                frontActor ! BreakoutGameEvent.Wrap(t.asInstanceOf[BreakoutGameEvent.WsMsgServer].fillMiddleBuffer(sendBuffer).result())
                Behaviors.same

              case _ =>
                Behaviors.same
            }
          }else{
            Behaviors.same
          }

        case DispatchMsg(m) =>
          if(m.asInstanceOf[BreakoutGameEvent.Wrap].isKillMsg) {
            frontActor ! m
            switchBehavior(ctx,"idle",idle(name,Some(frontActor)))
          }else{
            frontActor ! m
            Behaviors.same
          }

        case UserLeft(actor) =>
          ctx.unwatch(actor)
//          roomManager ! RoomManager.LeftRoom()
          Behaviors.stopped


        case _ =>
          log.debug(s"${ctx.self.path} recv an unknown msg")
          Behaviors.same
      }
    }

  }


}
