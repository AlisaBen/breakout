package com.neo.sk.breakout.front.components

import com.neo.sk.breakout.front.utils.JsFunc
import mhtml.{Var, emptyHTML}
import org.scalajs.dom
import org.scalajs.dom.html.Input

import scala.collection.mutable.HashMap
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * @author sky 缓存数据页码切换
  */

class PageMod[T](f: (Int, Int) => Future[(Option[Int], List[T])], cache:Boolean=false, confirmFunc:Int => Unit = (x:Int) => ()) {

  private final case class PageInfo(
                                     var page: Int,//当前显示页码
                                     var count: Int,//每页显示的数量
                                     var totalNum: Int,//总数据量
                                     var totalPage: Int,//总页数
                                     var bigPage: Int,//最大页数
                                     var data: HashMap[Int, List[T]]
                                   )

  //本页面page信息

  private val pageData = PageInfo(1, 10, 0, 0, 0, HashMap[Int, List[T]]()) //本页面翻页数据

  private val IndexData = Var(List.empty[T])

  private val bigPageVar = Var(5)
  private val totalPageVar = Var(1)

  def getCurrentPage = pageData.page

  def insertPageInfo(value:T) = {
      if(pageData.data.getOrElse(pageData.page,List.empty[T]).length < pageData.count){
        pageData.data.update(pageData.page,value::pageData.data.getOrElse(pageData.page,List.empty[T]))
        IndexData.update(r =>  r:::List(value))
        pageData.totalNum += 1
      }else{//当前页已经满了,不更新本页信息，刷新最后一页的数据
        f(pageData.totalPage,pageData.count).map{
          case (None,Nil) =>
            pageData.bigPage = Math.max(pageData.totalPage,Math.min(5,pageData.totalPage))
            bigPageVar := pageData.bigPage
          case (Some(dataNum),ls) =>
            pageData.totalNum = dataNum
            pageData.totalPage = if (pageData.totalNum % pageData.count == 0) pageData.totalNum / pageData.count else (pageData.totalNum / pageData.count) + 1
            totalPageVar:=pageData.totalPage
            pageData.bigPage = Math.max(Math.min(pageData.page + 2, pageData.totalPage),Math.min(5,pageData.totalPage))
            bigPageVar := pageData.bigPage
        }
      }
  }

  def deletePageInfo() = {
    initPageInfo(pageData.count)
  }

  private def changePageInfo(
                              page: Int
                            ): Unit = {
    if (page < 1 || page > pageData.totalPage) JsFunc.alert("请在页数范围内选择")
    else if (pageData.data.get(page).isDefined) {
      pageData.page = page
      IndexData := pageData.data(page)
      confirmFunc(page)
      println(s"-------ddd------${IndexData}")
      //下一页更新5页
      /*if (page > pageData.bigPage) {
        pageData.bigPage = page/5*5 + 5
        bigPageVar := pageData.bigPage
      } else if (page <= pageData.bigPage - 5) {
        pageData.bigPage = Math.max((page-1)/5*5+5, 1)
        bigPageVar := pageData.bigPage
      }*/
      pageData.bigPage = Math.max(Math.min(page + 2, pageData.totalPage),Math.min(5,pageData.totalPage))
      bigPageVar := pageData.bigPage
    } else {
      confirmFunc(page)
      f(page, pageData.count).map {
        case (None, Nil) => ()
          pageData.bigPage = Math.max(Math.min(page + 2, pageData.totalPage),Math.min(5,pageData.totalPage))
          bigPageVar := pageData.bigPage
        case data =>
//          println(s"-------fd------${IndexData}")
          pageData.page = page
          if(cache){
            pageData.data.put(page, data._2)
          }
          IndexData := data._2
          pageData.bigPage = Math.max(Math.min(page + 2, pageData.totalPage),Math.min(5,pageData.totalPage))
          bigPageVar := pageData.bigPage
      }
      /*if (page > pageData.bigPage) {
        pageData.bigPage = page/5*5 + 5
        bigPageVar := pageData.bigPage
      } else if (page <= pageData.bigPage - 5) {
        pageData.bigPage = Math.max((page-1)/5*5+5, 1)
        bigPageVar := pageData.bigPage
      }*/
    }
    println(s"send page:$page , bigPage:${pageData.bigPage} totalPage:${pageData.totalPage} totalNum:${pageData.totalNum}")
  }

  /**
    * @param state 是否停留原页码*/
  def initPageInfo(pageNum: Int, state:Boolean=false): Unit = {
    IndexData := Nil
    val page=if(state) pageData.page else 1
    f(page, pageNum).map {
      data =>
        pageData.count = pageNum
        if(!state) {
          pageData.page = page
          pageData.totalNum = data._1.getOrElse(0)
          pageData.totalPage = if (pageData.totalNum % pageData.count == 0) pageData.totalNum / pageData.count else (pageData.totalNum / pageData.count) + 1
          totalPageVar:= pageData.totalPage
          pageData.bigPage = math.min(5, pageData.totalPage)
          bigPageVar := pageData.bigPage
        }
        if(cache){
          pageData.data.clear()
          pageData.data.put(1, data._2)
        }
        IndexData := data._2
    }
  }

  def getNum = pageData.totalNum

  def indexData: Var[List[T]] = IndexData

  def pageDiv =
    totalPageVar.map{b=>
      if(b==1){
        emptyHTML
      }else{
        <div id="page" style="margin-bottom: 20px;margin-top:3%">
          <div style="display:inline-block;font-family: Helvetica;font-size: 14px;color: #42455F;letter-spacing: -0.15px;text-align: left;">共
            {b}
            页记录</div>{bigPageVar.map { r =>
          <div style="display:inline-block">
            <span class={if (pageData.page == 1) "pc-none" else "pc-edge"} onclick={() => if (pageData.page > 1) changePageInfo(page = pageData.page - 1)}>
              {"<"}
            </span>
            {for (a <- math.max(1, pageData.bigPage - 4) to math.min(pageData.bigPage, pageData.totalPage)) yield {
            <span class={if (pageData.page == a) "pc-checked" else "pc"} onclick={() => changePageInfo(a)}>
              {a}
            </span>
          }}
            <span class={if (pageData.page == pageData.totalPage) "pc-none" else "pc-edge"} onclick={() => if (pageData.page < pageData.totalPage) changePageInfo(page = pageData.page + 1)}>
              {">"}
            </span>
            <span style="font-family: PingFangSC-Regular;font-size: 14px;color: #8E98B4;text-align: left;line-height: 22px;margin-left: 8px;">跳至</span>
            <input id="pageNum" style="border: 1px solid rgba(142,152,180,0.50);border-radius: 4px;height:32px;width:48px;margin-left: 8px;" onchange={() =>
              val pageNum = dom.document.getElementById("pageNum").asInstanceOf[Input].value
              if (pageNum != "") {
                changePageInfo(page = pageNum.toInt)
              }}></input>
            <span style="font-family: PingFangSC-Regular;font-size: 14px;color: #8E98B4;text-align: left;line-height: 22px;margin-left: 8px;">页</span>
          </div>
        }}
        </div>
      }
    }
}
