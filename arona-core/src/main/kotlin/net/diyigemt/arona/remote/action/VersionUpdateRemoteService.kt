package net.diyigemt.arona.remote.action

import net.diyigemt.arona.Arona
import net.diyigemt.arona.remote.RemoteService
import net.diyigemt.arona.remote.RemoteServiceAction
import net.mamoe.mirai.console.plugin.version
import net.mamoe.mirai.console.util.SemVersion
import kotlin.reflect.full.createType

class VersionUpdateRemoteService: RemoteService<VersionInfo> {
  override val kType = VersionInfo::class.createType()
  override val type = RemoteServiceAction.VERSION_UPDATE

  override fun handleService(data: VersionInfo, time: String, aid: Long) {
    val version = data.version
    val nowVersion = SemVersion(version.replace("v", ""))
    val newFuture = data.newFuture
      .mapIndexed { index, element ->
        "${index + 1}. $element"
      }.joinToString("\n")
    val concat = "检测到版本更新,当前版本:${Arona.version}, 新版本:${nowVersion}\n更新日志:\n${newFuture}"
    // 如果本机使用测试版
    if (Arona.version.identifier != null) {
      // 新的版本发布就提醒更新
      if (Arona.version.major == nowVersion.major
          && Arona.version.minor == nowVersion.minor
          && Arona.version.patch == nowVersion.patch
          && Arona.version.identifier != nowVersion.identifier
        ) {
        Arona.sendMessageToAdmin(concat)
      }
    } else {
      if (nowVersion.identifier != null || Arona.version >= nowVersion) return // 忽略测试版和低版本
      Arona.sendMessageToAdmin(concat)
    }
  }
}

@kotlinx.serialization.Serializable
data class VersionInfo(
  val version: String,
  val newFuture: List<String>
)