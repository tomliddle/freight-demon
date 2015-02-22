package com.tomliddle.solution

/**
 * Created by tom on 20/02/2015.
 */
trait SwapUtilities {

	def swapStops(stops: List[Stop], from: Int, to: Int, size: Int, invert: Boolean): List[Stop] = {
		require(stops.size >= to, "to is bigger than list")
		//require(!(from == to && !invert), "swapping to nowhere")
		require(from + size <= stops.size, "Swapping past the end")

		val fromList =
			if (invert) stops.slice(from, from + size).reverse
			else stops.slice(from, from + size)

		val toList = stops.slice(to, to + size - from)
		val result = stops.patch(to, fromList, size)
		result.patch(from, toList, toList.size)
	}



}
