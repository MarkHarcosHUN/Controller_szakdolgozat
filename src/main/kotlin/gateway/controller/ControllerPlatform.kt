package gateway.controller

import io.moquette.broker.Server
import java.io.File

class ControllerPlatform {
    val mqttBroker = startBroker()
    private val controller = Controller()
    private val httpControllerManager = HttpControllerManager(controller)

    private fun startBroker(): Server {
        var server = Server()
        server.startServer(File("moquette.conf"))
        return server
    }
}
