package com.tomliddle.solution


import com.tomliddle.entity.{LocationMatrix, Stop, Depot, Link}
import com.tomliddle.util.Logging
import org.joda.time.{Duration, LocalTime}
import scala.math.BigDecimal.RoundingMode

/**
	* A truck with information about its route.
	* @param name
	* @param startTime
	* @param endTime
	* @param maxWeight
	* @param depot
	* @param stops
	* @param lm
	* @param userId
	* @param id
	*/
case class Truck(
										name: String,
										startTime: LocalTime,
										endTime: LocalTime,
										maxWeight: BigDecimal,
										depot: Depot,
										stops: Seq[Stop],
										lm: LocationMatrix,
										userId: Int,
										id: Option[Int] = None)
	extends TruckOptimiser with Ordered[Truck] with TruckLinks with Logging  {

	require(stops.size == stops.distinct.size, "Stops aren't distinct")

		override def compare(truck: Truck): Int = {
			if (cost < truck.cost) -1
			else if (cost > truck.cost) 1
			else 0
		}

	lazy val totalWeight: BigDecimal = {
		stops.foldLeft(BigDecimal(0)) { (totalWeight, link) => totalWeight + link.maxWeight }
				.setScale(2, RoundingMode.HALF_UP)
	}

	// For now we use the distance * 1.2 to represent cost.
	lazy val cost: BigDecimal = (distance * 1.2).setScale(2, RoundingMode.HALF_UP)

	// The max number of stops that should be swapped during optimisation
	lazy val getMaxSwapSize = stops.size / 2

	// TODO this should be part of the truck not generated every time.
	// There may be circumstances where the links don't need to be re-calculated
	// LM shouldn't be passed in to the truck either as this is part of the solution object.
	lazy val (links, valid) = getLinks

	lazy val distance: BigDecimal = {
			links.foldLeft(BigDecimal(0)) { (a: BigDecimal, b: Link) => a + b.travelDT.distance }
	}

	// The route time
	lazy val time: Duration = {
		links.foldLeft(new Duration(0)) { (a: Duration, b: Link) => a.plus(b.travelDT.time).plus(b.waitTime) }
	}

	lazy val isValid: Boolean = weightValid && valid

	lazy val weightValid: Boolean = totalWeight <= maxWeight

}


