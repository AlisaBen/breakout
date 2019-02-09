package com.neo.sk.breakout.core.control

import akka.actor.typed.ActorRef
import com.neo.sk.breakout.core.RoomActor
import com.neo.sk.breakout.shared.game.GameContainer
import com.neo.sk.breakout.shared.protocol.BreakoutGameEvent
import javax.xml.ws.Dispatch
import org.slf4j.Logger

/**
  * created by benyafang on 2019/2/8 21:52
  * A4
  * 1.移动拍子事件--发射子弹事件
  * 2.生成拍子位置--
  * 3.生成拍子、障碍物、球
  * 4.处理碰撞--障碍物被袭击
  * 5.离开游戏
  * 6.接收用户行为事件
  * 7.初始化
  * 8.逻辑更新
  *
  * */
case class GameContainerServerImpl(
                                  roomActorRef:ActorRef[RoomActor.Command],
                                  log:Logger,
                                  dispatch:BreakoutGameEvent.WsMsgServer => Unit,
                                  dispatchTo:(String,BreakoutGameEvent.WsMsgServer) => Unit
                                  )
//  extends GameContainer
{
  import scala.language.implicitConversions


}
