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
    db.run(tUserInfo.filter(t => t.userName === userName && t.password === password).map(_.isForbidden).result)
  }

  def insertBattleRecord(r:rBattleRecord) = {
    db.run(tBattleRecord.returning(tBattleRecord.map(_.recordId)) += r)
  }

  def updateUserInfo(name:String,isWin:Boolean) = {
    for{
      result <- db.run(tUserInfo.filter(_.userName === name).map(t => (t.playNum,t.winNum)).result)
    }yield {
      if(result.length > 0){
        db.run(tUserInfo.filter(_.userName === name).map(t => (t.playNum,t.winNum))
          .update((result.head._1 + 1,if(isWin)result.head._2 + 1 else result.head._2)))
        1
      }else{
        -1
      }
    }
  }

  def insertVisitorInfo(r:rVisitorInfo) = {
    db.run(tVisitorInfo.returning(tVisitorInfo.map(_.id)) += r)
  }

  def getIsForbidden(name:String) = {
    db.run(tUserInfo.filter(_.userName === name).map(_.isForbidden).result.headOption)
  }



}
