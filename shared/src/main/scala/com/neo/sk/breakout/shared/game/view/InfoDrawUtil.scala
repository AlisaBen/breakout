package com.neo.sk.breakout.shared.game.view

import com.neo.sk.breakout.shared.game.GameContainerClientImpl
import com.neo.sk.breakout.shared.model.Point
import com.neo.sk.breakout.shared.util.canvas.MiddleContext

/**
  * Created by sky
  * Date on 2018/11/21
  * Time at 下午4:03
  * 本文件中实现canvas绘制提示信息
  * C
  */
trait InfoDrawUtil {this:GameContainerClientImpl =>
//  private val combatImg = this.drawFrame.createImage("/img/dead.png")
//  private val winImg=drawFrame.createImage("/img/冠军.png")
//  private val failImg=drawFrame.createImage("/img/失败表情.png")

  def drawGameLoading():Unit = {
    ctx.setFill("rgb(0,0,0)")
    ctx.fillRec(0, 0, canvasSize.x * canvasUnit, canvasSize.y * canvasUnit)
    ctx.setFill("rgb(250, 250, 250)")
    ctx.setTextAlign("left")
    ctx.setTextBaseline("top")
    ctx.setFont(s"Helvetica","normal",3.6 * canvasUnit)
    ctx.fillText("请稍等，正在连接服务器", 150, 180)
  }

  def drawGameStop():Unit = {
    ctx.setFill("rgb(0,0,0)")
    ctx.fillRec(0, 0, canvasSize.x * canvasUnit, canvasSize.y * canvasUnit)
    ctx.setFill("rgb(250, 250, 250)")
    ctx.setTextAlign("left")
    ctx.setTextBaseline("top")
    ctx.setFont(s"Helvetica","normal",3.6 * canvasUnit)
    ctx.fillText(s"您已经死亡,被玩家=${this.killerName}所杀,等待倒计时进入游戏", 150, 180)
    println()
  }

  def drawUserLeftGame:Unit = {
    ctx.setFill("rgb(0,0,0)")
    ctx.fillRec(0, 0, canvasSize.x * canvasUnit, canvasSize.y * canvasUnit)
    ctx.setFill("rgb(250, 250, 250)")
    ctx.setTextAlign("left")
    ctx.setTextBaseline("top")
    ctx.setFont(s"Helvetica","normal",3.6 * canvasUnit)
    ctx.fillText(s"您已经离开该房间。", 150, 180)
    println()
  }

  def drawReplayMsg(m:String):Unit = {
    ctx.setFill("rgb(0,0,0)")
    ctx.fillRec(0, 0, canvasSize.x * canvasUnit, canvasSize.y * canvasUnit)
    ctx.setFill("rgb(250, 250, 250)")
    ctx.setTextAlign("left")
    ctx.setTextBaseline("top")
    ctx.setFont(s"Helvetica","normal",3.6 * canvasUnit)
    ctx.fillText(m, 150, 180)
    println()
  }

  def drawGameRestart(countDownTimes:Int,killerName:String): Unit = {
    ctx.setFill("rgb(0,0,0)")
    ctx.setTextAlign("center")
    ctx.setFont("楷体", "normal", 5 * canvasUnit)
    ctx.fillRec(0, 0, canvasSize.x * canvasUnit, canvasSize.y * canvasUnit)
    ctx.setFill("rgb(0,0,0)")
    ctx.fillText(s"重新进入房间，倒计时：${countDownTimes}", 300, 100)
    ctx.fillText(s"您已经死亡,被玩家=${killerName}所杀", 300, 180)
  }

  def drawDeadImg(s:String) = {
    ctx.setFill("rgb(0,0,0)")
    ctx.fillRec(0, 0, canvasSize.x * canvasUnit, canvasSize.y * canvasUnit)
    ctx.setFill("rgb(250, 250, 250)")
    ctx.setTextAlign("left")
    ctx.setTextBaseline("top")
    ctx.setFont("Helvetica","normal",36)
    ctx.fillText(s"$s", 150, 180)
  }
  private def clearScreen(color:String, alpha:Double, width:Float = canvasSize.x, height:Float = canvasSize.y, middleCanvas:MiddleContext , start:Point = Point(0,0)):Unit = {
    middleCanvas.setFill(color)
    middleCanvas.setGlobalAlpha(alpha)
    middleCanvas.fillRec(start.x * canvasUnit, start.y * canvasUnit,  width * this.canvasUnit, height * this.canvasUnit)
    middleCanvas.setGlobalAlpha(1)
  }
  def drawCombatGains():Unit = {
    clearScreen("#548B54",1, canvasSize.x, canvasSize.y, ctx)
    ctx.setFont("Arial", "normal", 4 * canvasUnit)
    ctx.setGlobalAlpha(1)
    ctx.setTextAlign("left")
    ctx.setFill("rgb(0,0,0)")
    var index = 0
//    ctx.fillText(s"${racketMap(0).name}:${racketMap(0).damageStatistics}  vs  ${racketMap(1).name}:${racketMap(1).damageStatistics}",canvasSize.x / 2 * canvasUnit,  (canvasSize.y / 2 + index)* canvasUnit)

    racketMap.foreach{score =>
      ctx.fillText(s"${score._2.name}:${score._2.damageStatistics}",canvasSize.x / 2 * canvasUnit,  (canvasSize.y / 2 + index)* canvasUnit)
      index += 6
    }
    ctx.fillText(s"vs",canvasSize.x / 2 * canvasUnit,  (canvasSize.y / 2 + (index - 3))* canvasUnit)

    //    ctx.fillText(s"",0.4 * canvasSize.x * canvasUnit, 0.12 * canvasSize.y * canvasUnit)
//    ctx.fillText(s"Damage：", 0.4 * canvasSize.x * canvasUnit, 0.2 * canvasSize.y * canvasUnit)
//    ctx.fillText(s"Killer：",0.4 * canvasSize.x * canvasUnit, 0.26 * canvasSize.y * canvasUnit)
//    ctx.fillText(s"Press Enter To Comeback!!!",0.4 * canvasSize.x * canvasUnit, 0.32 * canvasSize.y * canvasUnit)
    ctx.setFill("rgb(255,0,0)")
//    ctx.fillText(s"${this.killNum}", 0.5 * canvasSize.x * canvasUnit, 0.12 * canvasSize.y * canvasUnit)
//    ctx.fillText(s"${this.damageNum}",0.5 * canvasSize.x * canvasUnit, 0.2 * canvasSize.y * canvasUnit)
//    var pos = 0.5 * canvasSize.x * canvasUnit
//    this.killerList.foreach{r =>
//      ctx.fillText(s"【${r}】", pos, 0.26 * canvasSize.y * canvasUnit)
//      pos = pos + 2 * canvasUnit * s"【${r}】".length + 1 * canvasUnit}
//    ctx.drawImage(combatImg,0.25 * canvasSize.x * canvasUnit,0.1 * canvasSize.y * canvasUnit,Some(pos - 0.25 * canvasSize.x * canvasUnit + 2 * canvasUnit,0.22 * canvasSize.y * canvasUnit))
    //    ctx.drawImage(combatImg,0.25 * canvasSize.x * canvasUnit,0.1 * canvasSize.y * canvasUnit,Some(0.5* canvasSize.x * canvasUnit,0.22 * canvasSize.y * canvasUnit))

  }
}
