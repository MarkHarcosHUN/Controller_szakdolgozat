package gateway.controller

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import java.net.InetSocketAddress
import java.util.concurrent.Executors

class HttpControllerManager(private val controller: Controller) {
    private val PORT = 8001
    private val server : HttpServer

    init {
        server = HttpServer.create(InetSocketAddress( PORT),0)
        server.createContext("/", this::handleCommands)
        server.executor = Executors.newCachedThreadPool()
        server.start()
    }

    private fun handleCommands(exchange : HttpExchange) {
        when(exchange.requestURI.path){
            "/start" -> {
                sendReply(exchange,controller.start())
            }
            "/stop" -> {
                sendReply(exchange,controller.stop())
            }
            "/restart" -> {
                sendReply(exchange,controller.restart())
            }
            "/save_config" -> {
                sendReply(exchange,controller.saveConfig(""))
            }
        }
    }

    private fun sendReply(exchange: HttpExchange, responseObject: ResponseObject) {
        // val requestURI = exchange.requestURI
        val response = responseObject.message
        exchange.sendResponseHeaders(200,response.toByteArray().size.toLong())
        val os = exchange.responseBody
        os.write(response.toByteArray())
        os.close()
    }
}
