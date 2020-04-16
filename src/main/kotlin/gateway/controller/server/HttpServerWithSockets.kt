package gateway.controller.server


import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import java.net.InetSocketAddress
import java.util.concurrent.Executors
import io.netty.util.internal.SocketUtils.accept
import java.net.ServerSocket
import io.netty.util.internal.SocketUtils.accept




class HttpServerWithSockets(private val controller: Manageable) :Runnable {
    private val serverSocket: ServerSocket

    override fun run() {

    }

    private val PORT = 8001


    init {

        serverSocket = ServerSocket(PORT, 10)
        /* server.createContext("/", this::handleCommands)
         server.executor = Executors.newCachedThreadPool()
         server.start()

         */
    }
    private fun start(){

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
        exchange.sendResponseHeaders(statusCode, responseObject.message.toByteArray().size.toLong())
        val os = exchange.responseBody
        os.write(responseObject.message.toByteArray())
        os.close()
    }
    private fun getPostAsString(exchange: HttpExchange) : String{
        return exchange.requestBody.bufferedReader().use { it.readText() }
    }
}
