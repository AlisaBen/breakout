package com.neo.sk.breakout.core

import java.util.concurrent.atomic.AtomicInteger

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.{ActorContext, Behaviors, StashBuffer, TimerScheduler}
import com.neo.sk.breakout.models.SlickTables
import com.neo.sk.breakout.shared.ptcl.GameHallProtocol.GameModelReq
//import com.neo.sk.breakout.core.RoomActor.{Command, GameBattleRecord}
import com.neo.sk.breakout.models.DAO.AccountDAO
import com.neo.sk.breakout.shared.model
import com.neo.sk.breakout.shared.ptcl.AdminProtocol.RoomManagerProtocol.RoomInfo
import org.seekloud.byteobject.MiddleBufferInJvm
import org.slf4j.LoggerFactory
import com.neo.sk.breakout.Boot.{executor,timeout,scheduler}
import scala.collection.mutable
import scala.concurrent.duration.FiniteDuration

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

  case class ChooseModel(req:GameModelReq,userActor:ActorRef[UserActor.Command]) extends Command

  case class WaitingTimeOut(name:String,model:Int) extends Command
  case class LeftRoom[U](actorRef:ActorRef[U]) extends Command

  case class GameOver(roomId:Int) extends Command

  case class GetRoomList(replyTo:ActorRef[List[RoomInfo]]) extends Command

  private case object BehaviorChangeKey extends Command

  case class TimeOut(str: String) extends Command

  private case class UpdateUserInfo(r:model.Score,ls:List[model.Score],isStop:Boolean) extends Command
  case class GameBattleRecord(scores:List[model.Score]) extends Command


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

  def create(): Behavior[Command] = {
    log.debug(s"RoomManager start...")
    Behaviors.setup[Command] {
      ctx =>
        implicit val stashBuffer = StashBuffer[Command](Int.MaxValue)
        Behaviors.withTimers[Command] {
          implicit timer =>
            val uidGenerator = new AtomicInteger(1)
            idle(uidGenerator,mutable.HashMap[Int, List[(GameModelReq,ActorRef[UserActor.Command])]](),
              mutable.HashMap[Int,((GameModelReq,ActorRef[UserActor.Command]),(GameModelReq,ActorRef[UserActor.Command]))]())
        }
    }
  }

  def idle(
            uidGenerator:AtomicInteger,
           waithingToMatch:mutable.HashMap[Int,List[(GameModelReq,ActorRef[UserActor.Command])]],//modal--uid,name,actor
            roomMap:mutable.HashMap[Int,((GameModelReq,ActorRef[UserActor.Command]),(GameModelReq,ActorRef[UserActor.Command]))]
          )(
    implicit stashBuffer:StashBuffer[Command],
    timer:TimerScheduler[Command]
  ):Behavior[Command] = {
    Behaviors.receive[Command]{(ctx,msg) =>
      msg match {
        case ChooseModel(req,userActor) =>
          //fixme 优先考虑用户加入的时间
          if(waithingToMatch.contains(req.model)){
            if(waithingToMatch(req.model).contains((req,userActor))){
              log.debug(s"${ctx.self.path} 正在匹配请耐心等待...")
            }else{
              var userActorList = waithingToMatch(req.model)
              val enemy = userActorList((new util.Random).nextInt(waithingToMatch(req.model).length))
              userActorList = userActorList.filterNot(t => t == enemy)
              if(userActorList.nonEmpty)waithingToMatch.put(req.model,userActorList)
              else waithingToMatch.remove(req.model)
              val roomId = uidGenerator.getAndIncrement()
              log.debug(s"${ctx.self.path} 新建roomActor为${req.uid}和${enemy._1}:roomId = ${roomId}")
              getRoomActor(roomId,req,enemy._1,mutable.HashMap((req.uid,userActor),(enemy._1.uid,enemy._2)),ctx) ! RoomActor.BeginGame
              roomMap.put(roomId,((req,userActor),(req,enemy._2)))
            }
          }else{
            waithingToMatch.put(req.model,List((req,userActor)))
          }
          idle(uidGenerator,waithingToMatch,roomMap)

        case LeftRoom(actorRef) =>
          ctx.unwatch(actorRef)
          Behaviors.same

        case GameOver(roomId) =>
          idle(uidGenerator,waithingToMatch,roomMap.filterNot(_._1 == roomId))

        case GameBattleRecord(ls) =>
          AccountDAO.insertBattleRecord(
            SlickTables.rBattleRecord(-1l,System.currentTimeMillis(),ls(0).n,ls(1).n,ls(0).score,ls(1).score))
            .map{r =>
              log.debug(s"${ctx.self.path} 插入战绩")
//              ctx.self ! SwitchBehavior("idle",idle(uidGenerator,waithingToMatch,roomMap))
              ls.sortBy(_.id).foreach{r =>ctx.self ! UpdateUserInfo(r,ls,r.id == ls.map(_.id).max)}
            }.recover{
            case e:Exception =>
              log.debug(s"${ctx.self.path}插入战绩失败：${e}")
              ls.sortBy(_.id).foreach{r =>ctx.self ! UpdateUserInfo(r,ls,r.id == ls.map(_.id).max)}
//              ctx.self ! SwitchBehavior("idle",idle(uidGenerator,waithingToMatch,roomMap))
          }
//          switchBehavior(ctx,"busy",busy(uidGenerator,waithingToMatch,roomMap))

          Behaviors.same

        case UpdateUserInfo(r,ls,isStop) =>
          AccountDAO.updateUserInfo(r.n,r.score >= ls.map(_.score).max).map{t =>
            log.debug(s"${ctx.self.path}更新用户信息")
          }.recover{
            case e:Exception =>
              log.debug(s"${ctx.self.path} 更新用户信息失败：${e}")
          }
          Behaviors.same


        case GetRoomList(replyTo) =>
//          replyTo ! roomMap.map(r => RoomInfo(r._1,List(r._2._1._2,r._2._2._2))).toList
          Behaviors.same

        case WaitingTimeOut(name,model) =>
//          //fixme 超时消息可以不加模式
//          if(waithingToMatch(model).filterNot(_ == name).isEmpty){
//            waithingToMatch.-=(model)
//          }
//          else {
//            waithingToMatch.put(model, waithingToMatch(model).filterNot(_ == name))
//          }
          Behaviors.same

        case unknowMsg =>
          Behaviors.same
      }
    }
  }


  private def getRoomActor(roomId:Int,nameA:GameModelReq,nameB:GameModelReq,playerMap:mutable.HashMap[Long,ActorRef[UserActor.Command]],ctx:ActorContext[Command]) = {
    val childName = s"RoomActor-${roomId}"
    ctx.child(childName).getOrElse{
      val actor = ctx.spawn(RoomActor.create(roomId,nameA,nameB,playerMap),childName)
      ctx.watchWith(actor,ChildDead(childName,actor))
      actor
    }
      .upcast[RoomActor.Command]
  }

  private def busy(
                    uidGenerator:AtomicInteger,
                    waithingToMatch:mutable.HashMap[Int,List[(GameModelReq,ActorRef[UserActor.Command])]],//modal--uid,name,actor
                    roomMap:mutable.HashMap[Int,((GameModelReq,ActorRef[UserActor.Command]),(GameModelReq,ActorRef[UserActor.Command]))])
                  (
                    implicit stashBuffer:StashBuffer[Command],
                    timer:TimerScheduler[Command]
                  ): Behavior[Command] = {
    Behaviors.receive[Command] { (ctx, msg) =>
      msg match {
        case SwitchBehavior(name, behavior, durationOpt, timeOut) =>
          log.debug(s"${ctx.self.path} recv a SwitchBehavior Msg=${name}")
          switchBehavior(ctx, name, behavior, durationOpt, timeOut)

        case TimeOut(m) =>
          log.debug(s"${ctx.self.path} is time out when busy,msg=${m}")
          switchBehavior(ctx, "idle", idle(uidGenerator,waithingToMatch,roomMap))

        case unknownMsg =>
          stashBuffer.stash(unknownMsg)
          Behavior.same
      }
    }
  }

}
