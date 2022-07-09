package net.diyigemt.arona.util

import kotlinx.serialization.json.*
import net.diyigemt.arona.Arona
import net.diyigemt.arona.entity.Activity
import net.diyigemt.arona.entity.ActivityType
import net.diyigemt.arona.entity.ServerLocale
import org.jsoup.Jsoup
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.math.floor

object ActivityUtil {

  private val N3H3Plus = Regex("([3双2翻])倍")
  private val NormalTime = Regex("[Nn]ormal.*?(\\d+)[月/](\\d+).*?([–-]|~).*?(\\d+)[月/](\\d+)")
  private val HardTime = Regex("[Hh]ard.*?(\\d)[月/](\\d+).*?([–-]|~).*?(\\d)[月/](\\d+)")
  private val PickUpRegex = Regex("(\\d)?([\\u4e00-\\u9fa5A-z]+)(\\([\\u4e00-\\u9fa5A-z]+\\))?")
  private val PickUpTime = Regex("(\\d+)/(\\d+).*?[–-](\\d+)/(\\d+)")
  private val MaintenanceRegex = Regex("(\\d+)月(\\d+)日.*?[上下]午(\\d+)点")
  private val TotalAssault = Regex("([\\u4e00-\\u9fa5A-z. \\d]+) ?[（(]([\\u4e00-\\u9fa5A-z]+)[)）].*?(\\d+)/(\\d+)")
  private val ActivityJPRegex = Regex("(\\d+/\\d+/\\d+)( \\d+:\\d+)? ～ (\\d+/\\d+ \\d+:\\d+)")
  fun fetchJPActivity(): Pair<List<Activity>, List<Activity>> {
    val document = Jsoup.connect("https://wiki.biligame.com/bluearchive/%E9%A6%96%E9%A1%B5").get()
    val activities = document.getElementsByClass("activity")
    val active = mutableListOf<Activity>()
    val pending = mutableListOf<Activity>()
    activities.forEach {
      val startTime = it.attr("data-start").replace("维护后","17:00")
      val endTime = it.attr("data-end").replace("维护前","11:00")
      val parseStart = SimpleDateFormat("yyyy/MM/dd HH:mm").parse(startTime)
      val parseEnd = SimpleDateFormat("yyyy/MM/dd HH:mm").parse(endTime)
      val now = Calendar.getInstance().time
      if (now.after(parseEnd)) return@forEach
      val content = it.getElementsByClass("activity__name").text()
      doInsert(now, parseStart, parseEnd, active, pending, content, contentSourceJP = false)
    }
    return sortAndPackage(active, pending)
  }

