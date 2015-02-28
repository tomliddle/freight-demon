package Solution

import com.tomliddle.solution._
import org.joda.time.Duration
import org.scalatest.{Matchers, BeforeAndAfterEach, WordSpec}


class TruckLinksSpec extends WordSpec with Matchers with BeforeAndAfterEach with TestObjects {


	"TruckLinks" when {

		"calculating a normal route" should {

			"get correct links" in {
				val links = straightLineTruck.getLinks()

				links.size should equal (straightLineTruck.stops.size + 1)

				val linkDistanceTime = links.foldLeft(new DistanceTime()) {(a: DistanceTime, b: Link) => a + b.travelDistanceTime}

				// This is 200km
				linkDistanceTime.distance should equal (200 * 1000)

				// This is 20,000s which is 5h 55 mins
				linkDistanceTime.time should equal (new Duration(20 * 1000 * 1000))
			}

			"get correct links with start and end times" in {
				val links = straightLineTruckWithTimes.getLinks()

				links.size should equal (straightLineTruckWithTimes.stops.size + 1)

				val linkDistanceTime = links.foldLeft(new Link()) {(a: Link, b: Link) => a + b}

				// 200km
				linkDistanceTime.travelDistanceTime.distance should equal (200 * 1000)

				// This is 20,000s which is 5h 55 mins
				linkDistanceTime.travelDistanceTime.time should equal (new Duration(20 * 1000 * 1000))

				// TODO check value
				linkDistanceTime.elapsedTime should equal (new Duration(37200 * 1000))
			}

			"throw exception with an invalid route" in {

				try {
					val links = invalidTruck.getLinks()
					fail("Should have thrown an exception")
				}
				catch {
					case _: RouteInvalidException => // Expected, so continue
				}




			}

		}
	}
}


