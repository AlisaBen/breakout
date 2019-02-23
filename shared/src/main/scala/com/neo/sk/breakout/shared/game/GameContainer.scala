package com.neo.sk.breakout.shared.game

import com.neo.sk.breakout.shared.`object`._
import com.neo.sk.breakout.shared.config.GameConfig
import com.neo.sk.breakout.shared.model.Constants.ObstacleType
import com.neo.sk.breakout.shared.model.{Point, Rectangle, Score}
import com.neo.sk.breakout.shared.protocol.BreakoutGameEvent
import com.neo.sk.breakout.shared.protocol.BreakoutGameEvent._
import com.neo.sk.breakout.shared.util.QuadTree

import scala.collection.mutable

/**
  * Created by hongruying on 2018/8/22
  * Edit by benyafang on 2019/2/8
  * A1
  * 游戏逻辑的基类
  *
  * 逻辑帧更新逻辑：
  * 先处理玩家离开的游戏事件
  * 坦克和子弹的运动逻辑，障碍检测，（坦克吃道具事件，子弹攻击目标的事件）
  * 更新用户操作所影响坦克的状态
  * 伤害计算事件
  * 坦克吃道具事件
  * 用户生成子弹事件
  * 用户加入游戏事件的处理
  */

trait GameContainer extends KillInformation{

  import scala.language.implicitConversions

  def debug(msg: String): Unit

  def info(msg: String): Unit

  implicit val config:GameConfig

  val boundary : Point = config.boundary

  var currentRank = List.empty[Score]
//
//  var historyRankMap = Map.empty[Int,Score]
//  var historyRank = historyRankMap.values.toList.sortBy(_.d).reverse
//  var historyRankThreshold =if (historyRank.isEmpty)-1 else historyRank.map(_.d).min
//  val historyRankLength = 5
//  val tankLivesMap:mutable.HashMap[Int,TankState] = mutable.HashMap[Int,TankState]() // tankId -> lives
////  val tankEatPropMap = mutable.HashMap[Int,mutable.HashSet[Prop]]()//tankId -> Set(propId)
//
//  val maxFollowFrame=math.max(math.max(config.shotgunDuration,config.initInvincibleDuration),config.fillBulletDuration)

//  var racketId = -1
  var systemFrame:Long = 0L //系统帧数

  val racketMap = mutable.HashMap[Int,Racket]() //tankId -> Tank
  val ballMap = mutable.HashMap[Int,Ball]() //bulletId -> Bullet
  val obstacleMap = mutable.HashMap[Int,Obstacle]() //obstacleId -> Obstacle  可打击的砖头
//  val environmentMap = mutable.HashMap[Int,Obstacle]() //obstacleId -> steel and river  不可打击
//  val propMap = mutable.HashMap[Int,Prop]() //propId -> prop 道具信息
//
  val racketMoveAction = mutable.HashMap[Int,mutable.HashSet[Byte]]() //tankId -> pressed direction key code

  val quadTree : QuadTree = new QuadTree(Rectangle(Point(0,0),boundary))
//
  protected val racketHistoryMap = mutable.HashMap[Int,String]()
  protected val removeRacketHistoryMap=mutable.HashMap[Long,List[Int]]()
//
  protected val gameEventMap = mutable.HashMap[Long,List[GameEvent]]() //frame -> List[GameEvent] 待处理的事件 frame >= curFrame
  protected val actionEventMap = mutable.HashMap[Long,List[UserActionEvent]]() //frame -> List[UserActionEvent]
  protected val followEventMap = mutable.HashMap[Long,List[FollowEvent]]()  // 记录游戏逻辑中产生事件
  final protected def handleUserJoinRoomEvent(l:List[UserJoinRoom]) :Unit = {
    l foreach handleUserJoinRoomEvent
  }

//  protected def handleRemoveHistoryMapNow():Unit={
//    removeTankHistoryMap.get(systemFrame) match {
//      case Some(l)=>
//        l.foreach(t=>tankHistoryMap.remove(t))
//        removeTankHistoryMap.remove(systemFrame)
//      case None=>
//    }
//  }
//

