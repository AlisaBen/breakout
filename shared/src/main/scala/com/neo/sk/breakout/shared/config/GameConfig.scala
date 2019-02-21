package com.neo.sk.breakout.shared.config

import com.neo.sk.breakout.shared.model.Point
import javafx.scene.input.ScrollEvent.HorizontalTextScrollUnits

/**
  * Created by hongruying on 2018/8/28
  * Edit by benyafang on 2019/2/1 pm 23;51
  */
final case class GridBoundary(width:Int,height:Int){
  def getBoundary:Point = Point(width,height)
}

//final case class GridLittleMap(width:Int,height:Int){
//  def getBoundary:Point = Point(width,height)
//}

////todo
//final case class TankMoveSpeed(
//                                speeds:List[Int],
//                                accelerationTime:List[Int],
//                                decelerationTime:List[Int]
//                              ){
//  //  val lowType:Byte = 1
//  //  val intermediateType:Byte = 2
//  //  val highType:Byte = 3
//
//  def getTankSpeedByType(t:Byte) = Point(speeds(t-1),0)
//}
//
//final case class TankBloodLevel(
//                                 first:Int,
//                                 second:Int,
//                                 third:Int
//                               ){
//  //  val first:Byte = 1
//  //  val second:Byte = 2
//  //  val third:Byte = 3
//
//  def getTankBloodByLevel(level:Int) :Int  = {
//    level match {
//      case 2 => second
//      case 3 => third
//      case _ => first
//    }
//  }
//
//}

//final case class TankParameters(
//                                 tankSpeed:TankMoveSpeed,
//                                 tankBloodLevel: List[Int],
//                                 tankLivesLimit:Int,
//                                 tankMedicalLimit:Int,
//                                 tankRadius:Float,
//                                 tankGunWidth:Float,
//                                 tankGunHeight:Float,
//                                 maxBulletCapacity:Int,
//                                 fillBulletDuration:Int,
//                                 initInvincibleDuration:Int,
//                                 tankReliveDuration:Int
//                               ){
//  def getTankBloodByLevel(l:Byte):Int = tankBloodLevel(l-1)
//}

//todo 增加道具的时候再修改
//final case class PropParameters(
//                                 radius:Float,
//                                 medicalBlood:Int,
//                                 shotgunDuration:Int, //散弹持续时间
//                                 disappearTime:Int
//                               )
//
//final case class AirDropParameters(
//                                    blood:Int,
//                                    num:Int
//                                  )

final case class BrickParameters(
                                  horizontalNum:Int,
                                  verticalNum:Int,
                                  brickHeight:Float,
                                  brickValue:Int,
                                  brickSpace:Float
                                )

final case class RacketParameters(
                                   racketWidth:Float,
                                   racketHeight:Float,
                                   speed:Point
                                 )

//final case class RiverParameters(
//                                  typePos:List[List[(Int, Int)]], //河流的元素位置
//                                  barrierPos:List[List[(Int,Int)]]
//                                )
//
//final case class SteelParameters(
//                                  typePos:List[List[(Int, Int)]], //钢铁的元素位置
//                                  barrierPos:List[List[(Int,Int)]]
//                                )

final case class ObstacleParameters(
                                     width:Float,
                                     collisionWidthOffset: Float,
                                     //                                     airDropParameters: AirDropParameters,
                                     brickParameters: BrickParameters,
                                     //                                     racketParameters:RacketParameters
                                     //                                     riverParameters: RiverParameters,
                                     //                                     steelParameters: SteelParameters
                                   )


final case class BallParameters(
                                 //                                   ballLevelParameters:List[(Float,Int)], //size,damage length 3
                                 ballSpeed:Int,
                                 ballRadius:Float
                               ){
  //  require(ballLevelParameters.size >= 3,println(s"bullet level parameter failed"))

  def getBallRadius = ballRadius

  //  def getBallDamage(l:Byte) = {
  //    ballLevelParameters(l-1)._2
  //  }

  //  def getBallRadiusByDamage(d:Int):Float = {
  //    ballLevelParameters.find(_._2 == d).map(_._1).getOrElse(ballLevelParameters.head._1)
  //  }

  //  def getBallLevelByDamage(d:Int):Byte = {
  //    (ballLevelParameters.zipWithIndex.find(_._1._2 == d).map(_._2).getOrElse(0) + 1).toByte
  //  }
}

trait GameConfig{
  def frameDuration:Long
  def playRate:Int

  def getBallRadius:Float
  //  def getBallDamage(l:Byte):Int
  //  def getBLevel(damage:Int):Byte
  //  def getBulletMaxLevel():Byte

  def ballSpeed:Point



  def boundary:Point

  def obstacleWidth:Float
  def obstacleWO: Float

  //  def airDropBlood:Int
  //  def airDropNum:Int

  def brickVerticalNum:Int
  def brickHorizontalNum:Int
  def brickHeight:Float
  def brickValue:Int
  def brickSpace:Float

  //  def riverPosType:List[List[(Int, Int)]]
  //  def steelPosType:List[List[(Int, Int)]]

  //  def barrierPos4River:List[List[(Int,Int)]]
  //  def barrierPos4Steel:List[List[(Int,Int)]]

