package com.neo.sk.breakout.shared.ptcl

import com.neo.sk.breakout.shared.model.{Score, SimpleScore}

/**
  * created by benyafang on 2019/2/20 14:20
  * */
object AdminProtocol {
  object UserManagerProtocol{
    case class UserInfo(
                       name:String,
                       registerTime:Long,
                       isForbidden:Boolean,
                       playNum:Int = 0,
                       winNum:Int = 0
                       )
    case class GetUserInfoListReq(beginTimeOpt:Option[Long],endTimeOpt:Option[Long])
    case class GetUserInfoListRsp(
                                 userInfoList: List[UserInfo],
                                 errCode:Int = 0,
                                 msg:String = "ok"
                                 )

    case class UserForbiddenReq(
                               name:String,
                               isForbidden:Boolean
                               )

  }

  object RoomManagerProtocol{
    case class RoomInfo(
                       roomId:Int,
                       userList:List[String],
                       )
    case class GetAllRoomReq()
    case class GetAllRoomRsp(
                            roomList:List[RoomInfo],
                            errCode:Int = 0,
                            msg:String = "ok"
                            )

    @deprecated case class DeleteRoomReq(
                            roomId:Int
                            )

    @deprecated case class AddRoomReq()
    @deprecated case class AddRoomRsp(
                           roomId:Int,
                           errCode:Int = 0,
                           msg:String = "ok"
                         )
  }

  object StatisticProtocol{
//    4. 统计信息：游戏次数统计，游戏战绩，登录玩家的胜利次数，失败次数，游戏次数
    case class GameBattleInfo(
                             recordId:Long,
                             timestamp:Long,
                             result:List[SimpleScore]
                             )
    case class GameStatisticProfile()
    case class GameStatisticProfileRsp(
                                      gameBattleInfoLs: List[GameBattleInfo],
                                      errCode:Int = 0,
                                      msg:String ="ok"
                                      )

  }

}
