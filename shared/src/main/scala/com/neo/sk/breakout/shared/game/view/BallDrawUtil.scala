package com.neo.sk.breakout.shared.game.view

import com.neo.sk.breakout.shared.`object`.Ball
import com.neo.sk.breakout.shared.game.GameContainerClientImpl
import com.neo.sk.breakout.shared.model.Point

import scala.collection.mutable

/**
  * created by benyafang on 2019/2/8
  * 绘制球
  * C
  * */
trait BallDrawUtil {this:GameContainerClientImpl =>

  private def generateCanvas(bullet:Ball) = {
    val radius = bullet.getRadius
    val canvasCache = drawFrame.createCanvas(math.ceil(radius * canvasUnit * 2 + radius * canvasUnit / 5).toInt,math.ceil(radius * canvasUnit * 2 + radius * canvasUnit / 5).toInt)
    val ctxCache = canvasCache.getCtx

    val color = "#8B2323"
    ctxCache.setFill(color)
    ctxCache.beginPath()
    ctxCache.arc(radius * canvasUnit + radius * canvasUnit / 10,radius * canvasUnit + radius * canvasUnit / 10, radius * canvasUnit,0, 360)
    ctxCache.fill()
    ctxCache.setStrokeStyle("#474747")
    ctxCache.setLineWidth(radius * canvasUnit / 5)
    ctxCache.stroke()
    ctx.closePath()
    canvasCache.change2Image()
  }

  private val canvasCacheMap = mutable.HashMap[Byte,Any]()

  def updateBulletSize(canvasSize:Point)={
    canvasCacheMap.clear()
  }

  protected def drawBall(offset:Point, offsetTime:Long, view:Point) = {
    println(s"------------${ballMap.values}")
    ballMap.values.foreach{ ball =>
      val p = ball.getPosition4Animation(offsetTime) + offset
      if(p.in(view,Point(ball.getRadius * 4 ,ball.getRadius *4))) {
        val cacheCanvas = canvasCacheMap.getOrElseUpdate(1, generateCanvas(ball))
        val radius = ball.getRadius
        if(ball.racketId == this.racketId){
          ctx.drawImage(cacheCanvas, (p.x - ball.getRadius) * canvasUnit - radius * canvasUnit / 2.5, (p.y - ball.getRadius) * canvasUnit - radius * canvasUnit / 2.5)
        }else{
          ctx.drawImage(cacheCanvas, (p.x - ball.getRadius) * canvasUnit - radius * canvasUnit / 2.5, (view.y - p.y - ball.getRadius) * canvasUnit - radius * canvasUnit / 2.5)
        }

      }
    }
  }

}
