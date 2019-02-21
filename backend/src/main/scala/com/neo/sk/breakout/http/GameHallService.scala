package com.neo.sk.breakout.http

import akka.http.scaladsl.server.Directives._
import com.neo.sk.breakout.shared.model.Constants
import com.neo.sk.breakout.shared.ptcl.{ErrorRsp, GameHallProtocol, SuccessRsp}
import com.neo.sk.breakout.Boot.userManager
import com.neo.sk.breakout.core.{RoomManager, UserManager}
import org.slf4j.LoggerFactory
import akka.actor.typed.scaladsl.AskPattern._
import com.neo.sk.breakout.shared.ptcl.AccountProtocol.LoginRsp
import com.neo.sk.breakout.Boot.{executor, scheduler, timeout, userManager}
import com.neo.sk.breakout.models.DAO.AccountDAO
import com.neo.sk.breakout.models.SlickTables

import scala.concurrent.Future
/**
  * created by benyafang on 2019/2/8 21:28
  * 游戏大厅服务，选择游戏服务模式，选择某种模式之后就后台进行随机匹配玩家
  * 给roomManager发消息
  * A1
  * */
trait GameHallService extends ServiceUtils{

  import io.circe._
  import io.circe.generic.auto._

  private val log = LoggerFactory.getLogger(this.getClass)


  private def chooseGameModelErrorRsp(msg:String) = ErrorRsp(1000021,msg)
  private val chooseGameModel = (path("chooseGameModel") & post){
    entity(as[Either[Error,GameHallProtocol.GameModelReq]]){
      case Right(req) =>
        userManager ! UserManager.ChooseModel(req.uid,req.name,req.isVisitor,req.model)
        complete(SuccessRsp())
      case Left(error) =>
        log.debug(s"选择游戏模式请求失败：${error}")
        complete(chooseGameModelErrorRsp(s"选择游戏模式请求失败：${error}"))
    }
  }

  private def getUId4VisitorErrorRsp(msg:String) = ErrorRsp(100023,msg)
  private val getUId4Visitor = (path("getUId4Visitor") & post){
    entity(as[Either[Error,GameHallProtocol.GetUId4Visitor]]){
      case Right(req) =>
        val uidFuture:Future[Long] = userManager ? (UserManager.GetUserId(req.name,req.isVisitor,_))
        dealFutureResult(uidFuture.map(t =>
         dealFutureResult{
           AccountDAO.insertVisitorInfo(SlickTables.rVisitorInfo(-1l,req.name,System.currentTimeMillis())).map{r =>
             complete(LoginRsp(t))
           }.recover{
             case e:Exception =>
               log.debug(s"插入游客信息失败")
               complete(LoginRsp(t))
           }
         }))
      case Left(error) =>
        log.debug(s"请求游客uid失败：${error}")
        complete(getUId4VisitorErrorRsp(s"请求游客uid失败：${error}"))

    }
  }

  val gameHall = pathPrefix("gameHall"){
    chooseGameModel ~ getUId4Visitor
  }

}
