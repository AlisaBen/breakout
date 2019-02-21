package com.neo.sk.breakout.front.pages

import com.neo.sk.breakout.front.common.{Page, PageSwitcher}
import mhtml.{Cancelable, Rx, Var, mount}
import org.scalajs.dom
import com.neo.sk.breakout.front.model.ReplayInfo
import scala.xml.Elem

/**
  * Created by hongruying on 2018/4/8
  */
object MainPage extends PageSwitcher {




  override def switchPageByHash(): Unit = {
    val tokens = {
      val t = getCurrentHash.split("/").toList
      if (t.nonEmpty) {
        t.tail
      } else Nil
    }

    println(tokens)
    switchToPage(tokens)
  }


  private val currentPage: Rx[Elem] = currentPageHash.map {
    case "admin" :: "login" ::  Nil => AdminLoginPage.render
    case "login" :: Nil => LoginPage.render
    case "register" :: Nil => RegisterPage.render
//    case "visitor" ::Nil =>VisitorHall.render
    case _ => <div>Error Page</div>
  }

  def gotoPage(i: String) = {
    dom.document.location.hash = "" + i
  }


  def show(): Cancelable = {
    switchPageByHash()
    val page =
      <div>
        {currentPage}
      </div>
    mount(dom.document.body, page)
  }

}
