package gateway.controller

import kotlin.concurrent.thread

class Controller(private val innerDatabase: String, val webCommunicator: WebCommunicator) : Manageable {

    private var controllerState: ControllerState = ControllerState.NOT_RUNNING
    private var supervisionThread: Thread = Thread(Supervision())
    private lateinit var controllerConfigurationModel: ControllerConfigurationModel

    private fun setupAndStart() {
        controllerConfigurationModel = buildControllerConfigurationFromInnerDatabase()
        startModules(readGatewayConfigurationFromOutterDatabase())
        supervisionThread = Thread(Supervision()).also { it.start() }
    }

    private fun shutdown() {
        supervisionThread.interrupt()
        stopModules()
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

    override fun saveConfig(config: String) {
        synchronized(this) {
            if (controllerState == ControllerState.INITIALIZING) {
                webCommunicator.sendWebReply("Cant save config, because: $controllerState")
                return
            }
            thread {
                saveControllerConfig(config)
            }
        }
    }

    override fun start() {
        synchronized(this) {
            if (controllerState != ControllerState.NOT_RUNNING) {
                webCommunicator.sendWebReply("Cant start, because: $controllerState")
                return
            }
            controllerState = ControllerState.INITIALIZING
        }
        thread {
            setupAndStart()
            controllerState = ControllerState.RUNNING
        }
    }

    override fun stop() {
        synchronized(this) {
            if (controllerState != ControllerState.RUNNING) {
                webCommunicator.sendWebReply("Cant stop, because: $controllerState")
                return
            }
            controllerState = ControllerState.TERMINATING
        }
        thread {
            shutdown()
            controllerState = ControllerState.NOT_RUNNING
        }
    }

    override fun restart() {
        synchronized(this) {
            if (controllerState != ControllerState.RUNNING) {
                webCommunicator.sendWebReply("Cant restart, because: $controllerState")
                return
            }
            controllerState = ControllerState.TERMINATING
        }
        thread {
            shutdown()
            controllerState = ControllerState.INITIALIZING
            setupAndStart()
            controllerState = ControllerState.RUNNING
        }
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
    fun start()
    fun stop()
    fun restart()
    fun saveConfig(config: String)
}
