package gateway.controller.models

data class ControllerConfigurationModel(
    val modules: List<String>,
    val connectionOptions: ConnectionOptions

){ data class ConnectionOptions(
    val ip : String,
    val user : String,
    val password : String
) }
