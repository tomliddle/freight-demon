package com.tomliddle.solution

object ListUtils {

	/**
		* Adds functionality to lists to swap and take off sections of a list.
		*/
	implicit class SwapList[T](list: List[T]) {

		/** Swaps points in a list.
			* @param from the 0 based start position
			* @param to the 0 based final location
			* @param size the number to swap
			* @param reverse reverse the list to be swapped (not the whole list)
			*/
		def swap(from: Int, to: Int, size: Int, reverse: Boolean): List[T] = {
			val (mainList, rest) = takeOff(from, size)
			val takenOffList = if (reverse) rest.reverse else rest
			val (left, right) = mainList.splitAt(to)

			left ::: takenOffList ::: right
		}


		/**
			* Takes a number of elems from the list and returns the result
			* @param position position to take from
			* @param size size to take
			* @return (remainder of original list, taken list)
			*/
		def takeOff(position: Int, size: Int): (List[T], List[T]) = {
			require(position + size <= list.size)
			require(position >= 0 && size > 0)

			(list.take(position) ::: list.drop(position + size), list.slice(position, position + size))
		}
	}

}
