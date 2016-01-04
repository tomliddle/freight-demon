package com.tomliddle.solution

import com.tomliddle.entity.{Point, Stop}


object PointListUtils {

	/**
		* Adds get mean function to a List of [T <: Point]
		*/
	class PointList[T <: Point](val points: List[T]) {

		def mean: Option[Point] = {
			if (points.size > 0)
				Some (points.foldLeft(new Point(0, 0, "")) { (location1: Point, location2: Point) =>
					new Point(location1.x + location2.x, location1.y + location2.y, "")
				} / points.size)
			else None
		}

	}

	implicit def listToStopList[T <: Point](stops: List[T]) = new PointList(stops)

}
