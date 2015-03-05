package Solution

import org.joda.time.{Duration, DateTime}
import org.scalatest.{BeforeAndAfterEach, Matchers, WordSpec}

class LocationMatrixSpec extends WordSpec with Matchers with BeforeAndAfterEach with TestObjects {

	"LocationMatrix" when {

		"getting a time distance" should {

			"get the correct time distance" in {
				val distTime = truck.lm.getDistanceTime(truck.stops(0), truck.stops(1))

				distTime.distance should equal (BigDecimal(4))
				distTime.time should equal (new Duration(400))
			}
		}

		"getting the furthest stop" should  {

			"get the furthest stop" in {

				val stop = truck.lm.findFurthestStop(depot)

				stop should equal(stops.last)
			}

			"not get a depot and get the furthest stop" in {

				val stop = truck.lm.findFurthestStop(stops.last)

				stop should equal(stops(7))
			}
		}
	}
}