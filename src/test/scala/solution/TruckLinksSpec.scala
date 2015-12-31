package solution

import com.tomliddle.entity.{Link, DistanceTime}
import com.tomliddle.exception.RouteInvalidException
import com.tomliddle.solution._
import org.joda.time.Duration
import org.scalatest.{TryValues, Matchers, BeforeAndAfterEach, WordSpec}

import scala.util.Failure


class TruckLinksSpec extends WordSpec with Matchers with BeforeAndAfterEach with TestObjects with TryValues {


	"TruckLinks" when {

		"calculating a normal route" should {

			"get correct links" in {
				val links = straightLineTruck.getLinks.get

				links.size should equal(straightLineTruck.stops.size + 1)

				val linkDistanceTime = links.foldLeft(new DistanceTime()) {(a: DistanceTime, b: Link) => a + b.travelDT}

				// This is 200km
				linkDistanceTime.distance should equal(200 * 1000)

				// This is 20,000s which is 5h 55 mins
				linkDistanceTime.time should equal(new Duration(20 * 1000 * 1000))
			}

			"get correct links with start and end times" in {
				val links = straightLineTruckWithTimes.getLinks.get

				links.size should equal (straightLineTruckWithTimes.stops.size + 1)

				val linkDistanceTime = links.foldLeft(new Link()) {(a: Link, b: Link) => a + b}

				// 200km
				linkDistanceTime.travelDT.distance should equal(200 * 1000)

				// This is 20,000s which is 5h 55 mins
				linkDistanceTime.travelDT.time should equal(new Duration(20 * 1000 * 1000))

				// TODO check value
				linkDistanceTime.elapsedTime should equal(new Duration(37200 * 1000))
			}

			"should be a failure on invalid route" in {
					val links = invalidTruck.getLinks

					links.failure.exception.getClass should equal(classOf[RouteInvalidException])
			}

		}
	}
}


