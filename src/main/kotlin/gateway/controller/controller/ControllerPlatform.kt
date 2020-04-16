package gateway.controller.controller


import com.google.gson.Gson
import gateway.controller.models.ModuleParametersModel
import gateway.controller.models.MqttServerConfig
import io.moquette.broker.Server
import gateway.controller.server.HttpServer
import gateway.controller.utils.parseJsonTo
import gateway.controller.utils.readFileDirectlyAsText
import io.moquette.broker.config.IConfig
import org.eclipse.paho.client.mqttv3.*
import java.io.File

val mqttServerConfig  =parseJsonTo<MqttServerConfig>(readFileDirectlyAsText("config/mqtt.json"))

class ControllerPlatform {
    val mqttBroker = startBroker()
    private val controller = Controller()
    private val httpServer = HttpServer(ControllerHttpWrapper(controller))

    init{

      //  println(ModuleConfigFromMysql(InnerDatabase.getControllerConfig().connectionOptions).getConfig("validation"))
        var client=CouriousClient()
        //Connect().conn()
        //start that fuck!
        //val availabeModules = Gson().fromJson(readFileDirectlyAsText("config.json"), ModuleParametersModel::class.java)
      //  controller.setupAndStart()
    }
    private fun startBroker(): Server {
        var server = Server()
        server.startServer(File("moquette.conf"))
        return server
    }
}
class CouriousClient() : MqttCallback{
    private val client = MqttClient("tcp://localhost:1883", "courious")
        .apply {
            connect(MqttConnectOptions().apply {
                isCleanSession = true
            })
            setCallback(this@CouriousClient)
            subscribe("#")
        }
    override fun messageArrived(topic: String?, message: MqttMessage?) {
        println("$topic : ${message.toString()}")
    }

    override fun connectionLost(cause: Throwable?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun deliveryComplete(token: IMqttDeliveryToken?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}

