package videogamedb

import io.gatling.core.Predef._
import io.gatling.core.structure.ChainBuilder
import io.gatling.http.Predef._

class FullTest extends Simulation {


  val sentHeaders = Map("accept" -> "application/json", "content-type" -> "application/json")
  val httpCall = http.baseUrl("https://www.videogamedb.uk/api").headers(sentHeaders)
  val csvFeeder = csv("data/gameid.csv").random

  def USERS: Int = System.getProperty("USERS", "2").toInt

  def RAMPUSERTIME: Int = System.getProperty("RAMPTIME", "2").toInt

  def TESTEXECUTION: Int = System.getProperty("MAXDURATION", "10").toInt


  before {
    println(s"Total no of users ${USERS}")
    println(s"Total no of ramptime ${RAMPUSERTIME}")
    println(s"Total no of maxduration ${TESTEXECUTION}")
  }

  //get all games
  def getAllVideoGame(): ChainBuilder = {
    exec(
      http("Get all Video Game ")
        .get("/videogame")
        .check(status.is(200))
    )
  }

  // authenticate
  def authentication() = {
    exec(
      http("Request Authentication token")
        .post("/authenticate")
        .body(RawFileBody("data/authenticateAdminPayload.json")).asJson
        .check(status.is(200))
        .check(jsonPath("$.token").saveAs("jwttoken"))
    )
  }

  // create new game
  def createVideoGame() = {
      feed(csvFeeder)
        .exec(
          http("Create a video game post call ")
            .post("/videogame")
            .header("Authorization", "Bearer ${jwttoken}")
            .body(ElFileBody("data/createvideogame.json")).asJson
            .check(bodyString.saveAs("responseBody"))
        )
  }

  // get video game by id
  def getVideoGameByID(): ChainBuilder = {
    feed(csvFeeder)
    exec(
      http("Get Video Game By id")
        .get("/videogame/${gameid}")
        .check(status.is(200))
        .check(jsonPath("$.name").saveAs("gamename"))
    )
  }

  // Delete Video game
  def deleteVideoGame(): ChainBuilder = {
    feed(csvFeeder)
    exec(
      http("Delete video game")
        .delete("/videogame/${gameid}")
        .header("Authorization", "Bearer ${jwttoken}")
        .check(status.is(200))
        .check(bodyString.is("Video game deleted"))
    )
  }

  //scenario
  val scn = scenario("Full Test Case ")

    .exec(getAllVideoGame())
      .exec(authentication())
      .exec(createVideoGame())
      .exec(getVideoGameByID())
      .exec(deleteVideoGame())

  //setup

  setUp(
    scn.inject(nothingFor(3),rampUsers(USERS).during(RAMPUSERTIME))
      .protocols(httpCall)
  )

}
