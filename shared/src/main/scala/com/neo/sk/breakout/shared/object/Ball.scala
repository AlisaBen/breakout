package com.neo.sk.breakout.shared.`object`

import com.neo.sk.breakout.shared.config.GameConfig
import com.neo.sk.breakout.shared.model
import com.neo.sk.breakout.shared.model.{Point, Rectangle}
import javafx.scene.effect.Light

/**
  * Created by hongruying on 2018/7/8
  * Edit by benyafang on 2019/2/1 pm 23:52
  * 子弹
  */

case class BallState(bId:Int, racketId:Int,position:Point,momentum:Point)


case class Ball(
                 config:GameConfig,
                 override protected var position: Point,
//                 damage:Int, //威力
                 var momentum:Point,//移动的速度
                 bId:Int,
                 racketId:Int
                 ) extends CircleObjectOfGame{

  def this(config:GameConfig, ballState: BallState){
    this(config,ballState.position,ballState.momentum,ballState.bId,ballState.racketId)
  }


//  val momentum: Point = momentum
  //todo
  override val radius: Float = config.getBallRadius.toFloat

  // 获取子弹外形
  override def getObjectRect(): model.Rectangle = {
    model.Rectangle(this.position - Point(this.radius,this.radius),this.position + Point(this.radius, this.radius))
  }


  def getBallState(): BallState = {
    BallState(bId,racketId,position,momentum)
  }


  //子弹碰撞检测
  def isIntersectsObject(o: ObjectOfGame):Boolean = {
    this.isIntersects(o)
  }

  def isIntersectsObject(o:Seq[ObjectOfGame]):Boolean = {
    o.exists(t => t.isIntersects(this))
  }

  // 生命周期是否截至或者打到地图边界
  def isFlyEnd(boundary: Rectangle):Int = {
    if(position.x - radius <= boundary.topLeft.x || position.x + radius >= boundary.downRight.x){
      //左右边界
      1
    }else if(position.y - radius <= 0){
      //上边界
      2
    }else if(position.y + radius >= boundary.downRight.y){
      4
    }else
      3
  }

  // 先检测是否生命周期结束，如果没结束继续移动
  def move(boundary: Rectangle,frame:Long) = {
    isFlyEnd(boundary) match{
      case 1 =>
        //左右边界，不改变y轴的移动速度，改变x轴
        momentum = Point(-1 * momentum.x,momentum.y)
        this.position = this.position + momentum
//        val a  = momentum.*(Point(-1,1))
        false
      case 2 =>
        //上下边界，改变y轴
        momentum = Point(momentum.x,-1 * momentum.y)
        this.position = this.position + momentum
        false
//        momentum.*(Point(1,-1))
      case 3 =>
        this.position = this.position + momentum
        false
      case 4 => true
        //游戏结束
    }
  }

  def changeDirection(obstaclePosition:Point,obstacleWidth:Float,obstacleHeight:Float) = {
    val rightOrLeftCondition = position.x >= obstaclePosition.x + obstacleWidth / 2 && position.y <= obstaclePosition.y + obstacleHeight / 2 ||
    position.x <= obstaclePosition.x - obstacleWidth / 2 && position.y >= obstaclePosition.y - obstacleHeight / 2

    if(rightOrLeftCondition)momentum = Point(-1 * momentum.x,momentum.y)
    else momentum = Point(momentum.x,-1 * momentum.y)
  }

  // 检测是否子弹有攻击到，攻击到，执行回调函数
  def checkAttackObject[T <: ObjectOfGame](o:T,attackCallBack:T => Unit):Unit = {
    if(this.isIntersects(o)){
      attackCallBack(o)
    }
  }


  def getPosition4Animation(offsetTime:Long) = {
    this.position + momentum / config.frameDuration * offsetTime
  }

//  def getBulletLevel() = {
//    config.getBulletLevel(damage)
//  }


}

