package com.neo.sk.breakout.front.pages

import com.neo.sk.breakout.front.common.{Page, Routes}
import com.neo.sk.breakout.front.model.PlayerInfo
import com.neo.sk.breakout.front.control.GamePlayHolder
import com.neo.sk.breakout.front.utils.{Http, Shortcut}
import com.neo.sk.breakout.shared.model
import com.neo.sk.breakout.shared.model.Constants
import org.scalajs.dom.KeyboardEvent
import org.scalajs.dom.ext.KeyCode
import org.scalajs.dom.html.Div
//import com.neo.sk.breakout.shared.protocol.BreakoutGameEvent.PlayerInfo
import com.neo.sk.breakout.shared.ptcl.AccountProtocol.LoginRsp
import com.neo.sk.breakout.shared.ptcl.GameHallProtocol
import mhtml.{Var, emptyHTML}
import org.scalajs.dom
import org.scalajs.dom.html.Input
import com.neo.sk.breakout.front.pages.MainPage.gotoPage
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Random
import scala.xml.Elem

/**
  * created by benyafang on 2019/2/20 14:20
  * */
object VisitorHall extends Page{
  import io.circe.generic.auto._
  import io.circe._
  import io.circe.syntax._

  private val modal = Var(emptyHTML)
  private val canvas = <canvas id="GameView" tabindex="1"></canvas>

  private def chooseRandomNickname():Unit = {
    val rand = (new Random).nextInt(Constants.nameList.length)
    dom.document.getElementById("randomInput").asInstanceOf[Input].value = Constants.nameList(rand)
  }

  private def confirmNickname():Unit = {
    val nickname = dom.document.getElementById("randomInput").asInstanceOf[Input].value
    val url = Routes.GameHall.getUId4VisitorRoute
    val json = GameHallProtocol.GetUId4Visitor(nickname,true).asJson.toString()
    Http.postJsonAndParse[LoginRsp](url,json).map{rsp =>
      if(rsp.errCode == 0){
        val gamePlayHolder = new GamePlayHolder("GameView",PlayerInfo(rsp.uidOpt.get,nickname,true,None))
        modal := gamePlayHolder.getStartGameModal()
        dom.document.getElementById("loginBg").asInstanceOf[Div].setAttribute("class","invisible")
      }else{
        confirmNickname()
      }
    }
  }

  private def keyDownEnter(e:KeyboardEvent ) ={
    if(e.keyCode == KeyCode.Enter){
      val nickname = dom.document.getElementById("randomInput").asInstanceOf[Input].value
      val url = Routes.GameHall.getUId4VisitorRoute
      val json = GameHallProtocol.GetUId4Visitor(nickname,true).asJson.toString()
      Http.postJsonAndParse[LoginRsp](url,json).map{rsp =>
        if(rsp.errCode == 0){
          val gamePlayHolder = new GamePlayHolder("GameView",PlayerInfo(rsp.uidOpt.get,nickname,true,None))
          modal := gamePlayHolder.getStartGameModal()
          dom.document.getElementById("loginBg").asInstanceOf[Div].setAttribute("class","invisible")
        }else{
          confirmNickname()
        }
      }
    }

  }

  override def render: Elem = {
    {Shortcut.scheduleOnce(() => chooseRandomNickname(),0)}
    <div>
      <div class="bgp">
        {modal}
        <div>{canvas}</div>
      </div>
      <div id="loginBg" class="visible">
        <div class="loginForm" style="width:700px">
          <input type="text" id="randomInput" readonly={if(Constants.needSpecialName)""else "readonly"}></input>
          <div style="width:800px">
            <button id="randomName" style="width:150px" onclick={() =>chooseRandomNickname()}>随机昵称</button>
            <button id="confirmName" style="width:150px" onclick={() =>confirmNickname()}>确定昵称</button>
            <button id="confirmName" style="width:150px" onclick={() =>gotoPage("#/login")}>返回登录</button>
          </div>
        </div>
      </div>

    </div>
  }

}
