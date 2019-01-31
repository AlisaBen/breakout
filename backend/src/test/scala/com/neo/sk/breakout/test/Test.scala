package com.neo.sk.breakout.test

import java.util.concurrent.atomic.AtomicLong

import akka.actor.ActorSystem
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.{ActorContext, Behaviors, StashBuffer, TimerScheduler}
import akka.dispatch.MessageDispatcher
import akka.shapeless.HNil
import akka.stream.ActorMaterializer
import akka.util.Timeout
import org.slf4j.LoggerFactory

import concurrent.duration._
import scala.concurrent.Future
import concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.FiniteDuration
import shapeless.labelled.FieldType
import shapeless.{::, HList, LabelledGeneric, Lazy, Witness}

/**
  * Created by hongruying on 2018/3/11
  */
object Test {

  private val log = LoggerFactory.getLogger(this.getClass)

//  val x = Future{
//    Thread.sleep(2000)
//    println("sssss")
//  }

//  val y = (0 to 10).toList.map(t => Future{
//    println("start",t)
//    Thread.sleep((10-t)*1000);
//    println("end",t)
//  t}.onComplete(t =>println(t)))


  sealed trait Command

  case class Hello(id:Long) extends Command

  case class TimeOut(msg:String) extends Command

  final case class SwitchBehavior(
                                   name: String,
                                   behavior: Behavior[Command],
                                   durationOpt: Option[FiniteDuration] = None,
                                   timeOut: TimeOut = TimeOut("busy time error")
                                 ) extends Command

  private val autoLonger = new AtomicLong(100L)

  private[this] def switchBehavior(ctx: ActorContext[Command],
                                   behaviorName: String, behavior: Behavior[Command], durationOpt: Option[FiniteDuration] = None,timeOut: TimeOut  = TimeOut("busy time error"))
                                  (implicit stashBuffer: StashBuffer[Command],
                                   timer:TimerScheduler[Command]) = {
    log.debug(s"${ctx.self.path} becomes $behaviorName behavior.")
    stashBuffer.unstashAll(ctx,behavior)
  }


  def create() = {
    Behaviors.setup[Command] {
      ctx =>
        log.debug(s"${ctx.self.path} App is starting...")
        implicit val stashBuffer = StashBuffer[Command](Int.MaxValue)
        Behaviors.withTimers[Command] { implicit timer =>
          busy()
        }
    }
  }

  def idle() = {
    Behaviors.receive[Command]{ (ctx,msg) =>
      msg match {
        case Hello(id) =>
          log.debug(s"${ctx.self.path} recv a msg=${msg}")
          Behaviors.same
      }

    }
  }

