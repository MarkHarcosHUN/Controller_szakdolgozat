package gateway.controller.controller

import com.google.gson.JsonParser
import gateway.controller.models.ControllerConfigurationModel
import gateway.controller.utils.HistoryManager.Companion.updateHistory
import gateway.controller.db.InnerDatabase
import gateway.controller.modules.ModuleController
import gateway.controller.modules.ModuleRegistry
import org.eclipse.paho.client.mqttv3.*
import java.sql.DriverManager

val TOPIC_ALIVE = "modules/alive"
val TOPIC_START = "modules/0"
// Ack topic depends on the number of modules in the process.
lateinit var TOPIC_ACK : String

class Controller : MqttCallback{

    private var supervisionThread: Thread = Thread(Supervision())
    private lateinit var controllerConfigurationModel: ControllerConfigurationModel
    private lateinit var moduleController : ModuleController

    private lateinit var mqttClient : MqttClient

    internal fun setupAndStart() {
        updateHistory("Starting gateway...")
        controllerConfigurationModel = InnerDatabase.getControllerConfig()
        moduleController = ModuleController()
        TOPIC_ACK = "modules/${controllerConfigurationModel.modules.size}"
        mqttClient=setupMqttClient()

        updateHistory("Starting modules...")

        if(startModules()){
            mqttClient.publish(TOPIC_START, MqttMessage("start".toByteArray()))
            supervisionThread = Thread(Supervision()).also { it.start() }
            updateHistory("Gateway functionality started.")
        } else{
            moduleController.killModules()
            throw Exception("Unable to start modules.")
        }
    }
    private fun setupMqttClient() =
        MqttClient("tcp://${mqttServerConfig.url}", "controller")
            .apply {
                connect(MqttConnectOptions().apply {
                    isCleanSession = true
                })

                setCallback(this@Controller)
                subscribe(TOPIC_ALIVE)
                subscribe(TOPIC_ACK)
            }


    internal fun shutdown() {
        updateHistory("Shutting down gateway....")
        supervisionThread.interrupt()
        updateHistory("Stopping modules...")
        stopModules()
        mqttClient.disconnectForcibly()
        updateHistory("Gateway stopped.")
    }
    fun saveToDatabase(toString: String) {
    }

    private fun readGatewayConfigurationFromOutterDatabase(): String {
        return "{config}"
    }

    private fun startModules() : Boolean {
        var moduleRegistry=ModuleRegistry(controllerConfigurationModel.connectionOptions)

        controllerConfigurationModel.modules.forEachIndexed {index, moduleName ->

            var module=moduleRegistry.get(moduleName)

            var command = module.getStartCommand().split(" ").toMutableList().apply {

               // add("""{"topicAlive":"${moduleController.TOPIC_ALIVE}"}""")
              //  add("""{"topicToSubscribe":"modules/$index"}""")
              //  add("""{"topicToPublish":"modules/${index + 1}"}""")
                add("""$TOPIC_ALIVE""")
                add("""modules/$index""")
                add("""modules/${index + 1}""")
                add(module.getParams())
            }

            var proc = ProcessBuilder(convertToWin10(command)).start()
            moduleController.addModule(moduleName,proc)

        }
        println("Wait for modules to start")
        Thread.sleep(5000)
        return moduleController.isAllModuleStarted()
    }

    private fun convertToWin10(command: MutableList<String>): MutableList<String> {
        return command.map{ i-> i .replace("\"","\\\"")}.toMutableList()
    }


    private fun stopModules() {
        moduleController.killModules()
    }

    internal fun saveControllerConfig(configJson: String) {
        InnerDatabase.save(InnerDatabase.configKey,configJson)
        updateHistory("New configuration saved.")
    }

    override fun messageArrived(topic: String?, message: MqttMessage?) {
        if (topic == TOPIC_ALIVE) {
            var moduleName = JsonParser().parse(message.toString()).asJsonObject["module"].asString
            moduleController.setModuleStarted(moduleName)
        } else {

            var id = JsonParser().parse(message.toString()).asJsonObject["id"].asInt
            println(id)
            Class.forName("com.mysql.cj.jdbc.Driver")
                DriverManager.getConnection(controllerConfigurationModel.connectionOptions.url, controllerConfigurationModel.connectionOptions.user, controllerConfigurationModel.connectionOptions.password).use {
                    val stmt = it.createStatement()
                    stmt.executeUpdate("UPDATE erti_teszt.erti_test SET processed=1 where ID=$id")
                }


        }
    }

    override fun connectionLost(cause: Throwable?) {}

    override fun deliveryComplete(token: IMqttDeliveryToken?) {}
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



