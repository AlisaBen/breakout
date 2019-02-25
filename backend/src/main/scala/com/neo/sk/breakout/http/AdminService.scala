package com.neo.sk.breakout.http

import akka.http.scaladsl.server.Directives._
import com.neo.sk.breakout.models.DAO.AdminDAO
import com.neo.sk.breakout.shared.ptcl.{AdminProtocol, ErrorRsp, SuccessRsp}
import org.slf4j.LoggerFactory
import com.neo.sk.breakout.shared.ptcl.AdminProtocol.UserManagerProtocol._
import com.neo.sk.breakout.shared.ptcl.AdminProtocol.RoomManagerProtocol._
import com.neo.sk.breakout.shared.ptcl.AdminProtocol.StatisticProtocol._
import com.neo.sk.breakout.Boot.{roomManager,executor,scheduler,timeout}
import com.neo.sk.breakout.core.RoomManager.GetRoomList
import akka.actor.typed.scaladsl.AskPattern._
import com.neo.sk.breakout.shared.model.{Score, SimpleScore}

import scala.concurrent.Future
/**
  * created by benyafang on 2019/2/20 14:20
  * */
trait AdminService extends ServiceUtils{
  import io.circe._
  import io.circe.syntax._
  import io.circe.generic.auto._

  private val log = LoggerFactory.getLogger(this.getClass)

  private def getUserInfoListErrorRsp(msg:String) = ErrorRsp(10031,msg)
  private val getUserInfoList = (path("getUserInfoList") & post){
    authUser{u =>
      entity(as[Either[Error,AdminProtocol.UserManagerProtocol.GetUserInfoListReq]]){
        case Right(req) =>
          dealFutureResult{
            for{
              l <- AdminDAO.getUserInfoList(req.page,req.pageNum)
              n <- AdminDAO.getUserInfoLength()
            }yield{
//              log.debug(s"-----${l}")
              complete(AdminProtocol.UserManagerProtocol.GetUserInfoListRsp(l.map(t => UserInfo(t.userName,t.timestamp,t.isForbidden,t.playNum,t.winNum)).toList,Some(n)))
            }
          }
        case Left(error) =>
          log.debug(s"获取用户列表信息失败：${error}")
          complete(getUserInfoListErrorRsp(s"获取用户列表信息失败：${error}"))
      }

    }
  }

  private def userForbiddenErrorRsp(msg:String) = ErrorRsp(10034,msg)
  private val userForbidden = (path("userForbidden") & post){
    authUser{u =>
      entity(as[Either[Error,UserForbiddenReq]]){
        case Right(req) =>
          dealFutureResult{
            AdminDAO.updateUserForbidden(req.name,req.isForbidden).map{ls =>
              complete(SuccessRsp())
            }.recover{
              case e:Exception =>
                log.debug(s"更新用户禁用信息失败：${e}")
                complete(userForbiddenErrorRsp(s"更新用户禁用信息失败：${e}"))
            }
          }
        case Left(error) =>
          log.debug(s"更新用户禁用信息失败：${error}")
          complete(userForbiddenErrorRsp(s"更新用户禁用信息失败：${error}"))
      }
    }


  }

  private def getAllRoomErrorRsp(msg:String) = ErrorRsp(10032,msg)
  private val getAllRoom = (path("getAllRoom") & post){
    entity(as[Either[Error,GetAllRoomReq]]){
      case Right(req) =>
        val rspFuture:Future[List[RoomInfo]] = roomManager ? (GetRoomList(_))
        dealFutureResult(rspFuture.map(rsp =>complete(GetAllRoomRsp(rsp))))
      case Left(error) =>
        log.debug(s"获取所有房间列表失败：${error}")
        complete(getAllRoomErrorRsp(s"获取所有房间列表失败：${error}"))
    }
  }

  private def getGameStatisticErrorRsp(msg:String) = ErrorRsp(100035,msg)
  private val getGameStatistic = (path("getGameStatistic") & post){
    authUser{u =>
      entity(as[Either[Error,GameStatisticProfile]]){
        case Right(req) =>
          dealFutureResult{
            for{
              ls <- AdminDAO.getGameStatisticList(req.page,req.pageNum)
              n <- AdminDAO.getGameStatisticLength()
            }yield {
              complete(GameStatisticProfileRsp(
                ls.map{r => GameBattleInfo(r.recordId,r.timestamp,List(SimpleScore(r.nameA,r.damageStatisticA),SimpleScore(r.nameB,r.damageStatisticB)))}
                  .toList,Some(n)))
            }
          }
        case Left(error) =>
          log.debug(s"获取游戏战绩数据失败:$error")
          complete(getGameStatisticErrorRsp(s"获取游戏战绩数据失败:$error"))
      }

    }
  }


  val adminService = pathPrefix("adminService"){
    pathPrefix("userManagerPage"){
      getUserInfoList ~ userForbidden
    } ~
    pathPrefix("roomManagerPage"){
      getAllRoom
    } ~
    pathPrefix("statisticPage"){
      getGameStatistic
    }
  }

}
