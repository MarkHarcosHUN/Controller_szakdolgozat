package gateway.controller.server

interface Manageable {
    fun start() : HttpResponse
    fun stop() : HttpResponse
    fun restart() : HttpResponse
    fun saveConfig(postJson: String) : HttpResponse
    fun getLog(postJson: String) : HttpResponse
}
