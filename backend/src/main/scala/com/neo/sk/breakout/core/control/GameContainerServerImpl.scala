package com.neo.sk.breakout.core.control

import java.util.concurrent.atomic.AtomicInteger

import akka.actor.typed.ActorRef
import com.neo.sk.breakout.core.{RoomActor, UserActor}
import com.neo.sk.breakout.shared.`object`.{Ball, Brick, Racket}
import com.neo.sk.breakout.shared.config.GameConfig
import com.neo.sk.breakout.shared.game.GameContainer
import com.neo.sk.breakout.shared.model.Point
import com.neo.sk.breakout.shared.protocol.BreakoutGameEvent
import javax.xml.ws.Dispatch
import org.slf4j.Logger

import scala.collection.mutable
import scala.util.Random

/**
  * created by benyafang on 2019/2/8 21:52
  * A4
  * 1.移动拍子事件--发射子弹事件
  * 2.生成拍子位置--
  * 3.生成拍子、障碍物、球
  * 4.处理碰撞--障碍物被袭击
  * 5.离开游戏
  * 6.接收用户行为事件
  * 7.初始化
  * 8.逻辑更新
  *
  * */
case class GameContainerServerImpl(
                                    config: GameConfig,
                                    log:Logger,
                                    roomActorRef:ActorRef[RoomActor.Command],
                                    dispatch: BreakoutGameEvent.WsMsgServer => Unit,
                                    dispatchTo: (String, BreakoutGameEvent.WsMsgServer) => Unit
                                  ) extends GameContainer {

  import scala.language.implicitConversions

  private val random = new Random(System.currentTimeMillis())

  override def debug(msg: String): Unit = log.debug(msg)

  override def info(msg: String): Unit = log.info(msg)

  private val ballIdGenerator = new AtomicInteger(100)
  private val racketIdGenerator = new AtomicInteger(100)
  private val brickIdGenerator = new AtomicInteger(100)

  def generateBrick(position:Point) = {
    val oId = brickIdGenerator.getAndIncrement()
    val brick = Brick(config,oId,position)
    val objects = quadTree.retrieveFilter(brick).filter(t => t.isInstanceOf[Ball] || t.isInstanceOf[Brick] || t.isInstanceOf[Racket])
    if (brick.isIntersectsObject(objects)){
      log.debug(s"砖块位置错误")
      None
    }else{
      Some(brick)
    }
  }

  def generateRacket(position:Point,name:String) = {
    val racketId = racketIdGenerator.getAndIncrement()
    val racket = Racket(racketId,0,false,config,position,name)
    val objects = quadTree.retrieveFilter(racket).filter(t => t.isInstanceOf[Ball] || t.isInstanceOf[Brick] || t.isInstanceOf[Racket])
    if(racket.isIntersectsObject(objects)){
      log.debug(s"拍子位置错误")
      None
    }else{
      Some(racket)
    }
  }

  def generateBall(position:Point,racketId:Int) = {
    val ballId = ballIdGenerator.getAndIncrement()
    val ball = Ball(config,position,0,config.ballSpeed,ballId,racketId)
    val objects = quadTree.retrieveFilter(ball).filter(t => t.isInstanceOf[Ball] || t.isInstanceOf[Brick] || t.isInstanceOf[Racket])
    if(ball.isIntersectsObject(objects)){
      log.debug(s"拍子位置错误")
      None
    }else{
      Some(ball)
    }
  }

  init()
  private def init(): Unit = {
    /**
      * 生成砖块、拍子、球
      * */
    val width = config.boundary.x / config.brickHorizontalNum
    (1 to config.brickVerticalNum).foreach{verticalIndex =>
      (1 to config.brickHorizontalNum).foreach{horizontalIndex =>
        val x = (horizontalIndex - 1) * width + width / 2
        val y = (verticalIndex - 1) * config.brickHeight +  config.brickHeight / 2
        val brickOpt= generateBrick(Point(x,y.toFloat))
        brickOpt match{
          case Some(brick) =>
            val event = BreakoutGameEvent.GenerateObstacle(systemFrame,brick.getObstacleState())
            addGameEvent(event)
            obstacleMap.put(brick.oId,brick)
            quadTree.insert(brick)
          case None =>
        }
      }
    }
    clearEventWhenUpdate()
  }

  def generateRacketAndBall(nameA:String,nameB:String,playerMap:mutable.HashMap[String,ActorRef[UserActor.Command]]): Unit = {
    val racketAOpt = generateRacket(Point(config.boundary.x,config.boundary.y - 10),nameA)
    val racketBOpt = generateRacket(Point(config.boundary.x,10),nameB)
    racketAOpt.foreach{racketA =>
      generateBall(Point(config.boundary.x,config.boundary.y - 10 - 10),racketA.racketId)
        .foreach{ball =>
          val event = BreakoutGameEvent.UserJoinRoom(nameA,racketA.getRacketState(),ball.getBallState(),systemFrame)
          dispatch(event)
          addGameEvent(event)
          racketMap.put(racketA.racketId,racketA)
          quadTree.insert(racketA)
          racketHistoryMap.put(racketA.racketId,racketA.name)
          ballMap.put(ball.bId,ball)
          quadTree.insert(ball)
          playerMap(nameA) ! UserActor.JoinRoomSuccess(racketA,config.getGameConfigImpl(),roomActorRef)
        }
    }
    racketBOpt.foreach{racketB =>
      generateBall(Point(config.boundary.x,config.boundary.y - 10 - 10),racketB.racketId)
        .foreach{ball =>
          val event = BreakoutGameEvent.UserJoinRoom(nameB,racketB.getRacketState(),ball.getBallState(),systemFrame)
          dispatch(event)
          addGameEvent(event)
          racketMap.put(racketB.racketId,racketB)
          quadTree.insert(racketB)
          racketHistoryMap.put(racketB.racketId,racketB.name)
          ballMap.put(ball.bId,ball)
          quadTree.insert(ball)
          playerMap(nameB) ! UserActor.JoinRoomSuccess(racketB,config.getGameConfigImpl(),roomActorRef)
        }
    }
  }

  def getGameContainerState(frameOnly:Boolean = false): BreakoutGameEvent.GameContainerAllState = {
    BreakoutGameEvent.GameContainerAllState (
      systemFrame,
      racketMap.values.map(_.getRacketState()).toList,
      ballMap.values.map(_.getBallState()).toList,
      obstacleMap.values.map(_.getObstacleState()).toList,
      racketMoveAction.toList.map(t => (t._1,if(t._2.isEmpty) None else Some(t._2.toList)))
    )
  }

  override protected def clearEventWhenUpdate(): Unit = {
    gameEventMap -= systemFrame - 1
    actionEventMap -= systemFrame - 1
    followEventMap -= systemFrame - 1
    systemFrame += 1
  }
}
