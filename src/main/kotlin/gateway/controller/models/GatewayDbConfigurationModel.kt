package gateway.controller.models

import com.google.gson.annotations.SerializedName

data class GatewayDbConfigurationModel(
    @SerializedName("URL") val url : String,
    val name : String,
    val username : String,
    val password : String,
    val table : String)
