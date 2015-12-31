package com.tomliddle.solution

import com.tomliddle.entity.Stop


trait SwapUtilities {

	/** Swaps stops in a list.
		* @param stops the list
		* @param from the 0 based start position
		* @param to the 0 based final location
		* @param size the number to swap
		* @param reverse reverse the list to be swapped (not the whole list)
		*/
	def swapStops(stops: List[Stop], from: Int, to: Int, size: Int, reverse: Boolean): List[Stop] = {
		require(stops.size >= to, "to is bigger than list")
		require(from + size <= stops.size, "Swapping past the end")

		val fromList =
			if (reverse) stops.slice(from, from + size).reverse
			else stops.slice(from, from + size)

		val result = stops.patch(to, fromList, fromList.size)

		val diff = math.abs(to - from)
		val overlap = math.max(size - diff, 0)

		val toList =
			if (to > from) stops.slice(to + overlap, to + size)
			else stops.slice(to, to + size - overlap)

		val result2 =
			if (to < from) result.patch(from + overlap, toList, toList.size)
			else result.patch(from, toList, toList.size)

		require(result2.size == result2.distinct.size)
		result2
	}


	/**
		* Takes a number of stops from a list and returns the result
		*/
	def takeOff(stops: List[Stop], position: Int, size: Int): (List[Stop], List[Stop]) = {
		require(position + size <= stops.size)
		require(position >= 0 && size > 0)

		val stopsTuple = (stops.take(position) ++ stops.drop(position + size), stops.slice(position, position + size)) //TODO check this
		require(stopsTuple._1.size + stopsTuple._2.size == stopsTuple._1.distinct.size + stopsTuple._2.distinct.size)
		stopsTuple
	}

}
