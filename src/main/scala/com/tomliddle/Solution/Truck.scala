package com.tomliddle.solution


import org.joda.time.{Duration, LocalTime}
import org.slf4j.LoggerFactory
import scala.math.BigDecimal.RoundingMode

case class Truck(
					name: String,
					startTime: LocalTime,
					endTime: LocalTime,
					maxWeight: BigDecimal,
					depot: Depot,
					stops: List[Stop],
					lm: LocationMatrix,
					userId: Int, id: Option[Int] = None)
	extends SwapUtilities with Mean with TruckLinks {

	protected final val logger = LoggerFactory.getLogger(this.getClass)

	def copyOption(stops: List[Stop]): Option[Truck] = {
		val truck = copy(stops = stops)
		if (truck.isValid) Some(truck)
		else None
	}

	lazy val totalWeight: BigDecimal = {
		stops.foldLeft(BigDecimal(0)) { (totalWeight: BigDecimal, stop: Stop) => totalWeight + stop.maxWeight}
			.setScale(2, RoundingMode.HALF_UP)
	}

	lazy val cost: Option[BigDecimal] = {
		distance match {
			case Some(distance) => Some((distance * 1.2).setScale(2, RoundingMode.HALF_UP))
			case None => None
		}
	}

	lazy val distance: Option[BigDecimal] = {
		links match {
			case Some(links) => Some(links.foldLeft(BigDecimal(0)) {(a: BigDecimal, b: Link) => a + b.travelDistanceTime.distance})
			case None => None
		}
	}

	lazy val time: Option[Duration] = links match {
		case Some(links) => Some(links.foldLeft(new Duration(0)) {(a: Duration, b: Link) => a.plus(b.travelDistanceTime.time).plus(b.waitTime)})
		case None => None
	}

	lazy val links: Option[List[Link]] = getLinks


	def getMaxSwapSize = stops.size / 2

	def unload(position: Int, size: Int): (Truck, List[Stop]) = {
		val newStops = takeOff(stops, position, size)
		(copy(stops = newStops._1), newStops._2)
	}

	/*def loadSpecialCodes(cities: List[Stop]): (Truck, List[Stop]) = {
		val citiesToLoad: (List[Stop], List[Stop]) = cities.partition(stop => stop.specialCodes.contains(name))
		var truckResult: (Truck, List[Stop]) = load(citiesToLoad._1)
		(truckResult._1, citiesToLoad._2 ++ truckResult._2)
	}*/

	def load(city: Stop): (Truck, Option[Stop]) = {
		copy(stops = city :: stops).shuffleBySize(1) match {
			case Some(truck) => (truck, None)
			case _ => (this, Some(city))
		}
	}

	/**
	 * @param stops to load
	 * @return ones it couldn't load, plus a valid truck
	 */
	def load(stops: List[Stop]): (Truck, List[Stop]) = {
		var currCity: Stop = nextStopToLoad(stops)
		var notLoadedCities = List[Stop]()
		var currTruck = this
		stops.sortBy(lm.distanceTimeBetween(_, currCity).distance).foreach {
			city => {
				currTruck.load(city) match {
					case (truck, Some(cityNotLoaded)) => notLoadedCities = cityNotLoaded :: notLoadedCities
					case (truck, None) => currTruck = truck
				}
			}
		}
		(currTruck, notLoadedCities)
	}

	private def nextStopToLoad(stops: List[Stop]): Stop = {
		getMean(stops) match {
			case Some(mean) => stops.minBy(stop => lm.getMetresDistance(stop, mean))
			case None => lm.findFurthestStop(depot).asInstanceOf[Stop]
		}
	}
	// TODO - should this require a valid truck or not????
	// Shuffle algorithem
	def shuffle: Truck = {
		logger.debug("Shuffling ------------------")
		//require(isValid, "cannot shuffle a non valid truck")
		var best: Truck = this

		def doShuffle(groupSizeMin: Int, groupSizeMax: Int, solution: Truck): Truck = {
			require(groupSizeMax >= groupSizeMin)
			require(groupSizeMin > 0)
			logger.debug(s"Shuffle ${groupSizeMin} ${groupSizeMax} sol:${solution.stops.size}")

			(groupSizeMin to groupSizeMax).map {
				groupSize => {
					best.shuffleBySize(groupSize).map {
						truck =>
							if (truck.isValid && truck.cost.get < best.cost.get) {
								best = truck
								logger.debug("New solution found: {}", best.cost.get)
								doShuffle(1, getMaxSwapSize, best)
							}
					}
				}
			}
			best
		}

		if (getMaxSwapSize > 1)
			doShuffle(1, getMaxSwapSize, best)
		else this
	}
	// TODO - should this require a valid truck or not????
	// This could be more efficient
	private def shuffleBySize(groupSize: Int): Option[Truck] = {
		require(groupSize > 0, "groupsize is 0")
		require(groupSize <= stops.size, "groupsize too big")

		def swap(groupSize: Int, invert: Boolean): Option[Truck] = {

			def doSwap(from: Int, groupSize: Int, invert: Boolean, solution: Truck): Option[Truck] = {
				(0 to stops.size - groupSize).map {
					to =>
						copyOption(stops = swapStops(stops, from, to, groupSize, invert))
				}.flatten.toList.sortWith(_.cost.get < _.cost.get).headOption
			}

			// We add this on so head of list always has one solution
			(Some(this) :: (0 to stops.size - groupSize).map {
				from => doSwap(from, groupSize, invert, this)
			}.toList).flatten.filter(truck => truck.cost.isDefined).sortWith(_.cost.get < _.cost.get).headOption
		}

		val newSolution: Option[Truck] = swap(groupSize, false)
		val newSolution2: Option[Truck] = swap(groupSize, true)

		List(Some(this), newSolution, newSolution2).flatten.sortBy(_.cost).headOption
	}

	def isValid: Boolean = weightValid && timeValid //&& specialCodesValid()

	def weightValid: Boolean = totalWeight <= maxWeight

	def timeValid: Boolean = time.isDefined //&& time.get.isShorterThan(new Duration(5 * 1000 * 60 * 60))

	/*private def specialCodesValid(): Boolean = {
		stops.foldLeft(true) {
			(valid: Boolean, stop: Stop) => {
				if (stop.specialCodes.size > 0 && !stop.specialCodes.contains(name))
					false
				else
					valid && true
			}
		}
	}*/



}