  fun fetchENActivity(): Pair<List<Activity>, List<Activity>> {
    val fetchActivities = fetchActivities()
    val active = mutableListOf<Activity>()
    val pending = mutableListOf<Activity>()
    fetchActivities.forEach { js ->
      val content = js["orig_text"].toString().replace("\\r", "").replace("\\n", "")
      // N3H3
      if (content.contains(Regex("[Nn]ormal")) && content.contains(Regex("[Hh]ard"))) {
        val matchEntire = N3H3Plus.find(content)
        if (matchEntire != null && matchEntire.groups[0] != null) {
          val plus = matchEntire.groups[0]!!.value
          var power = 2
          if (plus.contains("3")) {
            power = 3
          }
          val nTime = NormalTime.find(content)?.groupValues ?: return@forEach
          val hTime = HardTime.find(content)?.groupValues ?: return@forEach
          if ((nTime.size == 5) && (hTime.size == 5)) {
            val n1 = nTime[0]
            val n2 = nTime[1]
            val n3 = nTime[3]
            val n4 = nTime[4]
            val h1 = hTime[0]
            val h2 = hTime[1]
            val h3 = hTime[3]
            val h4 = hTime[4]
            val now = Calendar.getInstance().time
            val year = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy"))
            val startN = parseDateString3(year, n1, n2)
            val endN = parseDateString259(year, n3, n4)
            val startH = parseDateString3(year, h1, h2)
            val endH = parseDateString259(year, h3, h4)
            val parseStartN = SimpleDateFormat("yyyy/MM/dd HH:mm").parse(startN)
            val parseEndN = SimpleDateFormat("yyyy/MM/dd HH:mm").parse(endN)
            val parseStartH = SimpleDateFormat("yyyy/MM/dd HH:mm").parse(startH)
            val parseEndH = SimpleDateFormat("yyyy/MM/dd HH:mm").parse(endH)
            val titleN = "Normal${power}倍掉落"
            val titleH = "Hard${power}倍掉落"
            doInsert(now, parseStartN, parseEndN, active, pending, titleN, type0 = ActivityType.N2_3)
            doInsert(now, parseStartH, parseEndH, active, pending, titleH, type0 = ActivityType.H2_3)
          }
        }
        return@forEach
      }
      // PickUp
      if (content.contains("PICK") && content.contains("UP") && (content.contains("募集") or content.contains("招募"))) {
        val source = content.substringAfter("PICK UP学生").replace("★", "").replace("＆", "").replace(" ", "")
        val timeSource = content.replace(" ", "")
        val findAll = PickUpRegex.findAll(source).toList()
        var student = "pick up"
        if (findAll.isNotEmpty()) {
          findAll.forEach {
            val groups = it.groupValues
            val size = groups.size
            val studentName = groups[size - 2]
            val studentStar = groups[size - 3].ifEmpty { 3 }
            student = if (size > (floor((size / 2).toDouble()) * 2)) {
              val extraName = groups[size - 1]
              "$student $studentStar★$studentName($extraName)"
            } else {
              "$student $studentStar★$studentName"
            }
          }
        } else return@forEach
        val findTime = PickUpTime.find(timeSource) ?: return@forEach
        val groupValue = findTime.groupValues
        if (groupValue.size >= 5) {
          val m1 = groupValue[1]
          val m2 = groupValue[3]
          val d1 = groupValue[2]
          val d2 = groupValue[4]
          val year = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy"))
          val start = parseDateString(year, m1, d1, "16:00")
          val end = parseDateString(year, m2, d2, "11:00")
          val parseStart = SimpleDateFormat("yyyy/MM/dd HH:mm").parse(start)
          val parseEnd = SimpleDateFormat("yyyy/MM/dd HH:mm").parse(end)
          val now = Calendar.getInstance().time
          doInsert(now, parseStart, parseEnd, active, pending, student, type0 = ActivityType.PICK_UP)
        }
        return@forEach
      }
      // 维护公告
      if (content.contains("维护公告")) {
        val find = MaintenanceRegex.find(content)
        val groupValue = find?.groupValues ?: return@forEach
        if (groupValue.size >= 4) {
          val m = groupValue[1]
          val d = groupValue[2]
          val hour = groupValue[3]
          val year = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy"))
          val start = "${year}/${if (m.toInt() < 10) "0$m" else m}/${if (d.toInt() < 10) "0$d" else d} ${if (hour.toInt() < 10) "0$hour" else hour}"
          val parseStart = SimpleDateFormat("yyyy/MM/dd HH").parse(start)
          val parseEnd = Calendar.getInstance()
          parseEnd.time = parseStart
          parseEnd.set(Calendar.HOUR, 16)
          val now = Calendar.getInstance().time
          val title = "游戏维护"
          if (now.before(parseStart)) {
            doInsert(now, parseStart, parseEnd.time, active, pending, title, type0 = ActivityType.MAINTENANCE)
          }
        }
        return@forEach
      }
      // 总力战
      if (content.contains("总力战预告")) {
        val source = content.substringAfter("总力战预告").trim()
        val find = TotalAssault.find(source)
        val groupValue = find?.groupValues ?: return@forEach
        if (groupValue.size >= 5) {
          val name = groupValue[1]
          val terrain = groupValue[2]
          val m = groupValue[3].toInt()
          val d = groupValue[4].toInt()
          val year = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy"))
          val start = "${year}/${if (m < 10) "0$m" else m}/${if (d < 10) "0$d" else d} 16:00"
          val parseStart = SimpleDateFormat("yyyy/MM/dd HH:mm").parse(start)
          val now = Calendar.getInstance().time
          val title = "总力战 $name($terrain)"
          val parseEnd = Calendar.getInstance()
          parseEnd.set(Calendar.MONTH, m - 1)
          parseEnd.set(Calendar.DAY_OF_MONTH, d)
          parseEnd.set(Calendar.DAY_OF_MONTH, parseEnd.get(Calendar.DAY_OF_MONTH) + 6)
          parseEnd.set(Calendar.HOUR_OF_DAY, 23)
          doInsert(now, parseStart, parseEnd.time, active, pending, title, type0 = ActivityType.DECISIVE_BATTLE)
        }
        return@forEach
      }
    }
    return sortAndPackage(active, pending)
  }

