package com.neo.sk.breakout.front.pages

import com.neo.sk.breakout.front.common.{Page, Routes}
import com.neo.sk.breakout.front.pages.control.GamePlayHolder
import com.neo.sk.breakout.front.utils.{Http, JsFunc}
import com.neo.sk.breakout.shared.ptcl.GameHallProtocol.GameModelReq
import com.neo.sk.breakout.shared.ptcl.SuccessRsp
import org.scalajs.dom

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


  def chooseModal(modal:Int):Unit = {
    val url = Routes.GameHall.chooseGameModelRoute
    val json = GameModelReq(playerName,modal).asJson.toString()
    Http.postJsonAndParse[SuccessRsp](url,json).map{rsp =>
      if(rsp.errCode == 0){
        val gameHolder = new GamePlayHolder(playerName)
        gameHolder.start(playerName,None)//建立websocket
        dom.window.location.hash = s"#/matchPlayer"
      }else{
        JsFunc.alert(s"模式选择失败")
      }
    }


  }



  override def render: Elem = {
    <div>BREAKOUT
      <button id="modal1" onclick={() => chooseModal(1)}>单机模式</button>
      <button id="modal2" onclick={() => chooseModal(2)}>双人模式</button>
    </div>
  }

}
