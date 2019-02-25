package com.neo.sk.breakout.front.pages

import com.neo.sk.breakout.front.common.{Page, Routes}
import com.neo.sk.breakout.front.model.PlayerInfo
import com.neo.sk.breakout.front.control.GamePlayHolder
import com.neo.sk.breakout.front.utils.{Http, Shortcut}
import com.neo.sk.breakout.shared.model
import com.neo.sk.breakout.shared.model.Constants
import com.neo.sk.breakout.shared.model.Constants.GameState
import org.scalajs.dom.KeyboardEvent
import org.scalajs.dom.ext.KeyCode
import org.scalajs.dom.html.Div
import org.scalajs.dom.raw.MouseEvent
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
import com.neo.sk.breakout.front.pages.LoginPage.{comebackDisplay,setComebackButtonShow}

/**
  * created by benyafang on 2019/2/20 14:20
  * */
object VisitorHall extends Page{
  import io.circe.generic.auto._
  import io.circe._
  import io.circe.syntax._

  private val modal = Var(emptyHTML)
  private val canvas = <canvas id="GameView" tabindex="1"></canvas>
//  private val comebackButtonShow = Var(false)
  var gamePlayHolder:GamePlayHolder = _
  val loginFormShow = Var(true)

//  def setComebackButtonShow(boolean: Boolean) = comebackButtonShow := boolean

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
        loginFormShow := false
        gamePlayHolder = new GamePlayHolder("GameView",PlayerInfo(rsp.uidOpt.get,nickname,true,None))
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
          loginFormShow := false
          gamePlayHolder = new GamePlayHolder("GameView",PlayerInfo(rsp.uidOpt.get,nickname,true,None))
          modal := gamePlayHolder.getStartGameModal()
          dom.document.getElementById("loginBg").asInstanceOf[Div].setAttribute("class","invisible")
        }else{
          confirmNickname()
        }
      }
    }

  }

  private def comeback2FirstPage(e:MouseEvent):Unit = {
    gamePlayHolder.canvas.setHeight(0)
    gamePlayHolder.canvas.setWidth(0)
    gamePlayHolder.setGameState(GameState.firstCome)
    gamePlayHolder.gameContainerOpt = None
    setComebackButtonShow(false)
    e.preventDefault()
  }

  private def quit(e:MouseEvent):Unit = {
    modal := emptyHTML
    gamePlayHolder.canvas.setHeight(0)
    gamePlayHolder.canvas.setWidth(0)
    gamePlayHolder.setGameState(GameState.firstCome)
    gamePlayHolder.gameContainerOpt = None
    gamePlayHolder.closeHolder
    setComebackButtonShow(false)
    gotoPage("#/login")
    e.preventDefault()
  }
//  private val comebackDisplay = comebackButtonShow.map{
//    case true =>"display:block;margin-left:10%"
//    case false =>"display:none"
//  }

  private val loginFormDisplay = loginFormShow.map{
    case true =>"display:block;width:480px;margin:auto"
    case false =>"display:none"
  }

  override def render: Elem = {
    {Shortcut.scheduleOnce(() => chooseRandomNickname(),0)}
    <div>
      <div class="bgp">
        {modal}
        <div style="margin: auto;width: 500px;">
          <button class="quitBtn" name="game_over_btn1" style={comebackDisplay} onclick={e:MouseEvent =>comeback2FirstPage(e)}>返回首页</button>
          <br></br>
          <button  class="quitBtn" name="game_over_btn2" style={comebackDisplay} onclick={e:MouseEvent => quit(e)}>退出</button>
          {canvas}
        </div>
      </div>
      <div id="loginBg" class="visible"  style="position: relative;z-index: 100000" >
        <div class="loginForm" style={loginFormDisplay}>
          <div style="width:800px;margin-bottom:10%">
            <img src="/breakout/static/img/warn.png" style="width: 30px;height: 30px;"></img>
            游客登录将会缺失部分游戏功能，降低游戏体验
          </div>
          <input type="text" id="randomInput" readonly={if(Constants.needSpecialName)""else "readonly"} style="display:block;margin:auto;margin-bottom:10%"></input>
          <div style="width:480px">
            <button id="randomName" style="width:150px" onclick={() =>chooseRandomNickname()}>随机昵称</button>
            <button id="confirmName" style="width:150px" onclick={() =>confirmNickname()}>确定昵称</button>
            <button id="confirmName" style="width:150px" onclick={() =>gotoPage("#/login")}>返回登录</button>
          </div>
        </div>
      </div>

    </div>
  }

}
