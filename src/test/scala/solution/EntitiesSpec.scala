package solution


import com.tomliddle.entity.{Stop, Point}
import org.scalatest.{BeforeAndAfterEach, Matchers, WordSpec}
import com.tomliddle.solution.PointListUtils._

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
				val mean = List[Stop]().mean

				mean.isDefined should equal (false)
			}

			"with 10 stops" in {
				val mean = List[Point](point, point2, point3, point4).mean

				mean.get.x should equal (4.5)
				mean.get.y should equal (16.375)
			}

		}
	}
}
