package com.neo.sk.breakout.shared.`object`

import com.neo.sk.breakout.shared.model.{Point, Rectangle, Shape}

/**
  * Created by hongruying on 2018/8/22
  * 游戏中的所有物体元素基类
  */
trait ObjectOfGame {

  protected var position:Point

  /**
   * 获取当前元素的位置
   * @return  point(x,y)
   */
  final def getPosition:Point = position

  /**
   * 获取当前元素的包围盒
   * @return  rectangle
   */
  def getObjectRect():Rectangle

  /**
   * 获取当前元素的外形
   * @return  shape
   */
  def getObjectShape():Shape

  /**
   * 判断元素是否和其他元素有碰撞
   * @param o 其他物体
   * @return  如果碰撞，返回true；否则返回false
   */
  def isIntersects(o: ObjectOfGame): Boolean


}
