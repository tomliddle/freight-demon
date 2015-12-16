package com.tomliddle.solution

import com.tomliddle.entity.Stop

/**
 * Created by tom on 20/02/2015.
 */
trait SwapUtilities {

	def swapStops(stops: List[Stop], from: Int, to: Int, size: Int, invert: Boolean): List[Stop] = {
		require(stops.size >= to, "to is bigger than list")
		//require(!(from == to && !invert), "swapping to nowhere")
		require(from + size <= stops.size, "Swapping past the end")
		require(stops.size == stops.distinct.size, "Stops aren't distinct")

		val fromList =
			if (invert) stops.slice(from, from + size).reverse
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

		require(result2.size == result2.distinct.size,
				s"${stops} \n" +
				s"size ${result.size} != ${result.distinct.size} \n" +
				s"result ${result} \n " +
				s"result2 ${result2} \n " +
				s"size2 ${result2.size} != ${result2.distinct.size} from:${from} to:${to} " +
				s"size ${size}  overlap ${overlap} \n" +
				s"tolist ${toList} \nfromlist ${fromList}"
		)
		result2
	}


	def takeOff(stops: List[Stop], position: Int, size: Int): (List[Stop], List[Stop]) = {
		require(position + size <= stops.size)
		require(position >= 0 && size > 0)

		val stopsTuple = (stops.take(position) ++ stops.drop(position + size), stops.slice(position, position + size)) //TODO check this
		require(stopsTuple._1.size + stopsTuple._2.size == stopsTuple._1.distinct.size + stopsTuple._2.distinct.size)
		stopsTuple
	}



}
