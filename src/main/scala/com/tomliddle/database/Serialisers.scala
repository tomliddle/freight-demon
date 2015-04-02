package com.tomliddle.database

import com.mongodb.casbah.commons.conversions.MongoConversionHelper
import org.bson.{BSON, Transformer}
import org.joda.time.LocalTime
import org.joda.time.format.DateTimeFormat

import scala.reflect.ClassTag


class LocalTimeConverter {
	// Massive hack
	private final val KEY = "LOCALTIME:"
	private final val fmt = DateTimeFormat.forPattern("HH:mm:ss.SSS")
	private val decodeTransformer = new Transformer {
		def transform(o: AnyRef): AnyRef = o match {
			case s: String if (s.startsWith(KEY)) => fmt.parseLocalTime(s.drop(KEY.length))
			case d: LocalTime => d
			case _ => o
		}
	}

	private val encodeTransformer = new Transformer {
		def transform(o: AnyRef): AnyRef = o match {
			case l: LocalTime => KEY + fmt.print(l)
			case _ => o
		}
	}

	new MyConversionHelper[LocalTime, String](classOf[LocalTime], encodeTransformer, classOf[String], decodeTransformer).register
}


class BigDecimalConverter {
	private val decodeTransformer = new Transformer {
		def transform(o: AnyRef): AnyRef = o match {
			case d: java.lang.Double => BigDecimal(d).asInstanceOf[AnyRef]
			case b: BigDecimal => b
			case _ => o
		}
	}

	private val encodeTransformer = new Transformer {
		def transform(o: AnyRef): AnyRef = o match {
			case d: BigDecimal => d.doubleValue.asInstanceOf[AnyRef]
			case _ => o
		}
	}

	new MyConversionHelper[BigDecimal, java.lang.Double](classOf[BigDecimal], encodeTransformer, classOf[java.lang.Double], decodeTransformer).register
}

// **************************************************************
class MyConversionHelper[T, U](
							encodeClass: Class[T], encodeTransformer: Transformer,
							decodeClass: Class[U], decodeTransformer: Transformer)
	extends MongoConversionHelper {

	override def register() {
		log.debug(s"registering")
		BSON.addEncodingHook(encodeClass, encodeTransformer)
		BSON.addDecodingHook(decodeClass, decodeTransformer)
		super.register()
	}

	override def unregister() {
		log.debug(s"De-registering")
		BSON.removeEncodingHook(encodeClass, encodeTransformer)
		BSON.removeDecodingHooks(decodeClass)
		super.unregister()
	}
}

