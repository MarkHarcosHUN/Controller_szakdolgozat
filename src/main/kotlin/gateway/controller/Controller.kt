package gateway.controller

import kotlin.concurrent.thread

class Controller : Manageable {
    private lateinit var controllerConfigurationModel: ControllerConfigurationModel
    private var controllerState: ControllerState
    private val webApi: WebApi
    private var testValue = 1

    init {
        controllerState = ControllerState.NOT_RUNNING
        webApi = WebApi(this).also {
            thread(start = true) {
                it.startListening()
            }
        }

        start(ControllerConfigurationModel(listOf("")))
    }

    override fun start(controllerConfigurationModel: ControllerConfigurationModel) {
        if (controllerState != ControllerState.NOT_RUNNING) throw ControllerException("Cant start: $controllerState")
        println("Starting gateway...")
        thread(start = true) {
            setup(controllerConfigurationModel)
            startSupervision()
            cleanup()
        }
    }

    override fun stop() {
        if (controllerState != ControllerState.RUNNING) {
            throw ControllerException("Cant stop: $controllerState")
        }

        println("stopping gateway...")
        controllerState = ControllerState.TERMINATING
    }

    override fun restart(controllerConfigurationModel: ControllerConfigurationModel) {
        if (controllerState != ControllerState.RUNNING) {
            throw ControllerException("Cant restart: $controllerState")
        }

        thread(start = true) {
            print("Restarting ...")
            stop()
            while (controllerState != ControllerState.NOT_RUNNING) {
                println(" waiting for modules to stop")
                Thread.sleep(1000)
            }
            // thread a threadben..lehet memory leak
            start(controllerConfigurationModel)
        }
    }

    private fun cleanup() {
        println("stopping gateway modules...")
        stopModules()
        controllerState = ControllerState.NOT_RUNNING
        println("Gateway stopped")
    }

    private fun setup(model: ControllerConfigurationModel) {
        controllerState = ControllerState.INITIALIZING
        controllerConfigurationModel = model
        startModules(fillModelFromDatabase())
    }

    @Suppress("UNUSED_PARAMETER")
    private fun startModules(model: GatewayConfigurationModel) {
        println("Starting modules")
    }

    private fun startSupervision() {
        println("Supervision started")
        controllerState = ControllerState.RUNNING
        while (controllerState == ControllerState.RUNNING) {
            println("testvalue: ${testValue++}")
            Thread.sleep(1000)
        }
    }

    private fun fillModelFromDatabase(): GatewayConfigurationModel {
        println("Reading configuration database")
        return GatewayConfigurationModel()
    }

    @Suppress("unused")
    private fun createControllerConfigurationModel(): ControllerConfigurationModel {
        return ControllerConfigurationModel(listOf(""))
    }

    private fun stopModules() {
        Thread.sleep(3000)
    }
}

interface Manageable {
    fun start(controllerConfigurationModel: ControllerConfigurationModel)
    fun stop()
    fun restart(controllerConfigurationModel: ControllerConfigurationModel)
}

enum class ControllerState {
    NOT_RUNNING, RUNNING, INITIALIZING, TERMINATING
}
