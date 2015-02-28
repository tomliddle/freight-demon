package Solution

import com.tomliddle.solution._
import org.joda.time.LocalTime


trait TestObjects {


	val location = Location(0, 0, "324234")
	val startTime = new LocalTime(0).withHourOfDay(9)
	val endTime = new LocalTime(0).withHourOfDay(19)
	val stop = Stop("1", location, startTime, endTime, BigDecimal(1), List(), 1)
	val depot: Depot = Depot("Depot1", location, 1, Some(1))


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

	val straightLineLocations = List(
		location.copy(x = 10000, y = 0),
		location.copy(x = 20000, y = 0),
		location.copy(x = 30000, y = 0),
		location.copy(x = 40000, y = 0),
		location.copy(x = 50000, y = 0),
		location.copy(x = 60000, y = 0),
		location.copy(x = 70000, y = 0),
		location.copy(x = 80000, y = 0),
		location.copy(x = 90000, y = 0),
		location.copy(x = 100000, y = 0)
	)

	val stops: List[Stop] = (0 to locationList.size - 1).map {
		id => stop.copy(id = Some(id), location = locationList(id))
	}.toList

	val truck: Truck = {
		val lm = new LocationMatrix(stops, List(depot)) with SimpleTimeAndDistCalc
		Truck("Truck1", startTime, endTime, BigDecimal(100), depot, stops, lm, 1, Some(1))
	}

	// Straight line to test distance
	val straightLineTruck: Truck = {
		val stops2: List[Stop] = (0 to straightLineLocations.size - 1).map {
			id => stop.copy(id = Some(id), location = straightLineLocations(id))
		}.toList
	
		val lm2 = new LocationMatrix(stops2, List(depot)) with SimpleTimeAndDistCalc
		Truck("Truck2", startTime, endTime, BigDecimal(100), depot, stops2, lm2, 1, Some(1))
	}

	// With delivery times to test start and end times
	val straightLineTruckWithTimes: Truck = {
		val stops2: List[Stop] = (0 to straightLineLocations.size - 1).map {
			id => stop.copy(
				id = Some(id),
				location = straightLineLocations(id),
				startTime = new LocalTime(0).withHourOfDay(id + 9),
				endTime = new LocalTime(0).withHourOfDay(id + 10)
			)
		}.toList

		val lm2 = new LocationMatrix(stops2, List(depot)) with SimpleTimeAndDistCalc
		Truck("Truck2", startTime, endTime, BigDecimal(100), depot, stops2, lm2, 1, Some(1))
	}

	// With delivery times to test start and end times
	val invalidTruck: Truck = {
		val stops2: List[Stop] = (0 to straightLineLocations.size - 1).map {
			id => stop.copy(
				id = Some(id),
				location = straightLineLocations(id),
				startTime = new LocalTime(0).withHourOfDay(11),
				endTime = new LocalTime(0).withHourOfDay(11)
			)
		}.toList

		val lm2 = new LocationMatrix(stops2, List(depot)) with SimpleTimeAndDistCalc
		Truck("Truck2", startTime, endTime, BigDecimal(100), depot, stops2, lm2, 1, Some(1))
	}

	val solution = Solution("Solution", depot, truck.stops, List(truck, truck, truck), 1)
}
