package com.neo.sk.breakout.front.control

import java.util.concurrent.atomic.AtomicInteger

import com.neo.sk.breakout.front.common.{Constants, Routes}
import com.neo.sk.breakout.front.components.StartGameModal
import com.neo.sk.breakout.front.model.PlayerInfo
import com.neo.sk.breakout.front.pages.LoginPage
import com.neo.sk.breakout.front.utils.{JsFunc, Shortcut}
import com.neo.sk.breakout.shared.`object`.Racket
import com.neo.sk.breakout.shared.game.GameContainerClientImpl
import com.neo.sk.breakout.shared.model.Constants.GameState
import com.neo.sk.breakout.shared.model.Point
import com.neo.sk.breakout.shared.protocol.BreakoutGameEvent
import com.neo.sk.breakout.shared.protocol.BreakoutGameEvent.GenerateObstacle
import org.scalajs.dom.raw.MouseEvent
//import com.sun.glass.events.MouseEvent
import com.neo.sk.breakout.front.pages.MainPage.gotoPage
import org.scalajs.dom
import org.scalajs.dom.ext.KeyCode
import org.scalajs.dom.html.Button
import org.scalajs.dom.raw.TouchEvent

import scala.collection.mutable
import scala.xml.Elem

/**
  * created by benyafang on 2019/2/8 20:55
  * 游戏监听、消息处理
  * A3
  * */
class GamePlayHolder(canvasName:String,playerInfo:PlayerInfo) extends GameHolder(canvasName) {
  private[this] val actionSerialNumGenerator = new AtomicInteger(0)
  private val preExecuteFrameOffset = com.neo.sk.breakout.shared.model.Constants.PreExecuteFrameOffset
  private val startGameModal = new StartGameModal(gameStateVar, start,comeback2FirstPage,playerInfo)
  private var lastTouchMoveFrame = 0L

  private val myKeySet = mutable.HashSet[Int]()

  private def changeKeys(k: Int): Int = k match {
    case KeyCode.W => KeyCode.Up
    case KeyCode.S => KeyCode.Down
    case KeyCode.A => KeyCode.Left
    case KeyCode.D => KeyCode.Right
    case origin => origin
  }

  object MoveSpace{
    val LEFT = 1
    val RIGHT = 2
  }

  def getActionSerialNum: Byte = (actionSerialNumGenerator.getAndIncrement()%127).toByte

  def getStartGameModal(): Elem = {
    startGameModal.render
  }

  def setCanvasName(canvasName:String) = {

  }

  def start(): Unit = {
    canvas.getCanvas.focus()
    if (firstCome) {
      firstCome = false
      setGameState(GameState.loadingPlay)
      val isVisitor:Int = if(playerInfo.isVisitor) 1 else 0
      webSocketClient.setup(Routes.getJoinGameWebSocketUri(playerInfo.userId,playerInfo.userName,isVisitor, None))
      addUserActionListenEvent()
//      if(playerInfo.isVisitor){
//      }else{
//        webSocketClient.setup(Routes.getJoinGameWebSocketUri(playerInfo.userId,playerInfo.userName,0, None))
//      }
      //      webSocketClient.sendMsg(TankGameEvent.StartGame(roomIdOpt,None))

      gameLoop()

    } else if (webSocketClient.getWsState) {
      setGameState(GameState.loadingPlay)
      init()
      gameLoop()
//      addUserActionListenEvent()

    } else {
      JsFunc.alert("网络连接失败，请重新刷新")
    }
  }
//  var touchFlag = 0//0--没有触摸，1--touchstart,2--touchmove,3--touchend

