package com.neo.sk.breakout.shared.game

/**
  * Created by hongruying on 2018/8/28
  * B
  */
trait KillInformation{ this:GameContainer =>
  private var killInfoList = List[(String,String,Long)]() //killerName,killedName, startTimer
  private val maxDisplaySize = 5
  private val displayDuration = 3 //s

//  private val displayFrameNum:Long = displayDuration * 1000 / this.config.frameDuration
//
//  protected def addKillInfo(killerName:String,killedName:String) = {
//    println(s"killInfo----------,$killedName,$killerName,${this.systemFrame}")
//    killInfoList = (killerName,killedName,this.systemFrame) :: killInfoList
//  }
//
//
//
//  protected def updateKillInformation():Unit = {
//    killInfoList = killInfoList.filterNot(_._3 + displayFrameNum < this.systemFrame)
//  }
//
//  protected def getDisplayKillInfo():List[(String,String,Long)] = {
//    val curDisplayNum = math.min(maxDisplaySize,killInfoList.size)
//    killInfoList.take(curDisplayNum)
//  }
//
//  protected def removeKillInfoByRollback(frame:Long) = {
//    killInfoList = killInfoList.filterNot(_._3 >= frame)
//  }


}
