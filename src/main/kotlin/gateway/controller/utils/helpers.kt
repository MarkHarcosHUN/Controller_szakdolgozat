package gateway.controller.utils

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
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
                    if(line==null) break;
                    array.add(line)
                }
                return array.joinToString("\n")
            }
        }
    }
}
@Throws(JsonSyntaxException::class)
fun <T> convertFromJson(json : String, to : Class<T>) : T {
   return Gson().fromJson(json, to)
}

