package com.neo.sk.breakout.models.DAO
import com.neo.sk.breakout.models.SlickTables._
import com.neo.sk.breakout.Boot.executor
import com.neo.sk.utils.DBUtil.db
import slick.jdbc.H2Profile.api._

import scala.concurrent.Future

/**
  * created by benyafang on 2019/2/2 16:04
  * */
object AccountDAO {

  def insertUserInfo(r:rUserInfo) = {
    db.run(tUserInfo.returning(tUserInfo.map(_.id)) += r)
  }

  def confirmUserInfo(userName:String,password:String) = {
    db.run(tUserInfo.filter(t => t.userName === userName && t.password === password).length.result)
  }

  def insertBattleRecord(r:rBattleRecord) = {
    db.run(tBattleRecord.returning(tBattleRecord.map(_.recordId)) += r)
  }



}
