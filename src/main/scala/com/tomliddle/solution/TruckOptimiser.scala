package com.tomliddle.solution

import com.tomliddle.entity.Stop
import com.tomliddle.util.Logging
import PointListUtils._
import ListUtils._

/**
	* Adds functionality to Truck to optimise, load and unload
	*/
trait TruckOptimiser extends Logging {
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
					case (newTruck, Some(stop)) => (newTruck, stop :: stopList)
					case (newTruck, None) => (newTruck, stopList)
				}
		}
	}

	def unload(position: Int, size: Int): (Truck, List[Stop]) = {
		stops.takeOff(position, size) match {
			case (loaded, unloaded) => (copy(stops = loaded), unloaded)
		}
	}


	private def nextStopToLoad(stops: List[Stop]): Stop = {
		stops.mean match {
			case Some(mean) => stops.minBy(stop => lm.getMetresDistance(stop, mean))
			case None => lm.findFurthestStop(depot)
		}
	}

	// Shuffle algorithm
	def shuffle: Truck = {
		logg.debug("Shuffling ------------------")
		logg.debug(s"Shuffle $getMaxSwapSize sol:${stops.size}")

		// We add this to begining of the list to simplify finding minimum for an empty list.
		(1 to getMaxSwapSize).map {
			groupSize => {
				shuffleBySize(groupSize).foldLeft(this) {
					(best, currTruck) =>
						if (currTruck.isValid && currTruck.cost.get < best.cost.get) {
							logg.debug(s"New solution found: ${currTruck.cost.get}")
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
						val truckCopy = copy(stops = stops.swap(from, to, groupSize, invert))
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

	// Iterates through two trucks trying to swap all points
	def swapBetween(swapTruck: Truck) : (Truck, Truck) = {

		def doSwapBetween(truck1: Truck, truck2: Truck, swapSize: Int) : (Truck, Truck) = {

			(0 to truck1.stops.size - swapSize).foldLeft(truck1, truck2) {
				case (_, truck1Pos) => {
					(0 to truck2.stops.size - swapSize).foldLeft(truck1, truck2){
						case ((bestTruck1: Truck, bestTruck2: Truck), truck2Pos: Int) => {
							val (t1Unloaded, t1stops) = truck1.unload(truck1Pos, swapSize)
							val (t2Unloaded, t2stops) = truck2.unload(truck2Pos, swapSize)
							val (t1New, t1NewStops) = t1Unloaded.load(t2stops)
							val (t2New, t2NewStops)  = t2Unloaded.load(t1stops)

							// If trucks fully reloaded, check the cost.
							if (t1NewStops.size == 0 && t2NewStops.size == 0 &&
									t1New.cost.get + t2New.cost.get < bestTruck1.cost.get + bestTruck2.cost.get)
								(t1New, t2New)
							else (bestTruck1, bestTruck2)
						}
					}
				}
			}
		}

		(1 to getMaxSwapSize).map{swapSize => doSwapBetween(this, swapTruck, swapSize)}
			.minBy(truckTup => truckTup._1.cost.get + truckTup._2.cost.get)
	}

}