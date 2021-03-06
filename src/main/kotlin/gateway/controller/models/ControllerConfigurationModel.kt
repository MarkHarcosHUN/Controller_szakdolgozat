package gateway.controller.models

import com.google.gson.annotations.SerializedName

data class ControllerConfigurationModel(
    val modules: List<String>,
    @SerializedName("connection_options")
    val connectionOptions: ConnectionOptions) {
        data class ConnectionOptions(
            @SerializedName("gateway_id")
            val gatewayId: Int,
            val db: String,
            val url: String,
            val user: String,
            val password: String
    )
    // necessary for checking if all input set, to ensure correctness need further checks.
    fun isValid(): Boolean {
        return !(connectionOptions.db == null || modules == null || connectionOptions.gatewayId == null || connectionOptions == null || connectionOptions.url == null || connectionOptions.user == null || connectionOptions.password == null)
    }
}
