package com.neo.sk.breakout.front.pages

import com.neo.sk.breakout.front.common.{Page, Routes}
import com.neo.sk.breakout.front.components.PageMod
import com.neo.sk.breakout.front.utils.{Http, JsFunc}
import com.neo.sk.breakout.shared.ptcl.AdminProtocol.RoomManagerProtocol.{GetAllRoomReq, GetAllRoomRsp}
import com.neo.sk.breakout.shared.ptcl.AdminProtocol.StatisticProtocol.{GameStatisticProfile, GameStatisticProfileRsp}
import com.neo.sk.breakout.shared.ptcl.AdminProtocol.UserManagerProtocol
import com.neo.sk.breakout.shared.ptcl.AdminProtocol.UserManagerProtocol.{GetUserInfoListReq, GetUserInfoListRsp, UserForbiddenReq}
import com.neo.sk.breakout.shared.ptcl.SuccessRsp
import mhtml.Var
import scala.concurrent.ExecutionContext.Implicits.global
import scala.xml.Elem

/**
  * created by benyafang on 2019/2/22 18:47
  * */
object AdminPage extends Page{
  import io.circe._
  import io.circe.generic.auto._
  import io.circe.syntax._

  private val userInfoListVar:Var[List[UserManagerProtocol.UserInfo]] = Var(Nil)
//  var pageMod = new PageMod[UserManagerProtocol.UserInfo](getUserInfoList,true,confirmFunc = confirmAllChoose)


  private def getUserInfoList() :Unit = {
    val url = Routes.AdminRoute.getUserInfoListRoute
    val json = GetUserInfoListReq.asJson.toString()
    Http.postJsonAndParse[GetUserInfoListRsp](url,json).map{rsp =>
      if(rsp.errCode == 0){
//        userInfoListVar := rsp.userInfoList
      }else{
        JsFunc.alert(s"获取用户列表失败${rsp.msg}")
      }
    }
  }

  private def setUserForbidden():Unit = {
    val name = ""
    val forbidden = true
    val url = Routes.AdminRoute.userForbiddenRoute
    val json = UserForbiddenReq(name,forbidden).asJson.toString()
    Http.postJsonAndParse[SuccessRsp](url,json).map{rsp =>
      if(rsp.errCode == 0){

      }else{
        JsFunc.alert(s"设置用户禁用失败")
      }
    }
  }

  private def getAllRoom():Unit = {
    val url = Routes.AdminRoute.getAllRoomRoute
    val json = GetAllRoomReq().asJson.toString()
    Http.postJsonAndParse[GetAllRoomRsp](url,json).map{rsp =>
      if(rsp.errCode == 0){

      }else{
        JsFunc.alert(s"获取所有房间列表失败${rsp.msg}")
      }
    }
  }

  private def getGameStatistic():Unit = {
    val url = Routes.AdminRoute.getGameStatisticRoute
    val json = GameStatisticProfile().asJson.toString()
    Http.postJsonAndParse[GameStatisticProfileRsp](url,json).map{rsp =>
      if(rsp.errCode == 0){

      }else{
        JsFunc.alert(s"获取游戏数据失败：${rsp.msg}")
      }
    }
  }
//
  private def userManager() = {
    getUserInfoList()
    setUserForbidden()
  }

//  private val userInfoListDiv = userInfoListVar.map{userInfo =>
//
//  }





  override def render: Elem = {
    <div>
      <button onclick={() => userManager()}>用户管理</button>
      <button onclick={() => getAllRoom()}>房间管理</button>
      <button onclick={() => getGameStatistic()}>统计数据</button>
    </div>
  }

}
