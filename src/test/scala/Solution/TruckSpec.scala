package com.bullionvault.bvbot.price

import com.tomliddle.solution._
import org.joda.time.{Duration, LocalTime}
import org.scalatest.{Matchers, BeforeAndAfterEach, WordSpec}

class TruckSpec extends WordSpec with Matchers with BeforeAndAfterEach {

	private val location = Location(0, 0, "324234")
	private val stop = Stop("1", location, time, time, BigDecimal(1), List(), 1)
	private val time = new LocalTime(0)

	val locationList = List(
		location.copy(x = 1, y = 5),
		location.copy(x = 1, y = 1),
		location.copy(x = 2, y = 3),
		location.copy(x = 0, y = 4),
		location.copy(x = 2, y = 4),
		location.copy(x = 2, y = 2),
		location.copy(x = 2, y = 2),
		location.copy(x = 1, y = 0),
		location.copy(x = 2, y = 0),
		location.copy(x = 10, y = 10)
	)

	private val stops: List[Stop] = (0 to 9).map {
		id => stop.copy(id = Some(id), location = locationList(id))
	}.toList
	private val depot: Depot = Depot("Depot1", location, 1, Some(1))
	private val lm = new LocationMatrix(stops, List(depot))

	private val truck = Truck("Truck1", time, time, BigDecimal(100), depot, stops, lm, 1, Some(1))

	"Truck" when {

		"Getting parameters" should {

			"get correct weight" in {
				truck.getTotalWeight should equal (10)
			}

			"get correct cost" in {
				// TODO check
				truck.getCost() should equal (BigDecimal(4480.61))
			}

			"get correct ditance time" in {
				val distanceTime = truck.getDistanceTime()
				// TODO Wrong
				truck.getDistanceTime() should equal (new DistanceTime(3671.32, new Duration(41488000)))
			}

			"get an optimised solution" in {
				val shuffledTruck = truck.shuffle()

				shuffledTruck.getCost() should be < (truck.getCost())

				// tODO more checks
			}

		}
	}
}