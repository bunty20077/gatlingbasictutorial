package videogamedb

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import sun.security.util.Length

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import scala.util.Random


class ComplexCustomFeeder extends Simulation {

  val sentHeaders = Map("accept" -> "application/json", "content-type" -> "application/json")
  val httpProtocol = http.baseUrl("https://www.videogamedb.uk/api").headers(sentHeaders)

  var idNum = (1 to 10).iterator
  val rand = new Random()
  val pattern = DateTimeFormatter.ofPattern("yyyy-MM-dd")
  val now = LocalDate.now()

  def getString(length: Int): String = {
    rand.alphanumeric.filter(_.isLetter).take(length).mkString
  }

  def getDate(startDate: LocalDate, random: Random): String = {
    startDate.minusDays(random.nextInt(30)).format(pattern)
  }

  val customfeeders = Iterator.continually(Map(
    "gameid" -> idNum.next(),
    "name" -> ("Game-" + getString(5)),
    "releaseDate" -> getDate(now, rand),
    "reviewScore" -> rand.nextInt(80),
    "rating" -> ("Rating-" + getString(4)),
    "category" -> ("Category-" + getString(4))
  ))

  def authenticate() = {
    exec(http("authenticate").post("/authenticate").body(RawFileBody("data/authenticateAdminPayload.json")).asJson
      .check(jsonPath("$.token").saveAs("jwtToken")))
  }

  def createNewGame() = {
    repeat(10) {
      feed(customfeeders)
        .exec(http("createnew game").post("/videogame")
          .header("authorization", "Bearer ${jwtToken}")
          .body(ElFileBody("data/template.json")).asJson
          .check(status.is(200))
          .check(bodyString.saveAs("responsebody")))
        .pause(1)
    }
  }


  val scn = scenario("complex customfeeder with template")
    .exec(authenticate())
    .exec { session => println(session("jwtToken")); session }
    .exec(createNewGame())
    .exec { session => println(session("responsebody")); session }

  setUp(
    scn.inject(atOnceUsers(1)).protocols(httpProtocol)
  )
}