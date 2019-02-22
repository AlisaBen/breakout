package com.neo.sk.breakout.front.components

import com.neo.sk.breakout.front.common.{Component, Routes}
import com.neo.sk.breakout.front.model.PlayerInfo
import com.neo.sk.breakout.front.utils.{Http, JsFunc}
import com.neo.sk.breakout.shared.model.Constants.GameState
import com.neo.sk.breakout.shared.ptcl.GameHallProtocol.GameModelReq
import com.neo.sk.breakout.shared.ptcl.SuccessRsp
import mhtml.Var

import scala.concurrent.ExecutionContext.Implicits.global
import scala.xml.Elem

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
    case GameState.loadingPlay => ""
    case GameState.play => "display:none"
    case GameState.stop => "display:none"//游戏结束之后添加返回首页按钮，点击返回按钮之后，设置gameState状态为firstCome
  }

  val comebackDisplay = gameState.map{
    case GameState.firstCome => "display:none"
    //    case GameState.matching => "display:block"
    case GameState.loadingPlay => "display:none"
    case GameState.play => "display:none"
    case GameState.stop => ""//游戏结束之后添加返回首页按钮，点击返回按钮之后，设置gameState状态为firstCome
  }

  override def render: Elem = {
    <div>
      <div style={displayDiv}>
        <div style="margin-bottom:300px">BREAKOUT</div>
        <button id="modal" onclick={() => chooseModal(1)}>单机模式</button>
        <br></br>
        <button id="modal" onclick={() => chooseModal(2)}>双人模式</button>
      </div>
      <div style={matchingDisplay}>
        <div style="text-align:center;font-size:40px">匹配中。。。</div>
      </div>
      <div style={comebackDisplay}>
        <button id="come_back" style="position:absolute;left:30px;top:30px;" onclick={() => setGameState(GameState.firstCome)}>返回首页</button>

      </div>
    </div>
  }
}
