package videogamedb

import io.gatling.core.Predef._
import io.gatling.core.structure.ChainBuilder
import io.gatling.http.Predef._

import scala.Console.println

class FullTestTwo extends Simulation {

  val sentHeaders = Map("accept" -> "application/json", "content-type" -> "application/json")

  val httpProtocol = http.baseUrl("https://www.videogamedb.uk/api").headers(sentHeaders)

  val csvFeeder = csv("data/gameid.csv").random

  def USERS: Int = System.getProperty("NO_OF_USER", "2").toInt

  def RAMPUSERTIME: Int = System.getProperty("RAMP_TIME", "2").toInt

  def TESTEXECUTION: Int = System.getProperty("MAXDURATION", "10").toInt

  before {
    println(s"total user for this simulation ${USERS}")
    println(s"total Ramup duration for this simulation ${RAMPUSERTIME}")
    println(s"total user for this simulation ${TESTEXECUTION}")
  }

  //get all games

  def getAllGames =
    exec(http("get All Games").get("/videogame").check(status.is(200)))


  def authenticate: ChainBuilder =
    exec(http("authenticate").post("/authenticate")
      .body(RawFileBody("data/authenticateAdminPayload.json")).asJson
      .check(jsonPath("$.token").saveAs("jwtToken")))

  //create new game
  def createNewGame: ChainBuilder =
    feed(csvFeeder).exec(http("Create New Game").post("/videogame")
      .header("authorization", "Bearer ${jwtToken}")
      .body(ElFileBody("data/template.json")).asJson
      .check(status.is(200))
      .check(bodyString.saveAs("responseBody")))
      .pause(1)

  def getSpecificGame =
    exec(http("get specific game ${name}").get("/videogame/${gameid}")
      .check(status.is(200)))
      .pause(1)

  def deleteSpecificGame =
    exec(http("delete any game ${gameid}").delete("/videogame/${gameid}")
      .header("authorization", "Bearer ${jwtToken}")
      .check(bodyString.is("Video game deleted")))
      .pause(1)

  val scn = scenario("full test")
    .forever {
      exec(getAllGames)
        .exec(authenticate)
        .exec(createNewGame)
        .exec { session => println(session("responseBody")); session }
        .exec(getSpecificGame)
        .exec(deleteSpecificGame)
    }


  setUp(
    scn.inject(nothingFor(3),
      rampUsers(USERS).during(RAMPUSERTIME)).protocols(httpProtocol)
  ).maxDuration(TESTEXECUTION)


}