  private fun parseDateString259(year: String, date1: String, date2: String) = parseDateString(year, date1, date2, "02:59")

  private fun parseDateString3(year: String, date1: String, date2: String) = parseDateString(year, date1, date2, "03:00")

  private fun parseDateString(year: String, date1: String, date2: String, suffix: String): String {
    return "${year}/${if (date1.toInt() < 10) "0$date1" else date1}/${if (date2.toInt() < 10) "0$date2" else date2} $suffix"
  }

  private fun fetchActivities(): List<JsonObject> {
    var offset = ""
    val res = mutableListOf<JsonObject>()
    for (i in 0 .. 2) {
      val document = Jsoup.connect("https://api.bilibili.com/x/polymer/web-dynamic/v1/feed/space?offset=${offset}&host_mid=1585224247&timezone_offset=-480")
        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/102.0.0.0 Safari/537.36")
        .ignoreContentType(true)
        .get()
      val data = Json.parseToJsonElement(document.text()).jsonObject
      if (clearExtraQute((data["code"] ?: "-1").toString()).toInt() != 0 || clearExtraQute((data["message"] ?: "-1").toString()).toInt() != 0) {
        Arona.error("catch BA en activities error, ${data["message"].toString()}")
        continue
      }
      val items = forceGets(data, "data.items").jsonArray
      offset = clearExtraQute(forceGets(data, "data.offset").toString())
      items.forEach {
        val pubType = forceGets(it, "modules.module_author.pub_action")
        if (pubType.toString().length > 2) return@forEach
        val desc = forceGets(it, "modules.module_dynamic.desc.rich_text_nodes").jsonArray
        res.add(desc[0].jsonObject)
      }
    }
    return res
  }

  fun fetchJPActivityFromJP(): Pair<List<Activity>, List<Activity>> {
    val active = mutableListOf<Activity>()
    val pending = mutableListOf<Activity>()
    val document = Jsoup.connect("https://bluearchive.wikiru.jp/?%E3%82%A4%E3%83%99%E3%83%B3%E3%83%88%E4%B8%80%E8%A6%A7")
      .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/102.0.0.0 Safari/537.36")
      .ignoreContentType(true)
      .get()
    val body = document.getElementById("body") ?: return Pair(active, pending)
    val activities = body.getElementsByClass("list-indent1")
    activities.forEach {
      val outer = it.getElementsByTag("li")
      if (outer.isEmpty()) return@forEach
      val target = outer[0]
      val containString = target.text().replace("メンテナンス前まで", "11:00")
      val timeSource = containString.substringAfterLast("(")
      var contentSource = containString
        .substringBeforeLast("(")
        .replace("・", "·")
        .replace("ガイドミッション", "指导任务")
        .trim()
      val findTime = ActivityJPRegex.find(timeSource)
      if (findTime == null || findTime.groups.size < 3) return@forEach
      val groups = findTime.groups
      val start = groups[1]!!.value
      val parseStart: Date = if (groups.filterNotNull().size == 3) {
        SimpleDateFormat("yyyy/M/d").parse(start)
      } else {
        SimpleDateFormat("yyyy/M/d HH:mm").parse("${start}${groups[2]!!.value}")
      }
      val year = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy"))
      val end = "${year}/${groups[groups.size - 1]!!.value}"
      val parseEnd = SimpleDateFormat("yyyy/M/d HH:mm").parse(end)
      val now = Calendar.getInstance().time
      // 判断是不是特别依赖(经验本钱本)
      val special = target.getElementsByTag("img")
      if (special.isNotEmpty()) {
        val find = Regex("(\\d)").find(special[0].attr("title"))
        if (find == null || find.groups.size != 2) return@forEach
        val pow = find.groups[1]!!.value.toInt()
        contentSource = "特殊作战掉落${pow}倍"
      }
      doInsert(now, parseStart, parseEnd, active, pending, contentSource)
    }
    return sortAndPackage(active, pending)
  }

