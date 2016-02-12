package com.tomliddle.solution

import com.tomliddle.entity.Stop
import com.tomliddle.common.OrderingDefaults._

/**
	* Adds functionality to Solution to optimise
	*/
trait SolutionOptimiser {
	this: Solution =>

	/**
		* Preload the initial set of stops by finding the furthest stop and loading it,
		* then adding the nearest stop to the mean location of the current loaded stops
		* @return A valid solution
		*/
	def preload: Solution = {
		val (unloadedStops, loadedTrucks) = trucks.foldLeft(Seq[Stop](), Seq[Truck]()) {
			case ((unloadedStops: Seq[Stop], trucks: Seq[Truck]), currTruck: Truck) =>
				currTruck.load(stopsToLoad) match {
					case (modifiedTruck: Truck, stops: Seq[Stop]) =>
						(unloadedStops ++ stops, trucks :+ modifiedTruck)
				}
		}

		copy(trucks = loadedTrucks, stopsToLoad = unloadedStops)
	}

	def optimise : Solution = copy(trucks = trucks.map { _.shuffle}).optimiseBetweenTrucks

	/**
		* Swaps stops from one trucks to all trucks
		* @return the lowest cost solution
		*/
	private def swapOneToAll(truck1Pos: Int): Solution = {

		trucks.indices.foldLeft(this) {
			(bestSol, truck2Pos) => {
				if (truck1Pos != truck2Pos) {
					bestSol.trucks(truck1Pos).swapBetween(bestSol.trucks(truck2Pos)) match {
						case (truck1, truck2) =>
							val patchedTrucks: Seq[Truck] = bestSol.trucks.patch(truck1Pos, Seq[Truck](truck1), 1).patch(truck2Pos, Seq[Truck](truck2), 1)
							assert(patchedTrucks.size == trucks.size, s"trucks size: ${trucks.size} patched trucks size: ${patchedTrucks.size}")
							copy(trucks = patchedTrucks)
					}
				}
				else this
			}
		}
	}

	/**
		* Swaps stops from all trucks to all trucks
		* @return the lowest cost list of trucks
		*/
	def optimiseBetweenTrucks: Solution = trucks.indices.map(swapOneToAll).min

}