package net.diyigemt.arona.util

import kotlinx.serialization.json.*
import net.diyigemt.arona.Arona
import net.diyigemt.arona.config.AronaNotifyConfig
import net.diyigemt.arona.entity.Activity
import net.diyigemt.arona.entity.ActivityType
import net.diyigemt.arona.entity.ServerLocale
import net.diyigemt.arona.util.GeneralUtils.clearExtraQute
import net.diyigemt.arona.util.ImageUtil.scale
import net.diyigemt.arona.util.TimeUtil.calcDiffDayAndHour
import net.diyigemt.arona.util.TimeUtil.translateTimeMoreReadable
import net.diyigemt.arona.util.scbaleDB.SchaleDBUtil
import org.jsoup.Jsoup
import java.awt.Color
import java.io.ByteArrayInputStream
import java.io.File
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import javax.imageio.ImageIO
import kotlin.math.floor
import kotlin.math.max
import kotlin.reflect.KFunction
import kotlin.reflect.KFunction0
import kotlin.reflect.jvm.isAccessible

object ActivityUtil {

  private val N3H3Plus = Regex("([3双2翻])倍")
  private val NormalTime = Regex("[Nn]ormal.*?(\\d+)[月/](\\d+).*?([–-]|~).*?(\\d+)[月/](\\d+)")
  private val HardTime = Regex("[Hh]ard.*?(\\d)[月/](\\d+).*?([–-]|~).*?(\\d)[月/](\\d+)")
  private val PickUpRegex = Regex("(\\d)?([\\u4e00-\\u9fa5A-z]+)([(（][\\u4e00-\\u9fa5A-z]+[)）])?")
  private val PickUpTime = Regex("(\\d+)/(\\d+).*?[–-](\\d+)/(\\d+)")
  private val PickUpReplace = Regex("[(（）)]")
  private val MaintenanceRegex = Regex("(\\d+)月(\\d+)日.*?[上下]午(\\d+)点")
  private val TotalAssault = Regex("([\\u4e00-\\u9fa5A-z. \\d]+) ?[（(]([\\u4e00-\\u9fa5A-z]+)[)）].*?(\\d+)[/月](\\d+)")
  private val ActivityJPRegex = Regex("(\\d+/\\d+/\\d+)( \\d+:\\d+)? ～ (\\d+/\\d+ \\d+:\\d+)")
  private const val WikiruCmd = "diff"
  private const val WikiruPage = "イベント一覧"
  const val DEFAULT_CALENDAR_FONT_SIZE = 144
  const val DEFAULT_CALENDAR_LINE_MARGIN = 30
  const val DEFAULT_IMAGE_SCALE = 0.2F
  const val ServerMaintenanceStartTimeEN = "11:00"
  const val ServerMaintenanceStartTimeJP = "10:00"
  const val ServerMaintenanceEndTimeEN = "15:00"
  const val ServerMaintenanceEndTimeJP = "16:00"
  private val ActivityColorMap: Map<Int, Pair<Int, Int>> = mapOf(
    1 to (0xFFFFFFFF.toInt() to 0xFFFF8C00.toInt()),
    2 to (0xFFFFFFFF.toInt() to 0xFF8A2BE2.toInt()),
    3 to (0xFFFFFFFF.toInt() to 0xFF107EF7.toInt()),
    4 to (0xFFFFFFFF.toInt() to 0xFFF56C6C.toInt()),
    5 to (0xFFFFFFFF.toInt() to 0xFF67C23A.toInt())
  )

  // 从b_wiki获取数据
  fun fetchJPActivityFromCN(): Pair<List<Activity>, List<Activity>> {
    val document = Jsoup.connect("https://wiki.biligame.com/bluearchive/%E9%A6%96%E9%A1%B5").get()
    val activities = document.getElementsByClass("activity")
    val active = mutableListOf<Activity>()
    val pending = mutableListOf<Activity>()
    activities.forEach {
      val startTime = it.attr("data-start").replace("维护后", ServerMaintenanceEndTimeJP)
      val endTime = it.attr("data-end").replace("维护前", ServerMaintenanceStartTimeJP)
      val parseStart = SimpleDateFormat("yyyy/MM/dd HH:mm").parse(startTime)
      val parseEnd = SimpleDateFormat("yyyy/MM/dd HH:mm").parse(endTime)
      val now = Calendar.getInstance().time
      if (now.after(parseEnd)) return@forEach
      val content = it.getElementsByClass("activity__name").text()
      insertJpActivity(now, parseStart, parseEnd, active, pending, content, from = ActivityJPSource.B_WIKI)
    }
    return sortAndPackage(active, pending)
  }

