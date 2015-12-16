package com.tomliddle.solution

import com.tomliddle.entity.Stop


trait SolutionAlgorithm extends SwapUtilities with TruckAlgorithm {

	this: Solution =>

	def preload: Solution = {
		var unloadedCities = stopsToLoad
		val truckSol = trucks.map {
			truck => {
				// Try and load the onloaded stops
				var truckRes: (Truck, List[Stop]) = load(truck, unloadedCities, lm)
				// Set unloaded stops to the ones that weren't loaded on the current truck
				unloadedCities = truckRes._2
				truckRes._1
			}
		}
		copy(trucks = truckSol, stopsToLoad = unloadedCities)
	}

	def shuffle : Solution = {
		val newTrucks = trucks.map { truck => shuffle(truck)}
		copy(trucks = newTrucks)
	}

	def swapBetweenTrucks: Solution = {

		// Iterates through two trucks trying to swap all points
		def swapBetween(truck1: Truck, truck2: Truck) : Option[(Truck, Truck)] = {

			def doSwapBetween(truck1: Truck, truck2: Truck, swapSize: Int) : Option[(Truck, Truck)] = {
				var returnTrucks: Option[(Truck, Truck)] = None
				(0 to truck1.stops.size - swapSize).foreach {
					truck1Pos => {
						(0 to truck2.stops.size - swapSize).foreach {
							truck2Pos => {
								val truck1Unloaded: (Truck, List[Stop]) = unload(truck1, truck1Pos, swapSize)
								val truck2Unloaded: (Truck, List[Stop]) = unload(truck2, truck2Pos, swapSize)
								val truck1Load: (Truck, List[Stop]) = load(truck1Unloaded._1, truck2Unloaded._2, lm)
								val truck2Load: (Truck, List[Stop]) = load(truck2Unloaded._1, truck1Unloaded._2, lm)

								// If trucks fully reloaded, check the cost.
								if (truck1Load._2.size == 0 && truck2Load._2.size == 0 &&
										truck1Load._1.cost.get + truck2Load._1.cost.get < truck1.cost.get + truck2.cost.get)
									returnTrucks = Some(truck1Load._1, truck2Load._1)
							}
						}
					}
				}
				returnTrucks
			}

			var bestSol: Option[(Truck, Truck)]  = None
			(1 to truck1.getMaxSwapSize).foreach {
				swapSize => {
					doSwapBetween(truck1, truck2, swapSize) match {
						case Some(trucks) => bestSol = Some(trucks)
						case None =>
					}
				}
			}
			bestSol
		}

		// Swaps from one truck to all trucks
		def swapOneToAll(trucks: List[Truck], truck1Pos: Int): List[Truck] = {
			var returnTrucks: List[Truck] = trucks
			(0 to trucks.size -1).foreach {
				truck2Pos: Int => {
					if (truck1Pos != truck2Pos) {
						swapBetween(returnTrucks(truck1Pos), returnTrucks(truck2Pos)) match {
							case Some(newTrucks) => {
								returnTrucks = returnTrucks
										.patch(truck1Pos, List[Truck](newTrucks._1), 1)
										.patch(truck2Pos, List[Truck](newTrucks._2), 1)
								assert(returnTrucks.size == trucks.size, s"trucks size: ${trucks.size} returntrucks size: ${returnTrucks.size}")
							}
							case None => None
						}
					}
				}
			}
			returnTrucks
		}

		def swapAllToAll(trucks: List[Truck]): List[Truck] = {
			(0 to trucks.size -1).map {
				truck1Pos => {
					swapOneToAll(trucks, truck1Pos)
				}
			}.sortBy(getTotalCost(_)).head
		}

		copy(trucks = swapAllToAll(trucks))
	}
}