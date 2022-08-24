package net.diyigemt.arona.util.scbaleDB

import com.google.gson.Gson
import net.diyigemt.arona.Arona
import net.diyigemt.arona.entity.schaleDB.*
import net.diyigemt.arona.quartz.QuartzProvider
import net.diyigemt.arona.service.AronaQuartzService
import org.jsoup.Jsoup
import org.quartz.Job
import org.quartz.JobExecutionContext
import org.quartz.JobKey
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 *@Author hjn
 *@Create 2022/8/20
 */
object SchaleDBDataSyncService : AronaQuartzService{
  override lateinit var jobKey: JobKey
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

  class SchaleDBDataSyncJob : Job{
    override fun execute(context: JobExecutionContext?) = getData()

    fun getData(){
      SchaleDBUtil.commonItem = getCommonData()
      SchaleDBUtil.studentItem = getStudentData()
      SchaleDBUtil.localizationItem = getLocalizationData()
      SchaleDBUtil.raidItem = getRaidData()
    }
  }

  class BirthdayJob : Job{
    override fun execute(context: JobExecutionContext?) {
      getBirthdayList()
    }

    fun getBirthdayList() {
      val formatter = DateTimeFormatter.ofPattern("yyyy/M/d")
      for (item in SchaleDBUtil.studentItem){
        val date = LocalDate.parse(LocalDate.now().year.toString() + "/" + item.BirthDay, formatter)
        if (date.isAfter(LocalDate.now()) && date.isBefore(LocalDate.now().plusWeeks(1))){
          SchaleDBUtil.birthdayList.add(Birthday(item.Name, date))
        }
      }
    }
  }

  fun getCommonData(): CommonDAO = getSchaleDBData(common)

  fun getStudentData(): StudentDAO = getSchaleDBData(student)

  private fun getLocalizationData(): LocalizationDAO = getSchaleDBData(localization)

  private fun getRaidData() : RaidDAO = getSchaleDBData(raid)

  private inline fun <reified T> getSchaleDBData(url : String) : T{
    val connection = Jsoup.connect(gitHub + url)
      .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/102.0.0.0 Safari/537.36")
      .header("Content-Type", "application/json;charset=UTF-8")
      .maxBodySize(0)
      .timeout(3000)
      .ignoreContentType(true)

    var res = kotlin.runCatching {
      connection.execute().body()
    }

    //GitHub超时，换国内镜像源，可能信息未及时同步
    if (res.isFailure){
      res = kotlin.runCatching {
        connection.url(CN + url).execute().body()
      }
    }

    if (res.isFailure) Arona.warning("获取数据源失败，请检查网络连接")

    return Gson().fromJson(res.getOrDefault(""), T::class.java)
  }

  override fun init() {
    registerService()
  }

  // TODO start cron task
  override fun enableService() {
    //数据同步，程序启动时立即执行，每小时刷新一次
    jobKey = QuartzProvider.createCronTask(
      SchaleDBDataSyncJob::class.java,
      "0 0 0/1 * * ? *",
      SchaleDBDataSyncServiceJobKey,
      SchaleDBDataSyncServiceJobKey
    ).first
    QuartzProvider.triggerTask(jobKey)

    //生日计算，程序启动5秒后进行，每周一刷新
    birthdayJobKey = QuartzProvider.createCronTask(
      BirthdayJob::class.java,
      "0 0 0 ? * 1 *",
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