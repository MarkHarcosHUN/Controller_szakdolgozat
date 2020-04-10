package gateway.controller.server


import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import java.net.InetSocketAddress
import java.util.concurrent.Executors


class HttpServer(private val controller: Manageable) {

    private val PORT = 8001
    private val server: HttpServer

    init {
        server = HttpServer.create(InetSocketAddress(PORT), 0)
        server.createContext("/controller", this::handleCommands)
        server.executor = Executors.newCachedThreadPool()
        server.start()
    }

    private fun handleCommands(exchange: HttpExchange) {
        val method= exchange.requestMethod
        val uriPath = exchange.requestURI.path

        when (method){
            "post" -> {
                when(uriPath) {
                    "/save_config" -> { }
                    "/get_log" -> { }
                }
            }
            "get" -> {
                when(uriPath){
                    "/start" -> { sendResponse(exchange,controller.start())}
                    "/stop" -> {}
                    "/restart" -> {}
                }
            }
        }
    }

    private fun sendResponse(exchange: HttpExchange, responseObject: HttpResponse) {
        exchange.sendResponseHeaders(200, responseObject.message.toByteArray().size.toLong())
        val os = exchange.responseBody
        os.write(responseObject.message.toByteArray())
        os.close()
    }
    private fun getPostAsString(exchange: HttpExchange) : String{
        return exchange.requestBody.bufferedReader().use { it.readText() }
    }
}
