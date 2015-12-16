package solution

import com.tomliddle.entity.Point
import com.tomliddle.solution.timeanddistance.LatLongTimeAndDistCalc
import org.joda.time.Duration
import org.scalatest.{BeforeAndAfterEach, Matchers, WordSpec}

class TimeAndDistanceCalcSpec extends WordSpec with Matchers with BeforeAndAfterEach {

	val point1 = new Point(0, 52.1, "")
	val point2 = new Point(1, 53, "")
	val timeAndDistCalc = new Object with LatLongTimeAndDistCalc

	"TimeAndDistCalcSpec" when {

		"calculating getMetresDistance" should {

			"calculate distance between " in {
				val timeAndDist = timeAndDistCalc.getDistanceTime(point1, point2)
				//timeAndDist.time should equal (new Duration(120694L))
				timeAndDist.distance should be (BigDecimal(120694.02083914289) +- 100)
			}

		}
	}
}