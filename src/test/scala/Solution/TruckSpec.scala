package com.bullionvault.bvbot.price

import java.math.{RoundingMode, MathContext}

import Solution.TestObjects
import com.tomliddle.solution._
import org.joda.time.{Duration, LocalTime}
import org.scalatest.{Matchers, BeforeAndAfterEach, WordSpec}


class TruckSpec extends WordSpec with Matchers with BeforeAndAfterEach with TestObjects {


	"Truck" when {

		"Getting parameters" should {

			"get correct weight" in {
				truck.totalWeight should equal (10)
			}

			"get correct cost" in {
				// TODO check
				truck.cost should equal (BigDecimal(21.26))
			}

			"get correct distance time" in {
				// TODO check
				truck.distance.setScale(2, BigDecimal.RoundingMode.HALF_UP) should equal (BigDecimal(17.72))
				truck.time should equal (new Duration((17715).toLong))

			}

			"get an optimised solution" in {
				val shuffledTruck = truck.shuffle()

				shuffledTruck.cost should be < (truck.cost)

				// tODO more checks
			}

		}
	}
}