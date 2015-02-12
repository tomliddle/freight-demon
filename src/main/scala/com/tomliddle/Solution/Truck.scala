package com.tomliddle.solution

import org.joda.time.{LocalTime, Duration}


case class Truck(name: String, startTime: LocalTime, endTime: LocalTime, maxWeight: BigDecimal, depot: Depot, stops: List[Stop], lm: LocationMatrix, userId: Int, id: Option[Int] = None) {

	def totalWeight: BigDecimal = stops.foldLeft(BigDecimal(0)) { (totalWeight: BigDecimal, stop: Stop) => totalWeight + stop.maxWeight}

	def cost: BigDecimal = distance * 1.2

	def distance: BigDecimal = {
		var distToFirstStop =
			if (stops.size > 0) lm.distanceBetween(depot, stops(0)) + lm.distanceBetween(depot, stops.last)
			else BigDecimal(0)

		if (stops.size > 1)
			stops.sliding(2).map {
				(currCities: List[Stop]) => lm.distanceBetween(currCities(0), currCities(1))
			}.foldLeft(distToFirstStop) { (a: BigDecimal, b: BigDecimal) => a + b}
		else distToFirstStop
	}

	def time: Duration = {
		// TODO this is wrong!
		val timeToFirstStop =
			if (stops.size > 0) lm.timeBetween(depot, stops.head)
			else new Duration(0)

		if (stops.size > 1) {
			stops.sliding(2).map {
				currStops: List[Stop] =>
					lm.timeBetween(currStops(0), currStops(1))
			}.foldLeft(timeToFirstStop) {(a: Duration, b: Duration) => a.plus(b)}
		}
		else timeToFirstStop
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

/*	def time: Duration = {
		val time =
			if (stops.size > 0) lm.timeBetween(depot, stops.head).plus(lm.timeBetween(depot, stops.last))
			else new Duration(0)


		if (stops.size > 1)
			time.plus(stops.sliding(2).map(
				(currCities: List[Stop]) => {
					val currTime = time.plus(lm.timeBetween(currCities(0), currCities(1)))

					currCities(1) match {
						case stop: Stop => {
							if (stop.startTime.isAfter(currTime))
								stop.startTime
							else currTime
						}
					}
				}).foldLeft(new Duration(0)) { (a: Duration, b: DateTime) => a.plus(b)})
		time
	}*/

	def maxSwapSize = this.stops.size / 2

	def unload(position: Int, size: Int): (Truck, List[Stop]) = {
		assert(position + size <= stops.size && position >= 0 && size > 0, "position:" + position + " size:" + size + " stops.size:" + stops.size)
		//var listBuffer: ListBuffer[Stop] = stops.to[ListBuffer]
		val list: List[Stop] = stops.take(position) ++ stops.drop(position + size) //TODO check this
		(copy(stops = stops), stops.slice(position, position + size))
	}

	def loadSpecialCodes(cities: List[Stop]): (Truck, List[Stop]) = {
		val citiesToLoad: (List[Stop], List[Stop]) = cities.partition(stop => stop.specialCodes.contains(name))
		var truckResult: (Truck, List[Stop]) = load(citiesToLoad._1)
		(truckResult._1, citiesToLoad._2 ++ truckResult._2)
	}

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
		if (stops.size > 0 && mean.x != 0.0 && mean.y != 0.0)
			stops.minBy(stop => distanceTo(stop.location, mean))
		else {
			lm.findFurthest(depot)
		}
	}

	private def distanceTo(point1: Location, point2: Location): BigDecimal = {
		BigDecimal(Math.sqrt(((point1.x - point2.x).pow(2) + (point1.y - point2.y).pow(2)).toDouble))
	}

	private def mean: Location = {
		new Location(
			stops.foldLeft(BigDecimal(0)) { (x: BigDecimal, stop: Stop) => x + stop.location.x} / stops.size,
			stops.foldLeft(BigDecimal(0)) { (y: BigDecimal, stop: Stop) => y + stop.location.y} / stops.size,
			""
		)
	}

	// Shuffle algorithem
	def shuffle: Truck = {
		var bestSol: Truck = this
		var currBest = bestSol.cost

		def doShuffle(start: Int, end: Int, solution: Truck): Truck = {
			(start to end).foreach {
				size => {
					currBest = bestSol.cost
					bestSol = bestSol.shuffleBySize(size)
					if (bestSol.cost < currBest) {
						println("New solution found: " + bestSol.cost)
						doShuffle(0, maxSwapSize, bestSol)
					}
				}
			}
			bestSol
		}

		if (stops.size > 1) {
			bestSol = doShuffle(0, maxSwapSize, bestSol)
			doShuffle(maxSwapSize, 1, bestSol)
		}
		else this
	}

	// This could be more efficient
	private def shuffleBySize(groupSize: Int): Truck = {

		def swap(groupSize: Int, invert: Boolean): Truck = {

			def extractFromList(from: Int, to: Int, size: Int, invert: Boolean): List[Stop] = {
				val toMove =
					if (invert) stops.slice(from, from + size).reverse
					else stops.slice(from, from + size)

				stops.take(from) ++ toMove ++ stops.drop(from + 1)
			}

			def doSwap(from: Int, groupSize: Int, invert: Boolean, solution: Truck): Truck = {
				(0 to stops.size - groupSize).map {
					to => copy(stops = extractFromList(from, to, groupSize, invert))
				}.toList.sortWith(_.cost < _.cost).head
			}

			// We add this on so head of list always has one solution
			(this :: (0 to stops.size - groupSize).map {
				from => doSwap(from, groupSize, invert, this)
			}.toList.filter(city => city.isValid)).sortWith(_.cost < _.cost).head
		}

		assert(groupSize <= stops.size)

		val newSolution: Truck = swap(groupSize, false)
		val newSolution2: Truck = swap(groupSize, true)

		assert(newSolution.stops.size == stops.size)
		assert(newSolution2.stops.size == stops.size)

		List(newSolution, newSolution2).sortBy(_.cost).head
	}

	def isValid: Boolean = weightValid && timeValid && specialCodesValid

	def weightValid: Boolean = totalWeight <= maxWeight

	def timeValid: Boolean = time.isShorterThan(new Duration(5 * 1000 * 60 * 60))

	def specialCodesValid: Boolean = {
		stops.foldLeft(true) {
			(valid: Boolean, stop: Stop) => {
				if (stop.specialCodes.size > 0 && !stop.specialCodes.contains(name))
					false
				else
					valid && true
			}
		}
	}

	override def toString =
		"Truck:" + name +
		" Time: " + time +
		" Distance: " + distance +
		" Weight:" + totalWeight +
		" " + stops.toString + "\n"


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
