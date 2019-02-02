package com.neo.sk.breakout.shared.`object`

import com.neo.sk.breakout.shared.config.GameConfig
import com.neo.sk.breakout.shared.model
import com.neo.sk.breakout.shared.model.Point

/**
  * Created by hongruying on 2018/7/8
  * Edit by benyafang on 2019/2/1 pm 23:52
  * 子弹
  */

case class BallState(bId:Int, brickId:Int, startFrame:Long, position:Point,momentum:Point)


case class Ball(
                 config:GameConfig,
                 override protected var position: Point,
                 startFrame:Long,
//                 damage:Int, //威力
                 momentum:Point,//移动的速度
                 bId:Int,
                 brickId:Int
                 ) extends CircleObjectOfGame{

  def this(config:GameConfig, ballState: BallState){
    this(config,ballState.position,ballState.startFrame,ballState.momentum,ballState.bId,ballState.brickId)
  }


//  val momentum: Point = momentum
  //todo
  override val radius: Float = config.getBallRadius

  val maxFlyFrame:Int = config.maxFlyFrame

  // 获取子弹外形
  override def getObjectRect(): model.Rectangle = {
    model.Rectangle(this.position - Point(this.radius,this.radius),this.position + Point(this.radius, this.radius))
  }


  def getBallState(): BallState = {
    BallState(bId,brickId,startFrame,position,momentum)
  }


  //子弹碰撞检测
  def isIntersectsObject(o: ObjectOfGame):Boolean = {
    this.isIntersects(o)
  }

  // 生命周期是否截至或者打到地图边界
  def isFlyEnd(boundary: Point,frame:Long):Boolean = {
    if( frame-this.startFrame > maxFlyFrame || position.x <= 0 || position.y <= 0 || position.x >= boundary.x || position.y >= boundary.y)
      true
    else
      false
  }

  // 先检测是否生命周期结束，如果没结束继续移动
  def move(boundary: Point,frame:Long,flyEndCallBack:Ball => Unit):Unit = {
    if(isFlyEnd(boundary,frame)){
      flyEndCallBack(this)
    } else{
      this.position = this.position + momentum
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

