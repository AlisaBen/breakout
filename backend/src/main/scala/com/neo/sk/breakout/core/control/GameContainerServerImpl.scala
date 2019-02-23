package com.neo.sk.breakout.core.control

import java.util.concurrent.atomic.AtomicInteger

import akka.actor.typed.scaladsl.TimerScheduler
import com.neo.sk.breakout.Boot.roomManager
import com.neo.sk.breakout.core.RoomActor.ActorStop
import com.neo.sk.breakout.core.RoomManager
import com.neo.sk.breakout.core.RoomManager.GameBattleRecord

//import akka.actor.TimerScheduler
import akka.actor.typed.ActorRef
//import com.neo.sk.breakout.core.RoomActor.GameBattleRecord
import com.neo.sk.breakout.core.{RoomActor, UserActor}
import com.neo.sk.breakout.shared.`object`._
import com.neo.sk.breakout.shared.config.GameConfig
import com.neo.sk.breakout.shared.game.GameContainer
import com.neo.sk.breakout.shared.model.Constants.ObstacleType
import com.neo.sk.breakout.shared.model.{Point, Score}
import com.neo.sk.breakout.shared.protocol.BreakoutGameEvent
import com.neo.sk.breakout.shared.protocol.BreakoutGameEvent.ObstacleCollision
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
                                    timer: TimerScheduler[RoomActor.Command],
                                    roomId:Int,
                                    roomActorRef:ActorRef[RoomActor.Command],
                                    dispatch: BreakoutGameEvent.WsMsgServer => Unit,
                                    dispatchTo: (Long, BreakoutGameEvent.WsMsgServer) => Unit
                                  ) extends GameContainer {

  import scala.language.implicitConversions

  private val random = new Random(System.currentTimeMillis())

  override def debug(msg: String): Unit = log.debug(msg)

  override def info(msg: String): Unit = log.info(msg)

  private val ballIdGenerator = new AtomicInteger(100)
  private val racketIdGenerator = new AtomicInteger(100)
  private val obstacleIdGenerator = new AtomicInteger(100)

  private val racketId2UserActorMap = mutable.HashMap[Int,Long]()

  def generateBrick(position:Point,racketId:Int) = {
    val oId = obstacleIdGenerator.getAndIncrement()
    val brick = new Brick(config,ObstacleState(racketId,oId,ObstacleType.brick,position,0))
//    val objects = quadTree.retrieveFilter(brick).filter(t => t.isInstanceOf[Ball] || t.isInstanceOf[Brick] || t.isInstanceOf[Racket])
//    if (brick.isIntersectsObject(objects)){
//      log.debug(s"砖块位置错误")
//      None
//    }else{
      if(true) Some(brick) else None
//    }
  }

  def generateRacket(position:Point,uid:Long,name:String) = {
    val racketId = racketIdGenerator.getAndIncrement()
    val racket = new Racket(config,RacketState(racketId,uid,name,position,0,false,0))
//    val objects = quadTree.retrieveFilter(racket).filter(t => t.isInstanceOf[Ball] || t.isInstanceOf[Brick] || t.isInstanceOf[Racket])
//    if(racket.isIntersectsObject(objects)){
//      log.debug(s"拍子位置错误")
//      None
//    }else{
      if(true) Some(racket) else None
//    }
  }

  def generateBall(position:Point,racketId:Int,direction:Float) = {
    val ballId = ballIdGenerator.getAndIncrement()
    val ball = new Ball(config,BallState(ballId,racketId,position,config.ballSpeed.rotate(direction)))
//    val objects = quadTree.retrieveFilter(ball).filter(t => t.isInstanceOf[Ball] || t.isInstanceOf[Brick] || t.isInstanceOf[Racket])
//    if(ball.isIntersectsObject(objects)){
//      log.debug(s"拍子位置错误")
//      None
//    }else{
    if(true){
      Some(ball)
    }else None

//    }
  }

  def generateRacketAndBall(nameA:String,nameB:String,uidA:Long,uidB:Long,playerMap:mutable.HashMap[Long,ActorRef[UserActor.Command]]): Unit = {
    clearEventWhenUpdate()
    def generateBricks4Racket(racketId:Int) = {
      val width = (config.boundary.x - config.brickHorizontalNum * 2 * config.brickSpace - 2 * config.brickSpace) / config.brickHorizontalNum
      val fakeWidth = width + 2 * config.brickSpace
      val fakeHeight = config.brickHeight + 2 * config.brickSpace
      (1 to config.brickVerticalNum).foreach{verticalIndex =>
        (1 to config.brickHorizontalNum).foreach{horizontalIndex =>
          //        val fakeWidth = width + 2 * config.brickSpace
          val x = (horizontalIndex - 1) * fakeWidth + fakeWidth / 2
          //        val fakeHeight = config.brickHeight + 2 * config.brickSpace
          val y = (verticalIndex - 1) * fakeHeight +  fakeHeight / 2 + config.getRankHeight
          val brickOpt= generateBrick(Point(x,y.toFloat),racketId)
          brickOpt match{
            case Some(brick) =>
              val event = BreakoutGameEvent.GenerateObstacle(systemFrame,brick.getObstacleState())
              addGameEvent(event)
              obstacleMap.put(brick.oId,brick)
              quadTree.insert(brick)
            case None =>
              log.debug(s"${roomActorRef.path} 生成砖块错误")
          }
        }
      }
    }
    val racketAOpt = generateRacket(Point(config.boundary.x / 2,(config.boundary.y - config.getRacketHeight / 2 - 3).toFloat),uidA,nameA)//自己
    val racketBOpt = generateRacket(Point(config.boundary.x / 2,(config.boundary.y - config.getRacketHeight / 2 - 3).toFloat),uidB,nameB)//对方
    if(racketAOpt.nonEmpty && racketBOpt.nonEmpty){
      racketId2UserActorMap.put(racketAOpt.get.racketId,uidA)
      racketId2UserActorMap.put(racketBOpt.get.racketId,uidB)
      playerMap(uidA) ! UserActor.JoinRoomSuccess(racketAOpt.get,config.getGameConfigImpl(),roomActorRef)
      playerMap(uidB) ! UserActor.JoinRoomSuccess(racketBOpt.get,config.getGameConfigImpl(),roomActorRef)
      racketAOpt.foreach{racketA =>
        var randDirection = (new Random).nextFloat() * math.Pi.toFloat / 2 + math.Pi.toFloat * 5 / 4
        while (randDirection == 2*math.Pi || randDirection == math.Pi|| randDirection == 0){
          randDirection = (new Random).nextFloat() * math.Pi.toFloat / 2 + math.Pi.toFloat * 5 / 4
        }
        generateBall(Point(config.boundary.x / 2,(config.boundary.y - config.getRacketHeight / 2 - 3 - config.getRacketHeight / 2 - config.getBallRadius).toFloat),racketA.racketId,randDirection)
          .foreach{ball =>
//            val event = BreakoutGameEvent.UserJoinRoom(nameA,racketA.getRacketState(),ball.getBallState(),systemFrame)
//            dispatch(event)
//            addGameEvent(event)
            racketMap.put(racketA.racketId,racketA)
            quadTree.insert(racketA)
            racketHistoryMap.put(racketA.racketId,racketA.name)
            ballMap.put(ball.bId,ball)
            quadTree.insert(ball)
            log.debug(s"${roomActorRef.path} 发送加入房间成功的消息")
//            playerMap(nameA) ! UserActor.JoinRoomSuccess(racketA,config.getGameConfigImpl(),roomActorRef)
          }
        generateBricks4Racket(racketA.racketId)
      }
      racketBOpt.foreach{racketB =>
        var randDirection = (new Random).nextFloat() * math.Pi.toFloat / 2 + math.Pi.toFloat * 5 / 4
        while (randDirection == 2*math.Pi || randDirection == math.Pi || randDirection == 0){
          randDirection = (new Random).nextFloat() * math.Pi.toFloat / 2 + math.Pi.toFloat * 5 / 4
        }
        generateBall(Point(config.boundary.x / 2,(config.boundary.y - config.getRacketHeight / 2 - 3 - config.getRacketHeight / 2 - config.getBallRadius - 5).toFloat),racketB.racketId,randDirection)
          .foreach{ball =>
//            val event = BreakoutGameEvent.UserJoinRoom(nameB,racketB.getRacketState(),ball.getBallState(),systemFrame)
//            dispatch(event)
//            addGameEvent(event)
            racketMap.put(racketB.racketId,racketB)
            quadTree.insert(racketB)
            racketHistoryMap.put(racketB.racketId,racketB.name)
            ballMap.put(ball.bId,ball)
            quadTree.insert(ball)
//            playerMap(nameB) ! UserActor.JoinRoomSuccess(racketB,config.getGameConfigImpl(),roomActorRef)
          }
        generateBricks4Racket(racketB.racketId)

      }
      val state = getGameContainerAllState()
      dispatch(BreakoutGameEvent.SyncGameAllState(state))
    }else{
      log.debug(s"${roomActorRef.path}生成拍子错误")
    }

  }

  override protected def gameOverCallBack(racket: Racket): Unit = {
    val gameOverEvent = BreakoutGameEvent.GameOver(racketMap.values.map(t => Score(t.racketId,t.name,t.damageStatistics)).toList)
    timer.cancel(RoomActor.GameLoopKey)
    dispatch(gameOverEvent)
    roomActorRef ! ActorStop
    roomManager !RoomManager.GameOver(roomId)
    roomManager ! GameBattleRecord(racketMap.values.map(t => Score(t.racketId,t.name,t.damageStatistics)).toList)
  }

  override protected def handleObstacleCollision(e:ObstacleCollision) :Unit = {
    super.handleObstacleCollision(e)
    val obstacleState = ObstacleState(e.enemyRacketId,obstacleIdGenerator.getAndIncrement(),ObstacleType.brick,
      Point(e.obstaclePosition.x,e.obstaclePosition.y - 2 * (e.obstaclePosition.y - config.boundary.y / 2)),0)
    val event = BreakoutGameEvent.GenerateObstacle(systemFrame,obstacleState)
    racketId2UserActorMap.get(obstacleState.racketId).foreach(a => dispatchTo(a,event))
    //fixme
    //该事件应该对应发，而不是群发
//    addGameEvent(event)
    obstacleMap.put(obstacleState.oId,obstacleState)
    quadTree.insert(obstacleState)
  }

  implicit def obstacleState2Impl(o:ObstacleState):Obstacle = new Brick(config,o)

  def getGameContainerAllState(frameOnly:Boolean = false): BreakoutGameEvent.GameContainerAllState = {
    BreakoutGameEvent.GameContainerAllState (
      systemFrame,
      racketMap.values.map(_.getRacketState()).toList,
      ballMap.values.map(_.getBallState()).toList,
      obstacleMap.values.map(_.getObstacleState()).toList,
      racketMoveAction.toList.map(t => (t._1,if(t._2.isEmpty) None else Some(t._2.toList)))
    )
  }

  def getGameContainerState(frameOnly:Boolean = false): BreakoutGameEvent.GameContainerState = {
    BreakoutGameEvent.GameContainerState(
      systemFrame,
      if(frameOnly) None else Some(racketMap.values.map(_.getRacketState()).toList),
      if(frameOnly) None else Some(racketMoveAction.toList.map(t => (t._1,if(t._2.isEmpty) None else Some(t._2.toList))))
    )
  }

  override protected def clearEventWhenUpdate(): Unit = {
    gameEventMap -= systemFrame - 1
    actionEventMap -= systemFrame - 1
    followEventMap -= systemFrame - 1
    systemFrame += 1
  }

  def receiveUserAction(preExecuteUserAction: BreakoutGameEvent.UserActionEvent): Unit = {
    //    println(s"receive user action preExecuteUserAction frame=${preExecuteUserAction.frame}----system fram=${systemFrame}")
    val f = math.max(preExecuteUserAction.frame, systemFrame)
    if (preExecuteUserAction.frame != f) {
      log.debug(s"preExecuteUserAction fame=${preExecuteUserAction.frame}, systemFrame=${systemFrame}")
    }
    val action = preExecuteUserAction match {
      case a: BreakoutGameEvent.UserTouchMove => a.copy(frame = f)
      case a:BreakoutGameEvent.UserTouchEnd => a.copy(frame = f)
    }

    addUserAction(action)
    dispatch(action)
  }
}
