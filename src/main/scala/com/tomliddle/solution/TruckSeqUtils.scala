package com.tomliddle.solution

/**
	* Adds functionality to a list of Trucks to perform swapping of stops and cost calculation
	*/
object TruckSeqUtils {

	implicit class TruckUtils(val trucks: Seq[Truck]) {

		def totalCost = trucks.foldLeft(BigDecimal(0)){(cost , truck) => cost + truck.cost.get}

		/**
			* Swaps stops from one trucks to all trucks
			* @return the lowest cost list of trucks
			*/
		def swapOneToAll(truck1Pos: Int): Seq[Truck] = {

			trucks.indices.foldLeft(trucks) {
				(returnTrucks, truck2Pos) => {
					if (truck1Pos != truck2Pos) {
						returnTrucks(truck1Pos).swapBetween(returnTrucks(truck2Pos)) match {
							case (truck1, truck2) =>
								val patchedTrucks: Seq[Truck] = returnTrucks.patch(truck1Pos, Seq[Truck](truck1), 1).patch(truck2Pos, Seq[Truck](truck2), 1)
								assert(patchedTrucks.size == trucks.size, s"trucks size: ${trucks.size} patched trucks size: ${patchedTrucks.size}")
								patchedTrucks
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
		def optimiseAllToAll: Seq[Truck] = {
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

}
