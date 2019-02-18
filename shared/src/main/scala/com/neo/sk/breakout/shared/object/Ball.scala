package com.neo.sk.breakout.shared.`object`

import com.neo.sk.breakout.shared.config.GameConfig
import com.neo.sk.breakout.shared.model
import com.neo.sk.breakout.shared.model.Point
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
  def isFlyEnd(boundary: Point,flyEndCallBack:Ball => Unit):Int = {
    if(position.x - radius <= 0 || position.x + radius >= boundary.x){
      //左右边界
      1
    }else if(position.y - radius <= 0){
      //上下边界
      2
    }else if(position.y + radius >= boundary.y){
      flyEndCallBack(this)
      4
    }else
      3
  }

  // 先检测是否生命周期结束，如果没结束继续移动
  def move(boundary: Point,frame:Long,flyEndCallBack:Ball => Unit):Unit = {
    isFlyEnd(boundary,flyEndCallBack) match{
      case 1 =>
        //左右边界，不改变y轴的移动速度，改变x轴
        momentum.*(Point(-1,1))
      case 2 =>
        //上下边界，改变y轴
        momentum.*(Point(1,-1))
      case 3 =>
    }
    this.position = this.position + momentum
  }

  def changeDirection(isLeftOpt:Option[Boolean] = None) = {
    isLeftOpt match {
      case Some(isLeft) =>
        momentum.*(Point(-1,1))
      case None => momentum.*(Point(1, -1))
    }
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

