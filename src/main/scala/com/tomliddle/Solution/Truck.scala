package com.tomliddle.Solution

import scala.collection.mutable.ListBuffer


class Truck(val name: String, val depot: Point, val stops: List[Stop], constraints: TruckConstraints) {

	def copyWith(name: String = name,
				depot:   Point    = depot,
				stops: List[Stop] = stops,
				constraints: TruckConstraints = constraints):
		Truck =	{ new Truck(name, depot, stops, constraints)}

	def totalWeight: Double = stops.foldLeft(0.0){(totalWeight: Double, city: Stop) => totalWeight + city.constraints.maxWeight}
	def cost: Double = distance * 1.2

	def distance: Double = {
		var dist1 = 0.0
		if (stops.size > 0)
			dist1 = depot.distancesAndTimes(stops(0))._1 + depot.distancesAndTimes(stops.last)._1

		if (stops.size > 1)
			dist1 += stops.sliding(2).map(
				(currCities: List[Stop]) => currCities(0).location.distancesAndTimes(currCities(1))._1
			).foldLeft(0.0){(a : Double, b: Double) => a + b}
		dist1
	}

	def time: Int = {
		var time = 0
		if (stops.size > 0)
			time = depot.distancesAndTimes(stops(0))._2 + depot.distancesAndTimes(stops.last)._2

		if (stops.size > 1)
			time += stops.sliding(2).map(
				(currCities: List[Stop]) => {
					val currTime = currCities(0).location.distancesAndTimes(currCities(1))._2

					currCities(1) match {
						case stop: Stop => {
							if (stop.constraints.startTime > currTime)
								stop.constraints.startTime
							else currTime
						}
					}
				}).foldLeft(0){(a : Int, b: Int) => a + b}
		time
	}

	val maxSwapSize = this.stops.size / 2

	def unload(position: Int, size: Int): (Truck, List[Stop]) = {
		assert(position + size <= stops.size && position >= 0 && size > 0, "position:" + position + " size:" + size + " stops.size:" + stops.size)
		var listBuffer: ListBuffer[Stop] = stops.to[ListBuffer]
		listBuffer.remove(position, size)
		(copyWith(stops = listBuffer.toList), stops.slice(position, position+size))
	}

	def loadSpecialCodes(cities: List[Stop]): (Truck, List[Stop]) = {
		val citiesToLoad: (List[Stop], List[Stop]) = cities.partition(stop => stop.constraints.specialCodes.contains(name))
		var truckResult: (Truck, List[Stop]) = load(citiesToLoad._1)
		(truckResult._1, citiesToLoad._2 ++ truckResult._2)
	}

	def load(city: Stop): (Truck, Option[Stop]) = {
		var truck: Truck = copyWith(stops = city :: stops).shuffleBySize(1)
		truck.isValid match  {
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
		stops.sortBy(_.location.distancesAndTimes(currCity)._1).foreach {
			city => {
				currTruck.load(city) match {
					case (truck, Some(cityNotLoaded)) => notLoadedCities =  cityNotLoaded :: notLoadedCities
					case (truck, None) => currTruck = truck
				}
			}
		}
		(currTruck, notLoadedCities)
	}

	private def nextStopToLoad(stops: List[Stop]): Stop = {
		if (stops.size > 0 && mean._1 != 0.0 && mean._2 != 0.0)
			stops.minBy(stop => distanceTo(stop, mean))
		else {
			depot.findFurthest
		}
	}

	private def distanceTo(stop: Stop, coords: (Double, Double)): Double = {
		Math.sqrt(Math.pow(stop.location.y - coords._2, 2) + Math.pow(stop.location.x - coords._1, 2))
	}

	private lazy val mean: (Double, Double) =
		(stops.foldLeft(0.0){(x: Double, stop: Stop) => x + stop.location.x} / stops.size,
			stops.foldLeft(0.0){(y: Double, stop: Stop) => y + stop.location.y} / stops.size)

	// Shuffle algorithem
	def shuffle : Truck = {
		var bestSol: Truck = this
		var currBest = bestSol.cost

		def doShuffle(start: Int, end: Int, solution: Truck) : Truck = {
			(start to end).foreach {
				size =>  {
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
	private def shuffleBySize(groupSize: Int) : Truck = {

		def swap(groupSize: Int, invert: Boolean): Truck = {

			def extractFromList(from: Int, to: Int, size: Int, invert: Boolean): List[Stop] = {
				var toMove: List[Stop] = stops.slice(from, from + size)
				if (invert)
					toMove = toMove.reverse
				var listBuffer: ListBuffer[Stop] = stops.to[ListBuffer]
				listBuffer.remove(from, size)
				listBuffer.insertAll(to, toMove)
				listBuffer.toList
			}

			def doSwap(from: Int, groupSize: Int, invert: Boolean, solution: Truck): Truck = {
				(0 to stops.size - groupSize).map {
					to => copyWith(stops = extractFromList(from, to, groupSize, invert))
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

	def isValid: Boolean = {
		weightValid &&
		timeValid &&
		specialCodesValid
	}

	def weightValid: Boolean = totalWeight <= constraints.maxWeight
	def timeValid: Boolean = time < 5000

	def specialCodesValid: Boolean = {
		stops.foldLeft(true){
			(valid: Boolean, stop: Stop) => {
				if (stop.constraints.specialCodes.size > 0 && !stop.constraints.specialCodes.contains(name))
					false
				else
					valid && true
			}
		}
	}

	override lazy val toString =
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
