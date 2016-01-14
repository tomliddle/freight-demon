package com.tomliddle.solution

import com.tomliddle.entity.{Point, Stop}


object PointListUtils {

	/**
		* Adds get mean function to a List of [T <: Point]
		*/
	implicit class PointList[T <: Point](val points: List[T]) {

		def mean: Option[Point] = {
			if (points.nonEmpty) Some(points.reduceLeft[Point]((a, b) => a + b) / points.size)
			else None
		}
	}

	}


