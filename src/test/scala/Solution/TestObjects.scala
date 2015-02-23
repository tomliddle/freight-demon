package Solution

import com.tomliddle.solution._
import org.joda.time.LocalTime


trait TestObjects {

	val location = Location(0, 0, "324234")
	val stop = Stop("1", location, time, time, BigDecimal(1), List(), 1)
	val time = new LocalTime(0)

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

	val stops: List[Stop] = (0 to 9).map {
		id => stop.copy(id = Some(id), location = locationList(id))
	}.toList
	val depot: Depot = Depot("Depot1", location, 1, Some(1))
	val lm = new LocationMatrix(stops, List(depot)) with SimpleTimeAndDistCalc

	val truck = Truck("Truck1", time, time, BigDecimal(100), depot, stops, lm, 1, Some(1))

	val solution = Solution("Solution", depot, stops, List(truck, truck, truck), 1)

}
