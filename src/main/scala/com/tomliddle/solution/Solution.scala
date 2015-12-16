package com.tomliddle.solution

import com.tomliddle.entity.{LocationMatrix, Stop, Depot, DistanceTime}
import org.bson.types.ObjectId


case class Solution(name: String, depot: Depot, stopsToLoad: List[Stop], trucks: List[Truck], lm: LocationMatrix, userId: Int, _id: ObjectId = new ObjectId)
		extends SolutionAlgorithm {

	lazy val isValid: Boolean = {
		loadedStops.size == loadedStops.distinct.size &&
				trucks.foldLeft(true)((valid: Boolean, truck: Truck) => valid && truck.isValid) &&
				stopsToLoad.distinct.size == stopsToLoad.size &&
				loadedStops.distinct.size == loadedStops.size
	}

	lazy val distanceTime: DistanceTime = trucks.foldLeft(new DistanceTime()) { (a: DistanceTime, b: Truck) => a + new DistanceTime(b.distance.get, b.time.get) }

	lazy val loadedStops: List[Stop] = trucks.foldLeft(List[Stop]())((links: List[Stop], truck: Truck) => links ::: truck.stops)

	lazy val maxSolutionSwapSize: Int = trucks.foldLeft(0) { (size: Int, truck: Truck) => size max truck.getMaxSwapSize }

	lazy val cost: BigDecimal = getTotalCost(trucks)

	protected def getTotalCost(trucks: List[Truck]): BigDecimal = trucks.foldLeft(BigDecimal(0)){(a : BigDecimal, b: Truck) => a + b.cost.get}
}