  protected def handleUserJoinRoomEvent(e:UserJoinRoom) :Unit = {
//    println(s"-------------------处理用户加入房间事件")
    val racket:Racket = e.racketState
    racketMap.put(e.racketState.racketId,racket)
    racketHistoryMap.put(e.racketState.racketId,e.racketState.name)
    quadTree.insert(racket)
    val ball:Ball = e.ballState
    ballMap.put(e.ballState.bId,ball)
    quadTree.insert(ball)
  }

  //fixme 重写服务器和客户端执行的逻辑不一样
  protected implicit def racketState2Impl(racket:RacketState):Racket = new Racket(config,racket)

  protected implicit def ballState2Impl(ball:BallState):Ball = new Ball(config,ball)
//
  //服务器和客户端执行的逻辑不一致
  protected def handleUserJoinRoomEventNow() = {
    gameEventMap.get(systemFrame).foreach{ events =>
      handleUserJoinRoomEvent(events.filter(_.isInstanceOf[UserJoinRoom]).map(_.asInstanceOf[UserJoinRoom]).reverse)
    }
  }
//
//  final protected def handleUserReliveEvent(l:List[UserRelive]):Unit = {
//    l foreach handleUserReliveEvent
//  }
//
//  protected def handleUserReliveEvent(e:UserRelive):Unit = {
//    val t = e.tankState
//    if(!tankMap.exists(_._1 == t.tankId)){
//      println(s"------------${e.tankState}")
//      tankMap.put(t.tankId,t)
//      quadTree.insert(t)
//    }
//  }
//
//  protected def handleUserReliveNow() = {
//    gameEventMap.get(systemFrame).foreach{events =>
//      handleUserReliveEvent(events.filter(_.isInstanceOf[UserRelive]).map(_.asInstanceOf[UserRelive]).reverse)
//    }
//  }

  protected final def handleUserActionEvent(actions:List[UserActionEvent]) = {
    /**
      * 用户行为事件
      * */
    //fixme
    actions.sortBy(t => (t.racketId,t.serialNum)).foreach{ action =>
      val racketMoveSet = racketMoveAction.getOrElse(action.racketId,mutable.HashSet[Byte]())
      racketMap.get(action.racketId) match {
        case Some(racket) =>
          action match {
            case a:UserTouchMove =>
              racket.setRacketDirection(Some(a.touchMove))
            case a:UserTouchEnd =>
              racket.setRacketDirection(None)
          }
        case None => info(s"tankId=${action.racketId} action=${action} is no valid,because the tank is not exist")
      }
    }
  }

  final protected def handleUserActionEventNow() = {
    actionEventMap.get(systemFrame).foreach{ actionEvents =>
      handleUserActionEvent(actionEvents.reverse)
    }
  }

//  /**
//    * 服务器和客户端执行的逻辑不一致
//    * 服务器需要进行坦克子弹容量计算，子弹生成事件，
//    * 客户端只需要进行子弹容量计算
//    * */
//  protected def tankExecuteLaunchBulletAction(tankId:Int,tank:Tank) : Unit


  protected def handleRacketCollision(e:RacketCollision) :Unit = {
    /**
      * 球的运行方向改变
      * */
    racketMap.get(e.racketId) match{
      case Some(racket) =>
//        ballMap.get(e.ballId).foreach(_.changeDirection(racket.getPosition,racket.getWidth,racket.getHeight))
      case None =>
    }
//    val bulletTankOpt = tankMap.get(e.bulletTankId)
//    tankMap.get(e.tankId).foreach{ tank =>
//      tank.attackedDamage(e.damage)
//      bulletTankOpt.foreach(_.damageStatistics += e.damage)
//      if(!tank.isLived()){
//        bulletTankOpt.foreach(_.killTankNum += 1)
//        quadTree.remove(tank)
//        tankMap.remove(e.tankId)
//        tankMoveAction.remove(e.tankId)
//        addKillInfo(tankHistoryMap.getOrElse(e.bulletTankId,"未知"),tank.name)
//        dropTankCallback(e.bulletTankId,tankHistoryMap.getOrElse(e.bulletTankId,"未知"),tank)
//      }
//    }
  }
//
//  protected def dropTankCallback(bulletTankId:Int, bulletTankName:String,tank:Tank):Unit = {}


  protected final def handleRacketCollision(es:List[RacketCollision]) :Unit = {
    es foreach handleRacketCollision
  }

