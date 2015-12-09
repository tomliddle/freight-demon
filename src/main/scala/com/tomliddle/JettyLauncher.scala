package com.tomliddle

import org.eclipse.jetty.server._
import org.eclipse.jetty.server.handler._
import org.eclipse.jetty.servlet.DefaultServlet
import org.eclipse.jetty.util.thread.QueuedThreadPool
import org.eclipse.jetty.webapp.WebAppContext
import org.scalatra.servlet.ScalatraListener


object JettyLauncher {
	// this is my entry object as specified in sbt project definition
	def main(args: Array[String]) {
		val port = 8080

		val threadPool = new QueuedThreadPool
		threadPool.setMaxThreads(20)
		val server = new Server(threadPool)

		// Extra options
		server.setDumpAfterStart(false)
		server.setDumpBeforeStop(false)
		server.setStopAtShutdown(true)

		// Handler Structure
		val handlers = new HandlerCollection
		val contexts = new ContextHandlerCollection()
		//val requestLogHandler = new RequestLogHandler
		handlers.setHandlers(List(contexts, new DefaultHandler()).toArray)
		server.setHandler(handlers)

		// Context handler
		val context = new WebAppContext()
		context setContextPath "/"
		context.setResourceBase("src/main/webapp")
		context.setDescriptor("src/main/webapp/WEB-INF/web.xml")
		context.addEventListener(new ScalatraListener)
		context.addServlet(classOf[DefaultServlet], "/")
		server.setHandler(context)

		// HTTP Configuration
		val http_config = new HttpConfiguration()
		//http_config.setSecureScheme("https")
		//http_config.setSecurePort(8443)
		http_config.setOutputBufferSize(32768)
		http_config.setRequestHeaderSize(8192)
		http_config.setResponseHeaderSize(8192)
		http_config.setSendServerVersion(true)
		http_config.setSendDateHeader(false)

		// Server connector
		val http = new ServerConnector(server, new HttpConnectionFactory(http_config))
		http.setPort(port)
		http.setIdleTimeout(10000)
		server.addConnector(http)

		server.start
		server.join
	}
}
