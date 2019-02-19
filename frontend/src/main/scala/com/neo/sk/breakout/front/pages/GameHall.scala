package com.neo.sk.breakout.front.pages

import com.neo.sk.breakout.front.common.{Page, Routes}
import com.neo.sk.breakout.front.pages.control.GamePlayHolder
import com.neo.sk.breakout.front.utils.{Http, JsFunc, Shortcut}
import com.neo.sk.breakout.shared.ptcl.GameHallProtocol.GameModelReq
import com.neo.sk.breakout.shared.ptcl.SuccessRsp
import org.scalajs.dom
import com.neo.sk.breakout.front.pages.MainPage._
import com.neo.sk.breakout.shared.model.Constants.GameState
import mhtml.Var

import scala.xml.Elem
import scala.concurrent.ExecutionContext.Implicits.global
/**
  * created by benyafang on 2019/2/8
  * 游戏大厅
  * 用户登录之后选择游戏模式：对战模式、人机模式
  * A1
  * */
object GameHall extends Page{
  import io.circe.generic.auto._
  import io.circe._
  import io.circe.syntax._
  var playerName = ""
  private val canvas = <canvas id="GameView" tabindex="1"></canvas>

  var gameHolder:GamePlayHolder = _

  var chooseFlag = false

  private val modal = Var("匹配中")

  def chooseModal(modal:Int):Unit = {
    if(modal == 1){
      JsFunc.alert("当前模式待开发")
      //      gotoPage("#/")
    }else{
      gameHolder.start(playerName,None)//建立websocket
      chooseFlag = true
      val url = Routes.GameHall.chooseGameModelRoute
      val json = GameModelReq(playerName,modal).asJson.toString()
      Http.postJsonAndParse[SuccessRsp](url,json).map{rsp =>
        if(rsp.errCode == 0){
          println(s"=====")
//          gotoPage("#/matchPlayer")
          //          dom.window.location.hash = s"#/matchPlayer"
        }else{
          JsFunc.alert(s"模式选择失败")
        }
      }
    }
  }

  val displayDiv = gameHolder.gameState match{
    case GameState.firstCome => ""
    case GameState.loadingPlay => "display:none"
    case GameState.play => "display:none"
    case GameState.stop => ""
  }

  val canvasDisplayDiv = gameHolder.gameState match{
    case GameState.firstCome => "display:none"
    case GameState.loadingPlay => "display:block"
    case GameState.play => "display:block"
    case GameState.stop => "display:block"
  }

  def init() = {
    gameHolder = new GamePlayHolder("GameView")
  }

  val modalDisplayDiv = gameHolder.gameState match{
    case GameState.firstCome => if(chooseFlag)"display:block"else "display:none"
    case GameState.loadingPlay => "display:none"
    case GameState.play => "display:none"
    case GameState.stop => "display:none"
  }




  override def render: Elem = {
    Shortcut.scheduleOnce(() => init(),0)
    <div id="gameHall">
      <div style={displayDiv}>
        <div style="margin-bottom:300px">BREAKOUT</div>
        <button id="modal" onclick={() => chooseModal(1)}>单机模式</button>
        <br></br>
        <button id="modal" onclick={() => chooseModal(2)}>双人模式</button>
      </div>
      <div style={modalDisplayDiv}>
        {modal}
      </div>
      <div style={canvasDisplayDiv}>
        {canvas}
      </div>
    </div>
  }

}
