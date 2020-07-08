
import scala.concurrent.duration._

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._

class RecordedSimulation extends Simulation {

	val httpProtocol = http
		.baseUrl("http://127.0.0.1:8085")
		.inferHtmlResources()

	val headers_0 = Map("Postman-Token" -> "f707c475-8578-4460-b395-5efc069c8f71")

	val headers_1 = Map("Postman-Token" -> "58f55563-a6cc-4e57-b767-d15f8ee14695")

	val headers_2 = Map("Postman-Token" -> "e7bb278c-4ad9-49a2-8af9-808c7c992b3f")



	val scn = scenario("RecordedSimulation")
		.exec(http("request_0")
			.get("/data/new")
			.headers(headers_0)
			.check(bodyString.saveAs("Id")))
		.pause(1)
		.exec(http("request_1")
			.get("/data/save")
			.headers(headers_1)
			.body(StringBody(StringBody("${Id}")))
			.check(status.is(200)))
		.pause(1)
		.exec(http("request_2")
			.get("/data/load")
			.headers(headers_2)
			.body(StringBody(StringBody("${Id}")))
			.check(status.is(200)))

	setUp(scn.inject(rampUsers(1000) during (20 minutes))).protocols(httpProtocol)
}