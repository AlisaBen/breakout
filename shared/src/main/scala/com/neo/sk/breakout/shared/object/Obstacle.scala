package com.neo.sk.breakout.shared.`object`

import com.neo.sk.breakout.shared.config.{GameConfig}
import com.neo.sk.breakout.shared.model.Constants.ObstacleType
import com.neo.sk.breakout.shared.model.Point

/**
  * Created by hongruying on 2018/7/9
  * Edit by benyafang on 2019/2/1
  * 游戏中的阻碍物
  * 砖头（可被球打碎）
  * 道具（暂时不加，快消、炸弹、加速、分裂）
  */
case class ObstacleState(oId:Int,t:Byte,p:Point,value:Int)//id,类型（racket,brick），位置

trait ObstacleBall

trait Obstacle extends RectangleObjectOfGame{

  val oId:Int

  val obstacleType:Byte


  def getObstacleState():ObstacleState

//  def attackDamage(d:Int):Unit

  final def isIntersectsObject(o:Seq[ObjectOfGame]):Boolean = {
    o.exists(t => t.isIntersects(this))
  }

}

object Obstacle{
  def apply(config: GameConfig, obstacleState: ObstacleState): Obstacle = obstacleState.t match {
//    case ObstacleType.airDropBox => new AirDropBox(config,obstacleState)
    case ObstacleType.brick => new Brick(config,obstacleState)
//    case ObstacleType.racket => new Racket(config,obstacleState)

  }
}
