package videogamedb

import io.gatling.core.Predef._
import io.gatling.core.structure.ChainBuilder
import io.gatling.http.Predef._
import org.checkerframework.checker.units.qual.Length

import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.{Calendar, Date}
import scala.util.Random

class CreateVideoGameComplexFeeder extends Simulation{

  //http Configuration
  val httpCall = http.baseUrl("https://www.videogamedb.uk/api")
                    .acceptHeader("application/json")
                    .contentTypeHeader("application/json")


  //custom feeder

 var idNumbers = ( 1 to 10 ).iterator

  val rnd = new Random()
  val pattern = DateTimeFormatter.ofPattern("yyyy-MM-dd")
  val now = LocalDate.now()

  def getRandomString(length: Int): String = {
    rnd.alphanumeric.filter(_.isLetter).take(length).mkString
  }

  def getDate(startDate: LocalDate, random: Random): String = {
    startDate.minusDays(random.nextInt(30)).format(pattern)
  }


 val customFeeder = Iterator.continually(Map(
            "gameID" -> idNumbers.next(),
            "name" -> ("Game "+ getRandomString(5)),
            "releaseDate" -> "1998-08-01",
            "reviewScore" ->  rnd.nextInt(10),
            "category" -> ("Cat "+ getRandomString(5)),
            "rating" -> ("Rating "+  rnd.nextInt(10))


  ))

  def authentication()={
    exec(
      http("Request Authentication token")
        .post("/authenticate")
        .body(RawFileBody("data/authenticateAdminPayload.json")).asJson
        .check(status.is(200))
        .check(jsonPath("$.token").saveAs("jwttoken"))
    )
  }


  def createVideoGame()={
    repeat(10){
      feed(customFeeder)
        .exec(
          http("Create a video game post call ")
            .post("/videogame")
            .header("Authorization","Bearer ${jwttoken}")
            .body(ElFileBody("data/createvideogame.json")).asJson
            .check(bodyString.saveAs("responseBody"))
        )
  }
  }
  // scenario

  val scn = scenario("Complex Feeder")
    .exec(authentication())
    .exec(createVideoGame())
    .exec{ session => println(session("responseBody"));session}
  //setup

  setUp(
    scn.inject(atOnceUsers(1))
      .protocols(httpCall)
  )

}
