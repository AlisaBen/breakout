package com.neo.sk.breakout.front.pages.control

import com.neo.sk.breakout.front.utils.Shortcut
import com.neo.sk.breakout.shared.protocol.BreakoutGameEvent

import scala.collection.mutable
import scala.collection.mutable.HashMap

/**
  * Created by hongruying on 2018/8/29
  * 计算网络时延
  * A4
  */
final case class NetworkLatency(latency: Long)

trait NetworkInfo {
  this: GameHolder =>

  private var lastPingTime = System.currentTimeMillis()
  private val PingTimes = 10
  private var latency: Long = 0L
  private var receiveNetworkLatencyList: List[NetworkLatency] = Nil

  val dataSizeMap: HashMap[String, Double] = HashMap[String, Double]()
  var dataSizeList: List[String] = Nil

  def ping(): Unit = {
    val curTime = System.currentTimeMillis()
    if (curTime - lastPingTime > 10000) {
      startPing()
      lastPingTime = curTime
    }
  }

  private def startPing(): Unit = {
    this.sendMsg2Server(BreakoutGameEvent.PingPackage(System.currentTimeMillis()))
  }

  protected def receivePingPackage(p: BreakoutGameEvent.PingPackage): Unit = {
    receiveNetworkLatencyList = NetworkLatency(System.currentTimeMillis() - p.sendTime) :: receiveNetworkLatencyList
    if (receiveNetworkLatencyList.size < PingTimes) {
      Shortcut.scheduleOnce(() => startPing(), 10)
    } else {
      latency = receiveNetworkLatencyList.map(_.latency).sum / receiveNetworkLatencyList.size
      receiveNetworkLatencyList = Nil
    }
  }

  protected def getNetworkLatency = latency

  Shortcut.schedule(() => {
    println("allDataSize---" + (dataSizeMap.values.sum/1000).formatted("%.2f") + "kb/s")
    dataSizeList = dataSizeMap.toList.sortBy(_._1).map { r =>
      val s = r._1 + ":" + {
        r._2 / 1000
      }.formatted("%.2f") + "kb/s"
      println(s)
      s
    }
    dataSizeMap.foreach(r => dataSizeMap.update(r._1, 0))
  }, 1000)

  protected def setDateSize(d: String, s: Double) = {
    dataSizeMap.get(d) match {
      case Some(m) =>
        dataSizeMap.update(d, m + s)
      case None =>
        dataSizeMap.put(d, s)
    }
  }

}