  var touchStartX :Double= 0
  var touchMoveEndX :Double= 0
  var mouseMove:Boolean = false

//  private def handleTouchStart(e:TouchEvent) = {
//    println(s"------------------touchstart")
//    touchStartX = e.touches.item(0).clientX
//    e.preventDefault()
//  }

//  private def handleTouchMove(e:TouchEvent) = {
//    println(s"=============touchmove")
//    touchMoveEndX = e.changedTouches.item(0).clientX
//    if(gameState == GameState.play && gameContainerOpt.nonEmpty && lastTouchMoveFrame != gameContainerOpt.get.systemFrame){
//      if(touchMoveEndX - touchStartX > 0){
//        val preExecuteAction = BreakoutGameEvent.UserTouchMove(gameContainerOpt.get.racketId,
//          gameContainerOpt.get.systemFrame + preExecuteFrameOffset,MoveSpace.RIGHT.toByte,getActionSerialNum)
//        lastTouchMoveFrame = gameContainerOpt.get.systemFrame
//        gameContainerOpt.get.preExecuteUserEvent(preExecuteAction)
//        sendMsg2Server(preExecuteAction)
//      }else if(touchMoveEndX - touchStartX < 0){
//        val preExecuteAction = BreakoutGameEvent.UserTouchMove(gameContainerOpt.get.racketId,
//          gameContainerOpt.get.systemFrame + preExecuteFrameOffset,MoveSpace.LEFT.toByte,getActionSerialNum)
//        lastTouchMoveFrame = gameContainerOpt.get.systemFrame
//        gameContainerOpt.get.preExecuteUserEvent(preExecuteAction)
//        sendMsg2Server(preExecuteAction)
//      }
//    }
//    touchStartX = touchMoveEndX
//    e.preventDefault()
//
//  }

//  private def handleTouchEnd(e:TouchEvent) = {
//    println(s"touchend")
//    touchStartX = 0
//    touchMoveEndX = 0
//    val preExecuteAction = BreakoutGameEvent.UserTouchEnd(gameContainerOpt.get.racketId,
//      gameContainerOpt.get.systemFrame + preExecuteFrameOffset,getActionSerialNum)
//    gameContainerOpt.get.preExecuteUserEvent(preExecuteAction)
//    sendMsg2Server(preExecuteAction)
//    e.preventDefault()
//  }

  private def addUserActionListenEvent(): Unit = {
    canvas.getCanvas.focus()
    canvas.getCanvas.onmousedown  = {e: dom.MouseEvent =>
      println(s"------------------touchstart")
      touchStartX = e.clientX
      //fixme 需要确定这个是不是需要
      e.preventDefault()
      mouseMove = true

    }
    canvas.getCanvas.onmousemove  = {e:dom.MouseEvent =>
      println(s"=============touchmove")
      touchMoveEndX = e.clientX
      if(gameState == GameState.play && mouseMove && gameContainerOpt.nonEmpty && lastTouchMoveFrame != gameContainerOpt.get.systemFrame){
        if(touchMoveEndX - touchStartX > 0){
          val preExecuteAction = BreakoutGameEvent.UserTouchMove(gameContainerOpt.get.racketId,
            gameContainerOpt.get.systemFrame + preExecuteFrameOffset,MoveSpace.RIGHT.toByte,getActionSerialNum)
          lastTouchMoveFrame = gameContainerOpt.get.systemFrame
          gameContainerOpt.get.preExecuteUserEvent(preExecuteAction)
          sendMsg2Server(preExecuteAction)
        }else if(touchMoveEndX - touchStartX < 0){
          val preExecuteAction = BreakoutGameEvent.UserTouchMove(gameContainerOpt.get.racketId,
            gameContainerOpt.get.systemFrame + preExecuteFrameOffset,MoveSpace.LEFT.toByte,getActionSerialNum)
          lastTouchMoveFrame = gameContainerOpt.get.systemFrame
          gameContainerOpt.get.preExecuteUserEvent(preExecuteAction)
          sendMsg2Server(preExecuteAction)
        }
//        val preExecuteAction = BreakoutGameEvent.UserTouchMove(gameContainerOpt.get.racketId,
//          gameContainerOpt.get.systemFrame + preExecuteFrameOffset,e.clientX.toFloat,getActionSerialNum)
//        lastTouchMoveFrame = gameContainerOpt.get.systemFrame
//        gameContainerOpt.get.preExecuteUserEvent(preExecuteAction)
//        sendMsg2Server(preExecuteAction)
      }
      touchStartX = touchMoveEndX
      e.preventDefault()
    }
    canvas.getCanvas.onmouseup  = {e:dom.MouseEvent =>
      println(s"----dragend")
      mouseMove = false
      touchStartX = 0
      touchMoveEndX = 0
      val preExecuteAction = BreakoutGameEvent.UserTouchEnd(gameContainerOpt.get.racketId,
        gameContainerOpt.get.systemFrame + preExecuteFrameOffset,getActionSerialNum)
      gameContainerOpt.get.preExecuteUserEvent(preExecuteAction)
      sendMsg2Server(preExecuteAction)
      e.preventDefault()
    }
//    canvas.getCanvas.addEventListener("touchstart",handleTouchStart,false)
//    canvas.getCanvas.addEventListener("touchmove",handleTouchMove,false)
//    canvas.getCanvas.addEventListener("touchend",handleTouchEnd,false)
  }

