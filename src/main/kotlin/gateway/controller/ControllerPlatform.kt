package gateway.controller

class ControllerPlatform : WebCommunicator {
    val webApiSendPort = "4000"
    val webApiRecievePort = "4001"

    private var innerDatabase: String = "inner sql lite"

    private lateinit var webProcess: Process

    private val controller = Controller(innerDatabase, this)

    init {
        setupWebApi()
        onWebMessage("start")
        Thread.sleep(1)
        onWebMessage("start")
        Thread.sleep(1)
        onWebMessage("stop")
        Thread.sleep(3000)
        onWebMessage("restart")
        Thread.sleep(100000)
    }

    fun setupWebApi() {
/*
        var webProcess=ProcessBuilder("java -jar web.jar",webApiSendPort,webApiReceivePort).also{
            it.start()
        }

 */
    }

    private fun onWebMessage(msg: String) {
        when (msg) {
            "start" -> {
                controller.start()
            }
            "stop" -> {
                controller.stop()
            }
            "restart" -> {
                controller.restart()
            }
        }
    }

    override fun sendWebReply(msg: String) {
        println(msg)
    }
}
