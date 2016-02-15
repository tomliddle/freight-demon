package com.tomliddle.solution

import com.tomliddle.common.OrderingDefaults._

/**
	* Adds functionality to a list of Trucks to perform swapping of stops and cost calculation
	*/
object TruckSeqUtils {

	implicit class TruckUtils(val trucks: Seq[Truck]) {

		def totalCost = trucks.foldLeft(BigDecimal(0, 2)) { (cost, truck) => cost + truck.cost }

		def lowestCostOption: Option[Truck] =
			if (trucks.isEmpty) None
			else Some(trucks.min)
	}
}
