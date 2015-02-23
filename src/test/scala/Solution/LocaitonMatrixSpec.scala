package Solution

import org.joda.time.{Duration, DateTime}
import org.scalatest.{BeforeAndAfterEach, Matchers, WordSpec}

class LocaitonMatrixSpec extends WordSpec with Matchers with BeforeAndAfterEach with TestObjects {

	"LocationMatrix" when {

		"getting a time distance" should {

			"get the correct time distance" in {
				val distTime = lm.getDistanceTime(stops(0).location, stops(1).location)

				distTime.distance should equal (BigDecimal(4))
				distTime.time should equal (new Duration(4000))
			}

		}
	}
}