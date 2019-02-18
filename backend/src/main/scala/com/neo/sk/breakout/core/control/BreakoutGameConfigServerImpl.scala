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

  private[this] val gridBoundaryWidth = config.getInt("breakoutGame.gridBoundary.width")
    .requiring(_ > 50,"minimum supported grid boundary width is 100")
  private[this] val gridBoundaryHeight = config.getInt("breakoutGame.gridBoundary.height")
    .requiring(_ > 50,"minimum supported grid boundary height is 50")
  private[this] val gridBoundary = GridBoundary(gridBoundaryWidth,gridBoundaryHeight)

  private[this] val gameFameDuration = config.getLong("breakoutGame.frameDuration")
    .requiring(t => t >= 1l,"minimum game frame duration is 1 ms")
  private[this] val gamePlayRate = config.getInt("breakoutGame.playRate")
    .requiring(t => t >= 1,"minimum game playRate duration is 1")

  private[this] val ballRadius = config.getDouble("breakoutGame.ball.ballRadius")
    .requiring(_ >= 0,"ball radius size has 3 type").toFloat
  //    .asScala.toList.map(_.toFloat)

  private[this] val racketSpeed = config.getInt("breakoutGame.racket.racketSpeedLevel")
    .requiring(_ > 0,"minimum supported racket speed size is 3")
  private[this] val racketWidth = config.getDouble("breakoutGame.racket.racketWidth")
    .requiring(_ > 0,"minimum supported racket speed size is 3").toFloat
  private[this] val racketHeight = config.getDouble("breakoutGame.racket.racketHeight")
    .requiring(_ > 0,"minimum supported racket speed size is 3").toFloat
  //  .asScala.map(_.toInt).toList

  //  private[this] val maxFlyFrameData = config.getInt("breakoutGame.ball.maxFlyFrame")
  //    .requiring(_ > 0,"minimum ball max fly frame is 1")
  private[this] val ballSpeedData = config.getInt("breakoutGame.ball.ballSpeed")
    .requiring(_ > 0,"minimum ball speed is 1")
  private val ballParameters = BallParameters(ballSpeedData,ballRadius)

  private[this] val obstacleWidthData = config.getDouble("breakoutGame.obstacle.width")
    .requiring(_ > 0,"minimum supported obstacle width is 1").toFloat
  private[this] val collisionWOffset = config.getDouble("breakoutGame.obstacle.collisionWidthOffset")
    .requiring(_ > 0,"minimum supported obstacle width is 1").toFloat

  //  private[this] val brickNumData = config.getInt("breakoutGame.obstacle.brick.num")
  //    .requiring(_ >= 0,"minimum supported brick num is 0")

  private[this] val brickHorizontalNumData = config.getInt("breakoutGame.obstacle.brick.brickHorizontalNum")
    .requiring(_ >= 0,"minimum supported obstacle width is 1")
  private[this] val brickVerticalNumData = config.getInt("breakoutGame.obstacle.brick.brickVerticalNum")
    .requiring(_ >= 0,"minimum supported obstacle width is 1")
  private[this] val brickHeightData = config.getDouble("breakoutGame.obstacle.brick.brickHeight")
    .requiring(_ >= 0,"minimum supported obstacle width is 1").toFloat

  private val obstacleParameters = ObstacleParameters(obstacleWidthData,collisionWOffset,
    brickParameters = BrickParameters(brickHorizontalNumData,brickVerticalNumData,brickHeightData),
  )

  val rankHeight = config.getDouble("breakoutGame.rankHeight")
    .requiring(_ >= 0,"minimum supported is 0").toFloat

  private val racketParameters = RacketParameters(racketWidth,racketHeight,Point(racketSpeed,0))

  private val breakoutGameConfig = GameConfigImpl(gridBoundary,gameFameDuration,gamePlayRate,rankHeight,ballParameters,obstacleParameters,racketParameters)


  def getBreakoutGameConfigImpl(): GameConfigImpl = breakoutGameConfig
  def getGameConfigImpl(): GameConfigImpl = breakoutGameConfig

  override def getRankHeight: Float = breakoutGameConfig.rankHeight
  /***/
  def frameDuration:Long = breakoutGameConfig.frameDuration
  def playRate:Int = breakoutGameConfig.playRate

  def getBallRadius:Float = breakoutGameConfig.getBallRadius

  def ballSpeed:Point = breakoutGameConfig.ballSpeed




  def boundary:Point = breakoutGameConfig.boundary

  def obstacleWidth:Float = breakoutGameConfig.obstacleWidth
  def obstacleWO: Float = obstacleParameters.collisionWidthOffset

  def brickVerticalNum:Int = breakoutGameConfig.obstacleParameters.brickParameters.verticalNum
  def brickHorizontalNum:Int = breakoutGameConfig.obstacleParameters.brickParameters.horizontalNum
  def brickHeight:Float = breakoutGameConfig.obstacleParameters.brickParameters.brickHeight

  def getRacketSpeedByType:Point = breakoutGameConfig.racketParameters.speed

  override def getRacketHeight: Float = breakoutGameConfig.racketParameters.racketHeight

  override def getRacketWidth: Float = breakoutGameConfig.racketParameters.racketWidth

  //  def getMoveDistanceByFrame:Point

}
