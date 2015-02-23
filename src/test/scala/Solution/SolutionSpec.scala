package Solution

import org.scalatest.{BeforeAndAfterEach, Matchers, WordSpec}

class SolutionSpec extends WordSpec with Matchers with BeforeAndAfterEach with TestObjects {


	"Solution" when {

		"calculating the cost" should {

			"calculate the correct cost" in {
				solution.getCost() should equal (truck.getCost() * 3)
			}

			"calculate the correct distance " in {
				solution.getDistanceTime().distance should equal (truck.getDistanceTime().distance * 3)
			}

			"calculate the correct time " in {
				solution.getDistanceTime().time should equal (truck.getDistanceTime().time.multipliedBy(3))
			}

			"calculate the correct shuffled cost" in {
				val shuffledSolution = solution.shuffle
				val cost = shuffledSolution.getCost()

				cost should be < (solution.getCost())

				cost should equal (truck.shuffle().getCost() * 3)

			}

			"calculate max swap size" in {
				solution.getMaxSolutionSwapSize() should equal (5)
			}

			"get loaded cities" in {
				solution.getLoadedCities().size should equal (30)
			}

		}
	}
}