  //  def propRadius:Float
  //  def propMedicalBlood:Int
  //  def shotgunDuration:Int


  //  def tankRadius:Float
  //  def tankGunWidth:Float
  //  def tankGunHeight:Float
  //  def maxBulletCapacity:Int
  //  def fillBulletDuration:Int
  //  def initInvincibleDuration:Int
  //  def getTankReliveDuration:Int
  def getRacketSpeedByType:Point
  def getRacketWidth:Float
  def getRacketHeight:Float

  def getRankHeight:Float
  //  def getTankAccByLevel(l: Byte): Int
  //  def getTankDecByLevel(l: Byte): Int
  //  def getTankSpeedMaxLevel():Byte
  //  def getTankBloodByLevel(l:Byte):Int
  //  def getTankBloodMaxLevel():Byte
  //  def getTankLivesLimit:Int
  //  def getTankMedicalLimit:Int

  //  def getBulletRadiusByDamage(d:Int):Float

  def getMoveDistanceByFrame:Point = getRacketSpeedByType * frameDuration / 1000

  def getGameConfigImpl(): GameConfigImpl

  //  def getPropDisappearFrame: Short
}

case class GameConfigImpl(
                           gridBoundary: GridBoundary,
                           frameDuration:Long,
                           playRate: Int,
                           rankHeight:Float,
                           ballParameters: BallParameters,
                           obstacleParameters: ObstacleParameters,
                           racketParameters: RacketParameters
                           //                               propParameters: PropParameters,
                           //                               tankParameters: TankParameters
                         ) extends GameConfig{

  def getGameConfigImpl(): GameConfigImpl = this


  def getBallRadius = {
    ballParameters.getBallRadius
  }

  //  def getBallDamage(l:Byte) = {
  //    ballParameters.getBallDamage(l)
  //  }

  //  def getBallLevel(damage:Int):Byte = ballParameters.getBallLevelByDamage(damage)

  //  def getBallMaxLevel():Byte = ballParameters.ballLevelParameters.size.toByte


  //  def getBulletRadiusByDamage(d:Int):Float = ballParameters.getBallRadiusByDamage(d)

  def ballSpeed = Point(ballParameters.ballSpeed,0)



  def boundary = gridBoundary.getBoundary

  def obstacleWidth = obstacleParameters.width
  def obstacleWO:Float = obstacleParameters.collisionWidthOffset

  //  def airDropBlood = obstacleParameters.airDropParameters.blood
  //  def airDropNum = obstacleParameters.airDropParameters.num

  def brickVerticalNum:Int = obstacleParameters.brickParameters.verticalNum
  def brickHorizontalNum:Int = obstacleParameters.brickParameters.horizontalNum
  def brickHeight:Float = obstacleParameters.brickParameters.brickHeight

  override def brickValue: Int = obstacleParameters.brickParameters.brickValue

  override def brickSpace: Float = obstacleParameters.brickParameters.brickSpace
  //  def riverPosType = obstacleParameters.riverParameters.typePos
  //  def steelPosType = obstacleParameters.steelParameters.typePos

  //  def barrierPos4River: List[List[(Int,Int)]] = obstacleParameters.riverParameters.barrierPos
  //  def barrierPos4Steel: List[List[(Int,Int)]] = obstacleParameters.steelParameters.barrierPos

  //  def propRadius = propParameters.radius
  //  def propMedicalBlood = propParameters.medicalBlood
  //  def shotgunDuration = propParameters.shotgunDuration
  //
  //
  //  def tankRadius = tankParameters.tankRadius
  //  def tankGunWidth = tankParameters.tankGunWidth
  //  def tankGunHeight = tankParameters.tankGunHeight
  //  def maxBulletCapacity = tankParameters.maxBulletCapacity
  //  def fillBulletDuration = tankParameters.fillBulletDuration
  //  def initInvincibleDuration = tankParameters.initInvincibleDuration
  //  def getTankReliveDuration = tankParameters.tankReliveDuration
  def getRacketSpeedByType = racketParameters.speed
  def getRacketWidth = racketParameters.racketWidth
  def getRacketHeight = racketParameters.racketHeight

  override def getRankHeight: Float = rankHeight
  //  def getMoveDistanceByFrame = getRacketSpeedByType * frameDuration / 1000
  //  def getTankSpeedMaxLevel():Byte = tankParameters.tankSpeed.speeds.size.toByte
  //
  //  def getTankBloodMaxLevel():Byte = tankParameters.tankBloodLevel.size.toByte
  //
  //  def getTankBloodByLevel(l:Byte):Int = tankParameters.getTankBloodByLevel(l)
  //
  //  def getTankLivesLimit: Int = tankParameters.tankLivesLimit
  //  def getTankMedicalLimit:Int = tankParameters.tankMedicalLimit
  //
  //
  //  def getTankAccByLevel(l: Byte): Int = tankParameters.tankSpeed.accelerationTime(l - 1)
  //  def getTankDecByLevel(l: Byte): Int = tankParameters.tankSpeed.decelerationTime(l - 1)
  //  def obstacleWO: Float = obstacleParameters.collisionWidthOffset
  //
  //  def getPropDisappearFrame: Short = (propParameters.disappearTime / frameDuration).toShort




}
