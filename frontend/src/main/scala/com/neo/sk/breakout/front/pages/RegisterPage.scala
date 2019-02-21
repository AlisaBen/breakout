package com.neo.sk.breakout.front.pages

import com.neo.sk.breakout.front.common.Page
import com.neo.sk.breakout.front.common.Routes.AccountRoute
import com.neo.sk.breakout.front.utils.{Http, LocalStorageUtil}
import com.neo.sk.breakout.shared.ptcl.{AccountProtocol, SuccessRsp}
import mhtml.Var
import org.scalajs.dom
import org.scalajs.dom.ext.KeyCode
import org.scalajs.dom.{KeyboardEvent, html}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.xml.Elem
import com.neo.sk.breakout.front.pages.MainPage._

object RegisterPage extends Page{
  import io.circe.generic.auto._
  import io.circe._
  import io.circe.syntax._

  val showWarning = Var(false)

  def enter:Unit = {
    val name = dom.window.document.getElementById("account").asInstanceOf[html.Input].value
    val pwd = dom.window.document.getElementById("password").asInstanceOf[html.Input].value
    val pwd2 = dom.window.document.getElementById("confirm_password").asInstanceOf[html.Input].value
    val data =  AccountProtocol.RegisterReq(name,pwd,System.currentTimeMillis()).asJson.noSpaces
    if(pwd != pwd2){
      showWarning := true
    }else{
      Http.postJsonAndParse[SuccessRsp](AccountRoute.registerRoute,data).map{rsp =>
        if(rsp.errCode == 0){
//          LocalStorageUtil.storeUserInfo(AccountProtocol.NameStorage(name))
          //todo
          dom.window.location.hash = s"#/login"
        }
        else{
          showWarning := true
          //todo
          dom.window.location.hash = s"#/login"
        }
      }
    }
  }

  def keyDownEnter(e:KeyboardEvent):Unit = {
    if(e.keyCode == KeyCode.Enter) {
      val name = dom.window.document.getElementById("account").asInstanceOf[html.Input].value
      val pwd = dom.window.document.getElementById("password").asInstanceOf[html.Input].value
      val pwd2 = dom.window.document.getElementById("confirm_password").asInstanceOf[html.Input].value
      val data = AccountProtocol.RegisterReq(name,pwd,System.currentTimeMillis()).asJson.noSpaces
      if(pwd != pwd2){
        showWarning := true
      }else{
        Http.postJsonAndParse[SuccessRsp](AccountRoute.registerRoute,data).map{rsp =>
          if(rsp.errCode == 0){
//            LocalStorageUtil.storeUserInfo(AccountProtocol.NameStorage(name))
            //todo
            dom.window.location.hash = s"#/login"
          }
          else{
            showWarning := true
            //todo
            dom.window.location.hash = s"#/login"
          }
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
        <div class="loginForm">
          <p id="loginTip">请注册</p>
          <p id="loginWar" style={displayOfP}>您输入的密码不一致<button onclick={() => showWarning:=false}></button></p>
          <p><input type="text" id="account" placeholder="账号"></input></p>
          <p><input type="password" id="password" placeholder="密码"></input></p>
          <p><input type="confirm_password" id="confirm_password" placeholder="重新输入密码"></input></p>
          <p>
            <button id="loginBtn" onclick={() => enter}>注册</button>
            <button id="registerBtn" onclick={() => gotoPage(s"#/login")}>登录</button>
          </p>

        </div>
      </div>
    </div>
  }


}