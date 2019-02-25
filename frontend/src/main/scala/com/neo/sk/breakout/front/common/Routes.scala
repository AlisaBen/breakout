package com.neo.sk.breakout.front.common

import com.neo.sk.breakout.front.model.PlayerInfo
import org.scalajs.dom
import com.neo.sk.breakout.front.model.ReplayInfo
import com.neo.sk.breakout.front.pages.MainPage

/**
  * User: Taoz
  * Date: 2/24/2017
  * Time: 10:59 AM
  */
object Routes {

  val base = "/breakout"

  object AccountRoute{
    val adminLoginRoute = base + "/account/adminLogin"
    val registerRoute = base + "/account/register"
    val loginRoute = base + "/account/login"
  }

  object GameHall{
    val chooseGameModelRoute = base + "/gameHall/chooseGameModel"
    val getUId4VisitorRoute = base + "/gameHall/getUId4Visitor"
  }

  object AdminRoute{
    val adminService = "/adminService"
    val getUserInfoListRoute = base + adminService + "/userManagerPage/getUserInfoList"
    val userForbiddenRoute = base + adminService + "/userManagerPage/userForbidden"
    val getAllRoomRoute = base + adminService + "/roomManagerPage/getAllRoom"
    val getGameStatisticRoute = base + adminService + "/statisticPage/getGameStatistic"
  }

  def genImgUrl(imgName:String) = base + s"/static/img/${imgName}"


  val getRoomListRoute = base + "/getRoomIdList"

  val getRecordListUrl = base + s"/getGameRec"
  val getRecordListByPlayerUrl = base + s"/getGameRecByPlayer"
  val getRecordListByRoomUrl = base + s"/getGameRecByRoom"
  val getRecordListByIdUrl = base + s"/getGameRecById"

  def wsJoinGameUrl(uid:Long,name:String,isVisitor:Int,roomIdOpt:Option[Long]) = {
    base + s"/game/join?uid=${uid}&name=${name}"+ s"&isVisitor=${isVisitor}" +
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



  def getJoinGameWebSocketUri(uid:Long,name:String,isVisitor:Int,roomIdOpt:Option[Long]): String = {
    val wsProtocol = if (dom.document.location.protocol == "https:") "wss" else "ws"
    s"$wsProtocol://${dom.document.location.host}${Routes.wsJoinGameUrl(uid,name,isVisitor,roomIdOpt)}"
  }


  def getReplaySocketUri(info:ReplayInfo): String = {
    val wsProtocol = if (dom.document.location.protocol == "https:") "wss" else "ws"
    s"$wsProtocol://${dom.document.location.host}${Routes.wsReplayGameUrl(info)}"
  }

  def getWsSocketUri(roomId:Long, accessCode:String, playerId:Option[String]): String = {
    val wsProtocol = if (dom.document.location.protocol == "https:") "wss" else "ws"
    s"$wsProtocol://${dom.document.location.host}${Routes.wsWatchGameUrl(roomId, accessCode, playerId)}"
  }

  def handleAuthError(errorCode:Int, msg:String)(func:() => Unit) ={
    if(errorCode == 1000202){
      MainPage.gotoPage("#/admin/login")
    } else {
      func()
    }
  }









}
