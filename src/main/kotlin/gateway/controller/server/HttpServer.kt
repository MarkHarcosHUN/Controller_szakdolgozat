package gateway.controller.server


import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import gateway.controller.utils.HistoryManager
import java.net.InetSocketAddress
import java.util.concurrent.Executors


class HttpServer(private val controller: Manageable) {

    private val PORT = 8001
    private val server: HttpServer

    init {
        server = HttpServer.create(InetSocketAddress(PORT), 0)
        server.createContext("/", this::handleCommands)
        server.executor = Executors.newCachedThreadPool()
        server.start()
    }

    private fun handleCommands(exchange: HttpExchange) {
        val method= exchange.requestMethod
        val uriPath = exchange.requestURI.path

        when (method){
            "POST" -> {
                when(uriPath) {
                    "/save_config" -> { sendResponse(exchange, controller.saveConfig(getPostAsString(exchange)))}
                    "/get_log" -> { sendResponse(exchange, controller.getLog(getPostAsString(exchange)))}
                    else -> {
                        sendResponse(exchange, HttpResponse("FORBIDDEN"),403)
                    }
                }
            }
            "GET" -> {
                when(uriPath){
                    "/start" -> { sendResponse(exchange,controller.start())}
                    "/stop" -> {sendResponse(exchange,controller.stop())}
                    "/restart" -> {sendResponse(exchange,controller.restart())}
                    else -> {
                        sendResponse(exchange,HttpResponse("FORBIDDEN"),403)
                    }
                }
            }
            else -> {
                sendResponse(exchange,HttpResponse("FORBIDDEN"),403)
            }
        }
    }

    private fun sendResponse(exchange: HttpExchange, responseObject: HttpResponse,statusCode : Int = 200) {
        HistoryManager.updateHistory(responseObject.message)
        try{
            exchange.sendResponseHeaders(statusCode, responseObject.message.toByteArray().size.toLong())
            val os = exchange.responseBody
            os.write(responseObject.message.toByteArray())
            os.close()
        } catch (e : Exception){
            HistoryManager.updateHistory("A problem occurred during sending response to a client.")
        }

    }
    private fun getPostAsString(exchange: HttpExchange) : String{
        return exchange.requestBody.bufferedReader().use { it.readText() }
    }
}
