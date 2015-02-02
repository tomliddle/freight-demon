package com.tomliddle.solution


trait Geocoding {

	def geocodeFromOnline(postcode: String): Option[Location] = {

		val url: String = "http://www.freethepostcode.org/geocode?postcode=" + postcode

		try {
			val str = scala.io.Source.fromURL(url).getLines().drop(1).next().split(" ")
			Some(Location(BigDecimal(str(1)), BigDecimal(str(0)), postcode))
		}
		catch {
			case _ => None
		}
	}

}

