package com.neo.sk.breakout.shared.model

import scala.util.Random

/**
  * Created by hongruying on 2018/8/28
  */
object Constants {

  val drawHistory = false

  object RacketParameter{
    val speed = Point(2,0)
  }

  object DirectionType {
    final val right:Float = 0
    final val upRight = math.Pi / 4 * 7
    final val up = math.Pi / 2 * 3
    final val upLeft = math.Pi / 4 * 5
    final val left = math.Pi
    final val downLeft = math.Pi / 4 * 3
    final val down = math.Pi / 2
    final val downRight = math.Pi / 4
  }

  object TankColor{
    val blue = "#1E90FF"
    val green = "#4EEE94"
    val red = "#EE4000"
    val tankColorList = List(blue,green,red)
    val gun = "#7A7A7A"
    def getRandomColorType(random:Random):Byte = random.nextInt(tankColorList.size).toByte

  }

  object InvincibleSize{
    val r = 5.5
  }

  object LittleMap {
    val w = 25
    val h = 20
  }

  object SmallBullet{
    val num = 4
    val height = 5
    val width = 1
  }

  object TankStar{
    val maxNum = 16
    val height = 2
    val width = 2
    val interval = 2
  }

  object ObstacleType{
//    val airDropBox:Byte = 1
    val brick:Byte = 1
    val racket:Byte = 2
//    val steel:Byte = 3
//    val river:Byte = 4
  }


  object PropGenerateType{
    val tank:Byte = 0
    val airDrop:Byte = 1
  }

  object GameAnimation{
    val bulletHitAnimationFrame = 8
    val tankDestroyAnimationFrame = 12
  }

  object PropAnimation{
    val DisAniFrame1 = 30
    val DisplayF1 = 6
    val DisappearF1 = 2
    val DisAniFrame2 = 10
    val DisplayF2 = 1
    val DisappearF2 = 1
  }


  val PreExecuteFrameOffset = 2 //预执行2帧
  val fakeRender = false

  object GameState{
    val firstCome = 1
    val play = 2
    val stop = 3
    val loadingPlay = 4
    val replayLoading = 5
    val leave = 6
  }


  final val WindowView = Point(192,108)

}
