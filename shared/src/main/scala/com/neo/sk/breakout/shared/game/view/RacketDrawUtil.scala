package com.neo.sk.breakout.shared.game.view

import com.neo.sk.breakout.shared.game.GameContainerClientImpl
import com.neo.sk.breakout.shared.model.Point
import com.neo.sk.breakout.shared.util.canvas.MiddleContext

import scala.collection.mutable

/**
  * created by benyafang on 2019/2/8
  * 绘制拍子
  * c
  * */
trait RacketDrawUtil {this:GameContainerClientImpl =>

  private val racketCanvasCacheMap = mutable.HashMap[Byte, Any]()

  private def generateRacketCacheCanvas(width: Float, height: Float, color: String): Any = {
    val cacheCanvas = drawFrame.createCanvas((width * canvasUnit).toInt, (height * canvasUnit).toInt)
    val ctxCache = cacheCanvas.getCtx
    drawRacket(Point(width / 2, height / 2), width, height, 1, color, ctxCache)
    cacheCanvas.change2Image()
  }


  private def drawRacket(centerPosition:Point, width:Float, height:Float, bloodPercent:Float, color:String, context:MiddleContext = ctx):Unit = {
    context.setFill(color)
    context.setStrokeStyle("#43CD80")
    context.setLineWidth(2)
    context.beginPath()
    context.fillRec((centerPosition.x - width / 2) * canvasUnit, (centerPosition.y + height / 2 - bloodPercent * height) * canvasUnit,
      width * canvasUnit, bloodPercent * height * canvasUnit)
    context.closePath()
    context.beginPath()
    context.rect((centerPosition.x - width / 2) * canvasUnit, (centerPosition.y - height / 2) * canvasUnit,
      width * canvasUnit, height * canvasUnit
    )
    context.stroke()
    context.closePath()
    context.setLineWidth(1)
  }

  protected def drawRackets(offset:Point,offsetTime:Long,view:Point) = {
    racketMap.values.foreach{r =>
//      val p = r.getPosition4Animation(boundary,quadTree,offsetTime) + offset
      if (r.getPosition.in(view, Point(r.getWidth * 2, r.getHeight * 2))){
        val position = r.getPosition + offset - Point(r.getWidth / 2, r.getHeight / 2)
        val cache = racketCanvasCacheMap.getOrElseUpdate(r.racketId.toByte, generateRacketCacheCanvas(r.getWidth, r.getHeight, "rgba(0, 255, 255, 1)"))
        ctx.drawImage(cache, position.x * canvasUnit, position.y * canvasUnit)
      }
    }
  }

}