  fun constructMessage(activities: Pair<List<Activity>, List<Activity>>): String {
    val activeString = activities.first
      .map { at -> "${at.content}     ${at.time}\n" }
      .reduceOrNull { prv, cur -> prv + cur }
    val pendingString = activities.second
      .map { at -> "${at.content}     ${at.time}" }
      .reduceOrNull { prv, cur -> "$prv\n$cur" }
    return "正在进行:\n${activeString ?: "无\n"}即将开始:\n${pendingString ?: '无'}"
  }

  private fun sortAndPackage(active: MutableList<Activity>, pending: MutableList<Activity>): Pair<List<Activity>, List<Activity>> {
    active.sortByDescending { it.type.level }
    pending.sortByDescending { it.type.level }
    return active to pending
  }

  private fun forceGet(target: JsonElement, key: String): JsonElement {
    return target.jsonObject[key]!!
  }

  private fun forceGets(target: JsonElement, key: String): JsonElement {
    val keys = key.split(".")
    var result: JsonElement = target
    keys.forEach {
      result = result.jsonObject
      result = (result as JsonObject)[it]!!
    }
    return result
  }

  private fun clearExtraQute(s: String): String {
    if (s.replace("\"", "").length + 2 == s.length) {
      return s.replaceFirst("\"", "").substring(0, s.length - 2)
    }
    return s
  }

  private fun extraActivityJPTypeFromJP(source: String): ActivityType {
    return ActivityType.NULL
  }

  private fun extraActivityJPTypeFromCN(source: String): ActivityType {
    return ActivityType.NULL
  }

  /**
   * 插入活动并对活动进行分类
   * @param now 当前时间
   * @param parseStart 活动开始时间
   * @param parseEnd 活动结束时间
   * @param active 正在进行的活动列表
   * @param pending 即将开始的活动列表
   * @param contentSourceJP 若是日服的活动, 那么数据来源是日服wiki还是b站wiki
   * @param type0 已知的活动类型(国际服才有)
   */
  private fun doInsert(
    now: Date,
    parseStart: Date,
    parseEnd: Date,
    active: MutableList<Activity>,
    pending: MutableList<Activity>,
    contentSource: String,
    contentSourceJP: Boolean = true,
    type0: ActivityType? = null
  ) {
    val type = type0 ?: if (contentSourceJP) extraActivityJPTypeFromJP(contentSource) else extraActivityJPTypeFromCN(contentSource)
    if (now.before(parseStart)) {
      pending.add(
        Activity(
          contentSource,
          TimeUtil.calcTime(now, parseStart, true),
          type,
          locale(type0)
        )
      )
    } else if (now.before(parseEnd)) {
      active.add(
        Activity(
          contentSource,
          TimeUtil.calcTime(now, parseEnd, false),
          type,
          locale(type0)
        )
      )
    }
  }

  private fun locale(type: ActivityType?): ServerLocale = if (type == null) ServerLocale.JP else ServerLocale.GLOBAL

}