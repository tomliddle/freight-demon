package com.tomliddle.solution

import com.tomliddle.entity.Point


object PointSeqUtils {

	/**
		* Adds get mean function to a List of [T <: Point]
		*/
	implicit class PointUtils[T <: Point](val points: Seq[T]) {

		def mean: Option[Point] = {
			if (points.nonEmpty) Some(points.reduceLeft[Point]((a, b) => a + b) / points.size)
			else None
		}
	}

	}


