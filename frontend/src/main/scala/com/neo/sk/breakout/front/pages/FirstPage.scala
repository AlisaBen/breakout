package com.neo.sk.breakout.front.pages

import com.neo.sk.breakout.front.common.{Component, Page, Routes}
import com.neo.sk.breakout.front.components.NavigationBar
import scala.xml.Elem

/**
  * User: Taoz
  * Date: 6/3/2017
  * Time: 1:33 PM
  */
object FirstPage extends Component {

  val header = NavigationBar("/")

  override def render: Elem =
    <div>
      <p>skkkkkkkkkkkkkkk</p>
      {header.render}
    </div>


}