  override protected def setKillCallback(racket: Racket) = {
    if (gameContainerOpt.nonEmpty&&racket.racketId ==gameContainerOpt.get.racketId) {
//      webSocketClient.sendMsg(BreakoutGameEvent.FailGame)
//      gameContainerOpt.foreach(_)
      val button1 = dom.document.getElementsByName("game_over_btn1").asInstanceOf[Button]
//      button1.setAttribute("class","visible")
      button1.onclick = {e:MouseEvent =>
        setGameState(GameState.firstCome)
        gameContainerOpt = None
        e.preventDefault()
      }
      val button2 = dom.document.getElementsByName("game_over_btn2").asInstanceOf[Button]
//      button1.setAttribute("class","visible")
      button2.onclick = {e:MouseEvent =>
        gameContainerOpt = None
        e.preventDefault()
      }
      setGameState(GameState.stop)
      LoginPage.setComebackButtonShow(true)
    }
  }

  override protected def wsMessageHandler(data: BreakoutGameEvent.WsMsgServer): Unit = {
    println(s"${data.getClass}")
    data match {
      case e: BreakoutGameEvent.WsSuccess =>
        webSocketClient.sendMsg(BreakoutGameEvent.StartGame(e.roomId))

      case e:BreakoutGameEvent.GenerateObstacle =>
        gameContainerOpt.foreach(_.receiveGameEvent(e))

      case e: BreakoutGameEvent.YourInfo =>
        println(s"new game the id is ${e.players}")
        println(s"玩家信息${e}")
//        setGameState(GameState.loadingPlay)
        timer = Shortcut.schedule(gameLoop, e.config.frameDuration / e.config.playRate)

        /**
          * 更新游戏数据
          **/
        gameContainerOpt = Some(GameContainerClientImpl(drawFrame, ctx, e.config, e.players.racketId, e.players.name, canvasBoundary, canvasUnit, setKillCallback))
        gameContainerOpt.get.changeRacketId(e.players.racketId)
        addUserActionListenEvent()

      case e: BreakoutGameEvent.GameOver =>
//        dom.window.cancelAnimationFrame(nextFrame)
        gameContainerOpt.foreach(_.updateRank(e.score))
        ctx.clearRect(0,0,canvasWidth,canvasHeight)
//        firstCome = true
//        gameContainerOpt.foreach(_.)
//        closeHolder
        Shortcut.cancelSchedule(timer)
        setGameState(GameState.stop)
        LoginPage.setComebackButtonShow(true)

      case e: BreakoutGameEvent.SyncGameAllState =>
        gameContainerOpt.foreach(_.receiveGameContainerAllState(e.gState))
        nextFrame = dom.window.requestAnimationFrame(gameRender())
        setGameState(GameState.play)

      case e:BreakoutGameEvent.SyncGameState =>
        gameContainerOpt.foreach(_.receiveGameContainerState(e.state))

      case e: BreakoutGameEvent.UserActionEvent =>
//        e match {
//          case e:BreakoutGameEvent.UserTouchMove=>
//            //fixme
//            if(gameContainerOpt.nonEmpty){
//              if(gameContainerOpt.get.myRacketId != e.racketId){
//                gameContainerOpt.foreach(_.receiveUserEvent(e))
//              }
//            }
//          case _=>
//            gameContainerOpt.foreach(_.receiveUserEvent(e))
//        }
        gameContainerOpt.foreach(_.receiveUserEvent(e))

      case e: BreakoutGameEvent.GameEvent =>
        gameContainerOpt.foreach(_.receiveGameEvent(e))

      case e: BreakoutGameEvent.PingPackage =>
        receivePingPackage(e)

      case _ => println(s"unknow msg={sss}")
    }
  }
}
