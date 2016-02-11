package com.tomliddle.solution

/**
	* Adds functionality to a list of Trucks to perform swapping of stops and cost calculation
	*/
object TruckSeqUtils {

	implicit class TruckUtils(val trucks: Seq[Truck]) {

		def totalCost = trucks.foldLeft(BigDecimal(0)) { (cost, truck) => cost + truck.cost }

		def lowestCostOption: Option[Truck] =
			if (trucks.isEmpty) None
			else Some(trucks.min)
	}
}
