package com.neo.sk.breakout.shared.game.view

import com.neo.sk.breakout.shared.game.GameContainerClientImpl
import com.neo.sk.breakout.shared.model.Point
import com.neo.sk.breakout.shared.util.canvas.MiddleContext

import scala.collection.mutable

/**
  * Created by benyafang on 2019/2/8 21:15
  * 绘制背景信息：分数
  * c
  */
trait BackgroundDrawUtil{ this:GameContainerClientImpl =>

  private val cacheCanvasMap = mutable.HashMap.empty[String, Any]
  private var canvasBoundary:Point=canvasSize

  private val rankWidth = 26
//  private val rankHeight = 50
//  private val currentRankNum = 10
//  private val currentRankCanvas=drawFrame.createCanvas(math.max(rankWidth * canvasUnit, 26 * 10),math.max(rankHeight * canvasUnit, 26 * 10))
//  private val historyRankCanvas=drawFrame.createCanvas(math.max(rankWidth * canvasUnit, 26 * 10),math.max(rankHeight * canvasUnit, 26 * 10))
//  var rankUpdated: Boolean = true
//  private val goldImg=drawFrame.createImage("/img/金牌.png")
//  private val silverImg=drawFrame.createImage("/img/银牌.png")
//  private val bronzeImg=drawFrame.createImage("/img/铜牌.png")
//
//  private val minimapCanvas=drawFrame.createCanvas(LittleMap.w * canvasUnit + 6,LittleMap.h * canvasUnit + 6)
//  private val minimapCanvasCtx=minimapCanvas.getCtx
//
//  var minimapRenderFrame = 0L
//

  def updateBackSize(canvasSize:Point)={
    cacheCanvasMap.clear()
    canvasBoundary=canvasSize
//
//    rankUpdated = true
//    minimapRenderFrame = systemFrame - 1
//    currentRankCanvas.setWidth(math.max(rankWidth * canvasUnit, 26 * 10))
//    currentRankCanvas.setHeight(math.max(rankHeight * canvasUnit, 24 * 10))
//    historyRankCanvas.setWidth(math.max(rankWidth * canvasUnit, 26 * 10))
//    historyRankCanvas.setHeight(math.max(rankHeight * canvasUnit, 24 * 10))
//    minimapCanvas.setWidth(LittleMap.w * canvasUnit + 6)
//    minimapCanvas.setHeight(LittleMap.h * canvasUnit + 6)
  }

//
//  private def generateBackgroundCanvas() = {
//    val cacheCanvas = drawFrame.createCanvas(((boundary.x + canvasBoundary.x) * canvasUnit).toInt,((boundary.y + canvasBoundary.y) * canvasUnit).toInt)
//    val cacheCanvasCtx=cacheCanvas.getCtx
//    clearScreen("#BEBEBE", 1, boundary.x + canvasBoundary.x, boundary.y + canvasBoundary.y, cacheCanvasCtx)
//    clearScreen("#E8E8E8",1, boundary.x, boundary.y, cacheCanvas.getCtx, canvasBoundary / 2)
//
//    cacheCanvasCtx.setLineWidth(1)
//    cacheCanvasCtx.setStrokeStyle("rgba(0,0,0,0.5)")
//    for(i <- 0  to((boundary.x + canvasBoundary.x).toInt,2)){
//      drawLine(Point(i,0), Point(i, boundary.y + canvasBoundary.y), cacheCanvasCtx)
//    }
//
//    for(i <- 0  to((boundary.y + canvasBoundary.y).toInt,2)){
//      drawLine(Point(0 ,i), Point(boundary.x + canvasBoundary.x, i), cacheCanvasCtx)
//    }
//    cacheCanvas.change2Image()
//  }
//
//
  private def clearScreen(color:String, alpha:Double, width:Float = canvasBoundary.x, height:Float = canvasBoundary.y, middleCanvas:MiddleContext , start:Point = Point(0,0)):Unit = {
    middleCanvas.setFill(color)
    middleCanvas.setGlobalAlpha(alpha)
    middleCanvas.fillRec(start.x * canvasUnit, start.y * canvasUnit,  width * this.canvasUnit, height * this.canvasUnit)
    middleCanvas.setGlobalAlpha(1)
  }

