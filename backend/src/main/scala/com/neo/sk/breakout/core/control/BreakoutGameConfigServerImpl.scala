package com.neo.sk.breakout.core.control

import java.util.concurrent.atomic.AtomicInteger

import akka.actor.typed.ActorRef
import akka.util.Helpers
import com.neo.sk.breakout.core.UserActor
import com.neo.sk.breakout.shared.`object`.{Ball, Brick, Racket}
import com.neo.sk.breakout.shared.config._
import com.neo.sk.breakout.shared.game.GameContainer
import com.neo.sk.breakout.shared.model.Point
import com.neo.sk.breakout.shared.protocol.BreakoutGameEvent
import com.typesafe.config.Config
import org.slf4j.Logger

import scala.collection.mutable
import scala.util.Random

/**
  * created by benyafang on 2019/2/8 21:52
  * C
  * */
case class BreakoutGameConfigServerImpl(
                                         config:Config
                                       ) extends GameConfig{
  import collection.JavaConverters._
  import Helpers.Requiring
  import Helpers.ConfigOps

  private[this] val gridBoundaryWidth = config.getInt("tankGame.gridBoundary.width")
    .requiring(_ > 50,"minimum supported grid boundary width is 100")
  private[this] val gridBoundaryHeight = config.getInt("tankGame.gridBoundary.height")
    .requiring(_ > 50,"minimum supported grid boundary height is 50")
  private[this] val gridBoundary = GridBoundary(gridBoundaryWidth,gridBoundaryHeight)

  private[this] val gameFameDuration = config.getLong("tankGame.frameDuration")
    .requiring(t => t >= 1l,"minimum game frame duration is 1 ms")
  private[this] val gamePlayRate = config.getInt("tankGame.playRate")
    .requiring(t => t >= 1,"minimum game playRate duration is 1")

  private[this] val ballRadius = config.getDouble("tankGame.bullet.bulletRadius")
    .requiring(_ >= 0,"bullet radius size has 3 type")
//    .asScala.toList.map(_.toFloat)

private[this] val racketSpeed = config.getInt("tankGame.tank.tankSpeedLevel")
  .requiring(_ > 0,"minimum supported tank speed size is 3")
//  .asScala.map(_.toInt).toList

//  private[this] val maxFlyFrameData = config.getInt("tankGame.bullet.maxFlyFrame")
//    .requiring(_ > 0,"minimum bullet max fly frame is 1")
  private[this] val ballSpeedData = config.getInt("tankGame.bullet.bulletSpeed")
    .requiring(_ > 0,"minimum bullet speed is 1")
  private val bulletParameters = BallParameters(ballSpeedData,ballRadius)

  private[this] val obstacleWidthData = config.getDouble("tankGame.obstacle.width")
    .requiring(_ > 0,"minimum supported obstacle width is 1").toFloat
  private[this] val collisionWOffset = config.getDouble("tankGame.obstacle.collisionWidthOffset")
    .requiring(_ > 0,"minimum supported obstacle width is 1").toFloat

//  private[this] val brickNumData = config.getInt("tankGame.obstacle.brick.num")
//    .requiring(_ >= 0,"minimum supported brick num is 0")

  private[this] val brickHorizontalNumData = config.getInt("tankGame.obstacle.brick.brickHorizontalNum")
      .requiring(_ >= 0,"minimum supported obstacle width is 1")
  private[this] val brickVerticalNumData = config.getInt("tankGame.obstacle.brick.brickVerticalNum")
    .requiring(_ >= 0,"minimum supported obstacle width is 1")
  private[this] val brickHeightData = config.getDouble("tankGame.obstacle.brick.brickHeight")
    .requiring(_ >= 0,"minimum supported obstacle width is 1")

  private val obstacleParameters = ObstacleParameters(obstacleWidthData,collisionWOffset,
    brickParameters = BrickParameters(brickHorizontalNumData,brickVerticalNumData,brickHeightData),
  )

  private val racketParameters = RacketParameters(Point(racketSpeed,0))

  private val breakoutGameConfig = GameConfigImpl(gridBoundary,gameFameDuration,gamePlayRate,bulletParameters,obstacleParameters,racketParameters)


  def getGameConfig:GameConfigImpl = breakoutGameConfig
  def getGameConfigImpl(): GameConfigImpl = breakoutGameConfig
/***/
  def frameDuration:Long = breakoutGameConfig.frameDuration
  def playRate:Int = breakoutGameConfig.playRate

  def getBallRadius:Double = breakoutGameConfig.getBallRadius

  def ballSpeed:Point = breakoutGameConfig.ballSpeed




  def boundary:Point = breakoutGameConfig.boundary

  def obstacleWidth:Float = breakoutGameConfig.obstacleWidth
  def obstacleWO: Float = obstacleParameters.collisionWidthOffset

  def brickVerticalNum:Int = breakoutGameConfig.obstacleParameters.brickParameters.verticalNum
  def brickHorizontalNum:Int = breakoutGameConfig.obstacleParameters.brickParameters.horizontalNum
  def brickHeight:Double = breakoutGameConfig.obstacleParameters.brickParameters.brickHeight

  def getRacketSpeedByType:Point = breakoutGameConfig.racketParameters.speed

//  def getMoveDistanceByFrame:Point

}
