package com.neo.sk.breakout.core

import java.util.concurrent.atomic.AtomicLong

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.{Behaviors, StashBuffer, TimerScheduler}
import akka.stream.OverflowStrategy
import akka.stream.scaladsl.Flow
import akka.stream.typed.scaladsl.ActorSource
import akka.stream.typed.scaladsl.ActorSink
import com.neo.sk.breakout.shared.protocol.BreakoutGameEvent
import com.neo.sk.breakout.shared.protocol.BreakoutGameEvent.TankGameSnapshot
import org.seekloud.byteobject.MiddleBufferInJvm
import org.slf4j.LoggerFactory

/**
  * created by benyafang on 2019/2/3 15:52
  *
  * */
object UserActor {

  private val log = LoggerFactory.getLogger(this.getClass)

  trait Command

  case class WebSocketMsg(reqOpt:Option[BreakoutGameEvent.WsMsgFront]) extends Command
  case object CompleteMsgFront extends Command
  case class FailMsgFront(ex:Throwable) extends Command
  case class UserFrontActor(actor:ActorRef[BreakoutGameEvent.WsMsgSource]) extends Command

  case class DispatchMsg(msg:BreakoutGameEvent.WsMsgSource) extends Command

  def flow(actor:ActorRef[UserActor.Command]):Flow[WebSocketMsg,BreakoutGameEvent.WsMsgSource,Any] = {
    val in = Flow[WebSocketMsg].to(
      ActorSink.actorRef[Command](actor,CompleteMsgFront,FailMsgFront.apply)(actor)
    )
    val out = ActorSource.actorRef[BreakoutGameEvent.WsMsgSource](
      completionMatcher = {
        case BreakoutGameEvent.CompleteMsgServer =>
      },
      failureMatcher = {
        case BreakoutGameEvent.FailMsgFrontServer(e) => e
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
            val uidGenerator = new AtomicLong(1L)
//            idle(uidGenerator)
            Behaviors.same
        }
    }
  }

  def idle()(
    implicit stashBuffer:StashBuffer[Command],
    sendBuffer:MiddleBufferInJvm,
    timer:TimerScheduler[Command]
  ) = {
    Behaviors.receive[Command]{(ctx,msg) =>
      msg match {
        case unknowMsg =>
          Behaviors.same

      }
    }
  }


}
