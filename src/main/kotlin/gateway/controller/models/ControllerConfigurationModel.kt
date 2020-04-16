package gateway.controller.models

import com.google.gson.annotations.SerializedName

data class ControllerConfigurationModel(
    val modules: List<String>,
    @SerializedName("connection_options")
    val connectionOptions: ConnectionOptions) {
        data class ConnectionOptions(
            @SerializedName("gateway_id")
            val gatewayId: Int,
            val url: String,
            val user: String,
            val password: String
    )
    // necessary for checking valid input
    fun isValid(): Boolean {
        return !(modules == null || connectionOptions.gatewayId == null || connectionOptions == null || connectionOptions.url == null || connectionOptions.user == null || connectionOptions.password == null)
    }
}
