package gateway.controller.utils

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import com.google.gson.JsonSyntaxException
import gateway.controller.db.InnerDatabase
import gateway.controller.models.ControllerConfigurationModel
import org.apache.commons.io.input.ReversedLinesFileReader
import java.io.File
import java.nio.charset.Charset
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class HistoryManager{
    companion object {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val historyFilePath = "history.log"
        fun  updateHistory(line : String){
            synchronized(this){
                File(historyFilePath).appendText("${LocalDateTime.now().format(formatter)} : $line\n")
            }
        }
        fun  getHistory(numLines : Int) : String {
            synchronized(this){
                var array = ArrayList<String>()
                var reader =ReversedLinesFileReader(File(historyFilePath), Charset.defaultCharset())
                for(num in 1..numLines){
                    var line=reader.readLine()
                    if(line==null) break
                    array.add(line)
                }
                return array.joinToString("\n")
            }
        }
    }
}
@Throws(JsonSyntaxException::class)
fun <T> convertFromJson(json : String, to : Class<T>) : T? {
   return Gson().fromJson(json, to)
}
inline fun <reified T : Any> parseJsonTo(str: String): T = Gson().fromJson(str, T::class.java)

fun readFileDirectlyAsText(fileName: String): String = File(fileName).readText(Charsets.UTF_8)

fun makeJsonPretty(json : String) : String{
    val parser = JsonParser()
    val json = parser.parse(json).asJsonObject
    val builder = GsonBuilder().setPrettyPrinting().create()
    return builder.toJson(json)
}
