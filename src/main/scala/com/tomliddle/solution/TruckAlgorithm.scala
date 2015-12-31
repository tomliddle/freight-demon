package com.tomliddle.solution

import com.tomliddle.entity.{Mean, LocationMatrix, Stop}
import com.tomliddle.util.Logging

trait TruckAlgorithm extends SwapUtilities with Mean with Logging {

	this: Truck =>

	def load(city: Stop): (Truck, Option[Stop]) = {
		copy(stops = city :: stops).shuffleBySize(1) match {
			case Some(truck) => (truck, None)
			case None => (this, Some(city))
		}
	}

	/**
		* @param stops to load
		* @return ones it couldn't load, plus a valid truck
		*/
	def load(stops: List[Stop]): (Truck, List[Stop]) = {
		val currStop: Stop = nextStopToLoad(stops)

		stops.sortBy(lm.distanceTimeBetween(_, currStop).distance).foldLeft(this, List[Stop]()) {
			case ((truck: Truck, stopList: List[Stop]), currStop: Stop) =>
				truck.load(currStop) match {
					// Stop wasn't loaded so add the stop to the not loaded ones
					case (truck, Some(stop)) => (truck, stop :: stopList)
					case (truck, None) => (truck, stopList)
				}
		}
	}

	def unload(position: Int, size: Int): (Truck, List[Stop]) = {
		val newStops = takeOff(stops, position, size)
		(copy(stops = newStops._1), newStops._2)
	}


	private def nextStopToLoad(stops: List[Stop]): Stop = {
		getMean(stops) match {
			case Some(mean) => stops.minBy(stop => lm.getMetresDistance(stop, mean))
			case None => lm.findFurthestStop(depot)
		}
	}

	// Shuffle algorithm
	def shuffle: Truck = {
		logger.debug("Shuffling ------------------")
		logger.debug(s"Shuffle ${1} ${getMaxSwapSize} sol:${this.stops.size}")

		// We add this to begining of the list to simplify finding minimum for an empty list.
		(1 to getMaxSwapSize).map {
			groupSize => {
				shuffleBySize(groupSize).foldLeft(this) {
					(best, currTruck) =>
						if (currTruck.isValid && currTruck.cost.get < best.cost.get) {
							logger.debug(s"New solution found: ${currTruck.cost.get}")
							currTruck
						}
						else best
				}
			}
		}.minBy(_.cost)
	}

	/**
		* Doesn't require a valid truck to start with
		* // This could be more efficient
		* @param groupSize
		* @return
		*/
	protected def shuffleBySize(groupSize: Int): Option[Truck] = {
		require(groupSize > 0, "groupsize is 0")
		require(groupSize <= stops.size, "groupsize too big")

		def swap(groupSize: Int, invert: Boolean): Option[Truck] = {

			def doSwap(from: Int, groupSize: Int, invert: Boolean, solution: Truck): Option[Truck] = {
				(0 to stops.size - groupSize).flatMap {
					to =>
						val truckCopy = copy(stops = swapStops(stops, from, to, groupSize, invert))
						if (truckCopy.isValid) Some(truckCopy)
						else None
				}.sortWith(_.cost.get < _.cost.get).headOption
			}

			(0 to stops.size - groupSize).flatMap {
				from => doSwap(from, groupSize, invert, this)
			}.filter(truck => truck.cost.isDefined).sortWith(_.cost.get < _.cost.get).headOption
		}

		// We add this on so head of list always have the current solution
		List(Some(this), swap(groupSize, false), swap(groupSize, true)).flatten.sortBy(_.cost).headOption
	}
}