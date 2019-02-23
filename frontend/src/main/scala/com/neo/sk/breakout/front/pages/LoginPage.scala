package com.neo.sk.breakout.front.pages

import com.neo.sk.breakout.front.common.{Page, Routes}
import com.neo.sk.breakout.front.utils.{Http, JsFunc, LocalStorageUtil}
import com.neo.sk.breakout.shared.ptcl.{AccountProtocol, SuccessRsp}
import mhtml.{Var, emptyHTML}
import org.scalajs.dom
import org.scalajs.dom.ext.KeyCode
import org.scalajs.dom.{KeyboardEvent, html}
import com.neo.sk.breakout.front.common.Routes._
import com.neo.sk.breakout.front.model.PlayerInfo

import scala.concurrent.ExecutionContext.Implicits.global
import scala.xml.Elem
import com.neo.sk.breakout.front.pages.MainPage._
import com.neo.sk.breakout.front.control.GamePlayHolder
import org.scalajs.dom.html.Div
//import com.neo.sk.breakout.shared.protocol.BreakoutGameEvent.PlayerInfo
import com.neo.sk.breakout.shared.ptcl.AccountProtocol.LoginRsp
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

  private val modal = Var(emptyHTML)
  private val canvas = <canvas id="GameView" tabindex="1"></canvas>

  def enter:Unit = {
    val name = dom.window.document.getElementById("account").asInstanceOf[html.Input].value
    val pwd = dom.window.document.getElementById("password").asInstanceOf[html.Input].value
    val data = AccountProtocol.LoginReq(name,pwd).asJson.noSpaces
    Http.postJsonAndParse[LoginRsp](AccountRoute.loginRoute,data).map{rsp =>
      if(rsp.errCode == 0){
        LocalStorageUtil.storeUserInfo(AccountProtocol.NameStorage(rsp.uidOpt.get,name,true))
        val gamePlayHolder = new GamePlayHolder("GameView",PlayerInfo(rsp.uidOpt.get,name,true,None))
        modal := gamePlayHolder.getStartGameModal()
        dom.document.getElementById("loginBg").asInstanceOf[Div].setAttribute("class","invisible")
      }
      else{
        showWarning := true
        gotoPage(s"#/login")
        //        dom.window.location.hash = s"#/login"
      }
    }
  }

  private def keyDownEnter(e:KeyboardEvent):Unit = {
    if(e.keyCode == KeyCode.Enter) {
      val name = dom.window.document.getElementById("account").asInstanceOf[html.Input].value
      val pwd = dom.window.document.getElementById("password").asInstanceOf[html.Input].value
      val data =  AccountProtocol.LoginReq(name,pwd).asJson.noSpaces
      Http.postJsonAndParse[LoginRsp](AccountRoute.loginRoute,data).map{rsp =>
        if(rsp.errCode == 0){
          LocalStorageUtil.storeUserInfo(AccountProtocol.NameStorage(rsp.uidOpt.get,name,true))
          val gamePlayHolder = new GamePlayHolder("GameView",PlayerInfo(rsp.uidOpt.get,name,true,None))
          modal := gamePlayHolder.getStartGameModal()
          dom.document.getElementById("loginBg").asInstanceOf[Div].setAttribute("class","invisible")
//          GameHall.playerName = name
//          gotoPage(s"#/gameHall")
          //          dom.window.location.hash = s"#/gameHall"
        }
        else{
          showWarning := true
          gotoPage(s"#/login")
          //          dom.window.location.hash = s"#/login"
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
        {modal}
        <div>{canvas}</div>
      </div>
      <div id="loginBg" class="visible" onkeydown = {e:KeyboardEvent => keyDownEnter(e)}>
        <div class="loginForm">
          <p id="loginTip">请登录</p>
          <p id="loginWar" style={displayOfP}>您输入的账户名或密码有误<button onclick={() => showWarning:=false}></button></p>
          <p><input type="text" id="account" placeholder="账号"></input></p>
          <p><input type="password" id="password" placeholder="密码"></input></p>
          <p>
            <button id="loginBtn" onclick={() => enter}>登录</button>
            <button id="registerBtn" onclick={() => gotoPage(s"#/register")}>注册</button>
            <button id="visitorBtn" onclick={() => gotoPage(s"#/visitor")}>游客</button>
          </p>
        </div>
      </div>
    </div>
  }



}