  final protected def handleRacketCollisionNow() = {
    followEventMap.get(systemFrame).foreach{ events =>
      handleRacketCollision(events.filter(_.isInstanceOf[RacketCollision]).map(_.asInstanceOf[RacketCollision]).reverse)
    }
  }

  protected def handleObstacleCollision(e:ObstacleCollision) :Unit = {
    /**
      * 这里需要增加对方对应位置增加砖块,后端执行不同
      * */
    obstacleMap.get(e.brickId).foreach{ obstacle =>
//      ballMap.get(e.ballId).foreach(_.changeDirection(obstacle.getPosition,obstacle.getWidth,obstacle.getHeight))
      obstacleMap.remove(e.brickId)
      quadTree.remove(obstacle)
      val ballRacketOpt = racketMap.get(e.ballId) match{
        case Some(ball) =>
          racketMap.get(ball.racketId) match {
            case Some(racket) =>Some(racket)
            case None =>None
          }
        case None =>None
      }
      obstacle.obstacleType match{
        case ObstacleType.brick =>
          ballRacketOpt.foreach(_.updateScore(obstacle.getObstacleState().value))
        case _ =>
      }

    }
  }



  protected final def handleObstacleCollision(es:List[ObstacleCollision]) :Unit = {
    es foreach handleObstacleCollision
  }

  final protected def handleObstacleCollisionNow() = {
    followEventMap.get(systemFrame).foreach{ events =>
      handleObstacleCollision(events.filter(_.isInstanceOf[ObstacleCollision]).map(_.asInstanceOf[ObstacleCollision]).reverse)
    }
  }

//  protected def handleTankEatProp(e:TankEatProp) :Unit = {
//    propMap.get(e.propId).foreach{ prop =>
//      quadTree.remove(prop)
//      tankMap.get(e.tankId).foreach(_.eatProp(prop))
//      propMap.remove(e.propId)
////      if(prop.propType == 4){
////        quadTree.remove(prop)
////        propMap.remove(e.propId)
////      } else{
////        quadTree.remove(prop)
////        tankMap.get(e.tankId).foreach(_.eatProp(prop))
////        propMap.remove(e.propId)
////      }
//    }
//  }
//
//
//  protected final def handleTankEatProp(es:List[TankEatProp]) :Unit = {
//    es foreach handleTankEatProp
//  }
//
//  final protected def handleTankEatPropNow() = {
//    /**
//      * 坦克吃道具,propType==4
//      * */
//    gameEventMap.get(systemFrame).foreach{ events =>
//      handleTankEatProp(events.filter(_.isInstanceOf[TankEatProp]).map(_.asInstanceOf[TankEatProp]).reverse)
//    }
//  }
//
//  protected def handleGenerateBullet(e:GenerateBullet) :Unit = {
//    //客户端和服务端重写
//    val bullet = new Bullet(config,e.bullet)
//    bulletMap.put(e.bullet.bId,bullet)
//    quadTree.insert(bullet)
//  }
//
//  protected final def handleGenerateBullet(es:List[GenerateBullet]) :Unit = {
//    es foreach handleGenerateBullet
//  }
//
//  final protected def handleGenerateBulletNow() = {
//    gameEventMap.get(systemFrame).foreach{ events =>
//      handleGenerateBullet(events.filter(_.isInstanceOf[GenerateBullet]).map(_.asInstanceOf[GenerateBullet]).reverse)
//    }
//  }
//
//  protected def handleGenerateProp(e:GenerateProp) :Unit = {
//    val prop = Prop(e.propState,config.propRadius)
//    propMap.put(prop.pId,prop)
//    quadTree.insert(prop)
//  }
//
//  protected final def handleGenerateProp(es:List[GenerateProp]) :Unit = {
//    es foreach handleGenerateProp
//  }
//
//  final protected def handleGeneratePropNow() = {
//    gameEventMap.get(systemFrame).foreach{ events =>
//      handleGenerateProp(events.filter(_.isInstanceOf[GenerateProp]).map(_.asInstanceOf[GenerateProp]).reverse)
//    }
//  }

  protected def handleGenerateObstacle(e:GenerateObstacle) :Unit = {
    val obstacle = Obstacle(config,e.obstacleState)
    if (e.obstacleState.t <= ObstacleType.brick){
      obstacleMap.put(obstacle.oId,obstacle)
      quadTree.insert(obstacle)
    }

  }

