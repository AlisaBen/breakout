package com.neo.sk.breakout.front.pages

import com.neo.sk.breakout.front.common.Page
import mhtml.Var

import scala.xml.Elem

/**
  * created by benyafang on 2019/2/8
  * 玩家匹配：在同一个模式下的玩家进行随机匹配
  * A2
  * 该页面绘制游戏匹配页面
  * */
object MatchPlayer extends Page{
  private val canvas = <canvas id="GameView" tabindex="1"></canvas>

  private val modal = Var("匹配中")

  def changeModalState = {
    modal := ""
  }

  override def render: Elem = {
    <div>{modal}
      {canvas}
    </div>
  }

}
