//package com.neo.sk.tank.test
//
//
//
//
//import javafx.event.EventHandler
//import javafx.scene.input
//import javafx.scene.shape.StrokeLineCap
//import scalafx.application.JFXApp
//import scalafx.application.JFXApp.PrimaryStage
//import scalafx.scene.{Scene, SnapshotParameters}
//import scalafx.scene.effect._
//import scalafx.scene.layout.HBox
//import scalafx.scene.paint.Color._
//import scalafx.scene.paint.{LinearGradient, Stops}
//import scalafx.scene.text.Text
//import scalafx.scene.canvas.Canvas
//import scalafx.scene.input.{KeyEvent, MouseEvent}
//import scalafx.scene.paint.Color
//import scalafx.scene.shape.ArcType
//
//
//object JavaFXCanvas extends JFXApp{
//  val helloStage = new PrimaryStage{
//    title = "test scalaFx"
//    width = 640
//    height = 600
//    scene = new Scene {
//      fill = White
//      content = new HBox {
//        children = Seq(
//          new Text {
//            text = "scala"
//            style = "-fx-font-size:100pt"
//            fill = new LinearGradient(
//              endX = 0,
//              stops = Stops(PaleGreen, SeaGreen)
//            )
//          }, new Text {
//            text = "FX"
//            style = "-fx-font-size: 100pt"
//            fill = new LinearGradient(
//              endX = 0,
//              stops = Stops(Cyan, DodgerBlue))
//            effect = new DropShadow {
//              color = DodgerBlue
//              radius = 25
//              spread = 0.25
//            }
//          })
//        effect = new Reflection {
//          fraction = 0.5
//          topOffset = -5.0
//          bottomOpacity = 0.75
//          input = new Lighting {
//            light = new Light.Distant {
//              elevation = 60
//            }
//          }
//        }
//      }
//    }
//  }
//
//
//  val canvas = new Canvas(1000, 900)
//  val gc = canvas.graphicsContext2D
//
//  gc.fill = Color.Green
//  gc.stroke = Color.Blue
//  gc.lineWidth = 5
//  gc.strokeLine(40, 10, 10, 40)
//
//  gc.fillOval(10, 60, 30, 30)
//  gc.strokeOval(60, 60, 30, 30)
//  gc.fillRoundRect(110, 60, 30, 30, 10, 10)
//  gc.strokeRoundRect(160, 60, 30, 30, 10, 10)
//  gc.fillArc(10, 110, 30, 30, 45, 240, ArcType.Open)
//  gc.fillArc(60, 110, 30, 30, 45, 240, ArcType.Chord)
//  gc.fillArc(110, 110, 30, 30, 45, 240, ArcType.Round)
//  gc.strokeArc(10, 160, 30, 30, 45, 240, ArcType.Open)
//  gc.strokeArc(60, 160, 30, 30, 45, 240, ArcType.Chord)
//  gc.strokeArc(110, 160, 30, 30, 45, 240, ArcType.Round)
//  gc.fillPolygon(Seq((10.0, 210), (40, 210), (10, 240), (40, 240)))
//  gc.strokePolygon(Seq((60.0, 210), (90, 210), (60, 240), (90, 240)))
//  gc.strokePolyline(Seq((110.0, 210), (140, 210), (110, 240), (140, 240)))
//  gc.setFill(Color.Red)
//  gc.setStroke(Color.Red)
//  gc.setLineWidth(2)
//  gc.beginPath()
//  gc.moveTo(100,10)
//  gc.lineTo(100,100)
//  gc.lineTo(700,100)
//  gc.lineTo(700,10)
//  gc.lineTo(100,10)
//  gc.closePath()
//  gc.stroke()
//  gc.fill()
//
//  gc.setLineCap(StrokeLineCap.SQUARE)
//  gc.setLineWidth(20)
//  gc.beginPath()
//  gc.moveTo(100,300)
//  gc.lineTo(700,600)
//  gc.closePath()
//  gc.stroke()
//
//  gc.setLineCap(StrokeLineCap.BUTT)
//  gc.setStroke(Color.Black)
//  gc.setLineWidth(20)
//  gc.beginPath()
//  gc.moveTo(100,300)
//  gc.lineTo(700,600)
//  gc.closePath()
//  gc.stroke()
//  //test cacheCanvas
//
//  val canvasCache = new Canvas(1000, 900)
//  val gcCache = canvasCache.graphicsContext2D
//  gcCache.fill = Color.Blue
//  gcCache.stroke = Color.Blue
//  gcCache.fillRect(0,0,1000,900)
//
//
//
//  gc.drawImage(canvasCache.snapshot(new SnapshotParameters(), null),0,0,1000,900)
//
//  val handler:MouseEvent => Unit = { e: MouseEvent =>
//    e.eventType match {
//      case MouseEvent.MouseClicked => println(s"mouse click=${(e.x,e.y)}")
//      case _ =>
//        if(e.eventType == MouseEvent.MouseMoved){
//          println(s"mouse move =${(e.x,e.y)}")
//        }
//        println(s"mouse unknow type=${e.eventType}")
//    }
//  }
//  val keyHandler : KeyEvent => Unit = { e:KeyEvent =>
//    e.eventType match {
//      case KeyEvent.KeyPressed => println(s"key code=${e.code} press")
//      case KeyEvent.KeyReleased => println(s"key code=${e.code} release")
//      case _ => println(s"key ssss")
//
//    }
//
//  }
//
//  import scalafx.Includes._
//  canvas.requestFocus()
//  canvas.handleEvent(MouseEvent.Any) (handler)
////  canvas.handleEvent(KeyEvent.Any) (keyHandler)
//
//
//
//
//
////  canvas.addEventHandler(MouseEvent.Any, {(e:MouseEvent) => println(s"Some mouse event handled,${}")})
////  canvas.handleEvent(MouseEvent.Any){ (me:MouseEvent) => {println("Some mouse event handled")}}
//
//
//  val img = canvas.snapshot(new SnapshotParameters(), null)
//  val myScene = new Scene {
//    content = canvas
//  }
//
//  myScene.onKeyPressed_=(new EventHandler[javafx.scene.input.KeyEvent]{
//    override def handle(event: input.KeyEvent): Unit = {
//      println(s"event type=${event.getEventType.getName}, code=${event.getCode}")
//    }
//  })
//
//  myScene.onKeyReleased_=(new EventHandler[javafx.scene.input.KeyEvent]{
//    override def handle(event: input.KeyEvent): Unit = {
//      println(s"event type=${event.getEventType.getName}, code=${event.getCode}")
//    }
//  })
//
//  println(img.getPixelReader.getPixelFormat().getType)
//  val canvasTestStage = new PrimaryStage {
//    title = "Drawing Operations Test"
//    width = canvas.width.toDouble
//    height = canvas.height.toDouble
//    scene = myScene
//  }
//
//  //不支持画线的帽
//
//  stage = canvasTestStage
//}
