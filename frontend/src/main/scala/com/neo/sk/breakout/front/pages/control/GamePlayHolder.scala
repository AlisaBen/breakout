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
  private var spaceKeyUpState = true
  private var lastMouseMoveAngle: Byte = 0
  private val perMouseMoveFrame = 3
  private var lastMoveFrame = -1L
  private val poKeyBoardMoveTheta = 2 * math.Pi / 72 //炮筒顺时针转
  private val neKeyBoardMoveTheta = -2 * math.Pi / 72 //炮筒逆时针转
  private var poKeyBoardFrame = 0L
  private var eKeyBoardState4AddBlood = true
  private val preExecuteFrameOffset = com.neo.sk.breakout.shared.model.Constants.PreExecuteFrameOffset
//  private val startGameModal = new StartGameModal(gameStateVar, start, name)

  private val watchKeys = Set(
    KeyCode.Left,
    KeyCode.Up,
    KeyCode.Right,
    KeyCode.Down
  )

  private val gunAngleAdjust = Set(
    KeyCode.K,
    KeyCode.L
  )

  private val myKeySet = mutable.HashSet[Int]()

  private def changeKeys(k: Int): Int = k match {
    case KeyCode.W => KeyCode.Up
    case KeyCode.S => KeyCode.Down
    case KeyCode.A => KeyCode.Left
    case KeyCode.D => KeyCode.Right
    case origin => origin
  }

  def getActionSerialNum: Byte = (actionSerialNumGenerator.getAndIncrement()%127).toByte

//  def getStartGameModal(): Elem = {
//    startGameModal.render
//  }

  def start(name: String, roomIdOpt: Option[Long]): Unit = {
    canvas.getCanvas.focus()
    dom.window.cancelAnimationFrame(nextFrame)
    Shortcut.cancelSchedule(timer)
    if (firstCome) {
      firstCome = false
      addUserActionListenEvent()
      setGameState(GameState.loadingPlay)
      webSocketClient.setup(Routes.getJoinGameWebSocketUri(name, None, roomIdOpt))
      //      webSocketClient.sendMsg(TankGameEvent.StartGame(roomIdOpt,None))

      gameLoop()

    } else if (webSocketClient.getWsState) {
      gameContainerOpt match {
        case Some(gameContainer) =>
//          gameContainerOpt.foreach(_.changeTankId(gameContainer.myTankId))
          if (Constants.supportLiveLimit) {
//            webSocketClient.sendMsg(BreakoutGameEvent.RestartGame(Some(gameContainer.myTankId), name))
          } else {
//            webSocketClient.sendMsg(BreakoutGameEvent.RestartGame(None, name))
          }

        case None =>
//          webSocketClient.sendMsg(BreakoutGameEvent.RestartGame(None, name))
      }
      setGameState(GameState.loadingPlay)
      gameLoop()

    } else {
      JsFunc.alert("网络连接失败，请重新刷新")
    }
  }

  private def handleTouchStart(e:TouchEvent) = {

  }

  private def handleTouchMove(e:TouchEvent) = {

  }

  private def handleTouchEnd(e:TouchEvent) = {

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
    data match {
      case e: BreakoutGameEvent.WsSuccess =>
        webSocketClient.sendMsg(BreakoutGameEvent.StartGame(e.roomId))

      case e: BreakoutGameEvent.YourInfo =>
        println(s"new game the id is ${e.players}")
        println(s"玩家信息${e}")
        timer = Shortcut.schedule(gameLoop, e.config.frameDuration / e.config.playRate)
        MatchPlayer.changeModalState
        //        audioForBgm.play()
        /**
          * 更新游戏数据
          **/
//        drawFrame: MiddleFrame,
//    ctx: MiddleContext,
//    override val config: GameConfig,
//    myRacketId: Int,
//    myName: String,
//    var canvasSize: Point,
//    var canvasUnit: Int,
//    setKillCallback: Racket => Unit
        gameContainerOpt = Some(GameContainerClientImpl(drawFrame, ctx, e.config, e.players.racketId, e.players.name, canvasBoundary, canvasUnit, setKillCallback))
//        gameContainerOpt.get(e.tankId)
      //        gameContainerOpt.foreach(e =>)

//      case e: BreakoutGameEvent.TankFollowEventSnap =>
//        println(s"game TankFollowEventSnap =${e} systemFrame=${gameContainerOpt.get.systemFrame} tankId=${gameContainerOpt.get.myTankId} ")
//        gameContainerOpt.foreach(_.receiveTankFollowEventSnap(e))

      case e: BreakoutGameEvent.GameOver =>

        /**
          * 死亡重玩
          **/
//        gameContainerOpt.foreach(_.updateDamageInfo(e.killTankNum, e.name, e.damageStatistics))
        //        dom.window.cancelAnimationFrame(nextFrame)
        //        gameContainerOpt.foreach(_.drawGameStop())
//        if ((Constants.supportLiveLimit && !e.hasLife) || (!Constants.supportLiveLimit)) {
          setGameState(GameState.stop)
//          gameContainerOpt.foreach(_.changeTankId(e.tankId))
          //          audioForBgm.pause()
          //          audioForDead.play()
//        }

      case e: BreakoutGameEvent.SyncGameAllState =>
        //fixme
        gameContainerOpt.foreach(_.receiveGameContainerAllState(e.gState))

      case e: BreakoutGameEvent.SyncGameAllState =>
        gameContainerOpt.foreach(_.receiveGameContainerAllState(e.gState))
        dom.window.cancelAnimationFrame(nextFrame)
        nextFrame = dom.window.requestAnimationFrame(gameRender())
        setGameState(GameState.play)

      case e: BreakoutGameEvent.UserActionEvent =>
        e match {
          case e:BreakoutGameEvent.UserTouchMove=>
//            if(gameContainerOpt.nonEmpty){
//              if(gameContainerOpt.get.myTankId!=e.tankId){
//                gameContainerOpt.foreach(_.receiveUserEvent(e))
//              }
//            }
          case _=>
            gameContainerOpt.foreach(_.receiveUserEvent(e))
        }


      case e: BreakoutGameEvent.GameEvent =>
        gameContainerOpt.foreach(_.receiveGameEvent(e))
//        e match {
//          case e: BreakoutGameEvent.UserRelive =>
//            if (e.userId == gameContainerOpt.get.myId) {
//              dom.window.cancelAnimationFrame(nextFrame)
//              nextFrame = dom.window.requestAnimationFrame(gameRender())
//            }
//          case _ =>
//        }

      case e: BreakoutGameEvent.PingPackage =>
        receivePingPackage(e)

//      case BreakoutGameEvent.RebuildWebSocket =>
//        gameContainerOpt.foreach(_.drawReplayMsg("存在异地登录。。"))
//        closeHolder

      case _ => println(s"unknow msg={sss}")
    }
  }
}
