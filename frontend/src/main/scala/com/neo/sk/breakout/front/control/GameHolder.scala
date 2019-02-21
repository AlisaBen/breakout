package com.neo.sk.breakout.front.control

import com.neo.sk.breakout.front.common.Constants
import com.neo.sk.breakout.front.model.PlayerInfo
import com.neo.sk.breakout.front.utils.{JsFunc, Shortcut}
import com.neo.sk.breakout.front.utils.canvas.MiddleFrameInJs
import com.neo.sk.breakout.shared.`object`.Racket
import com.neo.sk.breakout.shared.game.GameContainerClientImpl
import com.neo.sk.breakout.shared.model.Constants.GameState
import com.neo.sk.breakout.shared.model.Point
import com.neo.sk.breakout.shared.protocol.BreakoutGameEvent
import mhtml.Var
import org.scalajs.dom
import org.scalajs.dom.raw.{Event, VisibilityState}

/**
  * created by benyafang on 2019/2/8 22;14
  * A3
  * */

abstract class GameHolder(canvasName:String)  extends NetworkInfo  {
  val drawFrame = new MiddleFrameInJs
  protected var canvasWidth = dom.window.innerWidth.toFloat
  protected var canvasHeight = dom.window.innerHeight.toFloat

  protected val canvas = drawFrame.createCanvas(canvasName,canvasWidth, canvasHeight)
  protected val ctx = canvas.getCtx


  protected var canvasUnit = getCanvasUnit(canvasWidth)
  protected var canvasBoundary = Point(canvasWidth, canvasHeight) / canvasUnit

  protected var tickCount = 1//更新排行榜信息计时器
  protected val rankCycle = 20

  protected def setKillCallback(racket: Racket):Unit
//  //  protected val audioForBgm = dom.document.getElementById("GameAudioForBgm").asInstanceOf[Audio]
//  //  audioForBgm.volume = 0.3
//  //  protected val audioForDead = dom.document.getElementById("GameAudioForDead").asInstanceOf[Audio]
//  //  protected val audioForBullet = dom.document.getElementById("GameAudioForBullet").asInstanceOf[Audio]
//  //  var needBgm = true
//
//  println(s"test111111111111=${canvasUnit},=${canvasWidth}")
//
//  //  protected var killNum:Int = 0
//  //  protected var damageNum:Int = 0
//  //  var killerList = List.empty[String] //（击杀者）
//  var versionInfoOpt:Option[String]=None
//  val versionScript = dom.document.getElementById("js-version")
//  try {
//    versionScript match {
//      case script: Script =>
//        versionInfoOpt=Some(script.src.split("id=")(1))
//      case _ =>
//    }
//  }catch {case exception: Exception=>
//    println(exception.getCause)
//  }
//
//
  protected var firstCome = true

  protected val gameStateVar: Var[Int] = Var(GameState.firstCome)
  var gameState: Int = GameState.firstCome
//
//  //  protected var killerName:String = ""
//
//
  protected var gameContainerOpt: Option[GameContainerClientImpl] = None // 这里存储tank信息，包括tankId

  //fixme
  protected val webSocketClient: WebSocketClient = WebSocketClient(wsConnectSuccess, wsConnectError, wsMessageHandler, wsConnectClose, setDateSize )


  protected var timer: Int = 0
//  //  protected var reStartTimer:Int = 0
//  /**
//    * 倒计时，config
//    **/
//  protected val reStartInterval = 1000
//  protected val countDown = 3
//  protected var countDownTimes = countDown
  protected var nextFrame = 0
  protected var logicFrameTime = System.currentTimeMillis()

  //fixme 此处打印渲染时间
  /*private var renderTime:Long = 0
//  private var renderTimes = 0
//
//  Shortcut.schedule( () =>{
//    if(renderTimes != 0){
//      println(s"render page use avg time:${renderTime / renderTimes}ms")
//    }else{
//      println(s"render page use avg time:0 ms")
//    }
//    renderTime = 0
//    renderTimes = 0
//  }, 5000L)*/

