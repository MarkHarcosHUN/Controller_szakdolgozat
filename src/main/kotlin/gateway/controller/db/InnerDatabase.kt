package gateway.controller.db

import com.google.gson.Gson
import gateway.controller.models.ControllerConfigurationModel
import gateway.controller.utils.convertFromJson
import java.io.File
import org.iq80.leveldb.Options
import org.iq80.leveldb.impl.Iq80DBFactory.*
import java.lang.Exception

object InnerDatabase {
    val dbPath = "controller_database"
    val configKey = "configuration_key"

    private var options = Options()
    init {
        options.createIfMissing(true)
    }
    fun save(key: String, value: String) {
        synchronized(this){
            val db = factory.open(File(dbPath), options)
            db.use { db ->
                db.put(bytes(key), bytes(value))
            }
        }
    }

    fun get(key: String): String? {
        synchronized(this) {
            val db = factory.open(File(dbPath), options)
            db.use { db ->
                return asString(db.get(bytes(key)))
            }
        }
    }

    fun resetDb() {
        factory.destroy(File(dbPath), options)
    }
    fun getControllerConfig() : ControllerConfigurationModel {
        val jsonConfig= get(configKey) ?: throw Exception("You need to set configuration first.")
        //if there is configuration, it must be correct due to the saving method
        return convertFromJson(jsonConfig,ControllerConfigurationModel::class.java)!!
    }
}
