package com.neo.sk.breakout.shared.`object`

import com.neo.sk.breakout.shared.config.GameConfig
import com.neo.sk.breakout.shared.model
import com.neo.sk.breakout.shared.model.Constants.{DirectionType, ObstacleType, RacketParameter}
import com.neo.sk.breakout.shared.model.{Point, Rectangle}
import com.neo.sk.breakout.shared.util.QuadTree

/**
  * created by benyafang on 2019/2/1 pm 23:20
  *
  * */

case class RacketState(racketId:Int,name:String,position:Point,direction:Float,isMove:Boolean,damageStatistic:Int)
case class Racket(
                   racketId:Int,
                   var direction:Float,
                   var isMove:Boolean,
                   config: GameConfig,
                   var position:Point,
                   var damageStatistics:Int,
                   name:String
                 ) extends RectangleObjectOfGame with ObstacleBall {
  def this(config:GameConfig,racketState:RacketState) = {
    this(racketState.racketId,racketState.direction,racketState.isMove,config,racketState.position,racketState.damageStatistic,racketState.name)
  }
//  var position:Point = Point(config.boundary.x / 2)
  //todo racket height width
  override protected val height: Float = config.getRacketHeight
  override protected val width: Float = config.getRacketWidth
  override protected val collisionOffset: Float = config.obstacleWO
  var canvasFrame = 0

  def getRacketState():RacketState = RacketState(racketId,name,position,direction,isMove,damageStatistics)

  //todo 拍子的移动速度，可考虑移动到config中
  val moveSpeed: model.Point = RacketParameter.speed

  def isIntersectsObject(o:Seq[ObjectOfGame]):Boolean = {
    o.exists(t => t.isIntersects(this))
  }

  def move(quadTree:QuadTree,boundary:Point)(implicit breakoutGameConfig:GameConfig) = {
    if(isMove){
      val moveDistance = breakoutGameConfig.getMoveDistanceByFrame.rotate(direction)
      if(moveDistance.x != 0){
        val originPosition = this.position
        this.position = this.position + moveDistance
        val movedRec = Rectangle(this.position - Point(width / 2, height / 2), this.position + Point(width / 2, height / 2))
        val otherObjects = quadTree.retrieveFilter(this).filter(_.isInstanceOf[ObstacleBall])
        if (!otherObjects.exists(t => t.isIntersects(this)) && movedRec.topLeft > model.Point(0, 0) && movedRec.downRight < boundary) {
          quadTree.updateObject(this)
        } else {
          this.position = originPosition
        }
      }
    }

  }

  def canMove(boundary:Point, quadTree:QuadTree, canvasFrameLeft:Int)(implicit gameConfig: GameConfig):Option[Point] = {
      if(isMove){
        var moveDistance = gameConfig.getMoveDistanceByFrame.rotate(direction)
        val horizontalDistance = moveDistance.copy(y = 0)
        val verticalDistance = moveDistance.copy(x = 0)

        val originPosition = this.position

        List(horizontalDistance,verticalDistance).foreach{ d =>
          if(d.x != 0 || d.y != 0){
            val pos = this.position
            this.position = this.position + d
            val movedRec = Rectangle(this.position-Point(config.getRacketWidth.toFloat / 2,config.getRacketHeight.toFloat / 2),
              this.position+Point(config.getRacketWidth.toFloat / 2,config.getRacketHeight.toFloat / 2))
            val otherObjects = quadTree.retrieveFilter(this).filter(_.isInstanceOf[ObstacleBall])
            if(!otherObjects.exists(t => t.isIntersects(this)) && movedRec.topLeft > model.Point(0,0) && movedRec.downRight < boundary){
              quadTree.updateObject(this)
            }else{
              this.position = pos
              moveDistance -= d
            }
          }
        }
        this.position = originPosition
        Some(moveDistance)
      }else{
        None
      }
  }

  def getPosition4Animation(boundary: Point, quadTree: QuadTree, offSetTime: Long): Point = {
    val logicMoveDistanceOpt = this.canMove(boundary, quadTree, 4)(config)
    if (logicMoveDistanceOpt.nonEmpty) {
      if (canvasFrame <= 0 || canvasFrame >= 4) {
        this.position + logicMoveDistanceOpt.get / config.frameDuration * offSetTime
      } else position
    } else position
  }

  def setRacketDirection(action:Option[Byte]) = {
    val targetDirectionOpt = action match{
      case Some(direction) =>
        if(direction == 1)Some(DirectionType.left)
        else if(direction == 2) Some(DirectionType.right)
        else None
      case None =>None

    }
    if(targetDirectionOpt.nonEmpty){
      isMove = true
      this.direction = targetDirectionOpt.get
    }
    else isMove = false
  }





}
