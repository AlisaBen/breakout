package com.neo.sk.breakout.front.pages

import com.neo.sk.breakout.front.common.{Page, Routes}
import com.neo.sk.breakout.front.components.PageMod
import com.neo.sk.breakout.front.utils.{Http, TimeTool}
import com.neo.sk.breakout.shared.ptcl.AdminProtocol.StatisticProtocol.{GameBattleInfo, GameStatisticProfile, GameStatisticProfileRsp}
import com.neo.sk.breakout.front.pages.MainPage.gotoPage
import scala.concurrent.Future
import scala.xml.Elem
import scala.concurrent.ExecutionContext.Implicits.global

object StatisticPage extends Page{

  import io.circe._
  import io.circe.generic.auto._
  import io.circe.syntax._

  var statisticPageMod = new PageMod[GameBattleInfo](getGameStatistic,false,confirmAllChoose)

  private def confirmAllChoose(page:Int):Unit = {

  }

  private def getGameStatistic(page:Int,pageNum:Int):Future[(Option[Int],List[GameBattleInfo])] = {
    val url = Routes.AdminRoute.getGameStatisticRoute
    val json = GameStatisticProfile(page,pageNum).asJson.toString()
    Http.postJsonAndParse[GameStatisticProfileRsp](url,json).map{rsp =>
      if(rsp.errCode == 0){
        println(s"------${rsp.gameBattleInfoLs}")
        (rsp.totalNum,rsp.gameBattleInfoLs)
      }else{
        Routes.handleAuthError(rsp.errCode, rsp.msg) { () =>
          println(s"error:${rsp.msg}")
        }
        (None,Nil)
      }
    }
  }

  private val gameBattleTable =
    <div>
      {
      statisticPageMod.indexData.map{
        case Nil =>
          <div style="width: 100%;display: table;">
            <div style="text-align: center;display: table-cell;vertical-align: middle;padding-top: 50px;padding-bottom: 100px;">
              <img style="width:100px" src="/evoke/static/img/userList/无数据.png"></img>
              <h3>无数据</h3>
            </div>
          </div>
        case ls =>
          <div id="table-div">
            <table id="bot_table" style="cellpadding:2px;margin-left:1.7%;margin-right:1.7%;border: 1px solid #D9DFEB;border-radius: 4px;">
              <tr style="background: #F5F7FA;border: 1px solid #D9DFEB;border-radius: 4px 4px 0 0;height:48px">
                <th style="padding-left:0.7%">记录ID</th>
                <th id="general-css-word" style="padding-left:3%">记录时间</th>
                <th id="general-css-word">玩家1账户</th>
                <th id="general-css-word" >玩家1分数</th>
                <th id="general-css-word">玩家2账户</th>
                <th id="general-css-word" >玩家2分数</th>
              </tr>
              <tbody id="gameBattle-table">
                {
                ls.map{info =>
                  <tr class="tr-line" style="width:100%">
                    <td class="td-line-1" style="padding-left:0.7%">
                      <div class="qrSceneStrDiv" name="qrSceneStrDiv">{info.recordId}</div>
                    </td>
                    <td class="td-line-2" style="width:200px;position:relative;padding-left:3%;">
                      <div id="img-line">{TimeTool.dateFormatDefault(info.timestamp,"yyyy-MM-dd")}</div>
                    </td>
                    <td class="td-line-3" style="width:240px;font-family: PingFangSC-Regular;font-size: 14px;color: #1D2341;text-align: left;padding-right:3%;word-wrap:break-word">
                      {info.result(0).n}
                    </td>
                    <td class="td-line-4-1" style="width:20%;font-family: PingFangSC-Regular;font-size: 14px;color: #1D2341;text-align: left;">
                      {info.result(0).score}
                    </td>
                    <td class="td-line-3" style="width:240px;font-family: PingFangSC-Regular;font-size: 14px;color: #1D2341;text-align: left;padding-right:3%;word-wrap:break-word">
                      {info.result(1).n}
                    </td>
                    <td class="td-line-4-1" style="width:20%;font-family: PingFangSC-Regular;font-size: 14px;color: #1D2341;text-align: left;">
                      {info.result(1).score}
                    </td>
                  </tr>
                }
                }
              </tbody>
            </table>
            <div style="display:inline-block;width: 936px;text-align:right;padding-right:1.7%">
              {
              statisticPageMod.pageDiv
              }
            </div>
          </div>
      }
      }
    </div>
  override def render: Elem = {
    {
      statisticPageMod.initPageInfo(10)
    }
    <div>
      <div style="background: #FFFFFF;box-shadow: 0 2px 8px 0 rgba(0,0,0,0.07);border-radius: 4px;">
        <button onclick={() => gotoPage("#/admin/login")}>返回首页</button>
        <button onclick={() => gotoPage("#/userManager")}>统计数据页</button>
        <div>{gameBattleTable}</div>
      </div>
    </div>
  }
}
