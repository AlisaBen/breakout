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
  lazy val schema: profile.SchemaDescription = tUserInfo.schema
  @deprecated("Use .schema instead of .ddl", "3.0")
  def ddl = schema

  /** Entity class storing rows of table tUserInfo
   *  @param id Database column ID SqlType(INTEGER), AutoInc, PrimaryKey
   *  @param userName Database column USER_NAME SqlType(VARCHAR), Length(32,true)
   *  @param password Database column PASSWORD SqlType(VARCHAR), Length(32,true)
   *  @param timestamp Database column TIMESTAMP SqlType(BIGINT) */
  case class rUserInfo(id: Int, userName: String, password: String, timestamp: Long)
  /** GetResult implicit for fetching rUserInfo objects using plain SQL queries */
  implicit def GetResultrUserInfo(implicit e0: GR[Int], e1: GR[String], e2: GR[Long]): GR[rUserInfo] = GR{
    prs => import prs._
    rUserInfo.tupled((<<[Int], <<[String], <<[String], <<[Long]))
  }
  /** Table description of table USER_INFO. Objects of this class serve as prototypes for rows in queries. */
  class tUserInfo(_tableTag: Tag) extends profile.api.Table[rUserInfo](_tableTag, Some("PUBLIC"), "USER_INFO") {
    def * = (id, userName, password, timestamp) <> (rUserInfo.tupled, rUserInfo.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(userName), Rep.Some(password), Rep.Some(timestamp)).shaped.<>({r=>import r._; _1.map(_=> rUserInfo.tupled((_1.get, _2.get, _3.get, _4.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column ID SqlType(INTEGER), AutoInc, PrimaryKey */
    val id: Rep[Int] = column[Int]("ID", O.AutoInc, O.PrimaryKey)
    /** Database column USER_NAME SqlType(VARCHAR), Length(32,true) */
    val userName: Rep[String] = column[String]("USER_NAME", O.Length(32,varying=true))
    /** Database column PASSWORD SqlType(VARCHAR), Length(32,true) */
    val password: Rep[String] = column[String]("PASSWORD", O.Length(32,varying=true))
    /** Database column TIMESTAMP SqlType(BIGINT) */
    val timestamp: Rep[Long] = column[Long]("TIMESTAMP")
  }
  /** Collection-like TableQuery object for table tUserInfo */
  lazy val tUserInfo = new TableQuery(tag => new tUserInfo(tag))
}
