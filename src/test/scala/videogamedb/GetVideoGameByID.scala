package videogamedb

import io.gatling.core.Predef._
import io.gatling.core.scenario.Simulation
import io.gatling.core.structure.ChainBuilder
import io.gatling.http.Predef._

class GetVideoGameByID extends Simulation{

  //http Configuration
  val httpCall = http.baseUrl("https://www.videogamedb.uk/api")
                    .acceptHeader("application/json")
                    .contentTypeHeader("application/json")


  def getVideoGameByID(): ChainBuilder ={
    exec(
        http("Get Video Game By id")
          .get("/videogame/1")
          .check(status.is(200))
          .check(jsonPath("$.name").saveAs("gamename"))
    )
  }
  // scenario

  val scn = scenario("Get Call to fetch Game Name")
    .exec(getVideoGameByID())
            .exec{
              session => println(session("gamename"));session
            }
  //setup

  setUp(
    scn.inject(atOnceUsers(1))
      .protocols(httpCall)
  )

}
