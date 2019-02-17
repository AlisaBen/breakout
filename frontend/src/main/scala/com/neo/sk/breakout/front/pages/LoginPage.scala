package com.neo.sk.breakout.front.pages

import com.neo.sk.breakout.front.common.{Page, Routes}
import com.neo.sk.breakout.front.utils.{Http, JsFunc, LocalStorageUtil}
import com.neo.sk.breakout.shared.ptcl.{AccountProtocol, SuccessRsp}
import mhtml.Var
import org.scalajs.dom
import org.scalajs.dom.ext.KeyCode
import org.scalajs.dom.{KeyboardEvent, html}
import com.neo.sk.breakout.front.common.Routes._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.xml.Elem
import com.neo.sk.breakout.front.pages.MainPage._
/**
  * Created by wmy
  * Date on 2018/12/12
  * Time at 上午11:43
  */

object LoginPage extends Page{
  import io.circe.generic.auto._
  import io.circe._
  import io.circe.syntax._

  val showWarning = Var(false)

  def enter:Unit = {
    val name = dom.window.document.getElementById("account").asInstanceOf[html.Input].value
    val pwd = dom.window.document.getElementById("password").asInstanceOf[html.Input].value
    val data = AccountProtocol.LoginReq(name,pwd).asJson.noSpaces
    Http.postJsonAndParse[SuccessRsp](AccountRoute.loginRoute,data).map{rsp =>
      if(rsp.errCode == 0){
        LocalStorageUtil.storeUserInfo(AccountProtocol.NameStorage(name))
        dom.window.location.hash = s"#/gameHall"
        GameHall.playerName = name
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
      Http.postJsonAndParse[SuccessRsp](AccountRoute.loginRoute,data).map{rsp =>
        if(rsp.errCode == 0){
          LocalStorageUtil.storeUserInfo(AccountProtocol.NameStorage(name))
          dom.window.location.hash = s"#/gameHall"
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
    <div>
      <div class="bgp">

      </div>
      <div class="loginBg" onkeydown = {e:KeyboardEvent => keyDownEnter(e)}>
        <div class="loginForm">
          <p id="loginTip">请登录</p>
          <p id="loginWar" style={displayOfP}>您输入的账户名或密码有误<button onclick={() => showWarning:=false}></button></p>
          <p><input type="text" id="account" placeholder="账号"></input></p>
          <p><input type="password" id="password" placeholder="密码"></input></p>
          <p>
            <button id="loginBtn" onclick={() => enter}>登录</button>
            <button id="registerBtn" onclick={() => gotoPage(s"#/register")}>注册</button>
          </p>
        </div>
      </div>
    </div>
  }



}


