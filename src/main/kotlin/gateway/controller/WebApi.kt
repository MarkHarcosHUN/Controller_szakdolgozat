package gateway.controller

class WebApi(private val controller: Manageable) {
    fun startListening() {
        try {
            Thread.sleep(5000)
            // controller.start(ControllerConfigurationModel(listOf("")))
            controller.stop()
            controller.restart(ControllerConfigurationModel(listOf("")))
            controller.stop()
            controller.stop()
            controller.stop()
            controller.stop()

            controller.restart(ControllerConfigurationModel(listOf("")))
            Thread.sleep(5000)

            controller.restart(ControllerConfigurationModel(listOf("")))
        } catch (e: ControllerException) {
            println(e.message)
        }
        Thread.sleep(10000)
        controller.start(ControllerConfigurationModel(listOf("")))
    }
}
