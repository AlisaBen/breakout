package com.neo.sk.breakout.shared.game

import com.neo.sk.breakout.shared.`object`._
import com.neo.sk.breakout.shared.config.GameConfig
import com.neo.sk.breakout.shared.game.view._
import com.neo.sk.breakout.shared.model.Constants.{GameAnimation, ObstacleType}
import com.neo.sk.breakout.shared.model.{Point, Rectangle}
import com.neo.sk.breakout.shared.protocol.BreakoutGameEvent
import com.neo.sk.breakout.shared.protocol.BreakoutGameEvent.{GameContainerAllState, GameContainerState, GameEvent, UserActionEvent}
import com.neo.sk.breakout.shared.util.canvas.{MiddleContext, MiddleFrame}

import scala.collection.mutable


/**
  * Created by hongruying on 2018/8/24
  * 终端
  * A1
  */
//fixme
case class GameContainerClientImpl(
                                    drawFrame: MiddleFrame,
                                    ctx: MiddleContext,
                                    override val config: GameConfig,
                                    myRacketId: Int,
                                    myName: String,
                                    var canvasSize: Point,
                                    var canvasUnit: Int,
                                    setKillCallback: Racket => Unit
                                  ) extends GameContainer with EsRecover
  with BackgroundDrawUtil with BallDrawUtil with FpsComponentsDrawUtil with BrickDrawUtil with RacketDrawUtil with InfoDrawUtil {

  import scala.language.implicitConversions

  protected val obstacleAttackedAnimationMap = mutable.HashMap[Int, Int]()
  protected val tankAttackedAnimationMap = mutable.HashMap[Int, Int]()
  protected val tankDestroyAnimationMap = mutable.HashMap[Int, Int]() //prop ->

  protected var killerList = List.empty[String]
  protected var killNum: Int = 0
  protected var damageNum: Int = 0
  protected var killerName: String = ""

  var racketId: Int = myRacketId
  protected val myRacketMoveAction = mutable.HashMap[Long,List[UserActionEvent]]()

  def changeRacketId(id: Int) = racketId = id

//  def updateDamageInfo(myKillNum: Int, name: String, myDamageNum: Int): Unit = {
//    killerList = killerList :+ name
//    killerName = name
//    killNum = myKillNum
//    damageNum = myDamageNum
//  }
//
//  def getCurTankId:Int = tankId
//
//  def change2OtherTank:Int = {
//    val keys = tankMap.keys.toArray
//    val idx = (new util.Random).nextInt(keys.length)
//    keys(idx)
//  }
//
//  def isKillerAlive(killerId:Int):Boolean = {
//    if(tankMap.contains(killerId)) true else false
//  }
//
  override def debug(msg: String): Unit = {}

  override def info(msg: String): Unit = println(msg)

  private val esRecoverSupport: Boolean = true

  private val uncheckedActionMap = mutable.HashMap[Byte, Long]() //serinum -> frame

  private var gameContainerAllStateOpt: Option[GameContainerAllState] = None
  private var gameContainerStateOpt: Option[GameContainerState] = None
//
  protected var waitSyncData: Boolean = true

  private val preExecuteFrameOffset = com.neo.sk.breakout.shared.model.Constants.PreExecuteFrameOffset

  def updateClientSize(canvasS: Point, cUnit: Int) = {
    canvasUnit = cUnit
    canvasSize = canvasS
    updateBackSize(canvasS)
    updateBulletSize(canvasS)
    updateFpsSize(canvasS)
    updateObstacleSize(canvasS)
//    updateTankSize(canvasS)
  }

  override protected def handleObstacleCollision(e: BreakoutGameEvent.ObstacleCollision): Unit = {
    super.handleObstacleCollision(e)
    if (obstacleMap.get(e.brickId).nonEmpty) {
      obstacleAttackedAnimationMap.put(e.brickId, GameAnimation.bulletHitAnimationFrame)
    }
  }
//
//  override protected def handleGenerateBullet(e:GenerateBullet) = {
//    tankMap.get(e.bullet.tankId) match{
//      case Some(tank) =>
//        if(e.s){
//          tank.setTankGunDirection(math.atan2(e.bullet.momentum.y, e.bullet.momentum.x).toFloat)
//          tankExecuteLaunchBulletAction(tank.tankId,tank)
//        }
//      case None =>
//        println(s"--------------------该子弹没有对应的tank")
//    }
//    super.handleGenerateBullet(e)
//  }
//

  override protected def handleRacketCollision(e: BreakoutGameEvent.RacketCollision): Unit = {
    super.handleRacketCollision(e)
    if (racketMap.get(e.racketId).nonEmpty) {
      tankAttackedAnimationMap.put(e.racketId, GameAnimation.bulletHitAnimationFrame)
    }
  }

//  override protected def dropTankCallback(bulletTankId: Int, bulletTankName: String, tank: Tank) = {
//    setKillCallback(tank)
//  }
//
//  override protected def handleGenerateProp(e: TankGameEvent.GenerateProp): Unit = {
//    super.handleGenerateProp(e)
//    if (e.generateType == PropGenerateType.tank) {
//      tankDestroyAnimationMap.put(e.propState.pId, GameAnimation.tankDestroyAnimationFrame)
//    }
//  }
//
//
//  override def tankExecuteLaunchBulletAction(tankId: Int, tank: Tank): Unit = {
//    tank.launchBullet()(config)
//  }
//
//  override protected implicit def tankState2Impl(tank: TankState): Tank = {
//    new TankClientImpl(config, tank, fillBulletCallBack, tankShotgunExpireCallBack)
//  }

  def receiveGameEvent(e: GameEvent) = {
    if (e.frame >= systemFrame) {
      addGameEvent(e)
    } else if (esRecoverSupport) {
      println(s"rollback-frame=${e.frame},curFrame=${this.systemFrame},e=${e}")
      rollback4GameEvent(e)
    }
  }

  //接受服务器的用户事件
  def receiveUserEvent(e: UserActionEvent) = {
    if (e.racketId == racketId) {
      uncheckedActionMap.get(e.serialNum) match {
        case Some(preFrame) =>
          if (e.frame != preFrame) {
            println(s"preFrame=${preFrame} eventFrame=${e.frame} curFrame=${systemFrame}")
            //          require(preFrame <= e.frame)
            if (preFrame < e.frame && esRecoverSupport) {
              if (preFrame >= systemFrame) {
                removePreEvent(preFrame, e.racketId, e.serialNum)
                addUserAction(e)
              } else if (e.frame >= systemFrame) {
                removePreEventHistory(preFrame, e.racketId, e.serialNum)
                rollback(preFrame)
                addUserAction(e)
              } else {
                removePreEventHistory(preFrame, e.racketId, e.serialNum)
                addUserActionHistory(e)
                rollback(preFrame)
              }
            }
          }
        case None =>
          if (e.frame >= systemFrame) {
            addUserAction(e)
          } else if (esRecoverSupport) {
            rollback4UserActionEvent(e)
          }
      }
    } else {
      if (e.frame >= systemFrame) {
        addUserAction(e)
      } else if (esRecoverSupport) {
        rollback4UserActionEvent(e)
      }
    }
  }

  def preExecuteUserEvent(action: UserActionEvent) = {
    addUserAction(action)
    uncheckedActionMap.put(action.serialNum, action.frame)
  }

  final def addMyAction(action: UserActionEvent): Unit = {
    if (action.racketId == racketId) {
      myRacketMoveAction.get(action.frame - preExecuteFrameOffset) match {
        case Some(actionEvents) => myRacketMoveAction.put(action.frame - preExecuteFrameOffset, action :: actionEvents)
        case None => myRacketMoveAction.put(action.frame - preExecuteFrameOffset, List(action))
      }
    }
  }


  protected final def handleMyAction(actions:List[UserActionEvent]) = { //处理出现错误动作的帧
    def isHaveReal(id: Int) = {
      var isHave = false
      actionEventMap.get(systemFrame).foreach {
        list =>
          list.foreach {
            a =>
              if (a.racketId == id) isHave = true
          }
      }
      isHave
    }
    if (racketMap.contains(racketId)) {
      val tank = racketMap(racketId)
      if (!isHaveReal(racketId)) {
        //fixme
//        if (!tank.getMoveState()) {
//          val tankMoveSet = mutable.Set[Byte]()
//          actions.sortBy(t => t.serialNum).foreach {
//            case a: UserPressKeyDown =>
//              tankMoveSet.add(a.keyCodeDown)
//            case _ =>
//          }
//          tank.setTankDirection(tankMoveSet.toSet)
//        }
      }
    }
  }

//  protected def handleFakeActionNow():Unit = {
//    if(com.neo.sk.tank.shared.model.Constants.fakeRender) {
//      handleMyAction(myTankMoveAction.getOrElse(systemFrame,Nil).reverse)
//      myTankMoveAction.remove(systemFrame - 10)
//    }
//  }
//
//
//  override protected def handleUserJoinRoomEvent(e: TankGameEvent.UserJoinRoom): Unit = {
//    super.handleUserJoinRoomEvent(e)
//    tankInvincibleCallBack(e.tankState.tankId)
//  }
//
//  override protected def handleUserReliveEvent(e: TankGameEvent.UserRelive): Unit = {
//    super.handleUserReliveEvent(e)
//    tankInvincibleCallBack(e.tankState.tankId)
//  }
//
//  /** 同步游戏逻辑产生的延时事件 */
//  def receiveTankFollowEventSnap(snap: TankFollowEventSnap) = {
//    snap.invincibleList.foreach(addFollowEvent(_))
//    snap.tankFillList.foreach(addFollowEvent(_))
//    snap.shotExpireList.foreach(addFollowEvent(_))
//  }
//
  protected def handleGameContainerAllState(gameContainerAllState: GameContainerAllState) = {
    systemFrame = gameContainerAllState.f
    quadTree.clear()
    racketMap.clear()
    obstacleMap.clear()
//    propMap.clear()
    racketMoveAction.clear()
    ballMap.clear()
//    environmentMap.clear()

    gameContainerAllState.rackets.foreach { t =>
      val racket = if(t.racketId == racketId){
        new Racket(config, t)
      }else{
        new Racket(config, t.getRacketState().editRacketState(canvasSize))
      }
      quadTree.insert(racket)
      racketMap.put(t.racketId, racket)
      racketHistoryMap.put(t.racketId,racket.name)
    }
    gameContainerAllState.obstacles.foreach { o =>
      val obstacle = if(o.racketId == racketId){
        new Brick(config, o)
      }else{
        new Brick(config,o.getObstacleState().editObstacleState(canvasSize))
      }

      quadTree.insert(obstacle)
      obstacleMap.put(o.oId, obstacle)
    }
//    gameContainerAllState.props.foreach { t =>
//      val prop = Prop(t, config.propRadius)
//      quadTree.insert(prop)
//      propMap.put(t.pId, prop)
//    }
    gameContainerAllState.racketMoveAction.foreach { t =>
      val set = racketMoveAction.getOrElse(t._1, mutable.HashSet[Byte]())
      t._2.foreach(l=>l.foreach(set.add))
      racketMoveAction.put(t._1, set)
    }
    gameContainerAllState.balls.foreach { t =>
      val bullet = if(t.racketId == racketId){
        new Ball(config, t)
      }else{
//        new Ball(config, t)
        new Ball(config,t.getBallState().editDirection(canvasSize))
      }
      quadTree.insert(bullet)
      ballMap.put(t.bId, bullet)
    }
//    gameContainerAllState.environment.foreach { t =>
//      val obstacle = Obstacle(config, t)
//      quadTree.insert(obstacle)
//      environmentMap.put(obstacle.oId, obstacle)
//    }
    waitSyncData = false
  }

  implicit def obstacleState2Impl(obstacleState:ObstacleState):Obstacle = obstacleState.t match{
    case ObstacleType.brick =>new Brick(config,obstacleState)
    case _ => new Brick(config,obstacleState)
  }

  protected def handleGameContainerState(gameContainerState: GameContainerState) = {
    val curFrame = systemFrame
    val startTime = System.currentTimeMillis()
    (curFrame until gameContainerState.f).foreach { _ =>
      super.update()
      if (esRecoverSupport) addGameSnapShot(systemFrame, getGameContainerAllState())
    }
    val endTime = System.currentTimeMillis()
    if (curFrame < gameContainerState.f) {
      println(s"handleGameContainerState update to now use Time=${endTime - startTime} and systemFrame=${systemFrame} sysFrame=${gameContainerState.f}")
    }

    if(!judge(gameContainerState)||systemFrame!=gameContainerState.f){
      systemFrame = gameContainerState.f
      quadTree.clear()
      racketMap.clear()
      racketMoveAction.clear()
      gameContainerState.rackets match{
        case Some(rackets) =>
          rackets.foreach { t =>
            val racket = if(t.racketId == racketId){
              new Racket(config, t)
            }else{
              new Racket(config,t.getRacketState().editRacketState(canvasSize))
            }

            quadTree.insert(racket)
            racketMap.put(t.racketId, racket)
            racketHistoryMap.put(t.racketId,racket.name)
          }
        case None =>
          println(s"handle game container client--no tanks")
      }
      gameContainerState.racketMoveAction match {
        case Some(as)=>
          as.foreach { t =>
            val set = racketMoveAction.getOrElse(t._1, mutable.HashSet[Byte]())
            t._2.foreach(l=>l.foreach(set.add))
            racketMoveAction.put(t._1, set)
          }
        case None=>
      }
      obstacleMap.values.foreach(o=>quadTree.insert(o))
//      propMap.values.foreach(o=>quadTree.insert(o))

//      environmentMap.values.foreach(quadTree.insert)
      ballMap.values.foreach { bullet =>
        quadTree.insert(bullet)
      }
    }
  }

  private def judge(gameContainerState: GameContainerState) = {
    gameContainerState.rackets match{
      case Some(rackets) =>
        rackets.forall { racketState =>
          racketMap.get(racketState.racketId) match {
            case Some(t) =>
              if (t.getRacketState() != racketState) {
                println(s"judge failed,because tank=${racketState.racketId} no same,tankMap=${t.getRacketState()},gameContainer=${racketState}")
                false
              } else true
            case None => {
              println(s"judge failed,because tank=${racketState.racketId} not exists....")
              true
            }
          }
        }
      case None =>
        println(s"game container client judge function no tanks---")
        true
    }
  }

  def receiveGameContainerAllState(gameContainerAllState: GameContainerAllState) = {
    //fixme
//    gameContainerAllState.balls.filter(_.racketId == racketId).foreach(t => t.asInstanceOf[Ball].editDirection())
    gameContainerAllStateOpt = Some(gameContainerAllState)
//    if(gameContainerAllState.f > systemFrame){
//      gameContainerAllStateOpt = Some(gameContainerAllState)
//    }else if(gameContainerAllState.f == systemFrame){
//      info(s"收到同步数据，立即同步，curSystemFrame=${systemFrame},sync game container state frame=${gameContainerAllState.f}")
//      gameContainerAllStateOpt = None
//      handleGameContainerAllState(gameContainerAllState)
//    }else{
//      info(s"收到同步数据，但未同步，curSystemFrame=${systemFrame},sync game container state frame=${gameContainerAllState.f}")
//    }
 }

  def receiveGameContainerState(gameContainerState: GameContainerState) = {
    if (gameContainerState.f > systemFrame) {
      gameContainerState.rackets match {
        case Some(rackets) =>
          gameContainerStateOpt = Some(gameContainerState)
        case None =>
          gameContainerStateOpt match{
            case Some(state) =>
              gameContainerStateOpt = Some(GameContainerState(gameContainerState.f,state.rackets,state.racketMoveAction))
            case None =>
          }
      }
    } else if (gameContainerState.f == systemFrame) {
      gameContainerState.rackets match{
        case Some(tanks) =>
          info(s"收到同步数据，立即同步，curSystemFrame=${systemFrame},sync game container state frame=${gameContainerState.f}")
          gameContainerStateOpt = None
          handleGameContainerState(gameContainerState)
        case None =>
          info(s"收到同步帧号的数据")
      }
    } else {
      info(s"收到同步数据，但未同步，curSystemFrame=${systemFrame},sync game container state frame=${gameContainerState.f}")
    }
  }

  override def update(): Unit = {
    //    val startTime = System.currentTimeMillis()
    if (gameContainerAllStateOpt.nonEmpty) {
      val gameContainerAllState = gameContainerAllStateOpt.get
      info(s"立即同步所有数据，curSystemFrame=${systemFrame},sync game container state frame=${gameContainerAllState.f}")
      handleGameContainerAllState(gameContainerAllState)
      gameContainerAllStateOpt = None
      if (esRecoverSupport) {
        clearEsRecoverData()
        addGameSnapShot(systemFrame, this.getGameContainerAllState())
      }
    } else if (gameContainerStateOpt.nonEmpty && (gameContainerStateOpt.get.f - 1 == systemFrame || gameContainerStateOpt.get.f - 2 > systemFrame)) {
      info(s"同步数据，curSystemFrame=${systemFrame},sync game container state frame=${gameContainerStateOpt.get.f}")
      handleGameContainerState(gameContainerStateOpt.get)
      gameContainerStateOpt = None
      if (esRecoverSupport) {
        clearEsRecoverData()
        addGameSnapShot(systemFrame, this.getGameContainerAllState())
      }
    } else {
      super.update()
      if (esRecoverSupport) addGameSnapShot(systemFrame, getGameContainerAllState())
    }
  }
//    else if (gameContainerStateOpt.nonEmpty && (gameContainerStateOpt.get.f - 1 == systemFrame || gameContainerStateOpt.get.f - 2 > systemFrame)) {
//      info(s"同步数据，curSystemFrame=${systemFrame},sync game container state frame=${gameContainerStateOpt.get.f}")
//      handleGameContainerState(gameContainerStateOpt.get)
//      gameContainerStateOpt = None
//      if (esRecoverSupport) {
//        clearEsRecoverData()
//        addGameSnapShot(systemFrame, this.getGameContainerAllState())
//      }
//    }
//    else {
//      super.update()
//      if (esRecoverSupport) addGameSnapShot(systemFrame, getGameContainerAllState())
//    }


  override protected def clearEventWhenUpdate(): Unit = {
    if (esRecoverSupport) {
      addEventHistory(systemFrame, gameEventMap.getOrElse(systemFrame, Nil), actionEventMap.getOrElse(systemFrame, Nil))
    }
    gameEventMap -= systemFrame
    actionEventMap -= systemFrame
    followEventMap -= systemFrame
    systemFrame += 1
  }

  protected def rollbackUpdate(): Unit = {
    super.update()
    if (esRecoverSupport) addGameSnapShot(systemFrame, getGameContainerAllState())
  }

  override protected def gameOverCallBack(racket: Racket): Unit = {
    setKillCallback(racket)
  }

  override protected def collisionRacketCallBack(ball: Ball)(racket: Racket): Unit = {
    if(ball.racketId == racketId){
      println(s"sssssssssssssssss")
      if(ball.getBallState().position.y <= racket.getRacketState().position.y){
        super.collisionRacketCallBack(ball)(racket)
      }else{
        println(s"-------${racketId}--你输了球飞到拍子下面，游戏结束")
      }
    }else{
      println(s"ppppppppppppppppppppppp")
      if(ball.getBallState().position.y >= racket.getRacketState().position.y){
        super.collisionRacketCallBack(ball)(racket)
      }else{
        println(s"-------${ball.racketId}--你赢了球飞到拍子下面，游戏结束")
      }
    }
  }

  override protected def ballMove(): Unit = {
    ballMap.toList.sortBy(_._1).map(_._2).foreach{ball =>
      if(ball.racketId == racketId){
        println(s"-------我自己的")
        super.ballMove()
      }else{
        val objects = quadTree.retrieveFilter(ball)
        objects.filter(_.isInstanceOf[Racket]).map(_.asInstanceOf[Racket])
          .foreach{t =>
            if(t.racketId == ball.racketId){ ball.checkAttackObject(t,collisionRacketCallBack(ball))}
          }
        objects.filter(t => t.isInstanceOf[ObstacleBall] && t.isInstanceOf[Obstacle]).map(_.asInstanceOf[Obstacle])
          .foreach(t =>
            if(t.racketId == ball.racketId) ball.checkAttackObject(t,collisionObstacleCallBack(ball)))
        val gameOver = ball.move(true,Rectangle(Point(0,0),Point(config.boundary.x,config.boundary.y / 2)),systemFrame)
        if(gameOver){
          println("game over")
          racketMap.get(ball.racketId) match{
            case Some(racket) =>
            //                gameOverCallBack(racket)
            case None =>
          }
        }
      }
    }
  }

  def drawGame(time: Long, networkLatency: Long, dataSizeList: List[String], supportLiveLimit: Boolean = false): Unit = {
    val offsetTime = math.min(time, config.frameDuration)
    val h = canvasSize.y
    val w = canvasSize.x
    //    val startTime = System.currentTimeMillis()
    if (!waitSyncData) {
      ctx.setLineCap("round")
      ctx.setLineJoin("round")
      racketMap.get(racketId) match {
        case Some(racket) =>
//          val offset = canvasSize / 2 - racket.asInstanceOf[Racket].getPosition4Animation(boundary, quadTree, offsetTime)
          val offset = Point(0,0)
          drawBackground(offset)
          drawObstacles(offset, Point(w, h))
//          drawEnvironment(offset, Point(w, h))
//          drawProps(offset, Point(w, h))
          drawBall(offset, offsetTime, Point(w, h))
          drawRackets(offset, offsetTime, Point(w, h))
//          drawObstacleBloodSlider(offset)
//          drawMyTankInfo(tank.asInstanceOf[TankClientImpl], supportLiveLimit)
//          drawMinimap(tank)
          drawRank(supportLiveLimit)
          renderFps(networkLatency, dataSizeList)
//          drawKillInformation()
//          drawRoomNumber()
//          drawCurMedicalNum(tank.asInstanceOf[TankClientImpl])
//
          if (racket.canvasFrame >= 1) {
            racket.canvasFrame += 1
          }
          val endTime = System.currentTimeMillis()
//                  renderTimes += 1
//                  renderTime += endTime - startTime


        case None =>
        //          info(s"tankid=${myTankId} has no in tankMap.....................................")
        //          setGameState(GameState.stop)
        //          if(isObserve) drawDeadImg()
      }
    }
  }

//  def findAllTank(thisTank: Int) = {
//    if (tankMap.contains(thisTank))
//      Some(quadTree.retrieve(tankMap(thisTank)).filter(_.isInstanceOf[Tank]).map(_.asInstanceOf[Tank]))
//    else None
//  }
//
//  def findOtherBullet(thisTank: Int) = {
//    quadTree.retrieveFilter(tankMap(thisTank)).filter(_.isInstanceOf[Bullet]).map(_.asInstanceOf[Bullet])
//  }
//
//  def findOtherObstacle(thisTank: Tank) = {
//    quadTree.retrieveFilter(thisTank).filter(_.isInstanceOf[Obstacle]).map(_.asInstanceOf[Obstacle])
//  }


}
