package com.tomliddle.solution

object SeqUtils {

	/**
		* Adds functionality to lists to swap and take off sections of a list.
		*/
	implicit class SwapUtils[T](seq: Seq[T]) {

		/** Swaps points in a list.
			* @param from the 0 based start position
			* @param to the 0 based final location
			* @param size the number to swap
			* @param reverse reverse the list to be swapped (not the whole list)
			*/
		def swap(from: Int, to: Int, size: Int, reverse: Boolean): Seq[T] = {
			val (mainSeq, rest) = takeOff(from, size)
			val takenOffSeq = if (reverse) rest.reverse else rest
			val (left, right) = mainSeq.splitAt(to)

			left ++ takenOffSeq ++ right
		}


		/**
			* Takes a number of elems from the list and returns the result
			* @param position position to take from
			* @param size size to take
			* @return (remainder of original list, taken list)
			*/
		def takeOff(position: Int, size: Int): (Seq[T], Seq[T]) = {
			require(position + size <= seq.size)
			require(position >= 0 && size > 0)

			(seq.take(position) ++ seq.drop(position + size), seq.slice(position, position + size))
		}
	}

}
