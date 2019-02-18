package com.neo.sk.breakout.core

import java.util.concurrent.atomic.AtomicLong

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.{ActorContext, Behaviors, StashBuffer, TimerScheduler}
import akka.http.scaladsl.model.ws.{BinaryMessage, Message, TextMessage}
import akka.stream.{ActorAttributes, Supervision}
import akka.stream.scaladsl.Flow
import akka.util.ByteString
import org.slf4j.LoggerFactory
import com.neo.sk.breakout.shared.protocol.BreakoutGameEvent
import com.neo.sk.breakout.Boot.roomManager

import scala.collection.mutable
/**
  * created by benyafang on 2019/2/3 15:52
  * A5
  * */
object UserManager {
  import io.circe.generic.auto._
  import io.circe.syntax._
  import org.seekloud.byteobject.ByteObject._
  import org.seekloud.byteobject.MiddleBufferInJvm

  private val log = LoggerFactory.getLogger(this.getClass)

  trait Command

  case class ChildDead[U](childName:String,ctx:ActorContext[U]) extends Command

  case class GetAllUserActor(replyTo:ActorRef[List[ActorRef[UserActor.Command]]]) extends Command

  case class GetUserActor(name:String,replyTo:ActorRef[ActorRef[UserActor.Command]]) extends Command

  case class ChooseModel(name:String,model:Int) extends Command
  final case class GetWebSocketFlow(name:String,replyTo:ActorRef[Flow[Message,Message,Any]], roomId:Option[Long] = None) extends Command


  private val userMap:mutable.HashMap[String, ActorRef[UserActor.Command]] = mutable.HashMap[String, ActorRef[UserActor.Command]]()

  def create(): Behavior[Command] = {
    log.debug(s"UserManager start...")
    Behaviors.setup[Command] {
      ctx =>
        Behaviors.withTimers[Command] {
          implicit timer =>
            implicit val stashBuffer = StashBuffer[Command](Int.MaxValue)
            implicit val sendBuffer = new MiddleBufferInJvm(8192)
            val uidGenerator = new AtomicLong(1L)
//            idle(uidGenerator)
//            Behaviors.same
            idle()
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
        case GetWebSocketFlow(name,replyTo,roomIdOpt) =>
          println(s"ssssss,$roomIdOpt")
          getUserActorOpt(ctx,name) match {
            case Some(userActor) =>
              userActor ! UserActor.ChangeBehaviorToInit
            case None =>
          }
          val userActor = getUserActor(ctx, name)
          replyTo ! getWebSocketFlow(userActor)
          Behaviors.same

        case GetAllUserActor(replyTo) =>
          replyTo ! getAllUserActor(ctx)
          Behaviors.same

        case GetUserActor(name,replyTo) =>
          replyTo ! getUserActor(ctx,name)
          Behaviors.same

        case ChooseModel(name,model) =>
          userMap.put(name,getUserActor(ctx,name))
          roomManager ! RoomManager.ChooseModel(name,model,userMap)
          Behaviors.same

        case unknowMsg =>
          Behaviors.same

      }
    }
  }

  def getUserActor(ctx:ActorContext[Command],name:String) = {
    val childName = s"userActor--${name}"
    ctx.child(childName).getOrElse{
      val actor = ctx.spawn(UserActor.create(name),childName)
      ctx.watchWith(actor,ChildDead(childName,ctx))
      actor
    }
      .upcast[UserActor.Command]
  }
  private def getUserActorOpt(ctx: ActorContext[Command],name:String):Option[ActorRef[UserActor.Command]] = {
    val childName = s"userActor--${name}"
    ctx.child(childName).map(_.upcast[UserActor.Command])
  }

  def getAllUserActor(ctx:ActorContext[Command]) = {
    ctx.children.map(_.upcast[UserActor.Command]).toList
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
