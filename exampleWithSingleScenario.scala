import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._


class ExampleWithSingleScenario extends Simulation {

	/*val httpConf = http.baseURL("https://postman-echo.com").acceptHeader("application/json")
	val scn = scenario("ExampleWithSingleScenario").exec(http("SingleScenario").get("/get?test=123")).pause(5)
	setUp(scn.inject(constantUsersPerSec(50) during (30 minutes))).throttle(reachRps(100) in (1 second),holdFor(1 minute)).protocols(httpConf)
*/
	val concurrency = Integer.getInteger("concurrency", 50).toInt
	val rampUpTime = Integer.getInteger("ramp-up", 0).toInt
	val holdForTime = Integer.getInteger("hold-for", 60).toInt
	val throughput = Integer.getInteger("throughput", 500).toInt
	val iterationLimit = Integer.getInteger("iterations")

	val durationLimit = rampUpTime + holdForTime


	var httpConf = http.baseURL("https://postman-echo.com").header("Content-Type", "application/json")

	var testScenario = scenario("Taurus Scenario")

	var execution = exec(http("postmanGET").get("/get"))

	if (iterationLimit == null)
	testScenario = testScenario.forever{execution}
	else
	testScenario = testScenario.repeat(iterationLimit.toInt){execution}

	val virtualUsers =
	if (rampUpTime > 0)
	  rampUsers(concurrency) over (rampUpTime seconds)
	else
	  atOnceUsers(concurrency)

	var testSetup = setUp(testScenario.inject(virtualUsers).protocols(httpConf))

	if (throughput != 0)
	testSetup = testSetup.throttle(
	  reachRps(throughput) in (rampUpTime),
	  holdFor(durationLimit)
	)

	/*if (durationLimit > 0)
	testSetup.maxDuration(durationLimit)*/
}