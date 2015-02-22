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

		val diff = math.abs(to - from)
		val overlap =  size - diff
		val toList = stops.slice(to + overlap, to + size)

		val result = stops.patch(from, toList, toList.size)
		result.patch(to, fromList, size)
	}


	def takeOff(stops: List[Stop], position: Int, size: Int): (List[Stop], List[Stop]) = {
		require(position + size <= stops.size)
		require(position >= 0 && size > 0)

		(stops.take(position) ++ stops.drop(position + size), stops.slice(position, position + size)) //TODO check this
	}



}
