package solution

import com.tomliddle.entity.Stop
import com.tomliddle.solution.PointSeqUtils
import org.joda.time.{LocalTime, DateTime}
import org.scalatest.{BeforeAndAfterEach, Matchers, WordSpec}
import PointSeqUtils._
import com.tomliddle.solution.SeqUtils._

class SwapUtilitiesSpec extends WordSpec with Matchers with BeforeAndAfterEach with TestObjects {

	private val time = new LocalTime(0)
	private val testStops: IndexedSeq[Stop] = (0 to 9).map {id => stop.copy(id = Some(id))}

	"swap utilities" when {

		"swapping stops" should {

			"swap 2" in {
				val result = testStops.swap(0, 2, 1, false)

				result.size should equal (testStops.size)
				result.size should equal (result.distinct.size)
			}

			"swap 2 inverse" in {
				val result = testStops.swap(0, 2, 1, true)

				result.size should equal (testStops.size)
				result.size should equal (result.distinct.size)
			}

			"swap 3" in {
				val result = testStops.swap(0, 4, 4, true)

				result.size should equal (testStops.size)
				result.size should equal (result.distinct.size)

				testStops(0) should equal (result(7))
			}

			"swap 4" in {
				val result = testStops.swap(0, 5, 5, true)

				result.size should equal (testStops.size)
				result.size should equal (result.distinct.size)

				testStops(0) should equal (result(9))
				testStops(4) should equal (result(5))
			}

			"swap 5 from is after to" in {
				val result = testStops.swap(6, 4, 2, false)

				result.size should equal (testStops.size)
				result.size should equal (result.distinct.size)

				testStops(6) should equal (result(4))
				testStops(7) should equal (result(5))
			}

			"swap move 4 on" in {
				val result = testStops.swap(4, 5, 4, false)

				result.size should equal (testStops.size)
				result.size should equal (result.distinct.size)

				testStops(6) should equal (result(7))
				testStops(7) should equal (result(8))
			}

		}
		"take off stops" should {

			"swap 4" in {
				val result = testStops.takeOff(0, 5)

				result._1.size should equal (5)
				result._2.size should equal (5)

			}

		}
	}

}