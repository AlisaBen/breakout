package com.neo.sk.breakout.front.components

import com.neo.sk.breakout.front.common.Component
import com.neo.sk.breakout.front.pages.MainPage
import mhtml._

import scala.xml.Node
/**
  * Created by hongruying on 2018/4/8
  */
class NavigationBar(showPage:String) {

  private val navigationList=
    List(NavigationItem(0, "首页","/"), NavigationItem(1, "版本管理","/versionManager"),
      NavigationItem(2, "帖子管理","/postManager"),NavigationItem(3,"置顶管理","/sticky"),
      NavigationItem(4,"推荐版面管理","/recommendBoardManager"))

  private val internalItems = navigationList.map(item => structureItem(item))



  private case class structureItem(body: NavigationItem) extends Component {

    val visibilityStyle = (if (body.page==showPage) Var(true) else Var(false)).map{
      case true => "visibility:visible;"
      case _ => "visibility:hidden;"
    }

    val baseline =
      <div style ={visibilityStyle.map(" background: #9d9d9d;width:80%;height:5px;margin:0 auto;"+ _)}></div>


    override def render =
    <li class ="hidden-sm hidden-md">
      <div display ="inline-block" style ="margin: 13px 20px;" onclick ={() => MainPage.gotoPage(body.page)}>
        <span style="color: #9d9d9d;">{body.name}</span>
        {baseline}
      </div>
    </li>

  }
  //
  private val list =
    <ul class ="nav navbar-nav">
      {internalItems.map(t => t.render)}
    </ul>

  //
  //title left
  private val leftNav =
  <div class ="navbar-header">
    <span class ="navbar-brand hidden-sm">
      galaxy管理系统
    </span>
  </div>







  private val rightNav =
    <div class ="navbar-collapse collapse" role ="navigation">
      {list}
    </div>


  def render =
    <div class ="navbar navbar-inverse navbar-fixed-top">
      <div class ="container">
        {leftNav}{rightNav}
      </div>
    </div>


}

case class NavigationItem(id: Int, name: String,page:String)

object NavigationBar{
  def apply(showPage:String): NavigationBar = new NavigationBar(showPage)
}
