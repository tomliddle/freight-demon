package com.tomliddle.solution

case class Solution(name: String, depot: Depot, stopsToLoad: List[Stop], trucks: List[Truck], userId: Int, id: Option[Int] = None) {

	//var depot: Depot = null
	//var stopsToLoad: List[Stop] = List[Stop]()
	//var trucks: List[Truck] = List[Truck]()

	private def getTotalCost(trucks: List[Truck]): BigDecimal = trucks.foldLeft(BigDecimal(0)){(a : BigDecimal, b: Truck) => a + b.cost}

	def isValid: Boolean = {
		loadedCities.size == loadedCities.distinct.size &&
		trucks.foldLeft(true)((valid: Boolean, truck: Truck) => valid && truck.isValid) &&
		stopsToLoad.distinct.size == stopsToLoad.size &&
		loadedCities.distinct.size == loadedCities.size
	}

	def distance: BigDecimal = trucks.foldLeft(BigDecimal(0)){(a : BigDecimal, b: Truck) => a + b.distance}

	def getTotalLoaded: Int = loadedCities.size

	def loadedCities: List[Stop] = trucks.foldLeft(List[Stop]())((stops: List[Stop], truck: Truck) => stops ++ truck.stops)

	def maxSolutionSwapSize: Int = trucks.foldLeft(0){(size: Int, truck: Truck) => size max truck.maxSwapSize}

	def getCost: BigDecimal = getTotalCost(trucks)

	override def toString = {
		"Valid:" + isValid + " Cost:" + getCost + " Unloaded stops:" + stopsToLoad.size + " Distance:" + distance + "\n" +
		trucks.map(_.toString).toString
	}

	def loadSpecialCodes: Solution = {
		var unloadedStops = stopsToLoad
		var newTrucks = trucks.map {
			truck => {
				val truckResult: (Truck, List[Stop]) = truck.loadSpecialCodes(unloadedStops)
				unloadedStops = truckResult._2
				truckResult._1
			}
		}
		copy(trucks = newTrucks, stopsToLoad = unloadedStops)
	}

	def preload: Solution = {
		var unloadedCities = stopsToLoad
		val truckSol = trucks.map {
			truck => {
				// Try and load the onloaded stops
				var truckRes: (Truck, List[Stop]) = truck.load(unloadedCities)
				// Set unloaded stops to the ones that weren't loaded on the current truck
				unloadedCities = truckRes._2
				truckRes._1
			}
		}
		copy(trucks = truckSol, stopsToLoad = unloadedCities)
	}

	def shuffle : Solution = {
		val newTrucks = trucks.map { truck => truck.shuffle }
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
								var truck1Unloaded: (Truck, List[Stop]) = truck1.unload(truck1Pos, swapSize)
								var truck2Unloaded: (Truck, List[Stop]) = truck2.unload(truck2Pos, swapSize)
								var truck1Load: (Truck, List[Stop]) = truck1Unloaded._1.load(truck2Unloaded._2)
								var truck2Load: (Truck, List[Stop]) = truck2Unloaded._1.load(truck1Unloaded._2)

								// If trucks fully reloaded, check the cost.
								if (truck1Load._2.size == 0 && truck2Load._2.size == 0 &&
									truck1Load._1.cost + truck2Load._1.cost < truck1.cost + truck2.cost)
									returnTrucks = Option(truck1Load._1, truck2Load._1)
							}
						}
					}
				}
				returnTrucks
			}

			var bestSol: Option[(Truck, Truck)]  = None
			(1 to truck1.maxSwapSize).foreach {
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
								assert(returnTrucks.size == trucks.size, "trucks size:" + trucks.size + " returntrucks size:" + returnTrucks.size)
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
