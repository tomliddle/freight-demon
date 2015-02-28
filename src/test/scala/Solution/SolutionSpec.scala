package Solution

import org.scalatest.{BeforeAndAfterEach, Matchers, WordSpec}

class SolutionSpec extends WordSpec with Matchers with BeforeAndAfterEach with TestObjects {


	"Solution" when {

		"calculating the cost" should {

			"calculate the correct cost" in {
				solution.cost should equal (truck.cost * 3)
			}

			"calculate the correct distance " in {
				solution.distanceTime.distance should equal (truck.distance * 3)
			}

			"calculate the correct time " in {
				solution.distanceTime.time should equal (truck.time.plus(truck.time).plus(truck.time))
			}

			"calculate the correct shuffled cost" in {
				val shuffledSolution = solution.shuffle
				val cost = shuffledSolution.cost

				cost should be < (solution.cost)

				cost should equal (truck.shuffle().cost * 3)

			}

			"calculate max swap size" in {
				solution.maxSolutionSwapSize should equal (5)
			}

			"get loaded cities" in {
				solution.loadedCities.size should equal (30)
			}

		}
	}
}