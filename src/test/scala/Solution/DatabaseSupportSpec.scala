package Solution

import com.mongodb.casbah.Imports._
import com.tomliddle.database.MongoSupport
import com.tomliddle.solution._
import org.joda.time.{LocalTime, DateTime}
import org.scalatest.{BeforeAndAfterEach, Matchers, WordSpec}

class DatabaseSupportSpec extends WordSpec with Matchers with BeforeAndAfterEach with TestObjects {


	"Database support" when {

		"getting a truck" should {

			"get the correct location" in {
				//ph.confidence.toDouble should be (0.287 +- tolerance)
				//ph.confidence.toDouble should equal (0)
				//ph.purge.confidence.toDouble should be (0.221 +- tolerance)
			}

		}

		"using mongoDB" should {

			"connect to mongo db" in {
				val mongoClient = MongoConnection()//MongoClient("127.0.0.1", 27017)
				val mongoDb = mongoClient("testing_db")
				val mongoSolutions = mongoDb("solutions")

				mongoSolutions.drop
				mongoSolutions.insert(MongoDBObject("hello" -> "world"))

				mongoSolutions.size should equal(1)
			}

			"save the sol" in {
				/*val startTime = new LocalTime(0).withHourOfDay(9)
				val endTime = new LocalTime(0).withHourOfDay(19)
				val stop = Stop("1", 0, 0, "234234", startTime, endTime, BigDecimal(1), List(), 1)
				val depot: Depot = Depot("Depot1", 0 ,0 , "", 1, Some(1))

				val locationList = List(
					stop.copy(x = 1, y = 5),
					stop.copy(x = 1, y = 1),
					stop.copy(x = 2, y = 3),
					stop.copy(x = 0, y = 4),
					stop.copy(x = 2, y = 4),
					stop.copy(x = 2, y = 2),
					stop.copy(x = 2, y = 2),
					stop.copy(x = 1, y = 0),
					stop.copy(x = 2, y = 0),
					stop.copy(x = 10, y = 10)
				)

				val lm = new LocationMatrix(locationList, List(depot))
				val stops: List[Stop] = (0 to locationList.size - 1).map {
					stopId => locationList(stopId).copy(id = Some(stopId))
				}.toList
				val truck = Truck("Truck1", startTime, endTime, BigDecimal(100), depot, stops, lm, 1, Some(1))
				val solution = Solution("Solution", depot, truck.stops, List(truck), 1)*/

				val mongoSupport = new MongoSupport("test")
				mongoSupport.removeMSolutions(-20)
				mongoSupport.addMSolution(solution.copy(userId = -20))

				val solutions = mongoSupport.getMSolutions(-20)

				solutions.size should equal (1)
			}
		}
	}
}