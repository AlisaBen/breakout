package com.neo.sk.breakout.http

import akka.http.scaladsl.server.Directives._
import com.neo.sk.breakout.common.AppSettings
import com.neo.sk.breakout.protocol.AccountProtocol
import com.neo.sk.breakout.shared.ptcl.{ErrorRsp, SuccessRsp}
import org.slf4j.LoggerFactory
import com.neo.sk.breakout.models.DAO.AccountDAO._
import com.neo.sk.breakout.models.SlickTables._

/**
  * created by benyafang on 2019/2/2 pm 13:48
  * */
trait AccountService extends ServiceUtils{

  import io.circe._
  import io.circe.generic.auto._

  private val log = LoggerFactory.getLogger(this.getClass)


  //注册
  private def registerErrorRsp(msg:String) = ErrorRsp(100011,msg)
  val register = (path("register") & post){
    entity(as[Either[Error,AccountProtocol.RegisterReq]]){
      case Right(req) =>
        dealFutureResult{
          insertUserInfo(rUserInfo(-1,req.userName,req.password,req.timestamp)).map{r =>
            complete(SuccessRsp())
          }.recover{
            case e:Exception =>
              log.debug(s"用户信息插入数据库失败：${e}")
              complete(registerErrorRsp(s"用户信息插入数据库失败：${e}"))
          }
        }
      case Left(error) =>
        log.debug(s"用户注册失败：${error}")
        complete(registerErrorRsp(s"用户注册失败：${error}"))
    }

  }

  //登录
  private def loginErrorRsp(msg:String) = ErrorRsp(100012,msg)
  val login = (path("login") & post){
    entity(as[Either[Error,AccountProtocol.LoginReq]]){
      case Right(req) =>
        dealFutureResult{
          confirmUserInfo(req.userName,req.password).map{l =>
            if(l > 0){
              complete(SuccessRsp())
            }else{
              complete(loginErrorRsp("用户名或者密码错误，请重新输入"))
            }
          }.recover{
            case e:Exception =>
              log.debug(s"用户登录失败，查询数据库错误：${e}")
              complete(loginErrorRsp(s"用户登录失败，查询数据库错误：${e}"))
          }
        }
      case Left(error) =>
        log.debug(s"用户登录失败,请求格式错误：${error}")
        complete(loginErrorRsp(s"用户登录失败,请求格式错误：${error}"))
    }
  }

  private def adminLoginErrorRsp(msg:String) = ErrorRsp(100010,msg)
  val adminLogin = (path("adminLogin") & post){
    entity(as[Either[Error,AccountProtocol.LoginReq]]){
      case Right(req) =>
        if(req.userName == AppSettings.adminName && req.password == AppSettings.adminPassword){
          complete(SuccessRsp())
        }else{
          log.debug(s"管理员登录失败，用户名或者密码错误")
          complete(adminLoginErrorRsp("管理员登录失败，用户名或者密码错误"))
        }
      case Left(error) =>
        log.debug(s"管理员登录失败，请求格式错误：$error")
        complete(adminLoginErrorRsp(s"管理员登录失败，请求格式错误：$error"))

    }
  }

  val account = pathPrefix("account"){
    adminLogin ~ login ~ register
  }


}
