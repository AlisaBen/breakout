package com.neo.sk.breakout.front.pages

import com.neo.sk.breakout.front.common.{Page, PageSwitcher, Routes}
import com.neo.sk.breakout.front.utils.{Http, LocalStorageUtil}
import com.neo.sk.breakout.shared.ptcl.{AccountProtocol, SuccessRsp}
import mhtml.Var
import org.scalajs.dom
import org.scalajs.dom.ext.KeyCode
import org.scalajs.dom.{KeyboardEvent, html}
import com.neo.sk.breakout.front.common.Routes._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.xml.Elem

/**
  * Created by wmy
  * Date on 2018/12/12
  * Time at 上午11:43
  */
object AdminLoginPage extends Page {
  import io.circe.generic.auto._
  import io.circe._
  import io.circe.syntax._

  val showWarning = Var(false)

  def enter:Unit = {
    val name = dom.window.document.getElementById("account").asInstanceOf[html.Input].value
    val pwd = dom.window.document.getElementById("password").asInstanceOf[html.Input].value
    val data = AccountProtocol.LoginReq(name,pwd).asJson.noSpaces
    Http.postJsonAndParse[SuccessRsp](AccountRoute.adminLoginRoute,data).map{rsp =>
      if(rsp.errCode == 0){
//        LocalStorageUtil.storeUserInfo(AccountProtocol.NameStorage(name))
        dom.window.location.hash = s"#/main/user"
      }
      else{
        showWarning := true
        dom.window.location.hash = s"#/login"
      }
    }
  }

  def keyDownEnter(e:KeyboardEvent):Unit = {
    if(e.keyCode == KeyCode.Enter) {
      val name = dom.window.document.getElementById("account").asInstanceOf[html.Input].value
      val pwd = dom.window.document.getElementById("password").asInstanceOf[html.Input].value
      val data =  AccountProtocol.LoginReq(name,pwd).asJson.noSpaces
      Http.postJsonAndParse[SuccessRsp](AccountRoute.adminLoginRoute,data).map{rsp =>
        if(rsp.errCode == 0){
//          LocalStorageUtil.storeUserInfo(AccountProtocol.NameStorage(name))
          dom.window.location.hash = s"#/main/user"
        }
        else{
          showWarning := true
          dom.window.location.hash = s"#/login"
        }
      }
    }
  }

  val displayOfP = showWarning.map{r =>
    if(r)
      "display:block"
    else
      "display:none"
  }

  override def render: Elem ={
    {showWarning := false}
    <div>
      <div class="bgp">

      </div>
      <div class="loginBg" onkeydown = {e:KeyboardEvent => keyDownEnter(e)}>
        <div class="peitu" style="vertical-align: top;"><img src="/evoke/static/img/配图.png" width="500px" height="400px"></img></div>
        <div class="loginForm">
          <p id="loginTip">请登录</p>
          <p id="loginWar" style={displayOfP}>您输入的账户名或密码有误<button onclick={() => showWarning:=false}></button></p>
          <p><input type="text" id="account" placeholder="账号"></input></p>
          <p><input type="password" id="password" placeholder="密码"></input></p>
          <p><button id="loginBtn" onclick={() => enter}>登录</button></p>
        </div>
      </div>
    </div>
  }



}
