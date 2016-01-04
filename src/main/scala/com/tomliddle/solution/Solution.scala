package com.tomliddle.solution

import com.tomliddle.entity.{LocationMatrix, Stop, Depot, DistanceTime}
import org.bson.types.ObjectId
import TruckListUtils._

/**
	* Solution class - immutable, so once it has been defined it has all required properties
	* such as total cost, total distance etc.
	* SolutionAlgorithm adds functionality to optimise the solution (by returning a new object)
	* @param name
	* @param depot
	* @param stopsToLoad
	* @param trucks
	* @param lm
	* @param userId
	* @param _id
	*/
case class Solution(name: String, depot: Depot, stopsToLoad: List[Stop], trucks: List[Truck], lm: LocationMatrix, userId: Int, _id: ObjectId = new ObjectId)
		extends SolutionOptimiser {

	lazy val isValid: Boolean = {
		loadedStops.size == loadedStops.distinct.size &&
				trucks.foldLeft(true)((valid: Boolean, truck: Truck) => valid && truck.isValid) &&
				stopsToLoad.distinct.size == stopsToLoad.size &&
				loadedStops.distinct.size == loadedStops.size
	}

	lazy val distanceTime: DistanceTime = trucks.foldLeft(new DistanceTime()) { (a: DistanceTime, b: Truck) => a + new DistanceTime(b.distance.get, b.time.get) }

	lazy val loadedStops: List[Stop] = trucks.foldLeft(List[Stop]())((stops: List[Stop], truck: Truck) => stops ::: truck.stops)

	lazy val maxSolutionSwapSize: Int = trucks.foldLeft(0) { (size: Int, truck: Truck) => size max truck.getMaxSwapSize }

	lazy val cost: BigDecimal = trucks.totalCost

}


