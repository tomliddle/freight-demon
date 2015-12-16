package com.tomliddle.entity

import com.tomliddle.solution.timeanddistance.LatLongTimeAndDistCalc
import org.joda.time.{Duration, LocalTime}


class DistanceTime(val distance: BigDecimal = BigDecimal(0), val time: Duration = new Duration(0)) {
	def +(operand: DistanceTime): DistanceTime = {
		new DistanceTime(this.distance + operand.distance, this.time.plus(operand.time))
	}
}


case class Link(waitTime: Duration = new Duration(0), travelDT: DistanceTime = new DistanceTime()) {
	def +(operand: Link): Link = {
		new Link(this.waitTime.plus(operand.waitTime), this.travelDT + operand.travelDT)
	}

	def elapsedTime: Duration = travelDT.time.plus(waitTime)
}


class Point(val x: BigDecimal, val y: BigDecimal, val address: String) {
	def +(operand: Point): Point = {
		new Point(this.x + operand.x, this.y + operand.y, "")
	}
	def /(operand: BigDecimal): Point = {
		new Point(this.x / operand, this.y / operand, this.address)
	}
}


case class Depot(name: String, override val x: BigDecimal,  override val y: BigDecimal, override val address: String, userId: Int, id: Option[Int] = None) extends Point(x, y, address)

case class Stop(name: String, override val x: BigDecimal, override val y: BigDecimal, override val address: String, startTime: LocalTime, endTime: LocalTime, maxWeight: BigDecimal, userId: Int, id: Option[Int] = None) extends Point(x, y, address)

case class LocationMatrix(stops: List[Stop], depots: List[Depot]) extends LatLongTimeAndDistCalc {

	private val distancesAndTimes: Map[Point, Map[Point, DistanceTime]] = {
		(stops ::: depots).map {
			stop1: Point => {
				stop1 -> stops.map {
					stop2: Point =>
						stop2 -> getDistanceTime(stop1, stop2)
				}.toMap[Point, DistanceTime]
			}
		}.toMap
	}

	def findFurthestStop(point: Point): Stop  = {
		distancesAndTimes(point).collect{case (s: Stop, dt: DistanceTime) => (s, dt)}.toList.sortBy(_._2.distance).last._1
	}

	def distanceTimeBetween(stop1: Point, stop2: Point): DistanceTime = distancesAndTimes(stop1)(stop2)

}





trait Mean {
	def getMean(locations: List[Point]): Option[Point] = {
		if (locations.size > 0)
			Some (locations.foldLeft(new Point(0, 0, "")) { (location1: Point, location2: Point) =>
				new Point(location1.x + location2.x, location1.y + location2.y, "")
			} / locations.size)
		else None
	}
}
