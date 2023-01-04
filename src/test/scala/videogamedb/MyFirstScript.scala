package videogamedb

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.jsonpath.JsonPath

import javax.jms.Session

class MyFirstScript extends Simulation {

  //http Configuration
  val httpCall = http.baseUrl("https://www.videogamedb.uk/api")
                      .acceptHeader("application/json")
                      .contentTypeHeader("application/json")


  def authentication()={
    exec(
      http("Request Authentication token")
        .post("/authenticate")
        .body(RawFileBody("data/authenticateAdminPayload.json")).asJson
        .check(status.is(200))
        .check(jsonPath("$.token").saveAs("jwttoken"))
    )
  }

/*  def createVideoGame()={
    exec(
      http("Create a viodeo game post call")
        .post("/videogame")
        .header("Authorization","Bearer ${jwttoken}")
        .body(RawFileBody("data/createvideogame.json")).asJson
        .check(status.is(200))
        .check(jsonPath("$.id").saveAs("gameID"))

    )
  }*/


// jsonfeeder
  def createVideoGame()={
    exec(
      http("Create a viodeo game post call")
        .post("/videogame")
        .header("Authorization","Bearer ${jwttoken}")
        .body(RawFileBody("data/createvideogameold.json")).asJson
        .check(status.is(200))
        .check(jsonPath("$.id").saveAs("gameID"))
    )
  }

  // scenario
  val scn = scenario("Post Call for Authentication")
    .exec(authentication())
    .exec{
      session => println(session("jwttoken"));session
    }
    .exec(createVideoGame())
    .exec{
      session => println(session("gameID"));session
    }

  //setup

  setUp(
    scn.inject(atOnceUsers(3))
      .protocols(httpCall)
  )
}



