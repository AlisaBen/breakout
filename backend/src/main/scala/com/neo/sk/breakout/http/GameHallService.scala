package com.neo.sk.breakout.http

import akka.http.scaladsl.server.Directives._
import com.neo.sk.breakout.shared.model.Constants
import com.neo.sk.breakout.shared.ptcl.{ErrorRsp, GameHallProtocol, SuccessRsp}
import com.neo.sk.breakout.Boot.roomManager
import com.neo.sk.breakout.core.RoomManager
import org.slf4j.LoggerFactory
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
        roomManager ! RoomManager.ChooseModel(req.name,req.model)
        complete(SuccessRsp())
      case Left(error) =>
        log.debug(s"选择游戏模式请求失败：${error}")
        complete(chooseGameModelErrorRsp(s"选择游戏模式请求失败：${error}"))
    }
  }

  val gameHall = pathPrefix("gameHall"){
    chooseGameModel
  }

}
