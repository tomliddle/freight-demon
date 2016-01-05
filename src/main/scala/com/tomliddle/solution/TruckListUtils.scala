package com.tomliddle.solution

/**
	* Adds functionality to a list of Trucks to perform swapping of stops and cost calculation
	*/
object TruckListUtils {

	class TruckList(val trucks: List[Truck]) {

		def totalCost = trucks.foldLeft(BigDecimal(0)){(a : BigDecimal, b: Truck) => a + b.cost.get}

		/**
			* Swaps stops from one trucks to all trucks
			* @return the lowest cost list of trucks
			*/
		def swapOneToAll(truck1Pos: Int): List[Truck] = {

			trucks.indices.foldLeft(trucks) {
				(returnTrucks, truck2Pos) => {
					if (truck1Pos != truck2Pos) {
						returnTrucks(truck1Pos).swapBetween(returnTrucks(truck2Pos)) match {
							case (truck1, truck2) =>
								val patchedTruck = returnTrucks.patch(truck1Pos, List[Truck](truck1), 1).patch(truck2Pos, List[Truck](truck2), 1)
								assert(returnTrucks.size == trucks.size, s"trucks size: ${trucks.size} returntrucks size: ${returnTrucks.size}")
								patchedTruck
						}
					}
					else returnTrucks
				}
			}
		}

		/**
			* Swaps stops from all trucks to all trucks
			* @return the lowest cost list of trucks
			*/
		def swapAllToAll: List[Truck] = {
			val swappedTrucks = trucks.indices.map(swapOneToAll)

			// Return the lowest cost list of trucks
			swappedTrucks.foldLeft(trucks, trucks.totalCost){
				case ((bestTrucks, bestCost), currTrucks) =>
					val currCost = currTrucks.totalCost
					if (currCost < bestCost) (currTrucks, currCost)
					else (bestTrucks, bestCost)
			}._1
		}
	}

	implicit def listToTruckList(trucks: List[Truck]): TruckList = new TruckList(trucks)

}
