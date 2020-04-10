package gateway.controller.controller


import io.moquette.broker.Server
import gateway.controller.server.HttpServer
import java.io.File



class ControllerPlatform {
    val mqttBroker = startBroker()
    private val controller = Controller()
    private val httpServer = HttpServer(ControllerManager(controller))

    init{

    }
    private fun startBroker(): Server {
        var server = Server()
        server.startServer(File("moquette.conf"))
        return server
    }
}
