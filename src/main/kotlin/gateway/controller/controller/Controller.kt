package gateway.controller.controller

import com.google.gson.JsonParser
import gateway.controller.models.ControllerConfigurationModel
import gateway.controller.utils.HistoryManager.Companion.updateHistory
import gateway.controller.db.InnerDatabase
import gateway.controller.modules.ModuleController
import gateway.controller.modules.ModuleRegistry
import org.eclipse.paho.client.mqttv3.*
import java.io.File
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

        updateHistory("Gateway indítása folyamatban...")

        controllerConfigurationModel = InnerDatabase.getControllerConfig()
        moduleController = ModuleController()
        TOPIC_ACK = "modules/${controllerConfigurationModel.modules.size}"
        mqttClient=setupMqttClient()

        updateHistory("Modulok indítása megkezdve...")

        try{
            if(startModules()){
                mqttClient.publish(TOPIC_START, MqttMessage("start".toByteArray()))
                supervisionThread = Thread(Supervision()).also { it.start() }
            } else{
                throw Exception("Nem minden modult sikerült elindítani. Próbálkozzon újra.")
            }
        }catch(e: Exception){
            moduleController.killModules()
            throw e
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
        updateHistory("Gateway leállítása...")
        supervisionThread.interrupt()
        updateHistory("Modulok leállítása...")
        stopModules()
        mqttClient.disconnectForcibly()
        Thread.sleep(2000)
    }
    fun saveToDatabase(toString: String) {
    }

    private fun readGatewayConfigurationFromOutterDatabase(): String {
        return "{config}"
    }

    private fun startModules() : Boolean {
        var moduleRegistry=ModuleRegistry(controllerConfigurationModel.connectionOptions)

        File("config/tarolo.json").writeText(moduleRegistry.get("tarolo").getConfig())

        controllerConfigurationModel.modules.forEachIndexed {index, moduleName ->

            var module=moduleRegistry.get(moduleName)

            var command = module.getStartCommand().split(" ").toMutableList().apply {

                add("""$TOPIC_ALIVE""")
                add("""modules/$index""")
                add("""modules/${index + 1}""")
                add(module.getConfig())
            }
            println(command)
            var proc = ProcessBuilder(convertToWin10(command)).start()
            moduleController.addModule(moduleName,proc)

        }
        println("Várakozás a modulok indítására.")
        Thread.sleep(3000)
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
    }

    override fun messageArrived(topic: String?, message: MqttMessage?) {
        if (topic == TOPIC_ALIVE) {
            var moduleName = JsonParser().parse(message.toString()).asJsonObject["module"].asString
            moduleController.setModuleStarted(moduleName)
        } else {

            var id = JsonParser().parse(message.toString()).asJsonObject["id"].asInt
            println(id)
            Class.forName("com.mysql.cj.jdbc.Driver")
                DriverManager.getConnection("jdbc:mysql://"+ gatewayDbConfig.url+"/"+ gatewayDbConfig.name+"?serverTimezone=UTC", gatewayDbConfig.username, gatewayDbConfig.password).use {
                    val stmt = it.createStatement()
                    stmt.executeUpdate("UPDATE ${gatewayDbConfig.table} SET processed=1 where ID=$id")
                }


        }
    }

    override fun connectionLost(cause: Throwable?) {}

    override fun deliveryComplete(token: IMqttDeliveryToken?) {}
}

class Supervision : Runnable {
    override fun run() {
        try {
            println("Felügyeleti szál elindítva...")
            while (true) {
                Thread.sleep(4000)
            }
        } catch (e: InterruptedException) {
            println("Felügyelet megszakítva.")
        }
    }
}



