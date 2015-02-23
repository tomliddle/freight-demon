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
				truck.getTotalWeight should equal (10)
			}

			"get correct cost" in {
				// TODO check
				truck.getCost() should equal (BigDecimal(21.26))
			}

			"get correct distance time" in {
				val distanceTime = truck.getDistanceTime()
				// TODO check
				distanceTime.distance.setScale(2, BigDecimal.RoundingMode.HALF_UP) should equal (BigDecimal(17.72))
				distanceTime.time should equal (new Duration((17715).toLong))

			}

			"get an optimised solution" in {
				val shuffledTruck = truck.shuffle()

				shuffledTruck.getCost() should be < (truck.getCost())

				// tODO more checks
			}

		}
	}
}