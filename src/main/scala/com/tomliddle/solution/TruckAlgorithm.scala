package com.tomliddle.solution

import com.tomliddle.entity.{Mean, LocationMatrix, Stop}
import com.tomliddle.util.Logging

trait TruckAlgorithm extends SwapUtilities with Mean with Logging {

	def load(truck: Truck, city: Stop): (Truck, Option[Stop]) = {
		shuffleBySize(truck.copy(stops = city :: truck.stops), 1) match {
			case Some(truck) => (truck, None)
			case None => (truck, Some(city))
		}
	}

	/**
		* @param stops to load
		* @return ones it couldn't load, plus a valid truck
		*/
	def load(truck: Truck, stops: List[Stop], lm: LocationMatrix): (Truck, List[Stop]) = {
		var currCity: Stop = nextStopToLoad(truck, stops, lm)
		var notLoadedCities = List[Stop]()
		var currTruck = truck
		stops.sortBy(lm.distanceTimeBetween(_, currCity).distance).foreach {
			city => {
				load(currTruck, city) match {
					case (truck, Some(cityNotLoaded)) => notLoadedCities = cityNotLoaded :: notLoadedCities
					case (truck, None) => currTruck = truck
				}
			}
		}
		(currTruck, notLoadedCities)
	}

	def unload(truck: Truck, position: Int, size: Int): (Truck, List[Stop]) = {
		val newStops = takeOff(truck.stops, position, size)
		(truck.copy(stops = newStops._1), newStops._2)
	}


	private def nextStopToLoad(truck: Truck, stops: List[Stop], lm: LocationMatrix): Stop = {
		getMean(stops) match {
			case Some(mean) => stops.minBy(stop => lm.getMetresDistance(stop, mean))
			case None => lm.findFurthestStop(truck.depot)
		}
	}

	// Shuffle algorithm
	def shuffle(truck: Truck): Truck = {
		logger.debug("Shuffling ------------------")
		var best: Truck = truck

		//@tailrec
		def doShuffle(groupSizeMin: Int, groupSizeMax: Int): Truck = {
			//require(groupSizeMax >= groupSizeMin)
			require(groupSizeMin > 0)
			logger.debug(s"Shuffle ${groupSizeMin} ${groupSizeMax} sol:${best.stops.size}")

			(groupSizeMin to groupSizeMax).map {
				groupSize => {
					best.shuffleBySize(truck, groupSize).map {
						truck =>
							if (truck.isValid && truck.cost.get < best.cost.get) {
								best = truck
								logger.debug(s"New solution found: ${best.cost.get}")
								doShuffle(1, truck.getMaxSwapSize)
							}
					}
				}
			}
			best
		}

		if (truck.getMaxSwapSize > 1) {
			//We start from swapping 1 to max, then from max to 1 or it doesn't optimise properly.
			best = doShuffle(1, truck.getMaxSwapSize)
			doShuffle(truck.getMaxSwapSize, 1)
		}
		else truck
	}

	/**
		* Doesn't require a valid truck to start with
		* // This could be more efficient
		* @param groupSize
		* @return
		*/
	protected def shuffleBySize(truck: Truck, groupSize: Int): Option[Truck] = {
		require(groupSize > 0, "groupsize is 0")
		require(groupSize <= truck.stops.size, "groupsize too big")

		def swap(groupSize: Int, invert: Boolean): Option[Truck] = {

			def copyOption(stops: List[Stop]): Option[Truck] = {
				val truckCopy = truck.copy(stops = stops)
				if (truckCopy.isValid) Some(truckCopy)
				else None
			}

			def doSwap(from: Int, groupSize: Int, invert: Boolean, solution: Truck): Option[Truck] = {
				(0 to truck.stops.size - groupSize).map {
					to =>
						copyOption(stops = swapStops(truck.stops, from, to, groupSize, invert))
				}.flatten.toList.sortWith(_.cost.get < _.cost.get).headOption
			}

			// We add this on so head of list always has one solution
			(Some(truck) :: (0 to truck.stops.size - groupSize).map {
				from => doSwap(from, groupSize, invert, truck)
			}.toList).flatten.filter(truck => truck.cost.isDefined).sortWith(_.cost.get < _.cost.get).headOption
		}

		val newSolution: Option[Truck] = swap(groupSize, false)
		val newSolution2: Option[Truck] = swap(groupSize, true)

		List(Some(truck), newSolution, newSolution2).flatten.sortBy(_.cost).headOption
	}


}