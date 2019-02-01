package com.neo.sk.breakout.shared.`object`

import com.neo.sk.breakout.shared.config.GameConfig
import com.neo.sk.breakout.shared.model
import com.neo.sk.breakout.shared.model.Constants.{ObstacleType, RacketParameter}

/**
  * created by benyafang on 2019/2/1 pm 23:20
  *
  * */

case class Racket(
                 config:GameConfig,
                 override val oId: Int,
                 override protected var position: model.Point
                 ) extends Obstacle with ObstacleBall {
  def this(config: GameConfig,obstacleState: ObstacleState){
    this(config,obstacleState.oId,obstacleState.p)
  }

  override val obstacleType: Byte = ObstacleType.racket
  //todo racket height width
  override protected val height: Float = config.obstacleWidth
  override protected val width: Float = config.obstacleWidth
  override protected val collisionOffset: Float = config.obstacleWO

  override def getObstacleState(): ObstacleState = ObstacleState(oId,obstacleType,position)

  //todo 拍子的移动速度，可考虑移动到config中
  val moveSpeed: model.Point = RacketParameter.speed

}
