package Solution

import com.mongodb.casbah.Imports._
import com.tomliddle.database.MongoSupport
import org.scalatest.{BeforeAndAfterEach, Matchers, WordSpec}

class DatabaseSupportSpec extends WordSpec with Matchers with BeforeAndAfterEach with TestObjects {

	val mongoSupport = new MongoSupport("test")
	val USER_ID = -20

	override def beforeEach {
		mongoSupport.removeMSolutions(USER_ID)
	}

	"Database support" when {

		"connect to mongo db" in {
			val mongoClient = MongoConnection()//MongoClient("127.0.0.1", 27017)
			val mongoDb = mongoClient("testing_db")
			val mongoSolutions = mongoDb("solutions")

			mongoSolutions.drop
			mongoSolutions.insert(MongoDBObject("hello" -> "world"))

			mongoSolutions.size should equal(1)
		}

		"Save operations" should {

			"save the sol" in {
				mongoSupport.addMSolution(solution.copy(userId = USER_ID))

				val solutions = mongoSupport.getMSolutions(USER_ID)
				solutions.size should equal(1)
			}
		}

		"Remove operations " should {

			"remove all user solutions" in {
				val solutions = mongoSupport.getMSolutions(USER_ID)
				solutions.size should equal (0)

				mongoSupport.addMSolution(solution.copy(userId = USER_ID))
				mongoSupport.addMSolution(solution.copy(userId = USER_ID))
				mongoSupport.addMSolution(solution.copy(userId = USER_ID))

				val solutions2 = mongoSupport.getMSolutions(USER_ID)
				solutions2.size should equal (3)
			}
		}

		"Get operations" should {

			"get a record" in {
				val sol = simpleSolution.copy(userId = USER_ID, id = Some(1))
				mongoSupport.addMSolution(sol)

				val solOpt = mongoSupport.getMSolution(USER_ID, 1)

				solOpt.isDefined should equal (true)

				solOpt.get.name should equal (sol.name)
				solOpt.get.depot should equal (sol.depot)
				solOpt.get.id should equal (sol.id)
				solOpt.get.stopsToLoad should equal (sol.stopsToLoad)

				solOpt.get.userId should equal (sol.userId)
				solOpt.get.trucks(0).depot should equal (sol.trucks(0).depot)
				//solOpt.get.trucks(0).endTime should equal (sol.trucks(0).endTime)
				//solOpt.get.trucks(0).startTime should equal (sol.trucks(0).startTime)
				solOpt.get.trucks(0).id should equal (sol.trucks(0).id)
				solOpt.get.trucks(0).maxWeight should equal (sol.trucks(0).maxWeight)
				solOpt.get.trucks(0).name should equal (sol.trucks(0).name)
				solOpt.get.trucks(0).stops.head should equal (sol.trucks(0).stops.head)
				solOpt.get.trucks(0).userId should equal (sol.trucks(0).userId)
				solOpt.get.trucks(0).lm should equal (sol.trucks(0).lm)

			}
		}
	}
}