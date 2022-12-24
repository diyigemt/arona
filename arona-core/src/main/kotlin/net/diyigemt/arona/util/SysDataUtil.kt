package net.diyigemt.arona.util

import net.diyigemt.arona.db.DataBaseProvider
import net.diyigemt.arona.db.system.SystemConfigTable
import net.diyigemt.arona.db.system.SystemConfigTableModel
import net.diyigemt.arona.util.sys.SysStatic

/**
 * @description 存取系统信息数据库
 * @author diyigemt
 * @date 2022/9/19 16:17
 */
object SysDataUtil {

  fun get(key: SysStatic): String? = DataBaseProvider.query {
    SystemConfigTableModel.find { SystemConfigTable.key eq key.key }.firstOrNull()?.value
  }

  fun get(key: SysStatic, default: String): String = get(key) ?: default

  fun save(key: SysStatic, value: String) {
    // 防止重复key
    try {
      DataBaseProvider.query {
        SystemConfigTableModel.new {
          this.key = key.key
          this.value = value
        }
      }
    } catch (_: Exception) {}
  }

  fun increase(key: SysStatic, value: Int = 1) {
    DataBaseProvider.query {
      val record = SystemConfigTableModel.find { SystemConfigTable.key eq key.key }.firstOrNull()
      if (record == null) {
        save(key, value.toString())
      } else {
        record.value = (record.value.toInt() + value).toString()
      }
    }
  }

  fun saveRegisterDataOrDefault(value: String) {
    if (get(SysStatic.UUID) == null) {
      saveRegisterData(value)
    }
  }

  fun saveRegisterData(value: String) {
    DataBaseProvider.query {
      SystemConfigTableModel.new {
        this.key = SysStatic.UUID.key
        this.value = value
      }
    }
  }

}