  private def onVisibilityChanged = { e: Event =>
    if (dom.document.visibilityState == VisibilityState.visible) {
      println("change tab into current")
//      onCurTabEventCallback
    } else {
      println("has change tab")
    }
  }

//  protected def onCurTabEventCallback={
//    webSocketClient.sendMsg(BreakoutGameEvent.GetSyncGameState)
//  }

//  dom.window.addEventListener("visibilitychange", onVisibilityChanged, false)

  def closeHolder = {
    dom.window.cancelAnimationFrame(nextFrame)
    Shortcut.cancelSchedule(timer)
    webSocketClient.closeWs
  }

  protected def gameRender(): Double => Unit = { d =>
    import com.neo.sk.breakout.front.common.Constants
    val curTime = System.currentTimeMillis()
    val offsetTime = curTime - logicFrameTime
    drawGame(offsetTime,Constants.supportLiveLimit)
    if(gameState == GameState.stop) gameContainerOpt.foreach(_.drawCombatGains())
    nextFrame = dom.window.requestAnimationFrame(gameRender())
  }


  protected def setGameState(s: Int): Unit = {
    gameStateVar := s
    gameState = s
  }

  protected def sendMsg2Server(msg: BreakoutGameEvent.WsMsgFront): Unit = {
    if (gameState == GameState.play)
      webSocketClient.sendMsg(msg)
  }

  protected def checkScreenSize = {
    val newWidth = dom.window.innerWidth.toFloat
    val newHeight = dom.window.innerHeight.toFloat
    if (newWidth != canvasWidth || newHeight != canvasHeight) {
      println("the screen size is change")
      canvasWidth = newWidth
      canvasHeight = newHeight
      canvasUnit = getCanvasUnit(canvasWidth)
      canvasBoundary = Point(canvasWidth, canvasHeight) / canvasUnit
      println(s"update screen=${canvasUnit},=${(canvasWidth, canvasHeight)}")
      canvas.setWidth(canvasWidth.toInt)
      canvas.setHeight(canvasHeight.toInt)
      gameContainerOpt.foreach { r =>
        r.updateClientSize(canvasBoundary, canvasUnit)
      }
    }
  }

  protected def gameLoop(): Unit = {
    checkScreenSize
    gameState match {
      case GameState.loadingPlay =>
        println(s"等待同步数据")
        gameContainerOpt.foreach(_.drawGameLoading())
      case GameState.play =>

        /** */
        if(tickCount % rankCycle == 1){
//          gameContainerOpt.foreach(_.updateRanks())
          gameContainerOpt.foreach(t => t.rankUpdated = true)
        }
        gameContainerOpt.foreach(_.update())
        logicFrameTime = System.currentTimeMillis()
        ping()
        tickCount += 1

      case GameState.stop =>
//        dom.document.getElementById("input_mask_id").asInstanceOf[dom.html.Div].focus()
//        if(tickCount % rankCycle == 1){
//          gameContainerOpt.foreach(_.updateRanks())
//          gameContainerOpt.foreach(t => t.rankUpdated = true)
//        }
        gameContainerOpt.foreach{r =>
          r.update()
//          if(!r.isKillerAlive(r.getCurTankId)){
//            val newWatchId = r.change2OtherTank
//            r.changeTankId(newWatchId)
//          }
        }
        logicFrameTime = System.currentTimeMillis()
        ping()
//        tickCount += 1


      case _ => println(s"state=$gameState failed")
    }
  }

  private def drawGame(offsetTime: Long,supportLiveLimit:Boolean = false) = {
    gameContainerOpt.foreach(_.drawGame(offsetTime, getNetworkLatency,dataSizeList,supportLiveLimit))
  }

  protected def wsConnectSuccess(e: Event) = {
    println(s"连接服务器成功")
    e
  }

  protected def wsConnectError(e: Event) = {
    JsFunc.alert("网络连接失败，请重新刷新")
    e
  }

  protected def wsConnectClose(e: Event) = {
    JsFunc.alert("网络连接失败，请重新刷新")
    e
  }

  protected def wsMessageHandler(data: BreakoutGameEvent.WsMsgServer):Unit


  protected def getCanvasUnit(canvasWidth: Float): Int = (canvasWidth / Constants.WindowView.x).toInt
}
