package gateway.controller.controller


import gateway.controller.models.GatewayDbConfigurationModel
import gateway.controller.models.MqttServerConfigModel
import io.moquette.broker.Server
import gateway.controller.server.HttpServer
import gateway.controller.utils.parseJsonTo
import gateway.controller.utils.readFileDirectlyAsText
import org.eclipse.paho.client.mqttv3.*
import java.io.File

val mqttServerConfig  = parseJsonTo<MqttServerConfigModel>(readFileDirectlyAsText("config/mqtt.json"))
val gatewayDbConfig = parseJsonTo<GatewayDbConfigurationModel>(readFileDirectlyAsText("config/gateway_db.json"))

class ControllerPlatform {
    val mqttBroker = startBroker()
    private val controller = Controller()
    private val httpServer = HttpServer(ControllerHttpWrapper(controller))

    init{
        // register client to monitor mqtt activity
        var client=CouriousClient()
    }
    private fun startBroker(): Server {
        var server = Server()
        server.startServer(File("moquette.conf"))
        return server
    }
}
class CouriousClient : MqttCallback{
    private val client = MqttClient("tcp://${mqttServerConfig.url}", "courious")
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

    override fun connectionLost(cause: Throwable?) {}

    override fun deliveryComplete(token: IMqttDeliveryToken?) {}

}

