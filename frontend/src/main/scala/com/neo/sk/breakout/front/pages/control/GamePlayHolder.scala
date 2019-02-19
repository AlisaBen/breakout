package com.neo.sk.breakout.front.pages.control

import java.util.concurrent.atomic.AtomicInteger

import com.neo.sk.breakout.front.common.{Constants, Routes}
import com.neo.sk.breakout.front.pages.MatchPlayer
import com.neo.sk.breakout.front.utils.{JsFunc, Shortcut}
import com.neo.sk.breakout.shared.`object`.Racket
import com.neo.sk.breakout.shared.game.GameContainerClientImpl
import com.neo.sk.breakout.shared.model.Constants.GameState
import com.neo.sk.breakout.shared.model.Point
import com.neo.sk.breakout.shared.protocol.BreakoutGameEvent
import com.sun.glass.events.MouseEvent
import org.scalajs.dom
import org.scalajs.dom.ext.KeyCode
import org.scalajs.dom.raw.TouchEvent

import scala.collection.mutable
import scala.xml.Elem

/**
  * created by benyafang on 2019/2/8 20:55
  * 游戏监听、消息处理
  * A3
  * */
class GamePlayHolder(name:String) extends GameHolder(name) {
  private[this] val actionSerialNumGenerator = new AtomicInteger(0)
  private val preExecuteFrameOffset = com.neo.sk.breakout.shared.model.Constants.PreExecuteFrameOffset
//  private val startGameModal = new StartGameModal(gameStateVar, start, name)
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

//  def getStartGameModal(): Elem = {
//    startGameModal.render
//  }

  def start(name: String, roomIdOpt: Option[Long]): Unit = {
    canvas.getCanvas.focus()
//    dom.window.cancelAnimationFrame(nextFrame)
//    Shortcut.cancelSchedule(timer)
    if (firstCome) {
      addUserActionListenEvent()
      setGameState(GameState.loadingPlay)
      webSocketClient.setup(Routes.getJoinGameWebSocketUri(name, roomIdOpt))
      //      webSocketClient.sendMsg(TankGameEvent.StartGame(roomIdOpt,None))

      gameLoop()

    } else if (webSocketClient.getWsState) {
      setGameState(GameState.loadingPlay)
      gameLoop()

    } else {
      JsFunc.alert("网络连接失败，请重新刷新")
    }
  }
//  var touchFlag = 0//0--没有触摸，1--touchstart,2--touchmove,3--touchend

  var touchStartX :Double= 0
  var touchMoveEndX :Double= 0

  private def handleTouchStart(e:TouchEvent) = {
    println(s"------------------")
    touchStartX = e.touches.item(0).clientX
    //fixme 需要确定这个是不是需要
    e.preventDefault()
  }

  private def handleTouchMove(e:TouchEvent) = {
    println(s"=============")
    touchMoveEndX = e.changedTouches.item(0).clientX
    if(gameState == GameState.play && gameContainerOpt.nonEmpty && lastTouchMoveFrame != gameContainerOpt.get.systemFrame){
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
    }
    touchStartX = touchMoveEndX
    e.preventDefault()

  }

  private def handleTouchEnd(e:TouchEvent) = {
    touchStartX = 0
    touchMoveEndX = 0
    val preExecuteAction = BreakoutGameEvent.UserTouchEnd(gameContainerOpt.get.racketId,
      gameContainerOpt.get.systemFrame + preExecuteFrameOffset,getActionSerialNum)
    gameContainerOpt.get.preExecuteUserEvent(preExecuteAction)
    sendMsg2Server(preExecuteAction)
    e.preventDefault()
  }

  private def addUserActionListenEvent(): Unit = {
    canvas.getCanvas.focus()
    canvas.getCanvas.addEventListener("touchstart",handleTouchStart,false)
    canvas.getCanvas.addEventListener("touchmove",handleTouchMove,false)
    canvas.getCanvas.addEventListener("touchend",handleTouchEnd,false)
  }

  override protected def setKillCallback(racket: Racket) = {
    if (gameContainerOpt.nonEmpty&&racket.racketId ==gameContainerOpt.get.racketId) {
      setGameState(GameState.stop)
    }
  }

  override protected def wsMessageHandler(data: BreakoutGameEvent.WsMsgServer): Unit = {
    println(s"${data.getClass}")
    data match {
      case e: BreakoutGameEvent.WsSuccess =>
        webSocketClient.sendMsg(BreakoutGameEvent.StartGame(e.roomId))

      case e: BreakoutGameEvent.YourInfo =>
        println(s"new game the id is ${e.players}")
        println(s"玩家信息${e}")
        timer = Shortcut.schedule(gameLoop, e.config.frameDuration / e.config.playRate)
        MatchPlayer.changeModalState
        /**
          * 更新游戏数据
          **/
        gameContainerOpt = Some(GameContainerClientImpl(drawFrame, ctx, e.config, e.players.racketId, e.players.name, canvasBoundary, canvasUnit, setKillCallback))
        gameContainerOpt.get.changeRacketId(e.players.racketId)

      case e: BreakoutGameEvent.GameOver =>
        setGameState(GameState.stop)

      case e: BreakoutGameEvent.SyncGameAllState =>
        //fixme
        gameContainerOpt.foreach(_.receiveGameContainerAllState(e.gState))
        if(firstCome){
          firstCome = false
//          dom.window.cancelAnimationFrame(nextFrame)
          nextFrame = dom.window.requestAnimationFrame(gameRender())
          setGameState(GameState.play)
        }

      case e: BreakoutGameEvent.UserActionEvent =>
        e match {
          case e:BreakoutGameEvent.UserTouchMove=>
            //fixme
            if(gameContainerOpt.nonEmpty){
              if(gameContainerOpt.get.myRacketId != e.racketId){
                gameContainerOpt.foreach(_.receiveUserEvent(e))
              }
            }
          case _=>
            gameContainerOpt.foreach(_.receiveUserEvent(e))
        }

      case e: BreakoutGameEvent.GameEvent =>
        gameContainerOpt.foreach(_.receiveGameEvent(e))

      case e: BreakoutGameEvent.PingPackage =>
        receivePingPackage(e)

      case _ => println(s"unknow msg={sss}")
    }
  }
}
