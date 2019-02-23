package com.neo.sk.breakout.shared.ptcl

/**
  * created by benyafang on 2019/2/2 pm 13:56
  * */
object AccountProtocol {

  case class RegisterReq(
                        userName:String,
                        password:String,
                        timestamp:Long
                        )

  case class LoginReq(
                     userName:String,
                     password:String
                     )

  case class LoginRsp(
                     uidOpt:Option[Long],
                     errCode:Int = 0,
                     msg:String = "ok"
                     )

  case class NameStorage(
                        uid:Long,
                        name:String,
                        isVisitor:Boolean
                        )
}
