package com.neo.sk.breakout.shared.protocol

import com.neo.sk.breakout.shared.`object`.{BallState, ObstacleState, RacketState}
import com.neo.sk.breakout.shared.config.GameConfigImpl
import com.neo.sk.breakout.shared.model.{Point, Score}

/**
  * created by benyafang on 2019/2/3 14:30
  *
  * */
object BreakoutGameEvent {
  //todo 同步全量数据
  final case class GameContainerAllState(
                                        f:Long,
                                        rackets:List[RacketState],
                                        balls:List[BallState],
                                        obstacles:List[ObstacleState],
                                        racketMoveAction:List[(Int,Option[List[Byte]])]
                                        )

  final case class GameContainerState(
                                          f:Long,
                                          rackets:Option[List[RacketState]],
                                          racketMoveAction:Option[List[(Int,Option[List[Byte]])]]
                                        )

  /**前端建立WebSocket*/
  sealed trait WsMsgFrontSource
  case object CompleteMsgFrontServer extends WsMsgFrontSource
  case class FailMsgFrontServer(ex: Exception) extends WsMsgFrontSource

  sealed trait WsMsgFront extends WsMsgFrontSource // 前端发的数据

  /**后台建立WebSocket*/
  trait WsMsgSource
  case object CompleteMsgServer extends WsMsgSource
  case class FailMsgServer(ex: Exception) extends WsMsgSource

  sealed trait WsMsgServer extends WsMsgSource // 后端发过来的数据

  final case class WsMsgErrorRsp(errCode:Int, msg:String) extends WsMsgServer
  final case class WsSuccess(roomId:Option[Long]) extends WsMsgServer

  final case class DecodeError() extends WsMsgServer
//  touch事件基本类型：touchstart,touchmove,touchend
/**
  * 进入游戏事件（随机进入、指定房间进入），建立websocket，roomIdOpt,name
  * 玩家初始化信息，页面元素初始化同步状态
  * 成绩
  * 状态
  * 游戏结束
  * touch事件
  * 快照
  * */

  final case class StartGame(roomId:Option[Long]) extends WsMsgFront
  final case class PlayerInfo(
                             racketId:Int,//0-1 boolean?
                             name:String
                             )
  final case class YourInfo(players:PlayerInfo,config:GameConfigImpl) extends WsMsgServer//玩家信息储存在前端，写的时候考虑是否有必要
  final case class GameOver(score:List[Score]) extends WsMsgServer//游戏结束,该消息需要广播
  
  final case class Ranks(scores:List[Score]) extends WsMsgServer
  final case class SyncGameAllState(gState:GameContainerAllState) extends WsMsgServer
  final case class SyncGameState(state:GameContainerState) extends WsMsgServer
  final case class Wrap(ws:Array[Byte],isKillMsg:Boolean = false) extends WsMsgSource
  final case class PingPackage(sendTime:Long) extends WsMsgServer with WsMsgFront

  sealed trait GameEvent{
    val frame:Long
  }

  trait UserEvent extends GameEvent
  trait FollowEvent extends GameEvent  //游戏逻辑产生事件
  trait EnvironmentEvent extends GameEvent  //游戏环境产生事件
  trait UserActionEvent extends UserEvent{   //游戏用户动作事件
    val racketId:Int
    val serialNum:Byte
  }

  final case class UserJoinRoom(name:String, racketState:RacketState,ballState:BallState, override val frame: Long) extends  UserEvent with WsMsgServer

  final case class UserTouchMove(
                                  racketId:Int,
                                  override val frame: Long,
                                  touchMove: Byte,
                                  override val serialNum: Byte) extends UserActionEvent with WsMsgFront with WsMsgServer
  final case class UserTouchEnd(
                                 racketId:Int,
                                 override val frame: Long,
                                 override val serialNum: Byte
                               ) extends UserActionEvent with WsMsgFront with WsMsgServer
  
  final case object FailGame extends WsMsgFront
  
  final case class GenerateObstacle(override val frame:Long,obstacleState: ObstacleState) extends EnvironmentEvent with WsMsgServer

  final case class ObstacleRemove(obstacleId:Int, override val frame:Long) extends EnvironmentEvent with WsMsgServer
  /**球运动方向重新计算，分数计算*/
  final case class RacketCollision(racketId:Int,ballId:Int,override val frame:Long) extends FollowEvent

  final case class ObstacleCollision(brickId:Int, ballId:Int,enemyRacketId:Int,obstacleValue:Int,obstaclePosition:Point, isLeft:Option[Boolean] = None, override val frame:Long) extends FollowEvent

  sealed trait GameSnapshot

  final case class TankGameSnapshot(
                                     state:GameContainerAllState
                                   ) extends GameSnapshot


  final case class GameInformation(
                                    gameStartTime:Long,
                                    config: GameConfigImpl
                                  )





}
