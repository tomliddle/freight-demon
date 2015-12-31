package com.tomliddle.solution

import com.tomliddle.entity.Stop


trait SolutionAlgorithm extends SwapUtilities {

	this: Solution =>

	def preload: Solution = {
		val (unloadedStops, loadedTrucks) = trucks.foldLeft(List[Stop](), List[Truck]()) {
			case ((unloadedStops: List[Stop], truckList: List[Truck]), currTruck: Truck) =>
				currTruck.load(stopsToLoad) match {
					case (modifiedTruck: Truck, stopList: List[Stop]) =>
						(unloadedStops ::: stopList, modifiedTruck :: truckList)
				}
		}

		copy(trucks = loadedTrucks, stopsToLoad = unloadedStops)
	}

	def shuffle : Solution = {
		copy(trucks = trucks.map { _.shuffle}).swapBetweenTrucks
	}

	def swapBetweenTrucks: Solution = {

		// Iterates through two trucks trying to swap all points
		def swapBetween(truck1: Truck, truck2: Truck) : (Truck, Truck) = {

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

			(1 to truck1.getMaxSwapSize).map {
				swapSize => {
					doSwapBetween(truck1, truck2, swapSize)
				}
			}.minBy(truckTup => truckTup._1.cost.get + truckTup._2.cost.get)

		}

		// Swaps from one truck to all trucks
		def swapOneToAll(trucks: List[Truck], truck1Pos: Int): List[Truck] = {

			(0 to trucks.size -1).foldLeft(trucks) {
				(returnTrucks, truck2Pos) => {
					if (truck1Pos != truck2Pos) {
						swapBetween(returnTrucks(truck1Pos), returnTrucks(truck2Pos)) match {
							case (truck1, truck2) => {
								val patchedTruck = returnTrucks.patch(truck1Pos, List[Truck](truck1), 1).patch(truck2Pos, List[Truck](truck2), 1)
								assert(returnTrucks.size == trucks.size, s"trucks size: ${trucks.size} returntrucks size: ${returnTrucks.size}")
								patchedTruck
							}
						}
					}
					else returnTrucks
				}
			}
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