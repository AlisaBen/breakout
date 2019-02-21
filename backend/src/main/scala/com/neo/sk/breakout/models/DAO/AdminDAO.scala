package com.neo.sk.breakout.models.DAO
import com.neo.sk.breakout.models.SlickTables._
import com.neo.sk.breakout.Boot.executor
import com.neo.sk.breakout.shared.ptcl.AdminProtocol.UserManagerProtocol.UserInfo
import com.neo.sk.utils.DBUtil.db
import slick.jdbc.H2Profile.api._

/**
  * created by benyafang on 2019/2/20 15:11
  * */
object AdminDAO {

  def getUserInfoList() ={
    db.run(tUserInfo.result)
  }

  def getUserInfoList(beginTime:Long,endTime:Long) = {
    db.run(tUserInfo.filter(t => t.timestamp >= beginTime && t.timestamp <= endTime).result)
  }

  def getGameStatisticList() = {
    db.run(tBattleRecord.result)
  }

  def updateUserForbidden(name:String,isForbidden:Boolean) = {
    db.run(tUserInfo.filter(_.userName === name).map(_.isForbidden).update(isForbidden))
  }

}