  protected final def handleGenerateObstacle(es:List[GenerateObstacle]) :Unit = {
    es foreach handleGenerateObstacle
  }

  final protected def handleGenerateObstacleNow() = {
    gameEventMap.get(systemFrame).foreach{ events =>
      handleGenerateObstacle(events.filter(_.isInstanceOf[GenerateObstacle]).map(_.asInstanceOf[GenerateObstacle]).reverse)
    }
  }

  protected def handleObstacleRemove(e:ObstacleRemove) :Unit = {
    obstacleMap.get(e.obstacleId).foreach { obstacle =>
      quadTree.remove(obstacle)
      obstacleMap.remove(e.obstacleId)
    }
  }

  protected final def handleObstacleRemove(es:List[ObstacleRemove]) :Unit = {
    es foreach handleObstacleRemove
  }

  protected def handleObstacleRemoveNow()={
    gameEventMap.get(systemFrame).foreach{events=>
      handleObstacleRemove(events.filter(_.isInstanceOf[ObstacleRemove]).map(_.asInstanceOf[ObstacleRemove]).reverse)
    }
  }

//  protected def handleTankFillBullet(e:TankFillBullet) :Unit = {
//    tankMap.get(e.tankId).foreach{ tank =>
//      tank.fillABullet()
//    }
//  }
//
//  protected final def handleTankFillBullet(es:List[TankFillBullet]) :Unit = {
//    es foreach handleTankFillBullet
//  }
//
//  final protected def handleTankFillBulletNow() = {
//    followEventMap.get(systemFrame).foreach{ events =>
//      handleTankFillBullet(events.filter(_.isInstanceOf[TankFillBullet]).map(_.asInstanceOf[TankFillBullet]).reverse)
//    }
//  }
//
//
//  protected def handleTankInvincible(e:TankInvincible) :Unit = {
////    println(s"removeininEvent${e.tankId}")
//    tankMap.get(e.tankId).foreach{ tank =>
//      tank.clearInvincibleState()
//    }
//  }
//
//  protected final def handleTankInvincible(es:List[TankInvincible]) :Unit = {
//    es foreach handleTankInvincible
//  }
//
//  final protected def handleTankInvincibleNow() :Unit = {
////    println(s"---------------------------------------invicible")
//    followEventMap.get(systemFrame).foreach{ events =>
//      handleTankInvincible(events.filter(_.isInstanceOf[TankInvincible]).map(_.asInstanceOf[TankInvincible]).reverse)
//    }
//  }
//
//
//  protected def handleTankShotgunExpire(e:TankShotgunExpire) :Unit = {
//    tankMap.get(e.tankId).foreach{ tank =>
//      tank.clearShotgunState()
//    }
//  }
//
//  protected final def handleTankShotgunExpire(es:List[TankShotgunExpire]) :Unit = {
//    es foreach handleTankShotgunExpire
//  }
//
//  final protected def handleTankShotgunExpireNow() = {
//    followEventMap.get(systemFrame).foreach{ events =>
//      handleTankShotgunExpire(events.filter(_.isInstanceOf[TankShotgunExpire]).map(_.asInstanceOf[TankShotgunExpire]).reverse)
//    }
//  }



  protected def racketMove():Unit = {
    /**
      * 坦克移动过程中检测是否吃道具
      * */
    racketMap.toList.sortBy(_._1).map(_._2).foreach{ racket =>
      racket.move(quadTree,boundary)
    }
  }
//
//  //后台需要重写，生成吃到道具事件，客户端不必重写
//  protected def tankEatPropCallback(tank:Tank)(prop: Prop):Unit = {}
//
  protected def gameOverCallBack(racket: Racket):Unit = {}

