package videogamedb

import io.gatling.core.Predef._
import io.gatling.core.structure.ChainBuilder
import io.gatling.http.Predef._

class GetVideoGameByIDCustomFeeder extends Simulation{

  //http Configuration
  val httpCall = http.baseUrl("https://www.videogamedb.uk/api")
                    .acceptHeader("application/json")
                    .contentTypeHeader("application/json")


  //custom feeder

 var idNumbers = ( 1 to 10 ).iterator
  val customFeeder = Iterator.continually(Map("gameID" -> idNumbers.next()))

  def getVideoGameByID(): ChainBuilder = {
    repeat(10){
      feed(customFeeder)
        .exec(
          http("Get Video Game By id -> ${gameID}")
            .get("/videogame/${gameID}")
            .check(status.is(200))
            .check(jsonPath("$.name").saveAs("gamename"))
        )
    }
  }

  // scenario

  val scn = scenario("Get Call to fetch Game x  Name")
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
