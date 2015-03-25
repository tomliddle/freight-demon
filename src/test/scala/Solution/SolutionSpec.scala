package Solution

import com.tomliddle.solution.{Depot, Solution, Stop}
import org.scalatest.{BeforeAndAfterEach, Matchers, WordSpec}

class SolutionSpec extends WordSpec with Matchers with BeforeAndAfterEach with TestObjects {


	"Solution" when {

		"calculating the cost" should {

			"calculate the correct cost" in {
				solution.cost should equal (truck.cost.get * 3)
			}

			"calculate the correct distance " in {
				solution.distanceTime.distance should equal (truck.distance.get * 3)
			}

			"calculate the correct time " in {
				solution.distanceTime.time should equal (truck.time.get.plus(truck.time.get).plus(truck.time.get))
			}

			"calculate the correct shuffled cost" in {
				val shuffledSolution = solution.shuffle
				val cost = shuffledSolution.cost

				cost should be < (solution.cost)

				cost should equal (truck.shuffle.cost.get * 3)

			}

			"have the right stops" in {
				val shuffledSolution = solution.shuffle

				solution.loadedCities.size should equal (30)
				shuffledSolution.loadedCities.size should equal (30)

			}

			"calculate max swap size" in {
				solution.maxSolutionSwapSize should equal (5)
			}

			"get loaded cities" in {
				solution.loadedCities.size should equal (30)
			}

			"swap 4 " in {
				val stops = List(
					Stop("1", -0.09, 51.55, "234234", startTime, endTime, BigDecimal(1), List(), 1),
					Stop("1", -0.36, 51.58, "234234", startTime, endTime, BigDecimal(1), List(), 1),
					Stop("1", 0.11, 52.25, "234234", startTime, endTime, BigDecimal(1), List(), 1),
					Stop("1", -0.3, 51.47, "234234", startTime, endTime, BigDecimal(1), List(), 1)
				)

				val depot: Depot = Depot("Depot1", 0 ,0 , "", 1, Some(1))
				val solution = Solution("Solution", depot, truck.stops, List(truck, truck, truck), 1)
				val shuffledSolution = solution.shuffle

				solution.loadedCities.size should equal (30)
			}

		}
	}
}