  protected def ballMove():Unit = {
    /**
      * 后端拍子砖块和球的位置都是在屏幕的下半方，进行碰撞检测的时候和正常的流程是一样的
      * 前端进行碰撞检测的时候，每个客户端绘制的不一样，如果和前端的racket相同，和后端的碰撞检测相同，如果不一样就反过来判断
      * 还有游戏结束的逻辑
      * */
    ballMap.toList.sortBy(_._1).map(_._2).foreach{ ball =>
      val objects = quadTree.retrieveFilter(ball)
      objects.filter(_.isInstanceOf[Racket]).map(_.asInstanceOf[Racket])
        .foreach{t =>
          if(t.racketId == ball.racketId){ ball.checkAttackObject(t,collisionRacketCallBack(ball))}
         }
      objects.filter(t => t.isInstanceOf[ObstacleBall] && t.isInstanceOf[Obstacle]).map(_.asInstanceOf[Obstacle])
        .foreach(t =>
          if(t.racketId == ball.racketId) ball.checkAttackObject(t,collisionObstacleCallBack(ball)))
      val gameOver = ball.move(false,Rectangle(Point(0,config.getRankHeight),config.boundary),systemFrame)
      if(gameOver){
        println("game over")
        racketMap.get(ball.racketId) match{
          case Some(racket) =>
//            gameOverCallBack(racket)
          case None =>
        }
      }
//      if(ball.getPosition.y >= config.boundary.y / 2){
//
//      }else{
//        println(s"----小球飞到上边界")
////        val gameOver = ball.move(Rectangle(Point(0,config.boundary.y / 2),boundary),systemFrame)
////        if(gameOver){
////          racketMap.get(ball.racketId) match{
////            case Some(racket) =>
//////              gameOverCallBack(racket)
////            case None =>
////          }
////        }
//      }
    }
  }


//  protected def ballFlyEndCallback(ball: Ball):Unit = {
//    ball.changeDirection()
////    ballMap.remove(ball.bId)
////    quadTree.remove(ball)
//  }

  //游戏后端需要重写，生成伤害事件
  protected def collisionRacketCallBack(ball: Ball)(racket: Racket):Unit = {
    /**
      * 判断球和racket的碰撞的面
      * */
    ball.changeDirection(racket.getPosition,racket.getWidth,racket.getHeight)
//    val event = BreakoutGameEvent.RacketCollision(racket.racketId,ball.bId,systemFrame)
//    addFollowEvent(event)
  }


  //子弹攻击到障碍物的回调函数，游戏后端需要重写,生成伤害事件
  protected def collisionObstacleCallBack(ball: Ball)(o:Obstacle):Unit = {
    //fixme
    ball.changeDirection(o.getObstacleState().p,o.getWidth,o.getHeight)
    val event = BreakoutGameEvent.ObstacleCollision(o.oId,ball.bId,o.racketId,o.getObstacleState().p,frame = systemFrame)
    addFollowEvent(event)
  }

//  protected final def removeBullet(bullet: Bullet):Unit = {
//    bulletMap.remove(bullet.bId)
//    quadTree.remove(bullet)
//  }

  protected final def objectMove():Unit = {
    racketMove()
    ballMove()
  }

  protected final def addUserAction(action:UserActionEvent):Unit = {
    actionEventMap.get(action.frame) match {
      case Some(actionEvents) => actionEventMap.put(action.frame,action :: actionEvents)
      case None => actionEventMap.put(action.frame,List(action))
    }
  }


  protected final def addGameEvent(event:GameEvent):Unit = {
    gameEventMap.get(event.frame) match {
      case Some(events) => gameEventMap.put(event.frame, event :: events)
      case None => gameEventMap.put(event.frame,List(event))
    }
  }

