package com.neo.sk.breakout.front.breakoutClient

import com.neo.sk.breakout.front.common.Routes
import com.neo.sk.breakout.shared.protocol.BreakoutGameEvent
import org.scalajs.dom
import org.scalajs.dom.Blob
import org.scalajs.dom.raw._
import org.seekloud.byteobject.ByteObject.bytesDecode
import org.seekloud.byteobject.MiddleBufferInJs

import scala.scalajs.js.typedarray.ArrayBuffer


/**
  * Created by hongruying on 2018/7/9
  */
case class WebSocketClient(
                            connectSuccessCallback: Event => Unit,
                            connectErrorCallback:Event => Unit,
                            messageHandler:BreakoutGameEvent.WsMsgServer => Unit,
                            closeCallback:Event => Unit,
                            setDateSize: (String,Double) => Unit
                          ) {



  private var wsSetup = false

  private var webSocketStreamOpt : Option[WebSocket] = None

  def getWsState = wsSetup

  private val sendBuffer:MiddleBufferInJs = new MiddleBufferInJs(4096)

  def sendMsg(msg:BreakoutGameEvent.WsMsgFront) = {
    import org.seekloud.byteobject.ByteObject._
    webSocketStreamOpt.foreach{ s =>
      s.send(msg.fillMiddleBuffer(sendBuffer).result())
    }
  }


  def setup(wsUrl:String):Unit = {
    if(wsSetup){
      println(s"websocket已经启动")
    }else{
      val websocketStream = new WebSocket(wsUrl)

      webSocketStreamOpt = Some(websocketStream)
      websocketStream.onopen = { event: Event =>
        wsSetup = true
        connectSuccessCallback(event)
      }
      websocketStream.onerror = { event: Event =>
        wsSetup = false
        webSocketStreamOpt = None
        connectErrorCallback(event)
      }

      websocketStream.onmessage = { event: MessageEvent =>
        //        println(s"recv msg:${event.data.toString}")
        event.data match {
          case blobMsg:Blob =>
            val fr = new FileReader()
            fr.readAsArrayBuffer(blobMsg)
            fr.onloadend = { _: Event =>
              val buf = fr.result.asInstanceOf[ArrayBuffer]
              messageHandler(wsByteDecode(buf,blobMsg.size))
            }
          case jsonStringMsg:String =>
            import io.circe.generic.auto._
            import io.circe.parser._
            val data = decode[BreakoutGameEvent.WsMsgServer](jsonStringMsg).right.get
            messageHandler(data)
          case unknow =>  println(s"recv unknow msg:${unknow}")
        }

      }

      websocketStream.onclose = { event: Event =>
        wsSetup = false
        webSocketStreamOpt = None
        closeCallback(event)
      }
    }
  }

  def closeWs={
    wsSetup = false
    webSocketStreamOpt.foreach(_.close())
    webSocketStreamOpt = None
  }

  import org.seekloud.byteobject.ByteObject._

  private def wsByteDecode(a:ArrayBuffer,s:Double):BreakoutGameEvent.WsMsgServer={
    val middleDataInJs = new MiddleBufferInJs(a)
    bytesDecode[BreakoutGameEvent.WsMsgServer](middleDataInJs) match {
      case Right(r) =>
        try {
          setDateSize(s"${r.getClass.toString.split("TankGameEvent").last.drop(1)}",s)
        }catch {case exception: Exception=> println(exception.getCause)}
        r
      case Left(e) =>
        println(e.message)
        BreakoutGameEvent.DecodeError()
    }
  }


}
