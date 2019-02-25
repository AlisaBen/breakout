package com.neo.sk.breakout.http

import akka.http.scaladsl.server.Directives._
import com.neo.sk.breakout.common.AppSettings
import com.neo.sk.breakout.shared.ptcl.{AccountProtocol, ErrorRsp, SuccessRsp}
import org.slf4j.LoggerFactory
import com.neo.sk.breakout.models.DAO.AccountDAO._
import com.neo.sk.breakout.models.SlickTables._
import com.neo.sk.breakout.Boot.{executor, scheduler, timeout, userManager}
import com.neo.sk.breakout.core.UserManager.GetUserId
import akka.actor.typed.scaladsl.AskPattern._
import com.neo.sk.breakout.http.SessionBase.AccountSession
import com.neo.sk.breakout.shared.ptcl.AccountProtocol.LoginRsp
import com.neo.sk.utils.TimeUtil

import scala.concurrent.Future

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
            //fixme 给userManager发消息
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
  private def loginErrorRsp(errCode:Int,msg:String) = ErrorRsp(errCode,msg)
  val login = (path("login") & post){
    entity(as[Either[Error,AccountProtocol.LoginReq]]){
      case Right(req) =>
        dealFutureResult{
          confirmUserInfo(req.userName,req.password).map{l =>
            if(l.length > 0){
              if(l.head){
                log.debug(s"已经被禁用用户请求登录${req.userName}--${TimeUtil.format_yyyyMMddHH(System.currentTimeMillis())}")
                complete(loginErrorRsp(10022,s"该账户已经被禁用"))
              }else{
                log.debug(s"传userId")
                val uid:Future[Long] = userManager ? (GetUserId(req.userName,true,_))
                dealFutureResult(uid.map(t =>complete(LoginRsp(Some(t)))))
              }
            }else{
              complete(loginErrorRsp(100012,"用户名或者密码错误，请重新输入"))
            }
          }.recover{
            case e:Exception =>
              log.debug(s"用户登录失败，查询数据库错误：${e}")
              complete(loginErrorRsp(100012,s"用户登录失败，查询数据库错误：${e}"))
          }
        }
      case Left(error) =>
        log.debug(s"用户登录失败,请求格式错误：${error}")
        complete(loginErrorRsp(100012,s"用户登录失败,请求格式错误：${error}"))
    }
  }

  private def adminLoginErrorRsp(msg:String) = ErrorRsp(100010,msg)
  val adminLogin = (path("adminLogin") & post){
    entity(as[Either[Error,AccountProtocol.LoginReq]]){
      case Right(req) =>
        if(req.userName == AppSettings.adminName && req.password == AppSettings.adminPassword){
          val session = AccountSession(req.userName,System.currentTimeMillis()).toSessionMap
          addSession(session){
            log.info(s"admin ${req.userName} login success")
            complete(SuccessRsp())}
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
