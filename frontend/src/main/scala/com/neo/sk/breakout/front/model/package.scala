package com.neo.sk.breakout.front

/**
  * Created by hongruying on 2018/10/18
  */
package object model {
  case class PlayerInfo(userId:String, userName:String, accessCode:String, roomIdOpt:Option[Long])

  /**发送观看消息链接信息*/
  case class ReplayInfo(recordId:Long,playerId:String,frame:Int,accessCode:String)
}
