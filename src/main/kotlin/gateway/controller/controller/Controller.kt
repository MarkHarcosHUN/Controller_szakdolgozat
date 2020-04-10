package gateway.controller.controller

import gateway.controller.models.ControllerConfigurationModel
import org.eclipse.paho.client.mqttv3.*
import gateway.controller.utils.HistoryManager.Companion.updateHistory
import gateway.controller.utils.HistoryManager.Companion.getHistory
import gateway.controller.db.InnerDatabase
import gateway.controller.db.InnerDatabaseImpl
import gateway.controller.server.HttpResponse
import gateway.controller.server.Manageable
import gateway.controller.utils.convertFromJson

class Controller {

    var innerDatabase: InnerDatabase = InnerDatabaseImpl()
    internal var controllerState: ControllerState = ControllerState.NOT_RUNNING
    private var supervisionThread: Thread = Thread(Supervision())
    private lateinit var controllerConfigurationModel: ControllerConfigurationModel
    //private var mqttClient = MqttClient("tcp://localhost:1883", "controller").also { it.setCallback(this) }

    internal fun setupAndStart() {
        controllerConfigurationModel = buildControllerConfigurationFromInnerDatabase()
        setupMqttClient()
        startModules(readGatewayConfigurationFromOutterDatabase())
        supervisionThread = Thread(Supervision()).also { it.start() }
    }
    private fun setupMqttClient() {
      //  mqttClient.connect()
      //  mqttClient.subscribe("lol")
     //   mqttClient.publish("lol", MqttMessage("anyád".toByteArray()))
    }

    internal fun shutdown() {
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
        return convertFromJson(innerDatabase.get(InnerDatabase.CONFIG_KEY),ControllerConfigurationModel::class.java)
    }

    private fun stopModules() {
        println("Stopping modules blocking thread...")
        Thread.sleep(1000)
    }

    internal fun saveControllerConfig(configJson: String) {
        innerDatabase.save(InnerDatabase.CONFIG_KEY,configJson)
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

