package com.tomliddle.solution


import org.joda.time.{LocalTime, Duration}
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
	extends SwapUtilities with Mean {

	private final val logger = LoggerFactory.getLogger(this.getClass)

	def getTotalWeight(): BigDecimal = {
		stops.foldLeft(BigDecimal(0)) { (totalWeight: BigDecimal, stop: Stop) => totalWeight + stop.maxWeight}
			.setScale(2, RoundingMode.HALF_UP)
	}

	def getCost(): BigDecimal = (getDistanceTime().distance * 1.2).setScale(2, RoundingMode.HALF_UP)

	def getDistanceTime(): DistanceTime = {
		var distTimeToFirstStop =
			if (stops.size > 0) lm.distanceTimeBetween(depot, stops(0))
			else new DistanceTime()

		if (stops.size > 1)
			stops.sliding(2).map {
				(currCities: List[Stop]) =>
					lm.distanceTimeBetween(currCities(0), currCities(1))
			}.foldLeft(distTimeToFirstStop) { (a: DistanceTime, b: DistanceTime) => a + b}
		else distTimeToFirstStop
	}

	// TODO use this
	private def calcNextDuration(stop1: Stop, stop2: Stop, currEarliestStart: LocalTime, currLatestStart: LocalTime, currJourneyTime: Duration) = {

		var earliestStartTime = stop1.startTime.minus(currJourneyTime.toPeriod)
		if (earliestStartTime.isBefore(startTime))
			earliestStartTime = startTime

		var latestStartTime = stop2.endTime.minus((currJourneyTime.toPeriod))
		if (latestStartTime.isAfter(endTime))
			latestStartTime = endTime.minus(currJourneyTime.toPeriod)
	}


	def getMaxSwapSize() = this.stops.size / 2

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
		stops.sortBy( lm.distanceBetween(_, currCity)).foreach {
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
		val mean = getMean(stops.map(stop => stop.location))

		if (stops.size > 0) stops.minBy(stop => lm.getMetresDistance(stop.location, mean))
		else lm.findFurthest(depot)
	}

	// Shuffle algorithem
	def shuffle(): Truck = {
		var bestSol: Truck = this
		var currBest = bestSol.getCost()

		def doShuffle(groupSizeMin: Int, groupSizeMax: Int, solution: Truck): Truck = {
			require(groupSizeMax >= groupSizeMin)
			require(groupSizeMin > 0)

			(groupSizeMin to groupSizeMax).foreach {
				groupSize => {
					currBest = bestSol.getCost()
					bestSol = bestSol.shuffleBySize(groupSize)
					val x = bestSol.getCost

					if (bestSol.getCost < currBest) {
						logger.debug("New solution found: {}", bestSol.getCost())
						doShuffle(1, getMaxSwapSize(), bestSol)
					}
				}
			}
			bestSol
		}

		doShuffle(1, getMaxSwapSize(), bestSol)
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
				}.toList.sortWith(_.getCost < _.getCost).head
			}

			// We add this on so head of list always has one solution
			(this :: (0 to stops.size - groupSize).map {
				from => doSwap(from, groupSize, invert, this)
			}.toList.filter(truck => truck.isValid)).sortWith(_.getCost < _.getCost).head
		}

		val newSolution: Truck = swap(groupSize, false)
		val newSolution2: Truck = swap(groupSize, true)

		assert(newSolution.stops.size == stops.size)
		assert(newSolution2.stops.size == stops.size)

		List(newSolution, newSolution2).sortBy(_.getCost).head
	}

	def isValid(): Boolean = weightValid() && timeValid() //&& specialCodesValid()

	def weightValid(): Boolean = getTotalWeight() <= maxWeight

	def timeValid(): Boolean = getDistanceTime().time.isShorterThan(new Duration(5 * 1000 * 60 * 60))

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

	override def toString() = {
		val dt = getDistanceTime()
		"Truck:" + name +
			" Time: " + dt.time +
			" Distance: " + dt.distance +
			" Weight:" + getTotalWeight +
			" " + stops.toString + "\n"
	}

}

/*

loadStopsWithSpecialCodes
reduceTrucksUsed
loadStopsWithSpecialCodes
calculateSmallestAvailableStopCapacity
loadNotloads

truck
calculateRouteMean
specialCodesOk
getCostPerMetre
getCostPerTimeSpan

//Helper method, calculates the opportunity cost and actual cost for the current stop and adds this on to total cost
private bool addLinkTimeCostDistance(Stop previous, Stop current, ref TimeSpan tsRouteEarliestStart, ref TimeSpan tsRouteLatestStart, ref long lCurrRouteDistance, ref double dCurrRouteCost, ref TimeSpan tsRouteJourneyTime, bool bSetStopVars, int iOrder) {

	//Time (added time, plus wait time)
	TimeSpan tsAddedTime = new TimeSpan ();
	tsAddedTime = previous.liTimes[current.ID];
	tsRouteJourneyTime += tsAddedTime;
	tsRouteJourneyTime += current.tsWaitTime; //-not sure about

	//Cost (cost per dist, and time, and wait time)
	double dAddedCost = getCostPerMetre (previous.liDistances[current.ID]);
	dAddedCost += getCostPerTimeSpan(tsAddedTime);

	//Current Earliest start = earliest start to make this stop, same with latest
	TimeSpan currentEarliestStart = new TimeSpan (0,0,0);
	TimeSpan currentLatestStart = new TimeSpan(0, 0, 0);
	currentEarliestStart = current.tsEarlyTime - tsRouteJourneyTime;
	currentLatestStart = current.tsLateTime - tsRouteJourneyTime;


	//	  |  |							routeVars
	//		| |__________________| |    current e/l starts
	//		||							new routevars
	//if the earliest time at the stop - journey time means the truck has to start later
	//set the route earliest time to the later time.
	if (currentEarliestStart > tsRouteEarliestStart) {
		tsRouteEarliestStart = currentEarliestStart;
	}

	//		|	|						routeVars
	//		| |__________________| |    current e/l starts
	//		| |							new routevars
	if (currentLatestStart < tsRouteLatestStart) {
		tsRouteLatestStart = currentLatestStart;
	}

	//	  ||							routeVars
	//		| |__________________| |    current e/l starts
	//	   ||Wait Time					(new journeytime)
	//	   | routeVars (e & l)
	//If you need to wait before you get there...
	if (currentEarliestStart > tsRouteLatestStart) {

		//Time (add on the extra wait)
		tsRouteJourneyTime += currentEarliestStart - tsRouteLatestStart;

		//Cost (add on the extra cost of waiting)
		dAddedCost += getCostPerTimeSpan(currentEarliestStart - tsRouteLatestStart);

		//Set the earliest start to a bit later
		tsRouteEarliestStart = tsRouteLatestStart;
	}

	printCurrentTimeWindows((Stop)current, currentEarliestStart, currentLatestStart);//only prints if on


	//			||						routeVars
	//		| |__________________| |    current e/l starts
	//Even getting there at the latest time, cannot leave early enough
	if (tsRouteEarliestStart > currentLatestStart) {
		bReturn = false;
	}


	//Must be valid so return
	dCurrRouteCost += dAddedCost;
	if (bSetStopVars) {
		current.dCost = dAddedCost;
		current.tsEarliestArrival = tsRouteEarliestStart + tsRouteJourneyTime;
		current.tsLatestArrival = tsRouteLatestStart + tsRouteJourneyTime;
		current.iOrder = iOrder;
	}
	return bReturn;
}*/
