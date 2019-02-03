package com.neo.sk.breakout.core

import java.util.concurrent.atomic.AtomicLong

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.model.ws.{BinaryMessage, Message, TextMessage}
import akka.stream.{ActorAttributes, Supervision}
import akka.stream.scaladsl.Flow
import akka.util.ByteString
import org.slf4j.LoggerFactory
import com.neo.sk.breakout.shared.protocol.BreakoutGameEvent
/**
  * created by benyafang on 2019/2/3 15:52
  *
  * */
object UserManager {
  import io.circe.generic.auto._
  import io.circe.syntax._
  import org.seekloud.byteobject.ByteObject._
  import org.seekloud.byteobject.MiddleBufferInJvm

  private val log = LoggerFactory.getLogger(this.getClass)

  trait Command
  def create(): Behavior[Command] = {
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

  private def getWebSocketFlow(userActor:ActorRef[UserActor.Command]):Flow[Message,Message,Any] = {
    import scala.language.implicitConversions
    import org.seekloud.byteobject.ByteObject._

    implicit def parseJsonString2WsMsgFront(s: String): Option[BreakoutGameEvent.WsMsgFront] = {
      import io.circe.generic.auto._
      import io.circe.parser._

      try {
        val wsMsg = decode[BreakoutGameEvent.WsMsgFront](s).right.get
        Some(wsMsg)
      } catch {
        case e: Exception =>
          log.warn(s"parse front msg failed when json parse,s=${s}")
          None
      }
    }

    Flow[Message]
      .collect{
        case TextMessage.Strict(m) =>
          UserActor.WebSocketMsg(m)
        case BinaryMessage.Strict(m) =>
          bytesDecode[BreakoutGameEvent.WsMsgFront](new MiddleBufferInJvm(m.asByteBuffer)) match{
            case Right(req) => UserActor.WebSocketMsg(Some(req))
            case Left(e) =>
              log.error(s"decode binary Message failed ,error:${e.message}")
              UserActor.WebSocketMsg(None)
          }
      }
      .via(UserActor.flow(userActor))
      .map{
        case t: BreakoutGameEvent.Wrap => BinaryMessage.Strict(ByteString(t.ws))
        case t =>
          log.debug(s"akka stream receive unkown msg=${t}")
          TextMessage.apply("")
      }
      .withAttributes(ActorAttributes.supervisionStrategy{
        e:Throwable =>
          e.printStackTrace()
          log.error(s"WS stream failed with ${e}")
          Supervision.Resume
      })
  }

}
