package gateway.controller

import java.io.File
import org.iq80.leveldb.Options
import org.iq80.leveldb.impl.Iq80DBFactory.*

class InnerDatabaseImpl : InnerDatabase {
    val dbPath = "controller_database"

    private var options = Options()
    init {
        options.createIfMissing(true)
    }
    override fun save(key: String, value: String) {
        val db = factory.open(File(dbPath), options)
        try {
            db.put(bytes(key), bytes(value))
        } finally {
            db.close()
        }
    }

    override fun get(key: String): String {
        val db = factory.open(File(dbPath), options)
        try {
            return asString(db.get(bytes(key)))
        } finally {
            db.close()
        }
    }

    override fun resetDb() {
        factory.destroy(File(dbPath), options)
    }
}
