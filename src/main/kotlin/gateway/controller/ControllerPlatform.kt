package gateway.controller

import java.io.IOException
import com.librato.metrics.client.Throwables.propagate
import io.moquette.broker.Server
import io.moquette.broker.config.IConfig
import io.moquette.broker.config.MemoryConfig
import org.eclipse.paho.client.mqttv3.*
import java.io.File
import java.util.*


class ControllerPlatform : WebCommunicator {
    val webApiSendPort = "4000"
    val webApiRecievePort = "4001"

    private var innerDatabase: String = "inner sql lite"

    private lateinit var webProcess: Process

    val mqttBroker = startBroker()

    private val controller = Controller(innerDatabase, this)


    init {
        setupWebApi()
        var client= MqttClient("tcp://localhost:1883","asd")
        client.connect()
        println(mqttBroker.listConnectedClients())
        onWebMessage("start")
        onWebMessage("start")
        onWebMessage("start")
        onWebMessage("start")
        onWebMessage("stop")
        onWebMessage("stop")
        onWebMessage("restart")
        Thread.sleep(3000)
        onWebMessage("restart")
        Thread.sleep(3000)

        onWebMessage("stop")
        Thread.sleep(3000)
        onWebMessage("start")
    }

    fun setupWebApi() {
/*
        var webProcess=ProcessBuilder("java -jar web.jar",webApiSendPort,webApiReceivePort).also{
            it.start()
        }

 */
    }

    private fun onWebMessage(msg: String) {
        when (msg) {
            "start" -> {
                controller.start()
            }
            "stop" -> {
                controller.stop()
            }
            "restart" -> {
                controller.restart()
            }
            "save_config" -> {
                controller.saveConfig("")
            }
        }
    }

    override fun sendWebReply(msg: String) {
        println(msg)

    }
    private fun startBroker(): Server {
        var server = Server()
        server.startServer(File("moquette.conf"))
        return server
    }
}
