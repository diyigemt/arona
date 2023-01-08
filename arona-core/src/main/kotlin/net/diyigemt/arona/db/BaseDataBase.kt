package net.diyigemt.arona.db

import net.diyigemt.arona.Arona
import net.diyigemt.arona.annotations.SkipSchaleDbInit
import net.diyigemt.arona.db.DataBaseProvider.query
import net.diyigemt.arona.db.system.SystemConfigTable
import net.diyigemt.arona.db.system.SystemConfigTableModel
import net.diyigemt.arona.event.BaseDatabaseInitEvent
import net.diyigemt.arona.util.NetworkUtil
import net.diyigemt.arona.util.ReflectionUtil
import net.mamoe.mirai.event.broadcast
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import kotlin.reflect.full.findAnnotation

object BaseDataBase {

  fun init() {
    query {
      SchemaUtils.create(*ReflectionUtil.getInterfacePetObjectInstance<Table>()
        .filter { it::class.findAnnotation<SkipSchaleDbInit>() == null }
        .toTypedArray()
      )
      // 检查System表中是否有uuid(当然其他的也行), 如果没有说明是初始化, 从api获取数据替换?
      val uuid = SystemConfigTableModel.find { SystemConfigTable.key eq "uuid" }.toList()
      if (uuid.isEmpty()) {
        // 从后端下载初始的数据库
        // TODO 优化下载地址
        val basedDb = Arona.dataFolderFile("/arona.db")
        NetworkUtil.downloadFileFile("/db/arona.db", basedDb)
          .onSuccess {
            Arona.info("数据库下载成功")
            Arona.runSuspend {
              BaseDatabaseInitEvent().broadcast()
            }
          }.onFailure {
            Arona.error("下载数据库文件是出错")
          }
      } else {
        Arona.runSuspend {
          BaseDatabaseInitEvent().broadcast()
        }
      }
    }
  }
}