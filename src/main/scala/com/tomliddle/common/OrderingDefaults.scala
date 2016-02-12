package com.tomliddle.common

import com.tomliddle.solution.{Solution, Truck}


object OrderingDefaults {

	implicit val truckOrdering = new Ordering[Truck] {
		override def compare(a: Truck, b: Truck): Int = a.cost compare b.cost
	}

	implicit val solutionOrdering = new Ordering[Solution] {
		override def compare(a: Solution, b: Solution): Int = a.cost compare b.cost
	}

}
