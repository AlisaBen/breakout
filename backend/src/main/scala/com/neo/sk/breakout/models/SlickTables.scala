package com.neo.sk.breakout.models
// AUTO-GENERATED Slick data model
/** Stand-alone Slick data model for immediate use */
object SlickTables extends {
  val profile = slick.jdbc.H2Profile
} with SlickTables

/** Slick data model trait for extension, choice of backend or usage in the cake pattern. (Make sure to initialize this late.) */
trait SlickTables {
  val profile: slick.jdbc.JdbcProfile
  import profile.api._
  import slick.model.ForeignKeyAction
  // NOTE: GetResult mappers for plain SQL are only generated for tables where Slick knows how to map the types of all columns.
  import slick.jdbc.{GetResult => GR}

  /** DDL for all tables. Call .create to execute. */
  lazy val schema: profile.SchemaDescription = tBattleRecord.schema ++ tUserInfo.schema ++ tVisitorInfo.schema
  @deprecated("Use .schema instead of .ddl", "3.0")
  def ddl = schema

  /** Entity class storing rows of table tBattleRecord
    *  @param recordId Database column RECORD_ID SqlType(BIGINT), AutoInc, PrimaryKey
    *  @param timestamp Database column TIMESTAMP SqlType(BIGINT)
    *  @param nameA Database column NAME_A SqlType(VARCHAR), Length(32,true)
    *  @param nameB Database column NAME_B SqlType(VARCHAR), Length(32,true), Default(BOT)
    *  @param damageStatisticA Database column DAMAGE_STATISTIC_A SqlType(INTEGER), Default(0)
    *  @param damageStatisticB Database column DAMAGE_STATISTIC_B SqlType(INTEGER), Default(0) */
  case class rBattleRecord(recordId: Long, timestamp: Long, nameA: String, nameB: String = "BOT", damageStatisticA: Int = 0, damageStatisticB: Int = 0)
  /** GetResult implicit for fetching rBattleRecord objects using plain SQL queries */
  implicit def GetResultrBattleRecord(implicit e0: GR[Long], e1: GR[String], e2: GR[Int]): GR[rBattleRecord] = GR{
    prs => import prs._
      rBattleRecord.tupled((<<[Long], <<[Long], <<[String], <<[String], <<[Int], <<[Int]))
  }
  /** Table description of table BATTLE_RECORD. Objects of this class serve as prototypes for rows in queries. */
  class tBattleRecord(_tableTag: Tag) extends profile.api.Table[rBattleRecord](_tableTag, Some("PUBLIC"), "BATTLE_RECORD") {
    def * = (recordId, timestamp, nameA, nameB, damageStatisticA, damageStatisticB) <> (rBattleRecord.tupled, rBattleRecord.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(recordId), Rep.Some(timestamp), Rep.Some(nameA), Rep.Some(nameB), Rep.Some(damageStatisticA), Rep.Some(damageStatisticB)).shaped.<>({r=>import r._; _1.map(_=> rBattleRecord.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column RECORD_ID SqlType(BIGINT), AutoInc, PrimaryKey */
    val recordId: Rep[Long] = column[Long]("RECORD_ID", O.AutoInc, O.PrimaryKey)
    /** Database column TIMESTAMP SqlType(BIGINT) */
    val timestamp: Rep[Long] = column[Long]("TIMESTAMP")
    /** Database column NAME_A SqlType(VARCHAR), Length(32,true) */
    val nameA: Rep[String] = column[String]("NAME_A", O.Length(32,varying=true))
    /** Database column NAME_B SqlType(VARCHAR), Length(32,true), Default(BOT) */
    val nameB: Rep[String] = column[String]("NAME_B", O.Length(32,varying=true), O.Default("BOT"))
    /** Database column DAMAGE_STATISTIC_A SqlType(INTEGER), Default(0) */
    val damageStatisticA: Rep[Int] = column[Int]("DAMAGE_STATISTIC_A", O.Default(0))
    /** Database column DAMAGE_STATISTIC_B SqlType(INTEGER), Default(0) */
    val damageStatisticB: Rep[Int] = column[Int]("DAMAGE_STATISTIC_B", O.Default(0))
  }
  /** Collection-like TableQuery object for table tBattleRecord */
  lazy val tBattleRecord = new TableQuery(tag => new tBattleRecord(tag))

  /** Entity class storing rows of table tUserInfo
    *  @param id Database column ID SqlType(BIGINT), AutoInc, PrimaryKey
    *  @param userName Database column USER_NAME SqlType(VARCHAR), Length(32,true)
    *  @param password Database column PASSWORD SqlType(VARCHAR), Length(32,true)
    *  @param timestamp Database column TIMESTAMP SqlType(BIGINT)
    *  @param isForbidden Database column IS_FORBIDDEN SqlType(BOOLEAN), Default(false)
    *  @param playNum Database column PLAY_NUM SqlType(INTEGER), Default(0)
    *  @param winNum Database column WIN_NUM SqlType(INTEGER), Default(0) */
  case class rUserInfo(id: Long, userName: String, password: String, timestamp: Long, isForbidden: Boolean = false, playNum: Int = 0, winNum: Int = 0)
  /** GetResult implicit for fetching rUserInfo objects using plain SQL queries */
  implicit def GetResultrUserInfo(implicit e0: GR[Long], e1: GR[String], e2: GR[Boolean], e3: GR[Int]): GR[rUserInfo] = GR{
    prs => import prs._
      rUserInfo.tupled((<<[Long], <<[String], <<[String], <<[Long], <<[Boolean], <<[Int], <<[Int]))
  }
  /** Table description of table USER_INFO. Objects of this class serve as prototypes for rows in queries. */
  class tUserInfo(_tableTag: Tag) extends profile.api.Table[rUserInfo](_tableTag, Some("PUBLIC"), "USER_INFO") {
    def * = (id, userName, password, timestamp, isForbidden, playNum, winNum) <> (rUserInfo.tupled, rUserInfo.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(userName), Rep.Some(password), Rep.Some(timestamp), Rep.Some(isForbidden), Rep.Some(playNum), Rep.Some(winNum)).shaped.<>({r=>import r._; _1.map(_=> rUserInfo.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column ID SqlType(BIGINT), AutoInc, PrimaryKey */
    val id: Rep[Long] = column[Long]("ID", O.AutoInc, O.PrimaryKey)
    /** Database column USER_NAME SqlType(VARCHAR), Length(32,true) */
    val userName: Rep[String] = column[String]("USER_NAME", O.Length(32,varying=true))
    /** Database column PASSWORD SqlType(VARCHAR), Length(32,true) */
    val password: Rep[String] = column[String]("PASSWORD", O.Length(32,varying=true))
    /** Database column TIMESTAMP SqlType(BIGINT) */
    val timestamp: Rep[Long] = column[Long]("TIMESTAMP")
    /** Database column IS_FORBIDDEN SqlType(BOOLEAN), Default(false) */
    val isForbidden: Rep[Boolean] = column[Boolean]("IS_FORBIDDEN", O.Default(false))
    /** Database column PLAY_NUM SqlType(INTEGER), Default(0) */
    val playNum: Rep[Int] = column[Int]("PLAY_NUM", O.Default(0))
    /** Database column WIN_NUM SqlType(INTEGER), Default(0) */
    val winNum: Rep[Int] = column[Int]("WIN_NUM", O.Default(0))

    /** Uniqueness Index over (userName) (database name CONSTRAINT_BC_INDEX_5) */
    val index1 = index("CONSTRAINT_BC_INDEX_5", userName, unique=true)
  }
  /** Collection-like TableQuery object for table tUserInfo */
  lazy val tUserInfo = new TableQuery(tag => new tUserInfo(tag))

  /** Entity class storing rows of table tVisitorInfo
    *  @param id Database column ID SqlType(BIGINT), AutoInc, PrimaryKey
    *  @param nickname Database column NICKNAME SqlType(VARCHAR), Length(32,true)
    *  @param timestamp Database column TIMESTAMP SqlType(BIGINT) */
  case class rVisitorInfo(id: Long, nickname: String, timestamp: Long)
  /** GetResult implicit for fetching rVisitorInfo objects using plain SQL queries */
  implicit def GetResultrVisitorInfo(implicit e0: GR[Long], e1: GR[String]): GR[rVisitorInfo] = GR{
    prs => import prs._
      rVisitorInfo.tupled((<<[Long], <<[String], <<[Long]))
  }
  /** Table description of table VISITOR_INFO. Objects of this class serve as prototypes for rows in queries. */
  class tVisitorInfo(_tableTag: Tag) extends profile.api.Table[rVisitorInfo](_tableTag, Some("PUBLIC"), "VISITOR_INFO") {
    def * = (id, nickname, timestamp) <> (rVisitorInfo.tupled, rVisitorInfo.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(nickname), Rep.Some(timestamp)).shaped.<>({r=>import r._; _1.map(_=> rVisitorInfo.tupled((_1.get, _2.get, _3.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column ID SqlType(BIGINT), AutoInc, PrimaryKey */
    val id: Rep[Long] = column[Long]("ID", O.AutoInc, O.PrimaryKey)
    /** Database column NICKNAME SqlType(VARCHAR), Length(32,true) */
    val nickname: Rep[String] = column[String]("NICKNAME", O.Length(32,varying=true))
    /** Database column TIMESTAMP SqlType(BIGINT) */
    val timestamp: Rep[Long] = column[Long]("TIMESTAMP")
  }
  /** Collection-like TableQuery object for table tVisitorInfo */
  lazy val tVisitorInfo = new TableQuery(tag => new tVisitorInfo(tag))
}
