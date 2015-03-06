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
	//protected implicit def ordering[Duration]: Ordering[Duration] = Ordering.by(_.toString)

	lazy val totalWeight: BigDecimal = {
		stops.foldLeft(BigDecimal(0)) { (totalWeight: BigDecimal, stop: Stop) => totalWeight + stop.maxWeight}
			.setScale(2, RoundingMode.HALF_UP)
	}

	lazy val cost: BigDecimal = (distance * 1.2).setScale(2, RoundingMode.HALF_UP)

	lazy val distance = links.foldLeft(BigDecimal(0)) {(a: BigDecimal, b: Link) => a + b.travelDistanceTime.distance}

	lazy val time = links.foldLeft(new Duration(0)) {(a: Duration, b: Link) => a.plus(b.travelDistanceTime.time).plus(b.waitTime) }

	lazy val links = getLinks()


	def getMaxSwapSize() = stops.size / 2

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
		var truck: Truck = copy(stops = city :: stops).shuffleBySize(1)
		truck.isValid match {
			case true => (truck, None)
			case _ => (this, Some(city))
		}
	}

	/**
	 * @param stops to load
	 * @return ones it couldn't load
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

	private lazy val mean = getMean(stops)

	private def nextStopToLoad(stops: List[Stop]): Stop = {
		if (stops.size > 0) stops.minBy(stop => lm.getMetresDistance(stop, mean))
		else lm.findFurthestStop(depot).asInstanceOf[Stop]
	}

	// Shuffle algorithem
	def shuffle(): Truck = {
		logger.debug("Shuffling ------------------")
		var bestSol: Truck = this
		var currBest = bestSol.cost

		def doShuffle(groupSizeMin: Int, groupSizeMax: Int, solution: Truck): Truck = {
			require(groupSizeMax >= groupSizeMin)
			require(groupSizeMin > 0)
			logger.debug(s"Shuffle ${groupSizeMin} ${groupSizeMax} sol:${solution.stops.size}")

			(groupSizeMin to groupSizeMax).foreach {
				groupSize => {
					currBest = bestSol.cost
					bestSol = bestSol.shuffleBySize(groupSize)

					if (bestSol.cost < currBest) {
						logger.debug("New solution found: {}", bestSol.cost)
						doShuffle(1, getMaxSwapSize(), bestSol)
					}
				}
			}
			bestSol
		}

		if (getMaxSwapSize() > 1)
			doShuffle(1, getMaxSwapSize(), bestSol)
		else this
	}

	// This could be more efficient
	private def shuffleBySize(groupSize: Int): Truck = {
		require(groupSize > 0, "groupsize is 0")
		require(groupSize <= stops.size, "groupsize too big")

		def swap(groupSize: Int, invert: Boolean): Truck = {

			def doSwap(from: Int, groupSize: Int, invert: Boolean, solution: Truck): Truck = {
				(0 to stops.size - groupSize).map {
					to =>
						copy(stops = swapStops(stops, from, to, groupSize, invert))
				}.toList.sortWith(_.cost < _.cost).head
			}

			// We add this on so head of list always has one solution
			(this :: (0 to stops.size - groupSize).map {
				from => doSwap(from, groupSize, invert, this)
			}.toList.filter(truck => truck.isValid)).sortWith(_.cost < _.cost).head
		}

		val newSolution: Truck = swap(groupSize, false)
		val newSolution2: Truck = swap(groupSize, true)

		assert(newSolution.stops.size == stops.size)
		assert(newSolution2.stops.size == stops.size)
		assert(newSolution.stops.distinct.size == newSolution.stops.size, s"swapping ${groupSize} ${newSolution.stops.distinct.size} != ${newSolution.stops.size}")
		assert(newSolution2.stops.distinct.size == newSolution2.stops.size, s"swapping ${groupSize} ${newSolution2.stops.distinct.size} != ${newSolution2.stops.size}")

		List(newSolution, newSolution2).sortBy(_.cost).head
	}

	def isValid(): Boolean = weightValid() && timeValid() //&& specialCodesValid()

	def weightValid(): Boolean = totalWeight <= maxWeight

	def timeValid(): Boolean = time.isShorterThan(new Duration(5 * 1000 * 60 * 60))

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