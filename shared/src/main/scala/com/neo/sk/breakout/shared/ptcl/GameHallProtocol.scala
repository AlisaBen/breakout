package com.neo.sk.breakout.shared.ptcl

/**
  * created by benyafang on 2019/2/9 15:08
  *
  * */
object GameHallProtocol {
  case class GameModelReq(
                         uid:Long,
                         name:String,
                         isVisitor:Boolean,
                         model:Int
                         )

  case class GetUId4Visitor(
                           name:String,
                           isVisitor:Boolean
                           )

}
