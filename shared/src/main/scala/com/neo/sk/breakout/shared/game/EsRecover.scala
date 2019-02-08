package com.neo.sk.breakout.shared.game

import com.neo.sk.breakout.shared.protocol.BreakoutGameEvent.{GameContainerAllState, GameEvent, UserActionEvent}

import scala.collection.mutable

/**
  * Created by hongruying on 2018/8/24
  * 中间会记录所有的事件数据和快照数据，但是当同步全量数据时会将历史的事件数据更新掉，并且丢掉快照数据
  * 可支持回溯
  * C
  */
trait EsRecover { this:GameContainerClientImpl =>

//  private val gameEventHistoryMap = mutable.HashMap[Long,List[GameEvent]]()
//  private val actionEventHistoryMap = mutable.HashMap[Long,List[UserActionEvent]]()
//  private val gameSnapshotMap = mutable.HashMap[Long,GameContainerAllState]()
//
//  def addEventHistory(frame:Long,gameEvents:List[GameEvent],actionEvents:List[UserActionEvent]):Unit = {
//    gameEventHistoryMap.put(frame,gameEvents)
//    actionEventHistoryMap.put(frame,actionEvents)
//  }
//
//
//
//  def addGameSnapShot(frame:Long,gameState:GameContainerAllState) = {
//    gameSnapshotMap.put(frame,gameState)
//  }
//
//  def clearEsRecoverData():Unit = {
//    gameEventHistoryMap.clear()
//    actionEventHistoryMap.clear()
//    gameSnapshotMap.clear()
//  }
//
//  def rollback(frame:Long) = {
//    require(frame < this.systemFrame)
//
//    gameSnapshotMap.get(frame) match {
//      case Some(gameContainerAllState) =>
//        val startTime = System.currentTimeMillis()
//        val curFrame = this.systemFrame
//        handleGameContainerAllState(gameContainerAllState)
//        //同步所有数据
//        removeKillInfoByRollback(frame)
//        reSetFollowEventMap(frame)
//        (frame until curFrame).foreach{ f =>
//          this.addGameEvents(f,gameEventHistoryMap.getOrElse(f,Nil),actionEventHistoryMap.getOrElse(f,Nil))
//          this.rollbackUpdate()
//        }
//        val endTime = System.currentTimeMillis()
//        println(s"roll back to frame=${frame},nowFrame=${curFrame} use Time:${endTime - startTime}")
//      case None => println(s"there are not snapshot frame=${frame}")
//    }
//  }
//
//  def rollback4GameEvent(e:GameEvent) = {
//    println(s"roll back4GameEvent to frame=${e.frame},nowFrame=${systemFrame} because event:${e}")
//    gameEventHistoryMap.put(e.frame, e :: gameEventHistoryMap.getOrElse(e.frame, Nil))
//    rollback(e.frame)
//  }
//
//  def rollback4UserActionEvent(e:UserActionEvent) = {
//    println(s"roll back4UserAction to frame=${e.frame},nowFrame=${systemFrame} because event:${e}")
//    actionEventHistoryMap.put(e.frame, e :: actionEventHistoryMap.getOrElse(e.frame, Nil))
//    rollback(e.frame)
//  }
//
////
////  def removePreEventHistory(frame:Long, tankId:Int, serialNum:Byte):Unit = {
////    actionEventHistoryMap.get(frame).foreach{ actions =>
////      actionEventHistoryMap.put(frame,actions.filterNot(t => t.tankId == tankId && t.serialNum == serialNum))
////    }
////  }
//
//  def addUserActionHistory(e:UserActionEvent) = {
//    actionEventHistoryMap.put(e.frame, e :: actionEventHistoryMap.getOrElse(e.frame, Nil))
//  }

}
