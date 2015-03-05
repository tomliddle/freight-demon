package com.tomliddle.solution


trait Geocoding {

	def geocodeFromOnline(postcode: String): Option[(BigDecimal, BigDecimal)] = {

		val url: String = "http://www.freethepostcode.org/geocode?postcode=" + postcode

		try {
			val str = scala.io.Source.fromURL(url).getLines().drop(1).next().split(" ")
			Some((BigDecimal(str(1)), BigDecimal(str(0))))
		}
		catch {
			case _: Exception => None
		}
	}

}

