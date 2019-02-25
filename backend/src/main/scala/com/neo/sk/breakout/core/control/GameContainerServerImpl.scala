package com.neo.sk.breakout.core.control

import java.util.concurrent.atomic.AtomicInteger

import akka.actor.typed.scaladsl.TimerScheduler
import com.neo.sk.breakout.Boot.roomManager
import com.neo.sk.breakout.core.RoomActor.ActorStop
import com.neo.sk.breakout.core.RoomManager
import com.neo.sk.breakout.core.RoomManager.GameBattleRecord
import com.neo.sk.breakout.protocol.EsheepProtocol.PlayerInfo
import com.neo.sk.breakout.shared.ptcl.GameHallProtocol.GameModelReq

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
                                    userList:List[GameModelReq],
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

  def generateBrick(position:Point,racketId:Int,brickValue:Int,obstacleType:Byte) = {
    val oId = obstacleIdGenerator.getAndIncrement()
    val brick = new Brick(config,ObstacleState(racketId,oId,obstacleType,position,brickValue))
      if(true) Some(brick) else None
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

  def generateRacketAndBall(nameA:GameModelReq,nameB:GameModelReq,playerMap:mutable.HashMap[Long,ActorRef[UserActor.Command]]): Unit = {
    clearEventWhenUpdate()
    //fixme class上加nameA和nameB的信息 gameContainerOpt设置playerInfo
//    println(s"--name--${nameA}---${nameB}")

    def generateBricks4Racket(racketId:Int,playerInfo: GameModelReq) = {
      val width = (config.boundary.x - config.brickHorizontalNum * 2 * config.brickSpace - 2 * config.brickSpace) / config.brickHorizontalNum
      val fakeWidth = width + 2 * config.brickSpace
      val fakeHeight = config.brickHeight + 2 * config.brickSpace
      var fastMoveIndex = List[(Int,Int)]()
      val fastMoveNum = (new Random()).nextInt(config.brickVerticalNum) + 1
//      println(s"--visitor-${fastMoveNum}-${playerInfo.isVisitor}")
      if(!playerInfo.isVisitor){
        (0 to fastMoveNum).foreach{index =>
          var randomXIndex = (new Random()).nextInt(config.brickHorizontalNum) + 1
          var randomYIndex =(new Random()).nextInt(config.brickVerticalNum) + 1
          if(fastMoveIndex.contains((randomXIndex,randomYIndex))){
            randomXIndex = (new Random()).nextInt(config.brickHorizontalNum) + 1
            randomYIndex =(new Random()).nextInt(config.brickVerticalNum) + 1
          }else{
            fastMoveIndex = (randomXIndex,randomYIndex) :: fastMoveIndex
          }
        }
      }
//      log.debug(s"${roomActorRef.path} 快消道具的索引：${fastMoveIndex}")
      (1 to config.brickVerticalNum).foreach{verticalIndex =>
        (1 to config.brickHorizontalNum).foreach{horizontalIndex =>
          //        val fakeWidth = width + 2 * config.brickSpace
          val x = (horizontalIndex - 1) * fakeWidth + fakeWidth / 2
          //        val fakeHeight = config.brickHeight + 2 * config.brickSpace
          val y = (verticalIndex - 1) * fakeHeight +  fakeHeight / 2 + config.getRankHeight
          val brickOpt = if(fastMoveIndex.contains((horizontalIndex,verticalIndex))){
//          val brickOpt = if(!playerInfo.isVisitor){
              generateBrick(Point(x,y.toFloat),racketId,config.brickValue,ObstacleType.fastRemove)
            }else{
              generateBrick(Point(x,y.toFloat),racketId,2 * config.brickValue,ObstacleType.brick)
            }
//          }
//          val brickOpt= generateBrick(Point(x,y.toFloat),racketId)
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
    val racketAPosition = Point(config.boundary.x / 2,config.boundary.y - config.getRacketHeight / 2 - 5)
    val racketBPosition = Point(config.boundary.x / 2,config.boundary.y - config.getRacketHeight / 2 - 5)
    val racketAOpt = generateRacket(racketAPosition,nameA.uid,nameA.name)//自己
    val racketBOpt = generateRacket(racketBPosition,nameB.uid,nameB.name)//对方
    if(racketAOpt.nonEmpty && racketBOpt.nonEmpty){
      racketId2UserActorMap.put(racketAOpt.get.racketId,nameA.uid)
      racketId2UserActorMap.put(racketBOpt.get.racketId,nameB.uid)
      playerMap(nameA.uid) ! UserActor.JoinRoomSuccess(racketAOpt.get,config.getGameConfigImpl(),roomActorRef)
      playerMap(nameB.uid) ! UserActor.JoinRoomSuccess(racketBOpt.get,config.getGameConfigImpl(),roomActorRef)
      racketAOpt.foreach{racketA =>
        var randDirection = (new Random).nextFloat() * math.Pi.toFloat / 2 + math.Pi.toFloat * 5 / 4
        while (randDirection == 2*math.Pi || randDirection == math.Pi|| randDirection == 0 || randDirection == math.Pi / 2){
          randDirection = (new Random).nextFloat() * math.Pi.toFloat / 2 + math.Pi.toFloat * 5 / 4
        }
        generateBall(racketAPosition - Point(0,config.getRacketHeight / 2 + config.getBallRadius),racketA.racketId,randDirection)
          .foreach{ball =>
//            val event = BreakoutGameEvent.UserJoinRoom(nameA,racketA.getRacketState(),ball.getBallState(),systemFrame)
//            dispatch(event)
//            addGameEvent(event)
            racketMap.put(racketA.racketId,racketA)
            quadTree.insert(racketA)
            racketHistoryMap.put(racketA.racketId,racketA.name)
            ballMap.put(ball.bId,ball)
            quadTree.insert(ball)
//            log.debug(s"${roomActorRef.path} 发送加入房间成功的消息")
//            playerMap(nameA) ! UserActor.JoinRoomSuccess(racketA,config.getGameConfigImpl(),roomActorRef)
          }
        generateBricks4Racket(racketA.racketId,nameA)
      }
      racketBOpt.foreach{racketB =>
        var randDirection = (new Random).nextFloat() * math.Pi.toFloat / 2 + math.Pi.toFloat * 5 / 4
        while (randDirection == 2*math.Pi || randDirection == math.Pi || randDirection == 0 || randDirection == math.Pi / 2){
          randDirection = (new Random).nextFloat() * math.Pi.toFloat / 2 + math.Pi.toFloat * 5 / 4
        }
        generateBall(racketBPosition - Point(0,config.getRacketHeight / 2 + config.getBallRadius),racketB.racketId,randDirection)
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
        generateBricks4Racket(racketB.racketId,nameB)

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
    /**
      * 这里需要增加对方对应位置增加砖块,后端执行不同
      * */
    obstacleMap.get(e.brickId).foreach{ obstacle =>
      //      ballMap.get(e.ballId).foreach(_.changeDirection(obstacle.getPosition,obstacle.getWidth,obstacle.getHeight))
      val ballRacketOpt = racketMap.get(e.ballId) match{
        case Some(ball) =>
          racketMap.get(ball.racketId) match {
            case Some(racket) =>
//              println(s"检测到该球对应的拍子${racket.racketId}")
//              if(obstacleMap.filter(_._2.getObstacleState().racketId == racket.racketId).isEmpty){
//                gameOverCallBack(racket)
//              }
              Some(racket)
            case None =>None
          }
        case None =>None
      }
      obstacle.obstacleType match{
        case ObstacleType.brick =>
//          println(s"该被打掉的障碍物的racketId=${obstacle.getObstacleState().racketId},oId=${obstacle.getObstacleState().oId},value=${obstacle.getObstacleState().value}")
          ballRacketOpt.foreach(_.updateScore(obstacle.getObstacleState().value))
          //          ballRacketOpt.foreach(t =>println(s"此使racketId=${t.racketId},damage=${t.damageStatistics}"))
          obstacleMap.remove(e.brickId)
          quadTree.remove(obstacle)
        case ObstacleType.fastRemove =>
//          println(s"快消道具")
          ballRacketOpt match{
            case Some(racket) =>
              val objects = quadTree.retrieveFilter(obstacle).filter(_.isInstanceOf[Brick])
                .map(_.asInstanceOf[Brick]).filter(_.getObstacleState().racketId == racket.racketId)
              objects.foreach{o =>
                if(o.getObstacleState().p.y == obstacle.getObstacleState().p.y){
                  obstacleMap.remove(o.getObstacleState().oId)
                  quadTree.remove(o)
                }
              }
              obstacleMap.remove(e.brickId)
              quadTree.remove(obstacle)

            case None =>
          }
      }
    }
    if(e.obstacleValue != 0 && racketMap.filter(_._1 != e.enemyRacketId).keys.nonEmpty){
//      val racketIds = racketMap.filter(_._1 != e.enemyRacketId).keys
      val obstacleState = ObstacleState(racketMap.filter(_._1 != e.enemyRacketId).keys.head,obstacleIdGenerator.getAndIncrement(),ObstacleType.brick,
        Point(e.obstaclePosition.x,config.boundary.y / 2 + e.obstaclePosition.y),0)
      val event = BreakoutGameEvent.GenerateObstacle(systemFrame,obstacleState)
      racketId2UserActorMap.get(obstacleState.racketId) match{
        case None =>
          log.debug(s"${roomActorRef.path} 该玩家已经不在房间")
        case Some(uid) =>
          dispatchTo(uid,event)
//          log.debug(s"由${}${roomActorRef.path}将新生成障碍物发送给userActor=${uid}")
          obstacleMap.put(obstacleState.oId,obstacleState)
          quadTree.insert(obstacleState)
      }
    }

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
