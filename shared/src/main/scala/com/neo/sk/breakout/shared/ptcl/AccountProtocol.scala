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

  case class NameStorage(
                        name:String
                        )
}
