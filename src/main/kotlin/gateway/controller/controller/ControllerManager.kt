package gateway.controller.controller

import gateway.controller.server.HttpResponse
import gateway.controller.server.Manageable
import gateway.controller.utils.HistoryManager

class ControllerManager(var controller : Controller) : Manageable {

    override fun start(): HttpResponse {
        // start command only allowed when the gateway is in 'not running' state.
        synchronized(this) {
            if (controller.controllerState != ControllerState.NOT_RUNNING) {
                return HttpResponse("Cant start, because: ${controller.controllerState}")
            }
            controller.controllerState = ControllerState.INITIALIZING
        }

            controller.apply{
                setupAndStart()
                controllerState = ControllerState.RUNNING
            }

        return HttpResponse("Gateway started successfully...")
    }

    override fun stop(): HttpResponse {
        //stop command only allowed when the gateway is in 'running' state.
        synchronized(this) {
            if (controller.controllerState != ControllerState.RUNNING) {
                return HttpResponse("Cant stop, because: ${controller.controllerState}")
            }
            controller.controllerState = ControllerState.TERMINATING
        }

        controller.apply {
            shutdown()
            controllerState = ControllerState.NOT_RUNNING
        }

        return HttpResponse("Gateway stopped.")
    }

    override fun restart(): HttpResponse {
        synchronized(this) {
            if (controller.controllerState != ControllerState.RUNNING) {
                return HttpResponse("Cant restart, because: ${controller.controllerState}")
            }
            controller.controllerState = ControllerState.TERMINATING
        }

        controller.apply {
            shutdown()
            controllerState = ControllerState.INITIALIZING
            setupAndStart()
            controllerState = ControllerState.RUNNING
        }

        return HttpResponse("Gateway is running again.")
    }

    override fun saveConfig(postJson: String): HttpResponse {
        // to save the configuration the state should be either 'running' or 'not running',
        // during initialization it may cause problems.
        synchronized(this) {
            if (controller.controllerState == ControllerState.INITIALIZING || controller.controllerState == ControllerState.TERMINATING) {
                return HttpResponse("Cant save config, because: ${controller.controllerState}")
            }
            // CHECK POSTJSON saveControllerConfig(config)
            controller.saveControllerConfig(postJson)
            return HttpResponse("Configuration saved")
        }
    }

    override fun getLog(postJson: String): HttpResponse {
        // CHECK POSTJSON getHistory(amount)
        var history =""
        return HttpResponse(history)
    }


}
