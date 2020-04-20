package gateway.controller.models

// all
data class ModuleConfWrapper(val parameters : Any)
// converter
data class UnitConversion(val from: String, val to: String)

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

// tarolo

data class Conversion(
    val realColumnName: String,
    val originalColumnName: String)

data class SourceDatabaseDetails(
    val url : String,
    val user : String,
    val table : String,
    val database : String,
    val password : String)
