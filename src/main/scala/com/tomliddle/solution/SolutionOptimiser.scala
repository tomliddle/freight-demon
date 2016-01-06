package com.tomliddle.solution

import com.tomliddle.entity.Stop

/**
	* Adds functionality to Solution to optimise
	*/
trait SolutionOptimiser {
	this: Solution =>

	import TruckListUtils._

	/**
		* Preload the initial set of stops by finding the furthest stop and loading it,
		* then adding the nearest stop to the mean location of the current loaded stops
		* @return A valid solution
		*/
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

	def optimise : Solution = copy(trucks = trucks.map { _.shuffle}).optimiseBetweenTrucks

	def optimiseBetweenTrucks: Solution = copy(trucks = trucks.optimiseAllToAll)

}