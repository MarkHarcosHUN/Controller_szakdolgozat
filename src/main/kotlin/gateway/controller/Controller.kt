package gateway.controller

import kotlin.concurrent.thread
import org.eclipse.paho.client.mqttv3.*

class Controller() : Manageable, MqttCallback {
    override fun messageArrived(topic: String?, message: MqttMessage?) {
       when (topic) {
           "save" -> saveToDatabase(message.toString())
           // "connect" -> modules.get(message.toString()).setConnected()
       }
    }
    override fun connectionLost(cause: Throwable?) {
        TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
    }

    override fun deliveryComplete(token: IMqttDeliveryToken?) {
        TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
    }

    private var innerDatabase: InnerDatabase = InnerDatabaseImpl()
    private var controllerState: ControllerState = ControllerState.NOT_RUNNING
    private var supervisionThread: Thread = Thread(Supervision())
    private lateinit var controllerConfigurationModel: ControllerConfigurationModel
    private var mqttClient = MqttClient("tcp://localhost:1883", "controller").also { it.setCallback(this) }

    private fun setupAndStart() {
        controllerConfigurationModel = buildControllerConfigurationFromInnerDatabase()
        setupMqttClient()
        startModules(readGatewayConfigurationFromOutterDatabase())
        supervisionThread = Thread(Supervision()).also { it.start() }
    }
    private fun setupMqttClient() {
        mqttClient.connect()
        mqttClient.subscribe("lol")
        mqttClient.publish("lol", MqttMessage("anyád".toByteArray()))
    }

    private fun shutdown() {
        supervisionThread.interrupt()
        stopModules()
    }
    fun saveToDatabase(toString: String) {
    }

    private fun readGatewayConfigurationFromOutterDatabase(): String {
        return "{config}"
    }

    private fun startModules(conf: String) {
        println("Starting modules blocking thread...")
        controllerConfigurationModel
        conf
        for (i in 1..5) {

            println("Starting modules blocking thread...")
            Thread.sleep(500)
        }
    }

    private fun buildControllerConfigurationFromInnerDatabase(): ControllerConfigurationModel {
        innerDatabase
        return ControllerConfigurationModel(listOf())
    }

    private fun stopModules() {
        println("Stopping modules blocking thread...")
        Thread.sleep(1000)
    }

    private fun saveControllerConfig(config: String) {
        // insert config into database
    }

    override fun saveConfig(config: String): ResponseObject {
        synchronized(this) {
            if (controllerState == ControllerState.INITIALIZING || controllerState == ControllerState.TERMINATING) {
                return ResponseObject("Cant save config, because: $controllerState", false)
            }
            saveControllerConfig(config)
            return ResponseObject("Configuration saved", true)
        }
    }

    override fun start(): ResponseObject {
        synchronized(this) {
            if (controllerState != ControllerState.NOT_RUNNING) {
                return ResponseObject("Cant start, because: $controllerState", false)
            }
            controllerState = ControllerState.INITIALIZING
        }
        thread {
            setupAndStart()
            controllerState = ControllerState.RUNNING
        }
        return ResponseObject("Gateway starting...", true)
    }

    override fun stop(): ResponseObject {
        synchronized(this) {
            if (controllerState != ControllerState.RUNNING) {
                return ResponseObject("Cant stop, because: $controllerState", false)
            }
            controllerState = ControllerState.TERMINATING
        }
        thread {
            shutdown()
            controllerState = ControllerState.NOT_RUNNING
        }
        return ResponseObject("Gateway terminating...", true)
    }

    override fun restart(): ResponseObject {
        synchronized(this) {
            if (controllerState != ControllerState.RUNNING) {
                return ResponseObject("Cant restart, because: $controllerState", false)
            }
            controllerState = ControllerState.TERMINATING
        }
        thread {
            shutdown()
            controllerState = ControllerState.INITIALIZING
            setupAndStart()
            controllerState = ControllerState.RUNNING
        }
        return ResponseObject("Gateway restarting...", true)
    }
}

class Supervision : Runnable {
    override fun run() {
        try {
            while (true) {
                println("supervision communicate with modules")
                Thread.sleep(4000)
            }
        } catch (e: InterruptedException) {
            println("supervision interrupted!")
        }
    }
}

enum class ControllerState {
    NOT_RUNNING, RUNNING, INITIALIZING, TERMINATING
}

interface Manageable {
    fun start(): ResponseObject
    fun stop(): ResponseObject
    fun restart(): ResponseObject
    fun saveConfig(config: String): ResponseObject
}
data class ResponseObject(var message: String, var success: Boolean)
