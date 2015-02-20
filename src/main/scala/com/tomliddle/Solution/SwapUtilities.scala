package com.tomliddle.solution

/**
 * Created by tom on 20/02/2015.
 */
trait SwapUtilities {

	def extractFromList(stops: List[Stop], from: Int, to: Int, size: Int, invert: Boolean): (List[Stop], List[Stop]) = {
		val toMove =
			if (invert) stops.slice(from, from + size).reverse
			else stops.slice(from, from + size)

		(stops.take(from) ++ stops.drop(from + 1), toMove)
	}

}
