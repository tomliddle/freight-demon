package Solution


import com.tomliddle.solution.{Mean, Point}
import org.scalatest.{BeforeAndAfterEach, Matchers, WordSpec}

class EntitiesSpec extends WordSpec with Matchers with BeforeAndAfterEach {

	val point = new Point(5, 5, "")
	val point2 = new Point(4, 10, "")
	val point3 = new Point(7, 3, "")
	val point4 = new Point(2, 47.5, "")

	"Entities" when {

		"adding a point" should {
			"get the correct point" in {
				val result = point + point2 + point3 + point4

				result.x should equal (18)
				result.y should equal (65.5)
			}
		}

		"dividing a point" should {
			"get the correct result" in {
				val result = point + point2 + point3 + point4

				val div = result / 4

				div.x should equal (18.0 / 4.0)
				div.y should equal (65.5 / 4.0)
			}
		}

		"can calculate the mean " should {

			"with 0 stops" in {
				val obj = new Object with Mean
				val mean = obj.getMean(List())

				mean.isDefined should equal (false)
			}

			"with 10 stops" in {
				val obj = new Object with Mean
				val mean = obj.getMean(List(point, point2, point3, point4))

				mean.get.x should equal (4.5)
				mean.get.y should equal (16.375)
			}

		}
	}
}
