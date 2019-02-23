package com.neo.sk.utils

import slick.codegen.SourceCodeGenerator
import slick.jdbc.{JdbcProfile,PostgresProfile,H2Profile}
//import slick.jdbc.H2Profile
import scala.concurrent.Await
import scala.concurrent.duration.Duration

/**
  * User: Taoz
  * Date: 7/15/2015
  * Time: 9:33 AM
  */
object MySlickCodeGenerator {


  import concurrent.ExecutionContext.Implicits.global

  val slickDriver = "slick.jdbc.H2Profile"
//  val jdbcDriver = "org.postgresql.Driver"
//  val jdbcDriver = "org.h2.Driver"
  val jdbcDriver = "org.h2.Driver"
//  val url = "jdbc:postgresql://127.0.0.1:5432/breakout?useUnicode=true&characterEncoding=utf-8"
  val url = "jdbc:h2:D:\\doc\\project\\breakout\\backend\\src\\main\\resources\\sql\\breakout"
  val outputFolder = "target/gencode/genTablesPsql"
  val pkg = "com.neo.sk.breakout.models"
  val user = "breakout"
  val password = "breakout123"


  //val dbDriver = MySQLDriver
//  val dbDriver: JdbcProfile = PostgresProfile
  val dbDriver:JdbcProfile = H2Profile

  def genDefaultTables() = {

    slick.codegen.SourceCodeGenerator.main(
      Array(slickDriver, jdbcDriver, url, outputFolder, pkg, user, password)
    )


  }

  def genDatabase() = {

    //    TableQuery[tAdvertisement].schema.create
    //    TableQuery[tFooter].schema.create
    //    TableQuery[tSpecificType].schema.create
    //    TableQuery[tCarousel].schema.create
    //    TableQuery[tQuicknews].schema.create
    //    TableQuery[tLabel].schema.create
    //    TableQuery[tCategory].schema.create
    //    TableQuery[tRecommendation].schema.create

    //    (
    //      TableQuery[tApp].schema ++
    //        TableQuery[tSecure].schema ++
    //        TableQuery[tPackageReport].schema ++
    //      TableQuery[tCurrencyReport].schema).create


  }

  def genDDL() ={
    // fetch data model
    val driver: JdbcProfile =
//      Class.forName("slick.driver.PostgresDriver" + "$").getField("MODULE$").get(null).asInstanceOf[JdbcProfile]
    Class.forName("slick.driver.H2Profile" + "$").getField("MODULE$").get(null).asInstanceOf[JdbcProfile]
    val dbFactory = driver.api.Database
    val db = dbFactory.forURL(url, driver = jdbcDriver,
      user = user, password = password, keepAliveConnection = true)

    //    db.run(DBIOAction.seq(genDatabase()))
  }

  def main(args: Array[String]) {
    //    genDefaultTables()
    val dbDriver = com.neo.sk.utils.MyPostgresDriver
//    val dbDriver = H2Profile

    genCustomTables(dbDriver)

    println(s"Tables.scala generated in $outputFolder")

    //        genDDL()
    //        Thread.sleep(10000)

  }


  def genCustomTables(dbDriver: JdbcProfile) = {

    // fetch data model
    val driver: JdbcProfile =
      Class.forName(slickDriver + "$").getField("MODULE$").get(null).asInstanceOf[JdbcProfile]
    val dbFactory = driver.api.Database
    val db = dbFactory.forURL(url, driver = jdbcDriver,
      user = user, password = password, keepAliveConnection = true)


    // fetch data model
    val modelAction = dbDriver.createModel(Some(dbDriver.defaultTables)) // you can filter specific tables here
    val modelFuture = db.run(modelAction)

    // customize code generator
    val codeGenFuture = modelFuture.map(model => new SourceCodeGenerator(model) {
      // override mapped table and class name
      override def entityName =
        dbTableName => "r" + dbTableName.toCamelCase

      override def tableName =
        dbTableName => "t" + dbTableName.toCamelCase

      // add some custom import
      // override def code = "import foo.{MyCustomType,MyCustomTypeMapper}" + "\n" + super.code

      // override table generator
      /*    override def Table = new Table(_){
            // disable entity class generation and mapping
            override def EntityType = new EntityType{
              override def classEnabled = false
            }

            // override contained column generator
            override def Column = new Column(_){
              // use the data model member of this column to change the Scala type,
              // e.g. to a custom enum or anything else
              override def rawType =
                if(model.name == "SOME_SPECIAL_COLUMN_NAME") "MyCustomType" else super.rawType
            }
          }*/
      /*val models = new scala.collection.mutable.MutableList[String]

      override def packageCode(profile: String, pkg: String, container: String, parentType: Option[String]): String = {
        super.packageCode(profile, pkg, container, parentType) + "\n" + outsideCode
      }

      def outsideCode = s"${indent(models.mkString("\n"))}"

      override def Table = new Table(_) {
        override def EntityType = new EntityTypeDef {
          override def docWithCode: String = {
            models += super.docWithCode.toString + "\n"
            ""
          }
        }
      }*/


    })

    val codeGenerator = Await.result(codeGenFuture, Duration.Inf)
    codeGenerator.writeToFile(
      slickDriver, outputFolder, pkg, "SlickTables", "SlickTables.scala"
    )


  }


}


