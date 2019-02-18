package com.neo.sk.breakout.front.common

import com.neo.sk.breakout.shared.model.Point
import org.scalajs.dom.ext.Color

/**
  * Created by hongruying on 2018/7/7
  */
object Constants {

  object TankColor {
    val my = Color.Green.toString()
    val other = Color.Yellow.toString()
  }

  val supportLiveLimit = false

  final val WindowView = Point(108,192)
//  object GameState{
//    val firstCome = 1
//    val play = 2
//    val stop = 3
//    val loadingPlay = 4
//    val relive = 5
//  }

}
