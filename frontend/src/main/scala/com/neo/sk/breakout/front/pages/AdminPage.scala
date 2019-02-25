package com.neo.sk.breakout.front.pages

import java.util.concurrent.TimeUnit

import com.neo.sk.breakout.front.common.{Page, Routes}
import com.neo.sk.breakout.front.components.PageMod
import com.neo.sk.breakout.front.utils.{Http, JsFunc, TimeTool}
import com.neo.sk.breakout.shared.ptcl.AdminProtocol.RoomManagerProtocol.{GetAllRoomReq, GetAllRoomRsp}
import com.neo.sk.breakout.shared.ptcl.AdminProtocol.StatisticProtocol.{GameBattleInfo, GameStatisticProfile, GameStatisticProfileRsp}
import com.neo.sk.breakout.shared.ptcl.AdminProtocol.UserManagerProtocol
import com.neo.sk.breakout.shared.ptcl.AdminProtocol.UserManagerProtocol.{GetUserInfoListReq, GetUserInfoListRsp, UserForbiddenReq}
import com.neo.sk.breakout.shared.ptcl.SuccessRsp
import mhtml.Var
import org.scalajs.dom
import org.scalajs.dom.html.Button
//import org.scalajs.dom.html.Button
import com.neo.sk.breakout.front.pages.MainPage.gotoPage
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.xml.Elem

/**
  * created by benyafang on 2019/2/22 18:47
  * */
object AdminPage extends Page{
  import io.circe._
  import io.circe.generic.auto._
  import io.circe.syntax._

//  private val userInfoListVar:Var[List[UserManagerProtocol.UserInfo]] = Var(Nil)
  var userInfoListPageMod = new PageMod[UserManagerProtocol.UserInfo](getUserInfoList,false,confirmFunc = confirmAllChoose)

  private def confirmAllChoose(page:Int):Unit = {

  }


  private def getUserInfoList(page:Int,pageNum:Int) :Future[(Option[Int],List[UserManagerProtocol.UserInfo])] = {
    val url = Routes.AdminRoute.getUserInfoListRoute
    val json = GetUserInfoListReq(page,pageNum).asJson.toString()
    Http.postJsonAndParse[GetUserInfoListRsp](url,json).map{rsp =>
      if(rsp.errCode == 0 && rsp.userInfoList.nonEmpty){
        println(s"------${rsp.userInfoList}")
        (rsp.totalNum,rsp.userInfoList)
      }else{
        Routes.handleAuthError(rsp.errCode, rsp.msg) { () =>
          println(s"error:${rsp.msg}")
        }
        (None,Nil)
      }
    }
  }

  private def setUserForbidden(userInfo:UserManagerProtocol.UserInfo):Unit = {
    val name = userInfo.name
    val forbidden = !userInfo.isForbidden
    val url = Routes.AdminRoute.userForbiddenRoute
    val json = UserForbiddenReq(name,forbidden).asJson.toString()
    Http.postJsonAndParse[SuccessRsp](url,json).map{rsp =>
      if(rsp.errCode == 0){
        userInfoListPageMod.initPageInfo(10)
        dom.document.getElementById(userInfo.name).asInstanceOf[Button].value = if(forbidden) "取消禁用" else "禁用"
      }else{
        Routes.handleAuthError(rsp.errCode, rsp.msg) { () =>
          println(s"error:${rsp.msg}")
        }
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

  private val userInfoTable =
    <div>
      {
      userInfoListPageMod.indexData.map{
        case Nil =>
          <div style="width: 100%;display: table;">
            <div style="text-align: center;display: table-cell;vertical-align: middle;padding-top: 50px;padding-bottom: 100px;">
              <img style="width:100px" src="/breakout/static/img/userList/无数据.png"></img>
              <h3>无数据</h3>
            </div>
          </div>
        case ls =>
          <div id="table-div">
            <table id="bot_table" style="cellpadding:2px;margin-left:1.7%;margin-right:1.7%;border: 1px solid #D9DFEB;border-radius: 4px;">
              <tr style="background: #F5F7FA;border: 1px solid #D9DFEB;border-radius: 4px 4px 0 0;height:48px">
                <th style="padding-left:0.7%">账户</th>
                <th id="general-css-word" style="padding-left:3%">战斗次数</th>
                <th  id="general-css-word">胜利次数</th>
                <th id="general-css-word" >创建时间</th>
                <th  id="general-css-word">是否禁用</th>
              </tr>
              <tbody id="qrCode-table">
                {
                ls.map{info =>
                  <tr class="tr-line" style="width:100%">
                    <td class="td-line-1" style="padding-left:0.7%">
                      <div class="qrSceneStrDiv" name="qrSceneStrDiv">{info.name}</div>
                    </td>
                    <td class="td-line-2" style="width:200px;position:relative;padding-left:3%;">
                      <div id="img-line">{info.playNum}</div>
                    </td>
                    <td class="td-line-3" style="width:240px;font-family: PingFangSC-Regular;font-size: 14px;color: #1D2341;text-align: left;padding-right:3%;word-wrap:break-word">{info.winNum}</td>
                    <td class="td-line-4-1" style="width:20%;font-family: PingFangSC-Regular;font-size: 14px;color: #1D2341;text-align: left;">
                      {TimeTool.dateFormatDefault(info.registerTime,"yyyy-MM-dd")}
                    </td>
                    <td class="td-line-4-2" style="width:30%">
                      <button id={info.name} onclick={() => setUserForbidden(info)}>
                        {
                        if(info.isForbidden)"取消禁用" else "禁用"
                        }
                      </button>
                    </td>
                  </tr>
                }
                }
              </tbody>

            </table>
            <div style="display:inline-block;width: 936px;text-align:right;padding-right:1.7%">
              {
              userInfoListPageMod.pageDiv
              }
            </div>
          </div>
      }
      }
    </div>






  override def render: Elem = {
    {
      userInfoListPageMod.initPageInfo(10)
    }
    <div style="background: #FFFFFF;box-shadow: 0 2px 8px 0 rgba(0,0,0,0.07);border-radius: 4px;">
      <button onclick={() => gotoPage("#/admin/login")}>返回首页</button>
      <button onclick={() => gotoPage("#/statistic")}>统计数据页</button>
      <div>{userInfoTable}</div>
    </div>
  }

}
