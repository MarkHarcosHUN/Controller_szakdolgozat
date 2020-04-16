package gateway.controller.communication

import com.google.gson.JsonParser
import org.eclipse.paho.client.mqttv3.*
/*
class MqttClass(
    var serviceQuality: Int = 2,
    var broker: String = "tcp://127.0.0.1:1883",
    var clientId: String = "markharcoshun",
    var user: String = "nmark",
    var pass: String = "nmark"
) : MqttCallback {
    lateinit var client: MqttClient
    fun String.toMqttMsg() = MqttMessage(this.toByteArray()).apply { qos = serviceQuality } //.apply { isRetained=true }
    fun publishMsg(
        topic: String,
        msg: String,
        retained: Boolean = false
    ) { //kívülről is lehessen üzenetet küldeni - TESZT ricsinek küldés main.kotlin.main-ból
        val mqttmsg = if (retained) msg.toMqttMsg().apply { isRetained = true } else msg.toMqttMsg()//
        client.publish(topic, mqttmsg)
            .also { println("A Controller által publisholva: Topik: $topic . Üzenet: $msg .") }
    }

    fun subscribe(topic: String) {
        client.subscribe(topic).also { println("Feliratkozva: " + topic) }
    }

    fun unsubscribe(topic: String) {
        client.unsubscribe(topic).also { println("Leiratkozva: " + topic) }
    }

    override fun messageArrived(topic: String?, mqttMessage: MqttMessage?) {
        if (topic == TOPIC_ACK) {
            println("Ack érkezett az író modultól!: " + mqttMessage.toString())
            MonitoringObject.resetCounters()
            //need to create model for ACK msg.
            var ID_FROM_MESSAGE = JsonParser().parse(mqttMessage.toString()).asJsonObject["id"].asInt

            print("BEFORE THE UPDATE : ")
            SQLobject.showRow(ID_FROM_MESSAGE)
            SQLobject.flipSQLID(ID_FROM_MESSAGE)
            print("AFTER THE UPDATE : ")
            SQLobject.showRow(ID_FROM_MESSAGE)
        }
        if (topic == TOPIC_ALIVE) {
            var moduleName = JsonParser().parse(mqttMessage.toString()).asJsonObject["module"].asString
            println("Üzenet érkezett az ALIVE topicra: $moduleName")
            moduleStarted(moduleName)
        }
    }

    override fun connectionLost(p0: Throwable?) = println(p0?.printStackTrace())
    override fun deliveryComplete(p0: IMqttDeliveryToken?) {
        //println("delivery completed!")
    }

    fun start() = try {
        client = MqttClient(broker, clientId).apply {
            println("Csatlakozás az MQTT brókerhez...").run {
                connect(
                    MqttConnectOptions().apply {
                        isCleanSession = true
                        maxInflight = 10
                    }
                )
            }.run { println("Sikeresen csatlakozva az MQTT brókerhez!") }
            setCallback(this@MqttClass)
        }
    } catch (me: MqttException) {
        println("Reason: ${me.reasonCode}\nMessage: ${me.message}").also { me.printStackTrace() }
    }
}
*/