  private def busy()(
    implicit stashBuffer:StashBuffer[Command],
    timer:TimerScheduler[Command]
  ): Behavior[Command] =
    Behaviors.receive[Command] { (ctx, msg) =>
      msg match {
        case SwitchBehavior(name, behavior,durationOpt,timeOut) =>
          Thread.sleep(1000L)
          println("--------------")
          switchBehavior(ctx,name,behavior,durationOpt,timeOut)

        case TimeOut(m) =>
          log.debug(s"${ctx.self.path} is time out when busy,msg=${m}")
          switchBehavior(ctx,"stop",Behaviors.stopped)

        case unknowMsg =>
          stashBuffer.stash(unknowMsg)
          Behavior.same
      }
    }

//  sealed trait JsonValue
//  case class JsonObject(fields:List[(String,JsonValue)]) extends JsonValue
//  case class JsonArray(items: List[JsonValue]) extends JsonValue
//  case class JsonString(value:String) extends JsonValue
//  case class JsonNumber(value:Double) extends JsonValue
//  case class JsonBoolean(value:Boolean) extends JsonValue
//  case object JsonNull extends JsonValue
//
//
//  trait JsonEncoder[A]{
//    def encode(value:A):JsonValue
//  }
//
//  object JsonEncoder {
//    def apply[A](implicit enc:JsonEncoder[A]): JsonEncoder[A] = enc
//
//    def createEncoder[A](func: A => JsonValue) : JsonEncoder[A] = new JsonEncoder[A] {
//      def encode(value:A):JsonValue = func(value)
//    }
//
//
//    implicit val stringEncoder: JsonEncoder[String] = createEncoder(str => JsonString(str))
//    implicit val doubleEncoder: JsonEncoder[Double] = createEncoder(d => JsonNumber(d))
//    implicit val intEncoder: JsonEncoder[Int] = createEncoder(num => JsonNumber(num))
//    implicit val booleanEncoder: JsonEncoder[Boolean] = createEncoder(b => JsonBoolean(b))
//
//    implicit def listEncoder[A](implicit enc: JsonEncoder[A]):JsonEncoder[List[A]] = {
//      createEncoder(list => JsonArray(list.map(enc.encode)))
//    }
//
//    implicit def optionEncoder[A](implicit enc: JsonEncoder[A]):JsonEncoder[Option[A]] = {
//      createEncoder(opt => opt.map(enc.encode).getOrElse(JsonNull))
//    }
//  }
//
//  case class IceCream(name:String,num:Int,c:Boolean)
//
//  trait JsonObjectEncoder[A] extends JsonEncoder[A]{
//    def encode(value:A):JsonObject
//  }
//
//  def createObjectEncoder[A](fn:A => JsonObject):JsonObjectEncoder[A] =
//    new JsonObjectEncoder[A] {
//      override def encode(value: A): JsonObject = fn(value)
//    }
//
//  implicit val hnilEncoder:JsonObjectEncoder[HNil] = createObjectEncoder(t => JsonObject(Nil))
//
//
//  implicit def hlistObjectEncoder[K <: Symbol,H, T <: HList](
//                                                implicit
//                                                witness:Witness.Aux[K],
//                                                hEncoder:Lazy[JsonEncoder[H]],
//                                                tEncoder:JsonObjectEncoder[T]
//                                                ):JsonObjectEncoder[FieldType[K,H] :: T] = {
//    val fieldName = witness.value.name
//    createObjectEncoder{ hList =>
//      val head = hEncoder.value.encode(hList.head)
//      val tail = tEncoder.encode(hList.tail)
//
//      JsonObject((fieldName,head) :: tail.fields)
//    }
//  }
//
//  implicit def hlistObjectEncoder22[H, T <: HList](
//                                                              implicit
//                                                              hEncoder:JsonEncoder[H],
//                                                              tEncoder:JsonObjectEncoder[T]
//                                                            ):JsonObjectEncoder[H :: T] = {
//    createObjectEncoder{ hList =>
//      val head = hEncoder.encode(hList.head)
//      val tail = tEncoder.encode(hList.tail)
//
//      JsonObject(("",head) :: tail.fields)
//    }
//  }
//
//  import JsonEncoder._
//
//  implicit def genericObjectEncoder[A,H](
//                                        implicit
//                                        generic:LabelledGeneric.Aux[A,H],
//                                        hEncoder:Lazy[JsonObjectEncoder[H]]
//                                        ):JsonEncoder[A] = {
//    createObjectEncoder{ value =>
//      hEncoder.value.encode(generic.to(value))
//    }
//  }
//
//  val iceCream = IceCream("11",1,true)
//
//  import io.circe.Encoder
//
//  val gen = LabelledGeneric[IceCream]
//
//
// type XXX = Int :: HNil
//  println(JsonEncoder[Int :: HNil](hlistObjectEncoder22(intEncoder,hnilEncoder)))
//  val x = JsonEncoder[IceCream].encode(iceCream)
//  println(x)
//




  def main(args: Array[String]): Unit = {


//    val iceJson:JsonValue = JsonObject(List(
//      ("name",JsonString("xxx")),
//      ("num",JsonNumber(1)),
//      ("c",JsonBoolean(true))
//    ))
//
//    import shapeless.LabelledGeneric
//    import JsonEncoder._




  }

}
