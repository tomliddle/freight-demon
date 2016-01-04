package com.tomliddle.util

import org.slf4j.LoggerFactory

trait Logging {
	protected final val logg = LoggerFactory.getLogger(this.getClass)
}
