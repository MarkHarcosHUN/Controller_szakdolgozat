package gateway.controller.models

// all
data class ModuleConfWrapper(val parameters : Any)


// reader


// writer


// validation

data class ValidationModel(
    val baseValue : Double,
    val ranges : HashMap<String,Range>
)
data class Range(
    val from : Double,
    val to : Double
)
