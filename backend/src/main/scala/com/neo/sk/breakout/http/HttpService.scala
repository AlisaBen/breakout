package com.neo.sk.breakout.http

import java.net.URLEncoder

import akka.actor.{ActorSystem, Scheduler}
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.ws.Message
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import akka.stream.scaladsl.Flow
import akka.util.Timeout
import com.neo.sk.breakout.common.AppSettings
import akka.actor.typed.scaladsl.AskPattern._
import com.neo.sk.breakout.Boot.userManager
import com.neo.sk.breakout.core.UserManager

import scala.concurrent.{ExecutionContextExecutor, Future}
import com.neo.sk.breakout.shared.ptcl.ErrorRsp

import scala.util.Random

/**
  * Created by hongruying on 2018/3/11
  */
trait HttpService
  extends ResourceService
    with ServiceUtils
    with AccountService
    with GameHallService{

  import akka.actor.typed.scaladsl.AskPattern._
  import com.neo.sk.utils.CirceSupport._
  import io.circe.generic.auto._
  import io.circe._

  implicit val system: ActorSystem

  implicit val executor: ExecutionContextExecutor

  implicit val materializer: Materializer

  implicit val timeout: Timeout

  implicit val scheduler: Scheduler


  def platEnterRoute: Route = path("playGame"){
    parameter(
      'playerId.as[String],
      'playerName.as[String],
      'accessCode.as[String],
      'roomId.as[Long].?
    ) {
      case (playerId, playerName, accessCode, roomIdOpt) =>
        redirect(s"/tank/game/#/playGame/${playerId}/${URLEncoder.encode(playerName, "utf-8")}" + roomIdOpt.map(s => s"/$s").getOrElse("") + s"/$accessCode",
          StatusCodes.SeeOther
        )

    }
  } ~ path("watchGame") {
    parameter(
      'roomId.as[Long],
      'accessCode.as[String],
      'playerId.as[String].?
    ) {
      case (roomId, accessCode, playerIdOpt) =>
        redirect(s"/tank/game/#/watchGame/${roomId}" + playerIdOpt.map(s => s"/$s").getOrElse("") + s"/$accessCode",
          StatusCodes.SeeOther
        )

    }
  } ~ path("watchRecord") {
    parameter(
      'recordId.as[Long],
      'playerId.as[String],
      'frame.as[Long],
      'accessCode.as[String]
    ) {
      case (recordId, playerId, frame, accessCode) =>
        redirect(s"/tank/game/#/watchRecord/${recordId}/${playerId}/${frame}/${accessCode}",
          StatusCodes.SeeOther
        )
    }
  }

  private val webSocketConnection = path("join"){
    parameter(
      'name,
      'roomId.as[Long].?
    ){(name,roomIdOpt) =>
      val flowFuture:Future[Flow[Message,Message,Any]] = userManager ? (UserManager.GetWebSocketFlow(name,_,roomIdOpt))
      dealFutureResult(flowFuture.map(t => handleWebSocketMessages(t)))
    }
  }





  lazy val routes: Route = pathPrefix(AppSettings.rootPath){
    resourceRoutes ~ platEnterRoute ~ account ~ gameHall ~
      (pathPrefix("game") & get){
        pathEndOrSingleSlash{
          getFromResource("html/admin.html")
        } ~ webSocketConnection
      }
  }




}
