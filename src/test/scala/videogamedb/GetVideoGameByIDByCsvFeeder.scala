package videogamedb

import io.gatling.core.scenario.Simulation
import io.gatling.core.Predef._
import io.gatling.core.structure.ChainBuilder
import io.gatling.http.Predef._

class GetVideoGameByIDByCsvFeeder extends Simulation{

  //http Configuration
  val httpCall = http.baseUrl("https://www.videogamedb.uk/api")
                    .acceptHeader("application/json")
                    .contentTypeHeader("application/json")


  // csv feeder

  val csvFeeder = csv("data/gameid.csv").circular

  def getVideoGameByID(): ChainBuilder ={
    repeat(7){
      feed(csvFeeder)
      exec(
        http("Get Video Game Name By id for ${gameid}")
          .get("/videogame/${gameid}")
          .check(status.is(200))
          .check(jsonPath("$.name").saveAs("gamename"))
      )
    }
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
