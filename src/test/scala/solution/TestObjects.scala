package solution

import com.tomliddle.entity.{LocationMatrix, Stop, Depot}
import com.tomliddle.solution._
import com.tomliddle.solution.timeanddistance.SimpleTimeAndDistCalc
import org.joda.time.LocalTime


trait TestObjects {


	val startTime = new LocalTime(0).withHourOfDay(9)
	val endTime = new LocalTime(0).withHourOfDay(19)
	val stop = Stop("1", 0, 0, "234234", startTime, endTime, BigDecimal(1), 1)
	val depot: Depot = Depot("Depot1", 0 ,0 , "", 1, Some(1))

	private val locations = IndexedSeq(
		stop.copy(x = 1, y = 5),
		stop.copy(x = 1, y = 1),
		stop.copy(x = 2, y = 3),
		stop.copy(x = 0, y = 4),
		stop.copy(x = 2, y = 4),
		stop.copy(x = 2, y = 2),
		stop.copy(x = 2, y = 2),
		stop.copy(x = 1, y = 0),
		stop.copy(x = 2, y = 0),
		stop.copy(x = 10, y = 10)
	)

	private val straightLineStops = IndexedSeq(
		stop.copy(x = 10000, y = 0),
		stop.copy(x = 20000, y = 0),
		stop.copy(x = 30000, y = 0),
		stop.copy(x = 40000, y = 0),
		stop.copy(x = 50000, y = 0),
		stop.copy(x = 60000, y = 0),
		stop.copy(x = 70000, y = 0),
		stop.copy(x = 80000, y = 0),
		stop.copy(x = 90000, y = 0),
		stop.copy(x = 100000, y = 0)
	)

	val stops = locations.indices.map {
		stopId => locations(stopId).copy(id = Some(stopId))
	}

	val lm = new LocationMatrix(stops, IndexedSeq(depot)) with SimpleTimeAndDistCalc
	val truck = Truck("Truck1", startTime, endTime, BigDecimal(100), depot, stops, lm, 1, Some(1))
	val truck2 = Truck("Truck2", startTime, endTime, BigDecimal(100), depot, straightLineStops, lm, 1, Some(1))

	// Straight line to test distance
	val stops2 = straightLineStops.indices.map {
		id => straightLineStops(id).copy(id = Some(id))
	}
	val lm2 = new LocationMatrix(stops2 , IndexedSeq(depot)) with SimpleTimeAndDistCalc
	val straightLineTruck: Truck = Truck("Truck2", startTime, endTime, BigDecimal(100), depot, stops2, lm2, 1, Some(1))


	// With delivery times to test start and end times
	val straightLineTruckWithTimes: Truck = {
		val stops2: IndexedSeq[Stop] = straightLineStops.indices.map {
			id => straightLineStops(id).copy(
				id = Some(id),
				startTime = new LocalTime(0).withHourOfDay(id + 9),
				endTime = new LocalTime(0).withHourOfDay(id + 10)
			)
		}

		val lm2 = new LocationMatrix(stops2, IndexedSeq(depot)) with SimpleTimeAndDistCalc
		Truck("Truck2", startTime, endTime, BigDecimal(100), depot, stops2, lm2, 1, Some(1))
	}

	// With delivery times to test start and end times
	val invalidTruck: Truck = {
		val stops2: IndexedSeq[Stop] = straightLineStops.indices.map {
			id => straightLineStops(id).copy(
				id = Some(id),
				startTime = new LocalTime(0).withHourOfDay(11),
				endTime = new LocalTime(0).withHourOfDay(11)
			)
		}

		val lm2 = new LocationMatrix(stops2, Seq(depot)) with SimpleTimeAndDistCalc
		Truck("Truck2", startTime, endTime, BigDecimal(100), depot, stops2, lm2, 1, Some(1))
	}

	val solution = Solution("solution", depot, truck.stops, IndexedSeq(truck), lm2, 1)

	val simpleSolution = Solution("solution", depot, IndexedSeq(), IndexedSeq(truck), lm2, 1)
}
