package com.neo.sk.breakout.common

import com.neo.sk.breakout.shared.model.Point


/**
  * Created by hongruying on 2018/6/11
  */
object Constants {
  val steelPosition = List(
    (Point(60,150),true,5,false,4),
    (Point(180,45),false,3,false,5),
    (Point(180,45),true,3,false,0),
    (Point(320,40),false,4,true,4),
    (Point(340,90),false,9,true,0))//在position向右延伸4个,向下延伸3个
  val riverPosition = List(
    (Point(90,45),false,9,true,3),
    (Point(180,120),true,0,true,5),
    (Point(150,90),true,10,true,0),
    (Point(250,15),true,0,true,5),
    (Point(240,140),false,0,true,5))



  val TankGameUserIdPrefix = "tank_guest_"



}
