package gateway.controller.modules

import com.google.gson.Gson
import gateway.controller.controller.gatewayDbConfig
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
        val tarolo = "tarolo" // Not standrad modul, unable to start
    }

    fun get(moduleName : String) : Module{

        return when(moduleName){
            reader -> Reader()
            writer -> Writer()
            validation -> Validation()
            tarolo -> Tarolo()
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
                map.put(
                    rs.getString("code"),
                    Range(
                        rs.getDouble("min"),
                        rs.getDouble("max")
                    ))
            }
            return Gson().toJson((ValidationModel(-999.9,map)))
        }
    }

    private  inner class Reader : Module ,MySqlConfigAvailable{

        override fun getParams(): String =
                """
                |{"waitingPeriod":2000,
                |"limit":10,
                |"sendingPeriod":"1000",
                |"serviceQuality":2,
                |"broker":"${mqttServerConfig.url}",
                |"credentials":
                |{"clientId":"reader",
                |"user":"${mqttServerConfig.user}",
                |"pass":"${mqttServerConfig.password}"},
                |"database":
                |{"URL":"${gatewayDbConfig.url}",
                |"name":"${gatewayDbConfig.name}",
                |"username":"${gatewayDbConfig.username}",
                |"password":"${gatewayDbConfig.password}",
                |"table":"${gatewayDbConfig.table}",
                |"attributes":"ID,datum,temperatureat2meter,relativehumidity,table_from"}}""".trimMargin()

        override fun getStartCommand(): String = """java -jar modules/reader.jar"""

        override fun buildParamsFromDatabase(statement : Statement): String {
          return ""
        }
    }
    private inner class Tarolo : Module,  MySqlConfigAvailable {
        override fun getParams(): String {
            return getMysqlConfig(this)
        }

        override fun getStartCommand(): String = throw Exception("Tarolo modul nem indítható controller által")

        override fun buildParamsFromDatabase(statement : Statement): String {



            return """{
                    "init": {
                    "limit": "10",
                    "period": "100"
                    },
                    "sourceDatabases": [
                    {
                      "URL": " 217.61.5.143:3306",
                      "name": "erti",
                      "username": "erti",
                      "password": "Ert1dat4",
                      "tables": [
                        "erti_j1",
                        "erti_j2",
                        "erti_j3",
                        "erti_j4",
                        "erti_j5",
                        "erti_j6"
                      ]
                    }
                    ],
                    "targetDatabase": {
                    "URL": "localhost:3306",
                    "name": "erti_teszt",
                    "username": "root",
                    "password": "password",
                    "table": "erti_test"
                    },
                    "columnConversion": [
                    {
                      "realColumnName": "temperatureat2meter",
                      "originalColumnName": "v1"
                    },
                    {
                      "realColumnName": "relativehumidity",
                      "originalColumnName": "v2"
                    },
                    {
                      "realColumnName": "datum",
                      "originalColumnName": "datum"
                    }
                    ]
                    }
                    """
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
        DriverManager.getConnection("jdbc:mysql://"+connectionOptions.url+"/"+connectionOptions.db+"?serverTimezone=UTC", connectionOptions.user, connectionOptions.password).use {
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

