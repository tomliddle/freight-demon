package Solution

import org.joda.time.DateTime
import org.scalatest.{BeforeAndAfterEach, Matchers, WordSpec}

class DatabaseSupportSpec extends WordSpec with Matchers with BeforeAndAfterEach {

	val dateTime = DateTime.now
	val tenMinutesAgo = DateTime.now.minusMinutes(10)
	val tolerance = 0.001

	"Database support" when {

		"getting a truck" should {

			"get the correct location" in {
				//ph.confidence.toDouble should be (0.287 +- tolerance)
				//ph.confidence.toDouble should equal (0)
				//ph.purge.confidence.toDouble should be (0.221 +- tolerance)
			}

		}
	}
}