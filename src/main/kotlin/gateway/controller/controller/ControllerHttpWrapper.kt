package gateway.controller.controller

import com.google.gson.JsonSyntaxException
import gateway.controller.models.ControllerConfigurationModel
import gateway.controller.models.GetLogCommandModel
import gateway.controller.server.HttpResponse
import gateway.controller.server.Manageable
import gateway.controller.utils.HistoryManager
import gateway.controller.utils.HistoryManager.Companion.getHistory
import gateway.controller.utils.convertFromJson

class ControllerHttpWrapper(var controller: Controller) : Manageable {
    private var controllerState: ControllerState = ControllerState.NOT_RUNNING

    override fun start(): HttpResponse {
        // start command only allowed when the gateway is in 'not running' state.
        synchronized(this) {
            if (controllerState != ControllerState.NOT_RUNNING) {
                return HttpResponse("Cant start, because: ${controllerState}")
            }
            controllerState = ControllerState.INITIALIZING
        }
        try {
            controller.setupAndStart()
            controllerState = ControllerState.RUNNING

        } catch (e: Exception) {
            controllerState = ControllerState.NOT_RUNNING
            HistoryManager.updateHistory("Failed to start gateway: ${e.localizedMessage}")
            return HttpResponse(e.localizedMessage)
        }
        return HttpResponse("Gateway started successfully...")
}

    override fun stop(): HttpResponse {
        //stop command only allowed when the gateway is in 'running' state.
        synchronized(this) {
            if (controllerState != ControllerState.RUNNING) {
                return HttpResponse("Cant stop, because: $controllerState")
            }
            controllerState = ControllerState.TERMINATING
        }

        controller.shutdown()
        controllerState = ControllerState.NOT_RUNNING

        return HttpResponse("Gateway stopped.")
    }

    override fun restart(): HttpResponse {
        synchronized(this) {
            if (controllerState != ControllerState.RUNNING) {
                return HttpResponse("Cant restart, because: $controllerState")
            }
            controllerState = ControllerState.TERMINATING
        }

        controller.apply {
            shutdown()
            controllerState = ControllerState.INITIALIZING
            try {
                setupAndStart()
                controllerState = ControllerState.RUNNING
            } catch (ce: Exception) {
                controllerState = ControllerState.NOT_RUNNING
                return HttpResponse(ce.localizedMessage)
            }
        }

        return HttpResponse("Gateway is running again.")
    }

    override fun saveConfig(postJson: String): HttpResponse {
        // to save the configuration the state should be either 'running' or 'not running',
        // during initialization it may cause problems.
        synchronized(this) {
            if (controllerState == ControllerState.INITIALIZING || controllerState == ControllerState.TERMINATING) {
                return HttpResponse("Cant save config, because: $controllerState")
            }
            // check if the format of the post request ok.
            val model = try {
                convertFromJson(postJson, ControllerConfigurationModel::class.java)
            } catch (jse: JsonSyntaxException) {
                return HttpResponse("Bad configuration format.") // not json
            } ?: return HttpResponse("Empty Body") // empty post

            if (model.isValid()) {
                controller.saveControllerConfig(postJson)
                return HttpResponse("Configuration saved!")
            }
            else return HttpResponse("Invalid configuration.") // not all required parameter set
        }
    }

    override fun getLog(postJson: String): HttpResponse {
        var model = try {
            convertFromJson(postJson, GetLogCommandModel::class.java)
        } catch (jse: JsonSyntaxException) {
            return HttpResponse("Bad request format.")
        } ?: return HttpResponse("Empty body")

        return HttpResponse(getHistory(model.amount))
    }

}
enum class ControllerState {
    NOT_RUNNING, RUNNING, INITIALIZING, TERMINATING
}
