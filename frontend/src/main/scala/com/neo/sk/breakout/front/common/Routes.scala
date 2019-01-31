package com.neo.sk.breakout.front.common

import com.neo.sk.breakout.front.model.PlayerInfo
import org.scalajs.dom
import com.neo.sk.breakout.front.model.ReplayInfo

/**
  * User: Taoz
  * Date: 2/24/2017
  * Time: 10:59 AM
  */
object Routes {

  val base = "/tank"
  val getRoomListRoute = base + "/getRoomIdList"

  val getRecordListUrl = base + s"/getGameRec"
  val getRecordListByPlayerUrl = base + s"/getGameRecByPlayer"
  val getRecordListByRoomUrl = base + s"/getGameRecByRoom"
  val getRecordListByIdUrl = base + s"/getGameRecById"

  def wsJoinGameUrl(name:String,roomIdOpt:Option[Long]) = {
    base + s"/game/join?name=${name}"+
      (roomIdOpt match{
      case Some(roomId) =>s"&roomId=$roomId"
      case None =>""
      })
  }

  def wsWatchGameUrl(roomId:Long, accessCode:String, playerId:Option[String]) = base + s"/game/watchGame?roomId=$roomId&accessCode=${accessCode}" + playerId.map(t => s"&playerId=$t").getOrElse("")



  def wsJoinGameUrl(name:String, userId:String, userName:String, accessCode:String, roomIdOpt:Option[Long]): String = {
    base + s"/game/userJoin?name=$name&userId=$userId&userName=$userName&accessCode=$accessCode" +
      (roomIdOpt match {
        case Some(roomId) =>
          s"&roomId=$roomId"
        case None =>
          ""
      })
  }

  def wsReplayGameUrl(info:ReplayInfo) = base + s"/game/replay?rid=${info.recordId}&wid=${info.playerId}&f=${info.frame}&accessCode=${info.accessCode}"
  def wsReplayGameUrl(name:String,uid:String,rid:Long,wid:String,f:Int) = base + s"/game/replay?name=$name&uid=$uid&rid=$rid&wid=$wid&f=$f"



  def getJoinGameWebSocketUri(name:String, playerInfoOpt: Option[PlayerInfo],roomIdOpt:Option[Long]): String = {
    val wsProtocol = if (dom.document.location.protocol == "https:") "wss" else "ws"
    playerInfoOpt match {
      case Some(playerInfo) =>
        s"$wsProtocol://${dom.document.location.host}${Routes.wsJoinGameUrl(name,playerInfo.userId, playerInfo.userName, playerInfo.accessCode, playerInfo.roomIdOpt)}"
      case None =>
        s"$wsProtocol://${dom.document.location.host}${Routes.wsJoinGameUrl(name,roomIdOpt)}"
    }
  }


  def getReplaySocketUri(info:ReplayInfo): String = {
    val wsProtocol = if (dom.document.location.protocol == "https:") "wss" else "ws"
    s"$wsProtocol://${dom.document.location.host}${Routes.wsReplayGameUrl(info)}"
  }

  def getWsSocketUri(roomId:Long, accessCode:String, playerId:Option[String]): String = {
    val wsProtocol = if (dom.document.location.protocol == "https:") "wss" else "ws"
    s"$wsProtocol://${dom.document.location.host}${Routes.wsWatchGameUrl(roomId, accessCode, playerId)}"
  }










}