  private fun fetchENActivityFromSchaleDB(): Pair<List<Activity>, List<Activity>> = SchaleDBUtil.getGlobalEventData()

  private fun fetchENActivityFromGameKee(): Pair<List<Activity>, List<Activity>> =
    GameKeeUtil.getEventData(ServerLocale.GLOBAL)

  fun fetchENActivity(): Pair<List<Activity>, List<Activity>> {
    return fetchENActivityFromGameKee().let {
      // 加入生日信息
      it.first to it.second.toMutableList().also { a -> a.addAll(SchaleDBUtil.getENBirthdayData()) }
    }
  }

  fun fetchJPActivity(): Pair<List<Activity>, List<Activity>> {
    return fetchJPActivityFromGameKee().let { it.first to it.second.toMutableList().also { a -> a.addAll(SchaleDBUtil.getJPBirthdayData()) } }
  }

  private fun doFetch(
    list: MutableList<KFunction0<Pair<List<Activity>, List<Activity>>>>,
    select: KFunction<Pair<List<Activity>, List<Activity>>>
  ): Pair<List<Activity>, List<Activity>> {
    list.remove(select)
    var result = kotlin.runCatching {
      select.isAccessible = true
      select.call()
    }
    if (result.isFailure) {
      result.exceptionOrNull()
    }
    var data = result.getOrDefault(listOf<Activity>() to listOf())
    if (result.isSuccess && (data.first.isNotEmpty() || data.second.isNotEmpty())) return data
    for (function in list) {
      result = runCatching {
        function.invoke()
      }
      data = result.getOrDefault(listOf<Activity>() to listOf())
      if (data.first.isNotEmpty() || data.second.isNotEmpty()) break
    }
    return data
  }

