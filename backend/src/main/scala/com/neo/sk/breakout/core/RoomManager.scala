package com.neo.sk.breakout.core

import java.util.concurrent.atomic.AtomicLong

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import org.slf4j.LoggerFactory

/**
  * created by benyafang on 2019/2/3 15:52
  * 随机匹配玩家，匹配成功之后创建房间，给roomActor发消息，后端Actor开始进行游戏逻辑更新
  * 给userAcotr发消息
  * A2
  * */
object RoomManager {

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
}
