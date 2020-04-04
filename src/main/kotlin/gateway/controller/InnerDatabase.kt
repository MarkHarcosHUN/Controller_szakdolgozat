package gateway.controller

interface InnerDatabase {
    fun save(key : String, value : String)
    fun get(key : String) : String
    fun resetDb()
}
