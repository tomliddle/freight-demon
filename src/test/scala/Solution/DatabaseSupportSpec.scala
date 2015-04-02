package Solution

import com.mongodb.casbah.Imports._
import com.tomliddle.database.MongoSupport
import org.scalatest.{BeforeAndAfterEach, Matchers, WordSpec}

class DatabaseSupportSpec extends WordSpec with Matchers with BeforeAndAfterEach with TestObjects {

	val mongoSupport = new MongoSupport("test")
	val USER_ID = -20

	override def beforeEach {
		mongoSupport.removeSolutions(USER_ID)
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

			"save (update) the sol" in {
				val solToUpdate = solution.copy(userId = USER_ID)
				mongoSupport.addSolution(solToUpdate)

				val solutions = mongoSupport.getSolutions(USER_ID)
				solutions.size should equal(1)

				solToUpdate should equal(solutions.head)

				val updated = solToUpdate.copy(stopsToLoad = solToUpdate.stopsToLoad.tail)
				mongoSupport.updateSolution(updated)

				val solutions2 = mongoSupport.getSolutions(USER_ID)
				solutions2.size should equal(1)

				solutions2.head should equal(updated)
			}

			"save a new solution" in {
				val sol = solution.copy(userId = USER_ID)
				mongoSupport.addSolution(sol)

				val solutions = mongoSupport.getSolutions(USER_ID)
				solutions.size should equal(1)

				sol should equal(solutions.head)
			}
		}

		"Remove operations " should {

			"remove all user solutions" in {
				val solutions = mongoSupport.getSolutions(USER_ID)
				solutions.size should equal (0)


				mongoSupport.addSolution(solution.copy(userId = USER_ID, _id = new ObjectId))
				mongoSupport.addSolution(solution.copy(userId = USER_ID, _id = new ObjectId))
				mongoSupport.addSolution(solution.copy(userId = USER_ID, _id = new ObjectId))

				val solutions2 = mongoSupport.getSolutions(USER_ID)
				solutions2.size should equal (3)

				mongoSupport.removeSolutions(USER_ID)

				val solutions3 = mongoSupport.getSolutions(USER_ID)
				solutions3.size should equal (0)
			}

			"remove a single solution" in {
				val solutions = mongoSupport.getSolutions(USER_ID)
				solutions.size should equal (0)

				val sol1 = solution.copy(userId = USER_ID)
				mongoSupport.addSolution(sol1)

				val solToRemove = solution.copy(userId = USER_ID, _id = new ObjectId)
				mongoSupport.addSolution(solToRemove)

				val sol3 = solution.copy(userId = USER_ID, _id = new ObjectId)
				mongoSupport.addSolution(sol3)

				val solutions2 = mongoSupport.getSolutions(USER_ID)
				solutions2.size should equal (3)

				mongoSupport.removeSolution(solToRemove)
				val solutions3 = mongoSupport.getSolutions(USER_ID)
				solutions3.size should equal (2)

				solutions3.contains(sol1) should equal(true)
				solutions3.contains(sol3) should equal(true)

			}
		}

		"Get operations" should {

			"get a record" in {
				val sol = simpleSolution.copy(userId = USER_ID, _id = new ObjectId)
				val id = mongoSupport.addSolution(sol)

				val solOpt = mongoSupport.getSolution(USER_ID, id.get)

				solOpt.isDefined should equal (true)

				solOpt.get.name should equal (sol.name)
				solOpt.get.depot should equal (sol.depot)
				solOpt.get._id should equal (sol._id)
				solOpt.get.stopsToLoad should equal (sol.stopsToLoad)

				solOpt.get.userId should equal (sol.userId)
				solOpt.get.trucks.head.depot should equal (sol.trucks.head.depot)
				solOpt.get.trucks.head.endTime should equal (sol.trucks.head.endTime)
				solOpt.get.trucks.head.startTime should equal (sol.trucks.head.startTime)
				solOpt.get.trucks.head.id should equal (sol.trucks.head.id)
				solOpt.get.trucks.head.maxWeight should equal (sol.trucks.head.maxWeight)
				solOpt.get.trucks.head.name should equal (sol.trucks.head.name)
				solOpt.get.trucks.head.stops.head.startTime should equal (sol.trucks.head.stops.head.startTime)
				solOpt.get.trucks.head.stops.head.endTime should equal (sol.trucks.head.stops.head.endTime)
				solOpt.get.trucks.head.userId should equal (sol.trucks.head.userId)
				solOpt.get.trucks.head.lm should equal (sol.trucks.head.lm)

			}

			"get all records" in {
				val solutions = mongoSupport.getSolutions(USER_ID)
				solutions.size should equal (0)

				mongoSupport.addSolution(solution.copy(userId = USER_ID, _id = new ObjectId))
				mongoSupport.addSolution(solution.copy(userId = USER_ID, _id = new ObjectId))
				mongoSupport.addSolution(solution.copy(userId = USER_ID, _id = new ObjectId))

				val solutions2 = mongoSupport.getSolutions(USER_ID)
				solutions2.size should equal (3)
			}
		}
	}
}