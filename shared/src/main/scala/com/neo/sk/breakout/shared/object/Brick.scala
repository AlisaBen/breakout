package com.neo.sk.breakout.shared.`object`

import com.neo.sk.breakout.shared.config.GameConfig
import com.neo.sk.breakout.shared.model
import com.neo.sk.breakout.shared.model.Constants.ObstacleType
import com.neo.sk.breakout.shared.model.Point

/**
  * Created by hongruying on 2018/8/22
  * Edit by benyafang on 2019/2/1
  * 砖头元素
  */
case class BrickState(racketId:Int,oId:Int,t:Byte,p:Point,value:Int)//id,类型（racket,brick），位置

case class Brick(
                  config:GameConfig,
                  override val oId: Int,
                  override val racketId:Int,
                  override var position: model.Point,
                  value:Int,
                  obstacleType:Byte
                ) extends Obstacle with ObstacleBall{

  def this(config: GameConfig,obstacleState: ObstacleState){
    this(config,obstacleState.oId,obstacleState.racketId,obstacleState.p,obstacleState.value,obstacleState.t)
  }


//  override val obstacleType = ObstacleType.brick
  override protected val height: Float = config.brickHeight
  override protected val width: Float = config.boundary.x / config.brickHorizontalNum
  override protected val collisionOffset: Float = config.obstacleWO

  def getObstacleState():ObstacleState = ObstacleState(racketId,oId,obstacleType,position,value)
//  def getBrickState():BrickState = BrickState(racketId,oId,obstacleType,position,value)

}
