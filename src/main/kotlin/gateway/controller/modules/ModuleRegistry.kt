package gateway.controller.modules

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import gateway.controller.controller.gatewayDbConfig
import gateway.controller.controller.mqttServerConfig
import gateway.controller.models.*
import gateway.controller.models.ControllerConfigurationModel.ConnectionOptions
import gateway.controller.utils.convertFromJson
import java.sql.DriverManager
import java.sql.Statement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import gateway.controller.utils.makeJsonPretty

/*
This is the Module registry
All module should be defined here
This is a replacement of the config file which support reaching data from sql
 */

class ModuleRegistry(val connectionOptions: ConnectionOptions) {

    companion object {
        val validation = "validate"
        val reader = "reader"
        val writer = "writer"
        val tarolo = "tarolo" // Not standrad modul, unable to start
        val converter = "converter"
    }

    fun get(moduleName : String) : Module{

        return when(moduleName){
            reader -> Reader()
            writer -> Writer()
            validation -> Validation()
            tarolo -> Tarolo()
            converter -> Converter()
            else -> throw Exception("No module registered with name: $moduleName")
        }
    }
    // modules with mysql config build

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

    private  inner class Converter : Module ,MySqlConfigAvailable{

        override fun getParams(): String =
            """{
                "convertTemperature": ${getMysqlConfig(this)},
                  "convertLength": {},
                  "errorCodes": [
                    -999.9,-888.8
                  ]
                }"""

        override fun getStartCommand(): String = """java -jar modules/converter.jar"""

        override fun buildParamsFromDatabase(statement : Statement): String {
            var rs = statement.executeQuery("""SELECT fromUnit.unit as 'from',toUnit.unit as 'to', descriptors.code as 'sensor',stations.name, input_parameters.details FROM stations
                    inner join sensors on stations.id=sensors.station_id
                    inner join input_parameters on stations.input_parameter_id=input_parameters.id
                    inner join descriptors on sensors.descriptor_id=descriptors.id
                    inner join sensor_types on sensors.sensor_type_id=sensor_types.id
                    inner join units fromUnit on fromUnit.id=sensor_types.unit_id
                    inner join units toUnit on toUnit.id=descriptors.unit_id
                    where gateway_id=${connectionOptions.gatewayId};""")

            var conversionMap = HashMap<String,UnitConversion>()

            while (rs.next()) {
                // table doesnt need because the current implementation of conversion only can handle 1 conversion/column
                var table = convertFromJson(rs.getString("details"),SourceDatabaseDetails::class.java)!!.table
                val from = rs.getString("from")
                val to = rs.getString("to")
                val sensor = rs.getString("sensor")
                if(from != to){
                    conversionMap.put(sensor, UnitConversion(from,to))
                }

            }

            return Gson().toJson(conversionMap)
        }
    }
    private  inner class Reader : Module {

        override fun getParams(): String =
                """
                |{"waitingPeriod":5000,
                |"limit":10,
                |"sendingPeriod":"800",
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


    }

    private inner class Tarolo : Module,  MySqlConfigAvailable {
        override fun getParams(): String {
            return makeJsonPretty(getMysqlConfig(this))
        }

        override fun getStartCommand(): String = throw Exception("Tarolo modul nem indítható controller által")

        override fun buildParamsFromDatabase(statement: Statement): String {
            var rs =
                statement.executeQuery("SELECT sensors.code as 'original', descriptors.code as 'real' FROM sensors inner join descriptors on sensors.descriptor_id=descriptors.id inner join stations on sensors.station_id=stations.id where gateway_id=${connectionOptions.gatewayId};")
            var arrayOfConversions = ArrayList<Conversion>()
            while (rs.next()) {
                arrayOfConversions.add(
                    Conversion(rs.getString("real"),rs.getString("original"))
                )
            }
            // datum need to add manually because not db parameter, but its essential.
            arrayOfConversions.add(Conversion("datum","datum"))

            var columnConversion = Gson().toJson(arrayOfConversions)

            rs = statement.executeQuery("SELECT details FROM stations inner join input_parameters on stations.input_parameter_id = input_parameters.id where stations.gateway_id=${connectionOptions.gatewayId};")

            val listOfTables=ArrayList<String>()
            lateinit var details : SourceDatabaseDetails
            while (rs.next()) {
                details=convertFromJson(rs.getString("details"),SourceDatabaseDetails::class.java)!!
                listOfTables.add(details.table)
            }

            val jsonListOfTables=Gson().toJson(listOfTables)
            return """
                    {"init": {"limit": "10","period": "100"},
                    "sourceDatabases": [
                        {
                          "URL": " ${details.url}",
                          "name": "${details.database}",
                          "username": "${details.user}",
                          "password": "${details.password}",
                          "tables": $jsonListOfTables
                        }
                    ],
                    "targetDatabase": ${Gson().toJson(gatewayDbConfig)},
                    "columnConversion": $columnConversion
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

