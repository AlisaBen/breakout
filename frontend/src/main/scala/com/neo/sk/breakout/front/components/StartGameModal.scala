package com.neo.sk.breakout.front.components

import com.neo.sk.breakout.front.common.{Component, Routes}
import com.neo.sk.breakout.front.model.PlayerInfo
import com.neo.sk.breakout.front.pages.LoginPage
import com.neo.sk.breakout.front.utils.{Http, JsFunc}
import com.neo.sk.breakout.shared.model.Constants.GameState
import com.neo.sk.breakout.shared.ptcl.GameHallProtocol.GameModelReq
import com.neo.sk.breakout.shared.ptcl.SuccessRsp
import mhtml.Var

import scala.concurrent.ExecutionContext.Implicits.global
import scala.xml.Elem
import com.neo.sk.breakout.front.pages.MainPage.gotoPage
/**
  * created by benyafang on 2019/2/20 14:20
  * */
class StartGameModal(gameState:Var[Int],startGame:() => Unit, setGameState:Int => Unit,playerInfo:PlayerInfo) extends Component{

  import io.circe.generic.auto._
  import io.circe.syntax._

  def chooseModal(modal:Int):Unit = {
    if(modal == 1){
      JsFunc.alert("当前模式待开发")
    }else{
      val url = Routes.GameHall.chooseGameModelRoute
      val json = GameModelReq(playerInfo.userId,playerInfo.userName,playerInfo.isVisitor,modal).asJson.toString()
      Http.postJsonAndParse[SuccessRsp](url,json).map{rsp =>
        if(rsp.errCode == 0){
//          JsFunc.alert(s"模式选择成功")
//          LoginPage.canvasShow := true
          startGame()
        }else{
          JsFunc.alert(s"模式选择失败")
        }
      }
    }
  }

  val displayDiv =
    gameState.map{
      case GameState.firstCome => ""
//      case GameState.matching => "display:none"
      case GameState.loadingPlay => "display:none"
      case GameState.play => "display:none"
      case GameState.stop => "display:none"//游戏结束之后添加返回首页按钮，点击返回按钮之后，设置gameState状态为firstCome
    }

  val matchingDisplay = gameState.map{
    case GameState.firstCome => "display:none"
//    case GameState.matching => "display:block"
    case GameState.loadingPlay => "display:block"
    case GameState.play => "display:none"
    case GameState.stop => "display:none"//游戏结束之后添加返回首页按钮，点击返回按钮之后，设置gameState状态为firstCome
  }

  val comebackDisplay = gameState.map{
    case GameState.firstCome => "display:none"
    //    case GameState.matching => "display:block"
    case GameState.loadingPlay => "display:none"
    case GameState.play => "display:none"
    case GameState.stop => "display: block;"//游戏结束之后添加返回首页按钮，点击返回按钮之后，设置gameState状态为firstCome
  }

  private def comeback():Unit = {
    setGameState(GameState.firstCome)
//    LoginPage.canvasShow := false
  }

  private def quit():Unit = {
    gotoPage("#/login")
  }
  override def render: Elem = {
    <div>
      <div style={displayDiv}>
        <div id="title">BREAKOUT</div>
        <div style="width:65%;margin:auto">
          <button id="modal" class="singal" onclick={() => chooseModal(1)}>单机模式</button>
          <button id="modal" class="double" onclick={() => chooseModal(2)}>双人模式</button>
        </div>

      </div>
      <div id="match" style={matchingDisplay}>
        <img style="margin:auto;width:200px;display: block;" src="/breakout/static/img/loading.gif"></img>
        <div style="margin-top:3%;text-align:center;font-size:40px;width:500px">正在匹配玩家，请耐心等待</div>
      </div>
      <div style={comebackDisplay}>
        <button id="come_back" style="margin-top: 5px" onclick={() => comeback()}>返回首页</button>
        <button id="come_back" style="margin-top: 5px" onclick={() => quit()}>退出</button>
      </div>
    </div>
  }
}
