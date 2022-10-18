package net.diyigemt.arona.util.scbaleDB

import com.google.gson.Gson
import net.diyigemt.arona.Arona
import net.diyigemt.arona.db.DB
import net.diyigemt.arona.db.DataBaseProvider
import net.diyigemt.arona.db.data.schaledb.MD5
import net.diyigemt.arona.entity.schaleDB.*
import net.diyigemt.arona.quartz.QuartzProvider
import net.diyigemt.arona.service.AronaQuartzService
import okio.ByteString.Companion.toByteString
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.update
import org.jsoup.Jsoup
import org.quartz.Job
import org.quartz.JobExecutionContext
import org.quartz.JobKey
import java.security.MessageDigest
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 *@Author hjn
 *@Create 2022/8/20
 */
object SchaleDBDataSyncService : AronaQuartzService{
  override var jobKey: JobKey? = null
  lateinit var birthdayJobKey: JobKey
  override val id: Int = 19
  override val name: String = "数据同步服务"
  override var enable: Boolean = true
  private const val SchaleDBDataSyncServiceJobKey = "SchaleDBDataSyncService"
  private const val BirthdayJobKey = "Birthday"

  private const val CN = "https://schaledb.brightsu.cn/"
  private const val gitHub = "https://lonqie.github.io/SchaleDB/"
  private const val common = "data/common.min.json"
  private const val student = "data/cn/students.min.json"
  private const val localization = "data/cn/localization.json"
  private const val raid = "data/raids.min.json"
  private var resString = ""

  class SchaleDBDataSyncJob : Job{
    override fun execute(context: JobExecutionContext?) = getData()

    fun getData(){
      kotlin.runCatching {
        SchaleDBUtil.studentItem = getStudentData()
        SchaleDBUtil.localizationItem = getLocalizationData()
        SchaleDBUtil.raidItem = getRaidData()
        SchaleDBUtil.commonItem = getCommonData()
      }.onFailure {
        Arona.warning(it.toString())
        Arona.warning("数据同步失败，无法保证数据准确性")
      }
    }
  }

  class BirthdayJob : Job{
    override fun execute(context: JobExecutionContext?) = getBirthdayList()

    fun getBirthdayList() {
      val formatter = DateTimeFormatter.ofPattern("yyyy/M/d")
      SchaleDBUtil.birthdayList.clear()
      for (item in SchaleDBUtil.studentItem){
        val date = LocalDate.parse(LocalDate.now().year.toString() + "/" + item.BirthDay, formatter)
        if (date.isAfter(LocalDate.now()) && date.isBefore(LocalDate.now().plusWeeks(1))){
          SchaleDBUtil.birthdayList.add(Birthday(item.Name, date))
        }
      }
    }
  }

  enum class JSONDataType{
    COMMON,STUDENT,LOCALIZATION,RAID
  }

  enum class DataBaseType{
    STUDENT,EVENT,RAID
  }

  enum class RemoteType{
    GITHUB, MIRROR
  }

  private fun getCommonData(): CommonDAO = getSchaleDBData(common, JSONDataType.COMMON.name)

  private fun getStudentData(): StudentDAO = getSchaleDBData(student, JSONDataType.STUDENT.name)

  private fun getLocalizationData(): LocalizationDAO = getSchaleDBData(localization, JSONDataType.LOCALIZATION.name)

  private fun getRaidData() : RaidDAO = getSchaleDBData(raid, JSONDataType.RAID.name)

  private inline fun <reified T : BaseDAO> getSchaleDBData(url : String, dataType : String) : T{
    val connection = Jsoup.connect(gitHub + url)
      .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/102.0.0.0 Safari/537.36")
      .header("Content-Type", "application/json;charset=UTF-8")
      .maxBodySize(0)
      .timeout(3000)
      .ignoreContentType(true)

    var isGitHub = true
    var res = runCatching {
      connection.execute().body()
    }
    res.onFailure {
      res = runCatching {
        isGitHub = false
        //GitHub超时，换国内镜像源。注：可能信息会因镜像源未及时同步而丢掉
        connection.url(CN + url).execute().body()
      }.onFailure {
        Arona.warning("获取数据源: $dataType 失败，请检查网络连接")
      }
    }

    resString = ""
    var isUpdate = false
    val md5 = MessageDigest.getInstance("MD5").digest(res.getOrDefault("").toByteArray()).toByteString().hex()
    val query = runCatching {
      DataBaseProvider.query(DB.DATA.ordinal) { MD5.select(MD5.name eq dataType).first() }
    }.getOrNull()
    val dao = Gson().fromJson(res.getOrDefault(""), T::class.java)
    //GET失败时dao为null，返回模板对象的默认值
    if (dao == null){
      val new = T::class.java.getDeclaredConstructor().newInstance()
      return new.toModel(new)
    }
    resString += "Source: $dataType"
    if (query?.getOrNull(MD5.name) != null){
      when(query.getOrNull(MD5.remote)){
        RemoteType.GITHUB.name -> apply {
          resString += " from GitHub"
          if (query.getOrNull(MD5.md5) != md5 && isGitHub){
            dao.sendToDataBase()
            updateMD5(dataType, RemoteType.GITHUB.name, md5)
            isUpdate = true
          } else resString += " already up to date."
        }
        else -> apply {
          resString += " from mirror"
          if (query.getOrNull(MD5.md5) != md5){
            dao.sendToDataBase()
            updateMD5(dataType, RemoteType.MIRROR.name, md5)
            isUpdate = true
          } else resString += " already up to date."
        }
      }
    }else{
      dao.sendToDataBase()
      if (isGitHub) updateMD5(dataType, RemoteType.GITHUB.name, md5)
      else updateMD5(dataType, RemoteType.MIRROR.name, md5)
      isUpdate = true
    }
    Arona.info(resString)

    if (isUpdate) return dao

    return dao.toModel(dao)
  }

  private fun updateMD5(name : String, remote : String, md5 : String){
    val query = DataBaseProvider.query(DB.DATA.ordinal) { MD5.select(MD5.name eq name).toList() } ?: return
    if (query.isEmpty()){
      resString += " updated."
      DataBaseProvider.query(DB.DATA.ordinal) {
        MD5.insert {
          it[MD5.name] = name
          it[MD5.remote] = remote
          it[MD5.md5] = md5
        }
      }
    }
    else{
      resString += " added."
      DataBaseProvider.query(DB.DATA.ordinal) {
        MD5.update({ MD5.name eq name }) {
          it[MD5.remote] = remote
          it[MD5.md5] = md5
        }
      }
    }
  }

  override fun init() = registerService()

  // TODO start cron task
  override fun enableService() {
    //数据同步，程序启动时立即执行，每小时刷新一次
    jobKey = QuartzProvider.createCronTask(
      SchaleDBDataSyncJob::class.java,
      "0 0 0/1 * * ? *",
      SchaleDBDataSyncServiceJobKey,
      SchaleDBDataSyncServiceJobKey
    ).first
    QuartzProvider.triggerTask(jobKey!!)

    //生日计算，程序启动5秒后进行，每天0点刷新
    birthdayJobKey = QuartzProvider.createCronTask(
      BirthdayJob::class.java,
      "0 0 0 * * ? *",
      BirthdayJobKey,
      BirthdayJobKey
    ).first
    QuartzProvider.createSimpleDelayJob(5){
      QuartzProvider.triggerTask(birthdayJobKey)
    }
  }

  // TODO cancel cron task
  override fun disableService() {
    super.disableService()
  }
}