  protected def drawLine(start:Point,end:Point, middleCanvas:MiddleContext):Unit = {
    middleCanvas.beginPath
    middleCanvas.moveTo(start.x * canvasUnit, start.y * canvasUnit)
    middleCanvas.lineTo(end.x * canvasUnit, end.y * canvasUnit)
    middleCanvas.stroke()
    middleCanvas.closePath()
  }
//
//
//  /*protected def drawBackground(offset: Point) = {
//    clearScreen("#FCFCFC", 1, canvasBoundary.x, canvasBoundary.y, ctx)
//    val cacheCanvas = cacheCanvasMap.getOrElseUpdate("background", generateBackgroundCanvas())
//    ctx.drawImage(cacheCanvas, (-offset.x + canvasBoundary.x / 2) * canvasUnit, (-offset.y + canvasBoundary.y / 2) * canvasUnit,
//      Some(canvasBoundary.x * canvasUnit, canvasBoundary.y * canvasUnit))
//  }*/
//
  protected def drawBackground(offset:Point) = {
    clearScreen("#BEBEBE",1, canvasBoundary.x, canvasBoundary.y, ctx)
//    val boundStart = Point(canvasBoundary.x/2, canvasBoundary.y/2)
//    val boundEnd = Point(canvasBoundary.x/2 + boundary.x, canvasBoundary.y/2 + boundary.y)
//    val canvasStart = Point(-offset.x + canvasBoundary.x/2, -offset.y + canvasBoundary.y/2)
//    val canvasEnd = Point(-offset.x + canvasBoundary.x/2 * 3, -offset.y + canvasBoundary.y/2 * 3)
//    val start = Point(math.max(boundStart.x, canvasStart.x), math.max(boundStart.y, canvasStart.y))
//    val end = Point(math.min(boundEnd.x, canvasEnd.x), math.min(boundEnd.y, canvasEnd.y))
//    val width = end.x - start.x
//    val height = end.y - start.y
//    if(canvasStart.x < boundStart.x && canvasStart.y > boundStart.y){
//      clearScreen("#E8E8E8", 1, width, height, ctx, Point(canvasBoundary.x - width, 0))
//    }
//    else if(canvasStart.x > boundStart.x && canvasStart.y < boundStart.y){
//      clearScreen("#E8E8E8", 1, width, height, ctx, Point(0, canvasBoundary.y - height))
//    }
//    else if(canvasStart.x < boundStart.x && canvasStart.y < boundStart.y){
//      clearScreen("#E8E8E8", 1, width, height, ctx, Point(canvasBoundary.x - width, canvasBoundary.y - height))
//    }
//    else{
//      clearScreen("#E8E8E8", 1, width, height, ctx)
//    }
//    ctx.setLineWidth(3)
//    ctx.setStrokeStyle("rgba(0,0,0,0.05)")
    drawLine(Point(0,canvasBoundary.y / 2),Point(canvasBoundary.x, canvasBoundary.y / 2),ctx)


//    for(i <- (64 - canvasStart.x % 64) to canvasBoundary.x by 64f){
//      drawLine(Point(i,0), Point(i, canvasBoundary.y), ctx)
//    }
//    for(i <- (64 - canvasStart.y % 64) to canvasBoundary.y by 64f){
//      drawLine(Point(0, i), Point(canvasBoundary.x, i), ctx)
//    }

  }

//  protected def drawRank(supportLiveLimit:Boolean,curTankId:Int,curName:String):Unit = {
//    def drawTextLine(str: String, x: Double, y: Double, context:MiddleContext) = {
//      context.fillText(str, x, y)
//    }
//
//    def drawUnitRank(context:MiddleContext,unit:Double,index:Int,y:Double,score:Score,historyRank:Boolean,leftBegin:Double) = {
//      context.beginPath()
//      context.moveTo(leftBegin,y)
//      context.lineTo((rankWidth - 2) * unit,y)
//      context.stroke()
//      context.closePath()
//      context.setTextAlign("left")
//      if(historyRank) drawTextLine(s"[$index]: ${score.n.+("   ").take(3)} kill=${score.k} damage=${score.d}", leftBegin, y, context)
//      else {
//        val scoreText=if(score.d>=1000){
//          val a=score.d.toFloat/1000
//          a.formatted("%.1f")+"k"
//        }else{
//          score.d.toString
//        }
//        val liveInfo = if(supportLiveLimit) s"lives=${score.l}" else ""
//        drawTextLine(s"[$index]: ${score.n.+("   ").take(3)} kill=${score.k} damage=$scoreText ${liveInfo}", leftBegin, y, context)
//      }
//    }
//
//    def refreshCacheCanvas(context:MiddleContext, header: String, rank: List[Score], historyRank:Boolean): Unit ={
//      //绘制当前排行榜
//      val unit = currentRankCanvas.getWidth() / rankWidth
//
////      println(s"rank =${historyRankCanvas.getWidth()}, canvasUnit=${canvasUnit}, unit=${unit}")
//
//      val leftBegin = 5 * unit
//      context.setFont("Arial","bold",12)
//      context.clearRect(0,0,currentRankCanvas.getWidth(), currentRankCanvas.getHeight())
//
//      var index = 0
//      context.setFill("black")
//      context.setTextAlign("center")
//      context.setTextBaseline("middle")
//      context.setLineCap("round")
//      drawTextLine(header, currentRankCanvas.getWidth() / 2 , 1 * unit, context)
//      rank.take(currentRankNum).foreach{ score =>
//        index += 1
//        val drawColor = index match {
//          case 1 => "rgb(255,215,0)"
//          case 2 => "rgb(209,209,209)"
//          case 3 => "rgb(139,90,0)"
//          case _ => "rgb(202,225,255)"
//        }
//        val imgOpt = index match {
//          case 1 => Some(goldImg)
//          case 2 => Some(silverImg)
//          case 3 => Some(bronzeImg)
//          case _ => None
//        }
//        imgOpt.foreach{ img =>
//          context.drawImage(img, leftBegin - 5 * unit, (2 * index) * unit, Some(2 * unit,2 * unit))
//        }
//        context.setStrokeStyle(drawColor)
//        context.setLineWidth(1.8 * unit)
//        drawUnitRank(context,unit,index,(2 * index + 1) * unit,score,historyRank,leftBegin)
//      }
//      rank.zipWithIndex.find(_._1.id == curTankId) match{
//        case Some((score,indices)) =>
//          if(indices + 1 > 10){
//            context.setStrokeStyle("RGB(137,188,255)")
//            context.setLineWidth(1.8 * unit)
//            drawUnitRank(context,unit,indices + 1,(2 * index + 4) * unit,score,historyRank,leftBegin)
//          }
//        case None =>
//      }
////      drawTextLine(s"当前房间人数 ${index}", 28*canvasUnit, (2 * index + 1) * canvasUnit, context)
//
//    }
//
//
//
//    def refresh():Unit = {
//      refreshCacheCanvas(currentRankCanvas.getCtx, " --- Current Rank --- ", currentRank,false)
//      if(Constants.drawHistory){
//        refreshCacheCanvas(historyRankCanvas.getCtx, " --- History Rank --- ", historyRank,true)
//      }
//    }
//
//
//    if(rankUpdated){
//      refresh()
//      rankUpdated = false
//    }
//    ctx.setGlobalAlpha(0.8)
//    ctx.drawImage(currentRankCanvas.change2Image(),canvasBoundary.x * canvasUnit - rankWidth*10,0)
//    if(Constants.drawHistory){
//      ctx.drawImage(historyRankCanvas.change2Image(), canvasBoundary.x * canvasUnit - rankWidth*10,canvasBoundary.y * canvasUnit - rankHeight * 10)
//    }
//    ctx.setGlobalAlpha(1)
//  }
//
//
//  protected def drawMinimap(tank:Tank) = {
//    def drawTankMinimap(position:Point,color:String, context:MiddleContext) = {
//      val offset = Point(position.x / boundary.x * LittleMap.w, position.y / boundary.y * LittleMap.h)
//      context.beginPath()
//      context.setFill(color)
//      context.arc(offset.x * canvasUnit + 3, offset.y * canvasUnit + 3, 0.5 * canvasUnit,0,360)
//      context.fill()
//      context.closePath()
//    }
//
//
//    def refreshMinimap():Unit = {
//      val mapColor = "rgba(255,245,238,0.5)"
//      val myself = "#000080"
//      val otherTankColor = "#CD5C5C"
//
//      minimapCanvasCtx.clearRect(0, 0, minimapCanvas.getWidth(), minimapCanvas.getHeight())
//      minimapCanvasCtx.setFill(mapColor)
//      minimapCanvasCtx.fillRec(3, 3, LittleMap.w * canvasUnit ,LittleMap.h * canvasUnit)
//      minimapCanvasCtx.setStrokeStyle("rgb(143,143,143)")
//      minimapCanvasCtx.setLineWidth(6)
//      minimapCanvasCtx.beginPath()
//      minimapCanvasCtx.setFill(mapColor)
//      minimapCanvasCtx.rect(3, 3 ,LittleMap.w * canvasUnit ,LittleMap.h * canvasUnit)
//      minimapCanvasCtx.fill()
//      minimapCanvasCtx.stroke()
//      minimapCanvasCtx.closePath()
//
//      drawTankMinimap(tank.getPosition,myself, minimapCanvasCtx)
//      tankMap.filterNot(_._1 == tank.tankId).values.toList.foreach{ t =>
//        drawTankMinimap(t.getPosition,otherTankColor, minimapCanvasCtx)
//      }
//    }
//
//    if(minimapRenderFrame != systemFrame){
//      refreshMinimap()
//      minimapRenderFrame = systemFrame
//    }
//
//    ctx.drawImage(minimapCanvas.change2Image(), 0, (canvasBoundary.y - LittleMap.h) * canvasUnit - 6)
//
//  }
//
//
//  protected def drawKillInformation():Unit = {
//    val killInfoList = getDisplayKillInfo()
//    if(killInfoList.nonEmpty){
//      var offsetY = canvasBoundary.y - 30
//      ctx.beginPath()
//      ctx.setStrokeStyle("rgb(0,0,0)")
//      ctx.setTextAlign("left")
//      ctx.setFont("微软雅黑","bold",2.5*canvasUnit)
//      ctx.setLineWidth(1)
//
//      killInfoList.foreach{
//        case (killerName,killedName,_) =>
//          ctx.strokeText(s"$killerName 击杀了 $killedName",3 * canvasUnit, offsetY * canvasUnit, 40 * canvasUnit)
//          offsetY -= 3
//      }
//      ctx.closePath()
//    }
//
//  }
//
//  protected def drawRoomNumber():Unit = {
//
//    ctx.beginPath()
//    ctx.setStrokeStyle("rgb(0,0,0)")
//    ctx.setTextAlign("left")
//    ctx.setFont("Arial","normal",3*canvasUnit)
//    ctx.setLineWidth(1)
//    val offsetX = canvasBoundary.x - 20
//    ctx.strokeText(s"当前在线人数： ${tankMap.size}", 0,(canvasBoundary.y - LittleMap.h -6) * canvasUnit , 20 * canvasUnit)
//
//    ctx.beginPath()
//    ctx.setFont("Helvetica", "normal",2 * canvasUnit)
//    //      ctx.setTextAlign(TextAlignment.JUSTIFY)
//    ctx.setFill("rgb(0,0,0)")
//    versionInfo.foreach(r=>ctx.strokeText(s"Version： $r", offsetX*canvasUnit,(canvasBoundary.y -16) * canvasUnit , 20 * canvasUnit))
//
//
//  }
//

}