  protected final def addFollowEvent(event:FollowEvent):Unit = {
    followEventMap.get(event.frame) match {
      case Some(events) => followEventMap.put(event.frame, event :: events)
      case None => followEventMap.put(event.frame,List(event))
    }
  }
//
//  final protected def fillBulletCallBack(tid:Int):Unit={
//    addFollowEvent(TankGameEvent.TankFillBullet(tid,systemFrame+config.fillBulletDuration))
//  }
//
//  final protected def tankInvincibleCallBack(tid:Int):Unit={
//    addFollowEvent(TankGameEvent.TankInvincible(tid,systemFrame+config.initInvincibleDuration))
//  }
//
//  final protected def tankShotgunExpireCallBack(tid:Int):Unit={
//    //remind 删除之前的tank散弹失效事件
//    followEventMap.foreach{r=>
//      followEventMap.update(r._1,r._2.filterNot(e=>e.isInstanceOf[TankShotgunExpire]&&e.asInstanceOf[TankShotgunExpire].tankId==tid))
//    }
//    addFollowEvent(TankGameEvent.TankShotgunExpire(tid,systemFrame+config.shotgunDuration))
//  }
//
//  final protected def handlePropLifecycleNow() = {
//    propMap.values.foreach{ prop =>
//      if(!prop.updateLifecycle()){
//        quadTree.remove(prop)
//        propMap.remove(prop.pId)
//      }
//    }
//  }
//
  //更新本桢的操作
  def update():Unit = {
//    handleUserLeftRoomNow()
  objectMove()

  handleUserActionEventNow()

  handleRacketCollisionNow()
  handleObstacleCollisionNow()
//
//    handleTankFillBulletNow()
//    handleTankInvincibleNow()
//    handleTankShotgunExpireNow()
//
//    handleTankEatPropNow()
//
//    handlePropLifecycleNow()

//    handleObstacleRemoveNow() //此处需要结合坦克攻击，在移动之后
//    handleGenerateObstacleNow()
//    handleGeneratePropNow()

//    handleGenerateBulletNow()
//    handleUserJoinRoomEventNow()
//    handleUserReliveNow()

    quadTree.refresh(quadTree)
//    updateKillInformation()

//    handleRemoveHistoryMapNow()
    clearEventWhenUpdate()
  }
//
//  implicit val scoreOrdering = new Ordering[Score] {
//    override def compare(x: Score, y: Score): Int = {
//      var r = y.k - x.k
//      if (r == 0) {
//        r = y.d - x.d
//      }
//      if (r == 0) {
//        r = y.l - x.l
//      }
//      if (r == 0) {
//        r = (x.id - y.id).toInt
//      }
//      r
//    }
//  }
//
//  def updateRanks() = {
////    println(s"更新排行榜")
//    currentRank = tankMap.values.map(s => Score(s.tankId, s.name, s.killTankNum, s.damageStatistics, s.lives)).toList.sorted
//    var historyChange = false
//    currentRank.foreach { cScore =>
//      historyRankMap.get(cScore.id) match {
//        case Some(oldScore) if cScore.d > oldScore.d || cScore.l < oldScore.l =>
//          historyRankMap += (cScore.id -> cScore)
//          historyChange = true
//        case None if cScore.d > historyRankThreshold =>
//          historyRankMap += (cScore.id -> cScore)
//          historyChange = true
//        case _ =>
//
//      }
//    }
//
//    if (historyChange) {
//      historyRank = historyRankMap.values.toList.sorted.take(historyRankLength)
//      historyRankThreshold = historyRank.lastOption.map(_.d).getOrElse(-1)
//      historyRankMap = historyRank.map(s => s.id -> s).toMap
//    }
//  }
//
  protected def clearEventWhenUpdate():Unit

  def getGameContainerAllState():GameContainerAllState = {
    GameContainerAllState(
      systemFrame,
      racketMap.values.map(_.getRacketState()).toList,
      ballMap.values.map(_.getBallState()).toList,
      obstacleMap.values.map(_.getObstacleState()).toList,
//      environmentMap.values.map(_.getObstacleState()).toList,
      racketMoveAction.toList.map(t => (t._1,if(t._2.isEmpty) None else Some(t._2.toList)))
    )
  }

  /**
    * @author sky
    * 重置followEventMap
    * 筛选回溯之前帧产生的事件,不包含本帧
    * */
//  protected def reSetFollowEventMap(frame:Long)={
//    followEventMap.foreach{l=>
//      val eventList=l._2.filter(r=>
//        (r.isInstanceOf[TankGameEvent.TankInvincible]&&(r.frame-config.initInvincibleDuration<frame))||(r.isInstanceOf[TankGameEvent.TankFillBullet]&&(r.frame-config.fillBulletDuration<frame))||
//          (r.isInstanceOf[TankGameEvent.TankShotgunExpire]&&(r.frame-config.shotgunDuration<frame)))
//      followEventMap.put(l._1,eventList)
//    }
//  }

  protected def addGameEvents(frame:Long,events:List[GameEvent],actionEvents:List[UserActionEvent]) = {
    gameEventMap.put(frame,events)
    actionEventMap.put(frame,actionEvents)
  }

  def removePreEvent(frame:Long, tankId:Int, serialNum:Byte):Unit = {
    actionEventMap.get(frame).foreach{ actions =>
      actionEventMap.put(frame,actions.filterNot(t => t.racketId == tankId && t.serialNum == serialNum))
    }
  }
}