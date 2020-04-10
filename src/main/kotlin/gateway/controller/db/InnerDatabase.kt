package gateway.controller.db

interface InnerDatabase {
    companion object {
        val CONFIG_KEY = "controllerConfiguration"
    }
    fun save(key: String, value: String)
    fun get(key: String): String
    fun resetDb()
}
