package net.diyigemt.arona.util

import net.diyigemt.arona.entity.Activity
import net.diyigemt.arona.util.ScriptInterpreter.default
import net.diyigemt.arona.util.ScriptInterpreter.ref
import java.text.SimpleDateFormat
import java.util.*

/**
 *@Author Haythem723
 *@Create 2022/7/9
 */

object WikiruUtil {
  fun getValidData(raw : String) : String{
    val start = "&size(16){''報酬受け取り期間''};"
    val tmp = raw.substring(raw.indexOf("&size(16){''開催中のイベント''};"), raw.indexOf("#region([[イベント]]一覧)"))
    return tmp.removeRange(tmp.indexOf(start) + start.length, tmp.indexOf("&size(16){''開催予定のイベント''};"))
  }

  fun analyze(code : String) : Pair<MutableList<Activity>, MutableList<Activity>>{
    val active : MutableList<Activity> = mutableListOf()
    val pending : MutableList<Activity> = mutableListOf()
    var pointer = 0
    while (pointer != -1){
      val name = scriptDecoder(code.substring(code.indexOf("[[", pointer) + 2, code.indexOf(">", pointer)))
      pointer =code.indexOf("]]", pointer)
      val time = code.substring(code.indexOf("(", pointer) + 1, code.indexOf(")", pointer)).replace("メンテナンス後", ActivityUtil.ServerMaintenanceEndTimeJP)
      val timeStart = SimpleDateFormat("yyyy/M/d HH:mm").parse(time)
      val timeEnd = SimpleDateFormat("M/d HH:mm").parse(time.substring(time.indexOf("～") + 1))
      timeEnd.year = Calendar.getInstance().get(Calendar.YEAR) - 1900

      ActivityUtil.doInsert(Calendar.getInstance().time, timeStart, timeEnd, active, pending, name)
      pointer = code.indexOf(")\n\n", pointer)
      pointer = code.indexOf("-", pointer)
    }
    active.sortByDescending { it.type.level }
    pending.sortByDescending { it.type.level }

    return active to pending
  }

  private fun scriptDecoder(code : String) : String{
    val res: String
    val regex = """&[A-z]*\("""
    val tag = Regex(regex).find(code)
    if (tag != null){
      res = Tags.getInterpreterByTag(tag.value.substring(1, tag.value.length - 1))(code.substring(code.indexOf("("), code.indexOf(";")))
      return res
    }
    return code
  }
}

private enum class Tags(val Callback : (String) -> String){
  DEFAULT(::default),
  REF(::ref);

  companion object{
    fun getInterpreterByTag(tag : String) : ((String) -> String) {
      for(i in values().iterator()){
        if(i.name.lowercase() == tag) return i.Callback
      }

      return DEFAULT.Callback
    }
  }
}

private object ScriptInterpreter{
  fun default(code : String) : String = code

  fun ref (code : String) : String = code.substring(code.lastIndexOf("/") + 1, code.indexOf("."))
}