  private fun fetchENActivityFromBiliBili(): Pair<List<Activity>, List<Activity>> {
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
          if ((nTime.size == 6) && (hTime.size == 6)) {
            val n1 = nTime[1]
            val n2 = nTime[2]
            val n3 = nTime[4]
            val n4 = nTime[5]
            val h1 = hTime[1]
            val h2 = hTime[2]
            val h3 = hTime[4]
            val h4 = hTime[5]
            val now = Calendar.getInstance().time
            val year = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy"))
            val startN = parseDateString3(year, n1, n2)
            val endN = parseDateString259(year, n3, n4)
            val startH = parseDateString3(year, h1, h2)
            val endH = parseDateString259(year, h3, h4)
            val parseStartN = simpleDateFormatParse(startN)
            val parseEndN = simpleDateFormatParse(endN)
            val parseStartH = simpleDateFormatParse(startH)
            val parseEndH = simpleDateFormatParse(endH)
            val titleN = "Normal${power}倍掉落"
            val titleH = "Hard${power}倍掉落"
            insertEnActivity(
              now,
              parseStartN,
              parseEndN,
              active,
              pending,
              titleN,
              from = ActivityENSource.BILIBILI,
              type0 = ActivityType.N2_3
            )
            insertEnActivity(
              now,
              parseStartH,
              parseEndH,
              active,
              pending,
              titleH,
              from = ActivityENSource.BILIBILI,
              type0 = ActivityType.N2_3
            )
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
              "$student $studentStar★$studentName"
            } else {
              val extraName = groups[size - 1].replace(PickUpReplace, "")
              "$student $studentStar★$studentName($extraName)"
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
          val start = parseDateString(year, m1, d1, ServerMaintenanceStartTimeEN)
          val end = parseDateString(year, m2, d2, ServerMaintenanceEndTimeEN)
          val parseStart = simpleDateFormatParse(start)
          val parseEnd = simpleDateFormatParse(end)
          val now = Calendar.getInstance().time
          insertEnActivity(
            now,
            parseStart,
            parseEnd,
            active,
            pending,
            student,
            from = ActivityENSource.BILIBILI,
            type0 = ActivityType.PICK_UP
          )
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
          val start = parseDateString(year, m, d, ServerMaintenanceStartTimeEN)
          val parseStart = simpleDateFormatParse(start)
          val parseEnd = Calendar.getInstance()
          parseEnd.time = parseStart
          parseEnd.set(Calendar.HOUR, ServerMaintenanceEndTimeEN.substringBefore(":").toInt())
          val now = Calendar.getInstance()
          val title = "游戏维护"
          if (now.before(parseStart)) {
            insertEnActivity(
              now.time,
              parseStart,
              parseEnd.time,
              active,
              pending,
              title,
              from = ActivityENSource.BILIBILI,
              type0 = ActivityType.MAINTENANCE
            )
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
          val m = groupValue[3]
          val d = groupValue[4]
          val year = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy"))
          val start = parseDateString(year, m, d, ServerMaintenanceEndTimeEN)
          val parseStart = simpleDateFormatParse(start)
          val now = Calendar.getInstance().time
          val title = "总力战 $name($terrain)"
          val parseEnd = Calendar.getInstance()
          parseEnd.set(Calendar.MONTH, m.toInt() - 1)
          parseEnd.set(Calendar.DAY_OF_MONTH, d.toInt())
          parseEnd.set(Calendar.DAY_OF_MONTH, parseEnd.get(Calendar.DAY_OF_MONTH) + 6)
          parseEnd.set(Calendar.HOUR_OF_DAY, 23)
          insertEnActivity(
            now,
            parseStart,
            parseEnd.time,
            active,
            pending,
            title,
            from = ActivityENSource.BILIBILI,
            type0 = ActivityType.DECISIVE_BATTLE
          )
        }
        return@forEach
      }
    }
    return sortAndPackage(active, pending)
  }

  private fun parseDateString259(year: String, date1: String, date2: String) =
    parseDateString(year, date1, date2, "02:59")

  private fun parseDateString3(year: String, date1: String, date2: String) =
    parseDateString(year, date1, date2, "03:00")

  private fun parseDateString(year: String, date1: String, date2: String, suffix: String): String {
    return "${year}/${if (date1.toInt() < 10) "0$date1" else date1}/${if (date2.toInt() < 10) "0$date2" else date2} $suffix"
  }

  private fun simpleDateFormatParse(date: String, formatter: String = "yyyy/MM/dd HH:mm"): Date {
    return SimpleDateFormat(formatter).parse(date)
  }

  private fun fetchActivities(): List<JsonObject> {
    var offset = ""
    val res = mutableListOf<JsonObject>()
    for (i in 0..2) {
      val document =
        Jsoup.connect("https://api.bilibili.com/x/polymer/web-dynamic/v1/feed/space?offset=${offset}&host_mid=1585224247&timezone_offset=-480")
          .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/102.0.0.0 Safari/537.36")
          .ignoreContentType(true)
          .get()
      val data = Json.parseToJsonElement(document.text()).jsonObject
      if (clearExtraQute((data["code"] ?: "-1").toString()).toInt() != 0 || clearExtraQute(
          (data["message"] ?: "-1").toString()
        ).toInt() != 0
      ) {
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

  // 从wikiru网页爬取
  @Deprecated("new api use")
  fun fetchJPActivityFromJP0(): Pair<List<Activity>, List<Activity>> {
    val active = mutableListOf<Activity>()
    val pending = mutableListOf<Activity>()
    val document =
      Jsoup.connect("https://bluearchive.wikiru.jp/?%E3%82%A4%E3%83%99%E3%83%B3%E3%83%88%E4%B8%80%E8%A6%A7")
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
      insertJpActivity(now, parseStart, parseEnd, active, pending, contentSource, from = ActivityJPSource.WIKI_RU)
    }
    return sortAndPackage(active, pending)
  }

  fun fetchJPActivityFromJP(): Pair<List<Activity>, List<Activity>> {
    var res = Jsoup.connect("https://bluearchive.wikiru.jp?cmd=${WikiruCmd}&page=${WikiruPage}")
      .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/102.0.0.0 Safari/537.36")
      .ignoreContentType(true)
      .get()
      .body()
      .getElementById("body")
      ?.getElementsByTag("pre")
      ?: return mutableListOf<Activity>() to mutableListOf()
    res.select(".diff_removed").remove()
    val str = WikiruUtil.getValidData(res.text())
    val parse = WikiruUtil.analyze(str)
    kotlin.runCatching {
      fetchJPMaintenanceActivityFromJP(parse.first, parse.second)
    }
    return parse
  }

  private fun fetchJPActivityFromGameKee(): Pair<List<Activity>, List<Activity>> =
    GameKeeUtil.getEventData(ServerLocale.JP)

  private fun fetchJPActivityFromSchaleDB(): Pair<List<Activity>, List<Activity>> = SchaleDBUtil.getJPEventData()

  // 从wikiru网页爬取维护信息
  private fun fetchJPMaintenanceActivityFromJP(active: MutableList<Activity>, pending: MutableList<Activity>) {
    val res =
      Jsoup.connect("https://bluearchive.wikiru.jp/?%E3%83%96%E3%83%AB%E3%83%BC%E3%82%A2%E3%83%BC%E3%82%AB%E3%82%A4%E3%83%96%EF%BC%88%E3%83%96%E3%83%AB%E3%82%A2%E3%82%AB%EF%BC%89%E6%94%BB%E7%95%A5+Wiki")
        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/102.0.0.0 Safari/537.36")
        .ignoreContentType(true)
        .get()
        .body()
        .getElementById("body")
        ?: return
    val tags = res.getElementsByTag("strong")
      .toList()
      .filter {
        it.text().contains("メンテナンス")
      }.also {
        if (it.isEmpty() || it.size != 3) return
      }
    val currentTag = tags[1].parents().parents().text()
    // 没有维护信息
    if (currentTag.contains("ありません")) return
    val timeString = currentTag.substringAfter("\n").substringBefore(" ~")
    val start = SimpleDateFormat("yyyy/MM/dd HH:mm").parse(timeString)
    val now = Calendar.getInstance()
    val end = now.clone() as Calendar
    end.time = start
    end.set(Calendar.HOUR_OF_DAY, ServerMaintenanceEndTimeJP.substringBefore(":").toInt())
    insertJpActivity(
      now.time,
      start,
      end.time,
      active,
      pending,
      "游戏维护",
      from = ActivityJPSource.WIKI_RU,
      type0 = ActivityType.MAINTENANCE
    )
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

  private fun sortAndPackage(
    active: MutableList<Activity>,
    pending: MutableList<Activity>
  ): Pair<List<Activity>, List<Activity>> {
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

  private fun extraActivityJPTypeFromJPAndTranslate(activity: Activity): Activity {
    val source = activity.content
      .replace("キャンペーン", "") // キャンペーン->活动
      .replace("&br;", "") // 多余的html换行符
      .replace("スケジュール", "课程表") // 课程表

    activity.type = when {
      source.contains("游戏维护") -> {
        ActivityType.MAINTENANCE
      }

      source.contains("合同火力演習") -> {
        ActivityType.JOINT_EXERCISES
      }

      source.contains("特殊作戦") -> {
        activity.content = source.replace("デカグラマトン", "十字神明")
        ActivityType.KABALA
      }

      source.contains("総力戦") -> {
        activity.content = source.replace("ビナー", "bina")
        activity.content = source.replace("ヒエロニムス", "主教")
        activity.content = source.replace("KAITEN FX Mk.0", "bina")
        activity.content = source.replace("シロ＆クロ", "黑白")
        activity.content = source.replace("ペロロジラ", "佩罗洛斯拉")
        activity.content = source.replace("ケセド", "Kesed(球)")
        activity.content = source.replace("ホド", "Hod")
        ActivityType.DECISIVE_BATTLE
      }

      source.contains("報酬2倍") || source.contains("報酬3倍") -> {
        activity.content = source.replace("ハード", "H")
        activity.content = source.replace("ノーマル", "N")
        ActivityType.SPECIAL_DROP
      }

      source.contains("指名手配") -> {
        ActivityType.WANTED_DROP
      }

      source.contains("学園交流会") -> {
        ActivityType.COLLEGE_EXCHANGE_DROP
      }

      source.contains("スケジュール") -> {
        activity.content = source.replace("スケジュール", "课程表") // 课程表
        ActivityType.SCHEDULE
      }

      else -> {
        activity.content = source.replace("_バナー", "") // banner
          .replace("ログインボーナス", "登录奖励")
        ActivityType.ACTIVITY
      }
    }
    return activity
  }

  private fun extraActivityJPTypeFromCN(activity: Activity): Activity {
    return activity
  }

  private fun extraActivityTypeFromGameKee(activity: Activity, server: ServerLocale): Activity {
    var source = activity.content
      .replace("【" + server.serverName + "】", "")
      .replace("（", "(")
      .replace("）", ")")
    activity.type = ActivityType.ACTIVITY
    when {
      source.contains("卡池") -> {
        activity.type = ActivityType.PICK_UP
        source = source.replace("【" + server.serverName + "卡池" + "】", "Pick Up: ")
      }

      source.contains("指名手配") -> activity.type = ActivityType.WANTED_DROP
      source.contains("悬赏通缉") -> activity.type = ActivityType.WANTED_DROP
      source.contains("学院交流") -> activity.type = ActivityType.COLLEGE_EXCHANGE_DROP
      source.contains("特别依赖") || source.contains("特殊任务") -> activity.type = ActivityType.SPECIAL_DROP
      source.contains("日程") || source.contains("课程表") -> activity.type = ActivityType.SCHEDULE
      source.contains("总力战") -> activity.type = ActivityType.DECISIVE_BATTLE
      source.contains("Normal") -> activity.type = ActivityType.N2_3
      source.contains("Hard") -> activity.type = ActivityType.H2_3
      source.contains("合同火力演习") || source.contains("综合战术考试") -> activity.type = ActivityType.JOINT_EXERCISES
    }
    activity.content = source
    return activity
  }

  private fun extraActivityJPTypeFromSchaleDB(activity: Activity): Activity = activity

  /**
   * 插入日服活动并对活动进行分类
   * @param now 当前时间
   * @param parseStart 活动开始时间
   * @param parseEnd 活动结束时间
   * @param active 正在进行的活动列表
   * @param pending 即将开始的活动列表
   * @param contentSource 活动内容
   * @param type0 已知的活动类型(如果已经得知可以直接填，未知请在本方法里注册方法解析出来)
   */
  fun insertJpActivity(
    now: Date,
    parseStart: Date,
    parseEnd: Date,
    active: MutableList<Activity>,
    pending: MutableList<Activity>,
    contentSource: String,
    from: ActivityJPSource = ActivityJPSource.GAME_KEE,
    type0: ActivityType = ActivityType.NULL,
  ) {
    var activity = Activity(
      contentSource,
      TimeUtil.calcTime(parseStart, true),
      serverLocale = ServerLocale.JP,
    )
    if (type0 == ActivityType.NULL) {
      activity = extraActivityJPTypeFromCN(activity)
    } else {
      activity.type = type0
    }
    doInsert0(now, parseStart, parseEnd, active, pending, activity)
  }

  /**
   * 插入国际服活动并对活动进行分类
   * @param now 当前时间
   * @param parseStart 活动开始时间
   * @param parseEnd 活动结束时间
   * @param active 正在进行的活动列表
   * @param pending 即将开始的活动列表
   * @param contentSource 活动内容
   * @param type0 已知的活动类型(如果已经得知可以直接填，未知请在本方法里注册方法解析出来)
   */
  fun insertEnActivity(
    now: Date,
    parseStart: Date,
    parseEnd: Date,
    active: MutableList<Activity>,
    pending: MutableList<Activity>,
    contentSource: String,
    from: ActivityENSource = ActivityENSource.SCHALE_DB,
    type0: ActivityType = ActivityType.NULL
  ) {
    var activity = Activity(
      contentSource,
      TimeUtil.calcTime(parseStart, true),
      serverLocale = ServerLocale.GLOBAL,
    )
    if (type0 == ActivityType.NULL) {
      activity = extraActivityTypeFromGameKee(activity, ServerLocale.GLOBAL)
    } else {
      activity.type = type0
    }
    doInsert0(now, parseStart, parseEnd, active, pending, activity)
  }


  /**
   * 插入活动并对活动进行分类
   * @param now 当前时间
   * @param parseStart 活动开始时间
   * @param parseEnd 活动结束时间
   * @param active 正在进行的活动列表
   * @param pending 即将开始的活动列表
   */
  private fun doInsert0(
    now: Date,
    parseStart: Date,
    parseEnd: Date,
    active: MutableList<Activity>,
    pending: MutableList<Activity>,
    activity: Activity
  ) {
    if (now.before(parseStart)) {
      pending.add(activity)
    } else if (now.before(parseEnd)) {
      activity.time = TimeUtil.calcTime(parseEnd, false)
      active.add(activity)
    }
  }

  private fun locale(type: ActivityType?): ServerLocale = if (type == null) ServerLocale.JP else ServerLocale.GLOBAL

  private fun sortActive(active: Activity): Int {
    val diff = calcDiffDayAndHour(active.time)
    return diff.first * 24 + diff.second
  }

  fun createActivityImage(
    activities: Pair<List<Activity>, List<Activity>>,
    server: ServerLocale = ServerLocale.JP
  ): File {
    val title = "${server.serverName}活动日历"
    val active = activities.first.sortedBy { sortActive(it) }
    val pending = activities.second.sortedBy { sortActive(it) }
    val image = ImageUtil.createCalendarImage(
      max(active.size, 1) + max(pending.size, 1),
      max(active.maxOfOrNull { it.content.length } ?: 0, pending.maxOfOrNull { it.content.length } ?: 0))
    var lineIndex = 1
    fun calcY(offset: Int = 0): Int {
      return DEFAULT_CALENDAR_FONT_SIZE * (lineIndex + offset) + DEFAULT_CALENDAR_LINE_MARGIN * (lineIndex + offset - 1)
    }
    ImageUtil.init(image, 0xFFFFFFFF.toInt())
    ImageUtil.drawText(image, title, DEFAULT_CALENDAR_FONT_SIZE, ImageUtil.TextAlign.CENTER)
    lineIndex++
    ImageUtil.drawText(
      image,
      SimpleDateFormat("yyyy/M/d").format(Calendar.getInstance().time),
      calcY(),
      ImageUtil.TextAlign.RIGHT
    )
    ImageUtil.drawText(image, "正在进行", calcY())
    lineIndex++
    fun drawActivity(list: List<Activity>, future: Boolean) {
      if (list.isEmpty()) {
        ImageUtil.drawText(image, "无", calcY())
        lineIndex++
      } else {
        list.forEach {
          val y = calcY()
          val color = ActivityColorMap[it.type.level]!!
          ImageUtil.drawRoundRect(
            image,
            0,
            (calcY(-1) + DEFAULT_CALENDAR_LINE_MARGIN * 1.5).toInt(),
            image.width,
            DEFAULT_CALENDAR_FONT_SIZE + DEFAULT_CALENDAR_LINE_MARGIN / 2,
            40,
            color.second
          )
          ImageUtil.drawText(image, it.content, y, color = color.first)
          ImageUtil.drawText(image, translateTimeMoreReadable(it.time, future), y, color = color.first, align = ImageUtil.TextAlign.RIGHT)
          lineIndex++
        }
      }
    }
    drawActivity(active, false)
    ImageUtil.drawText(image, "即将开始", calcY())
    lineIndex++
    drawActivity(pending, true)
    ImageUtil.drawText(image, "数据来源: https://ba.gamekee.com/", calcY())
    val imageFile = File(Arona.dataFolder.absolutePath + "/activity-${server.serverName}.png")
    image.scale(DEFAULT_IMAGE_SCALE, DEFAULT_IMAGE_SCALE).makeImageSnapshot().also {
      ImageIO.write(ImageIO.read(ByteArrayInputStream(it.encodeToData()?.bytes)), "png", imageFile)
    }
    return imageFile
  }

  enum class ActivityJPSource(val source: String) {
    B_WIKI("https://wiki.biligame.com/bluearchive/"),
    WIKI_RU("https://bluearchive.wikiru.jp/"),
    GAME_KEE("https://ba.gamekee.com/"),
    SCHALE_DB("https://lonqie.github.io/SchaleDB/")
  }

  enum class ActivityENSource(val source: String) {
    SCHALE_DB("https://lonqie.github.io/SchaleDB/"),
    BILIBILI("https://space.bilibili.com/1585224247"),
    GAME_KEE("https://ba.gamekee.com/")
  }
}
