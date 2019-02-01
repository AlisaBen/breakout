package com.neo.sk.breakout.shared.`object`

import com.neo.sk.breakout.shared.config.GameConfig
import com.neo.sk.breakout.shared.model
import com.neo.sk.breakout.shared.model.Constants.ObstacleType

/**
  * Created by hongruying on 2018/8/22
  * Edit by benyafang on 2019/2/1
  * 砖头元素
  */
case class Brick(
                  config:GameConfig,
                  override val oId: Int,
                  override protected var position: model.Point
                ) extends Obstacle with ObstacleBall{

  def this(config: GameConfig,obstacleState: ObstacleState){
    this(config,obstacleState.oId,obstacleState.p)
  }


  override val obstacleType = ObstacleType.brick
  override protected val height: Float = config.obstacleWidth
  override protected val width: Float = config.obstacleWidth
  override protected val collisionOffset: Float = config.obstacleWO

  def getObstacleState():ObstacleState = ObstacleState(oId,obstacleType,position)

}
