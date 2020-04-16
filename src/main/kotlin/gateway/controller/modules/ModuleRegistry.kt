package gateway.controller.modules

import com.google.gson.Gson
import gateway.controller.controller.mqttServerConfig
import gateway.controller.models.ControllerConfigurationModel.ConnectionOptions
import gateway.controller.models.ModuleConfWrapper
import gateway.controller.models.Range
import gateway.controller.models.ValidationModel
import java.sql.DriverManager
import java.sql.Statement

class ModuleRegistry(val connectionOptions: ConnectionOptions) {

    companion object {
        val validation = "validate"
        val reader = "reader"
        val writer = "writer"
    }

    fun get(moduleName : String) : Module{

        return when(moduleName){
            reader -> Reader()
            writer -> Writer()
            validation -> Validation()
            else -> throw Exception("No module registered with name: $moduleName")
        }
    }
    private inner class Validation : Module,  MySqlConfigAvailable {
        override fun getParams(): String = getMysqlConfig(this)

        override fun getStartCommand(): String = "java -jar modules/validate.jar"

        override fun buildParamsFromDatabase(statement : Statement): String {
            val rs = statement.executeQuery("SELECT validations.min, validations.max, descriptors.code FROM validations inner join sensors on sensors.validation_id=validations.id inner join descriptors on sensors.descriptor_id=descriptors.id inner join stations on sensors.station_id=stations.id where stations.gateway_id=${connectionOptions.gatewayId}")
            var map = HashMap<String, Range>()
            while (rs.next()) {
                map.put(rs.getString(3), Range(rs.getDouble(1), rs.getDouble(2)))
            }
            return Gson().toJson((ValidationModel(-999.9,map)))
        }
    }

    private  inner class Reader : Module ,MySqlConfigAvailable{
        override fun getParams(): String = getMysqlConfig(this)

        override fun getStartCommand(): String = """java -jar modules/reader.jar"""

        override fun buildParamsFromDatabase(statement : Statement): String {
          return """
              |{"waitingPeriod":5000,
              |"limit":10,
              |"sendingPeriod":"1000",
              |"serviceQuality":2,
              |"broker":"${mqttServerConfig.url}",
              |"credentials":
              |{"clientId":"kebabseller6",
              |"user":"${mqttServerConfig.user}",
              |"pass":"${mqttServerConfig.password}"},
              |"database":
              |{"URL":"localhost:3306",
              |"name":"erti_teszt",
              |"username":"root",
              |"password":"password",
              |"table":"erti_test",
              |"attributes":"ID,datum,temperatureat2meter,relativehumidity,table_from"}}""".trimMargin()
        }
    }

    private inner class Writer : Module ,MySqlConfigAvailable{
        override fun getParams(): String =
                """{
                  "broker": "${mqttServerConfig.url}",
                  "serviceQuality": 2,
                  "credentials": {
                    "clientId": "writer",
                    "user": "${mqttServerConfig.user}",
                    "pass": "${mqttServerConfig.password}"
                  },"sensorhub":${getMysqlConfig(this)}}
                """

        override fun getStartCommand(): String = "java -jar modules/writer.jar"

        override fun buildParamsFromDatabase(statement : Statement): String {
            val rs = statement.executeQuery(" SELECT output_parameters FROM gateways where id=1")
            rs.next()
            return rs.getString(1)
        }
    }

    private fun getMysqlConfig(module : MySqlConfigAvailable) : String{
        Class.forName("com.mysql.cj.jdbc.Driver")
        DriverManager.getConnection(connectionOptions.url, connectionOptions.user, connectionOptions.password).use {
            val stmt = it.createStatement()
            return module.buildParamsFromDatabase(stmt)
        }
    }

}
interface Module {
    fun getStartCommand() : String
    fun getParams() : String
}
interface MySqlConfigAvailable {
    fun buildParamsFromDatabase(statement : Statement) : String
}

