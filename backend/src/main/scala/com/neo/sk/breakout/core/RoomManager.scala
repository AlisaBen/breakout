package com.neo.sk.breakout.core

import java.util.concurrent.atomic.AtomicLong

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.{ActorContext, Behaviors, StashBuffer, TimerScheduler}
import com.neo.sk.breakout.core.RoomActor.Command
import org.seekloud.byteobject.MiddleBufferInJvm
import org.slf4j.LoggerFactory

import scala.collection.mutable

/**
  * created by benyafang on 2019/2/3 15:52
  * 随机匹配玩家，匹配成功之后创建房间，给roomActor发消息，后端Actor开始进行游戏逻辑更新
  * 给userAcotr发消息
  * A2
  * */
object RoomManager {

  private val log = LoggerFactory.getLogger(this.getClass)

  trait Command

  case class ChildDead[U](childName:String,ctx:ActorRef[U]) extends Command

  case class ChooseModel(name:String,model:Int,userMap:mutable.HashMap[String,ActorRef[UserActor.Command]]) extends Command

  case class WaitingTimeOut(name:String,model:Int) extends Command
  case class LeftRoom[U](actorRef:ActorRef[U]) extends Command


  def create(): Behavior[Command] = {
    log.debug(s"UserManager start...")
    Behaviors.setup[Command] {
      ctx =>
        implicit val stashBuffer = StashBuffer[Command](Int.MaxValue)
        Behaviors.withTimers[Command] {
          implicit timer =>
            val uidGenerator = new AtomicLong(1L)
//            idle(uidGenerator)
//            Behaviors.same
            idle(mutable.HashMap[Int,List[String]]())
        }
    }
  }

  def idle(waithingToMatch:mutable.HashMap[Int,List[String]])(
    implicit stashBuffer:StashBuffer[Command],
    timer:TimerScheduler[Command]
  ):Behavior[Command] = {
    Behaviors.receive[Command]{(ctx,msg) =>
      msg match {
        case ChooseModel(name,model,userMap) =>
          //fixme 优先考虑用户加入的时间
          if(waithingToMatch.contains(model)){
            if(waithingToMatch(model).contains(name)){
              log.debug(s"${ctx.self.path} 正在匹配请耐心等待...")
            }else{
              val enemy = waithingToMatch(model)((new util.Random).nextInt(waithingToMatch(model).length))
              if(waithingToMatch(model).filterNot(_ == enemy).isEmpty){
                waithingToMatch.-=(model)
              }else{
                waithingToMatch.put(model,waithingToMatch(model).filterNot(t => t == enemy || t == name))
              }
              getRoomActor(name,enemy,userMap.filter(t => t._1 == name || t._1 == enemy),ctx) ! RoomActor.BeginGame
            }

          }else{
            waithingToMatch.put(model,List(name))
          }
          Behaviors.same

        case LeftRoom(actorRef) =>
          ctx.unwatch(actorRef)
          Behaviors.same

        case WaitingTimeOut(name,model) =>
          //fixme 超时消息可以不加模式
          if(waithingToMatch(model).filterNot(_ == name).isEmpty){
            waithingToMatch.-=(model)
          }
          else {
            waithingToMatch.put(model, waithingToMatch(model).filterNot(_ == name))
          }
          Behaviors.same

        case unknowMsg =>
          Behaviors.same
      }
    }
  }


  private def getRoomActor(nameA:String,nameB:String,playerMap:mutable.HashMap[String,ActorRef[UserActor.Command]],ctx:ActorContext[Command]) = {
    val childName = s"RoomActor--${nameA}+${nameB}"
    ctx.child(childName).getOrElse{
      val actor = ctx.spawn(RoomActor.create(nameA,nameB,playerMap),childName)
      ctx.watchWith(actor,ChildDead(childName,actor))
      actor
    }
      .upcast[RoomActor.Command]
  }
}
