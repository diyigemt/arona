package net.diyigemt.arona.config

import net.diyigemt.arona.advance.NGAImageTranslatePusher
import net.diyigemt.arona.annotations.ConfigKey
import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value

object AronaConfig : AutoSavePluginConfig("arona") {

  @ValueDescription("运行arona的qq")
  var qq: Long by value()

  @ValueDescription("具有管理员权限的qq号")
  var managerGroup: MutableList<Long> by value()

  @ValueDescription("自动检查更新的时间")
  var updateCheckTime: Int by value(8)

  @ValueDescription("是否允许arona收集匿名统计信息")
  var sendStatus: Boolean by value(false)

  @ValueDescription("远端操作查询间隔 设置为0表示不开启, 单位是小时")
  var remoteCheckInterval: Int by value(1)

  @ValueDescription("webui监听端口")
  @ConfigKey("webui.port")
  val webuiPort: Int by value(8080)


  @ValueDescription(
    """
      登录后产生的cookie, 可以从名叫\"ngaPassportCid\"的cookie中获取,
      具体配置方法看这里 https://github.com/diyigemt/arona/blob/master/doc/using.md#nga-config
    """
  )
  @ConfigKey("nga.cid")
  var ngaCid: String by value("")

}