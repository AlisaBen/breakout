package com.neo.sk.breakout.shared.model

import scala.util.Random

/**
  * Created by hongruying on 2018/8/28
  */
object Constants {

  val nameList = List("安琪拉","白起","不知火舞","妲己","狄仁杰","典韦","韩信","老夫子","刘邦",
  "刘禅","鲁班七号","墨子","孙膑","孙尚香","孙悟空","项羽","亚瑟","周瑜",
  "庄周","蔡文姬","甄姬","廉颇","程咬金","后羿","扁鹊","钟无艳","小乔","王昭君",
  "虞姬","李元芳","张飞","刘备","牛魔王","张良","兰陵王","露娜","貂蝉","达摩","曹操",
  "芈月","荆轲","高渐离","钟馗","花木兰","关羽","李白","宫本武藏","吕布","嬴政",
  "娜可露露","武则天","赵云","姜子牙","哪吒","诸葛亮","黄忠","大乔","东皇太一",
  "庞统","干将莫邪","鬼谷子","女娲")
  val needSpecialName = false

  val colorList = List("#525252","#515151","#404040","#3D3D3D","#5E5E5E","#666666","#6B6B6B","#7A7A7A",
  "#36648B","#4169E1","#458B74","#4A708B","#4682B4","#388E8E","#2E8B57","#548B54")

  val drawHistory = false

  object BoundaryProperty{
    val up = 1
    val down = 2
    val right = 3
    val left = 4
    val middle = 5
  }

  object RacketParameter{
    val speed = Point(2,0)
  }

  object DirectionType {
    final val right:Float = 0
    final val upRight = math.Pi / 4 * 7
    final val up = math.Pi / 2 * 3
    final val upLeft = math.Pi / 4 * 5
    final val left:Float = math.Pi.toFloat
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
//    val racket:Byte = 2
    val fastRemove:Byte = 3
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
//    val matching = 7
  }


  final val WindowView = Point(108,192)

  object GameModel{
    val doubleFight = 1
    val manMachine = 2
  }

}
