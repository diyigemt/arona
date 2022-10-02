package net.diyigemt.arona.command

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import net.diyigemt.arona.Arona
import net.diyigemt.arona.advance.RemoteActionItem
import net.diyigemt.arona.command.cache.GachaCache
import net.diyigemt.arona.config.AronaGachaConfig
import net.diyigemt.arona.db.DataBaseProvider
import net.diyigemt.arona.db.DataBaseProvider.query
import net.diyigemt.arona.db.gacha.*
import net.diyigemt.arona.remote.RemoteServiceAction
import net.diyigemt.arona.remote.action.GachaCharacter
import net.diyigemt.arona.remote.action.GachaPoolUpdateData
import net.diyigemt.arona.service.AronaManageService
import net.diyigemt.arona.util.GachaUtil
import net.diyigemt.arona.util.NetworkUtil
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.register
import net.mamoe.mirai.console.command.CompositeCommand
import net.mamoe.mirai.console.command.UserCommandSender
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.Group
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.select
import net.diyigemt.arona.db.gacha.GachaCharacter as GC

object GachaConfigCommand : CompositeCommand(
  Arona,"gacha", "抽卡",
  description = "设置发情触发的关键词"
), AronaManageService {

  @SubCommand("setpool")
  @Description("设置激活的池子")
  suspend fun UserCommandSender.setPool(pool: Int) {
    val targetPool = GachaCache.updatePool(pool)
    if (targetPool == null) {
      subject.sendMessage("没有找到池子")
      return
    }
    subject.sendMessage("池子设置为:${targetPool.name}")
  }

  @SubCommand
  @Description("重置某一池子的记录")
  suspend fun UserCommandSender.reset(pool: Int = AronaGachaConfig.activePool) {
    query {
      GachaHistoryTable.deleteWhere { (GachaHistoryTable.pool eq pool) and (GachaHistoryTable.group eq (subject as Group).id) }
    }
    GachaLimitTable.forceUpdate(if (subject is Group) subject.id else null)
    subject.sendMessage("历史记录重置成功")
  }

  @SubCommand("1s")
  @Description("设置1星出货率")
  suspend fun UserCommandSender.s1(rate: Float) {
    AronaGachaConfig.star1Rate = rate
    subject.sendMessage("1星出货率设置为${rate}%")
    AronaGachaConfig.init()
  }

  @SubCommand("2s")
  @Description("设置2星出货率")
  suspend fun UserCommandSender.s2(rate: Float) {
    AronaGachaConfig.star2Rate = rate
    subject.sendMessage("2星出货率设置为${rate}%")
    AronaGachaConfig.init()
  }
  @SubCommand("3s")
  @Description("设置3星出货率")
  suspend fun UserCommandSender.s3(rate: Float) {
    AronaGachaConfig.star3Rate = rate
    subject.sendMessage("3星出货率设置为${rate}%")
    AronaGachaConfig.init()
  }

  @SubCommand("p2s")
  @Description("设置2星PickUp出货率")
  suspend fun UserCommandSender.ps2(rate: Float) {
    AronaGachaConfig.star2PickupRate = rate
    subject.sendMessage("2星PickUp出货率设置为${rate}%")
    AronaGachaConfig.init()
  }

  @SubCommand("p3s")
  @Description("设置3星PickUp出货率")
  suspend fun UserCommandSender.ps3(rate: Float) {
    AronaGachaConfig.star3PickupRate = rate
    subject.sendMessage("3星PickUp出货率设置为${rate}%")
    AronaGachaConfig.init()
  }

  @SubCommand("time")
  @Description("设置设置撤回时间")
  suspend fun UserCommandSender.time(time: Int) {
    if (time > 0) {
      AronaGachaConfig.revokeTime = time
      subject.sendMessage("撤回时间设置为${time}")
    } else {
      AronaGachaConfig.revokeTime = 0
      subject.sendMessage("关闭抽卡结果撤回")
    }
    AronaGachaConfig.init()
  }

  @SubCommand("limit")
  @Description("设置设置撤回时间")
  suspend fun UserCommandSender.gachalimit(time: Int) {
    if (time > 0) {
      AronaGachaConfig.limit = time
      subject.sendMessage("每日限制次数设置为${time}")
    } else {
      AronaGachaConfig.limit = 0
      subject.sendMessage("每日限制次数设置为不限制每日抽卡次数")
    }
    AronaGachaConfig.init()
  }


  @SubCommand("update")
  @Description("从远端更新池子")
  suspend fun UserCommandSender.updatePool(id: Int) {
    doUpdate(id, subject)
  }

  @SubCommand("update2")
  @Description("从远端更新池子并重命名")
  suspend fun UserCommandSender.updatePool2(id: Int, name: String) {
    doUpdate(id, subject, name)
  }

  @SubCommand("list")
  @Description("查看最近两个卡池的pickup内容")
  suspend fun UserCommandSender.list() {
    val poolList = query {
      GachaPool
        .all()
        .orderBy(GachaPoolTable.id to SortOrder.DESC)
        .limit(2)
        .toList()
    }!!
    if (poolList.isEmpty()) {
      subject.sendMessage("没有任何池子信息")
      return
    }
    val msg = poolList.map {
      val students = query { _ ->
        GachaPoolCharacterTable
          .innerJoin(GachaCharacterTable)
          .slice(GachaCharacterTable.name, GachaCharacterTable.star)
          .select {
            GachaPoolCharacterTable.poolId eq it.id
          }.toList()
      }!!
      return@map if (students.isEmpty()) {
        "${it.name}(id: ${it.id}): 没有关联的学生"
      } else {
        "${it.name}(id: ${it.id}): ${students.joinToString(", ") { s -> GachaUtil.mapStudentInfo(s[GachaCharacterTable.name], s[GachaCharacterTable.star]) }}"
      }
    }.reversed().joinToString("\n")
    subject.sendMessage(msg)
  }

//  @SubCommand("adds")
//  @Description("给卡池添加新学生")
  suspend fun UserCommandSender.adds(name: String, star: Int, limit: Int) {
  // 模糊搜索判断是否重名
    val data = query {
      GC.find {
        GachaCharacterTable.name eq name
      }
    }
  }

  @OptIn(InternalSerializationApi::class)
  private suspend fun doUpdate(id: Int, subject: Contact, poolName: String? = null) {
    val data = kotlin.runCatching {
      val resp = NetworkUtil.fetchDataFromServer<RemoteActionItem>("/action/one", mapOf(
        "id" to id.toString()
      ))
      if (resp.data.action != RemoteServiceAction.POOL_UPDATE.action) {
        subject.sendMessage("id错误")
        return
      }
      Json.decodeFromString(GachaPoolUpdateData::class.serializer(), resp.data.content)
    }.onFailure {
      subject.sendMessage("从远端获取池子信息失败")
      it.printStackTrace()
      return
    }.getOrNull() ?: return
    var pool = query {
      GachaPool.find { GachaPoolTable.name eq data.name }.firstOrNull()
    }
    // 没有同名池子 新建池子
    if (pool == null) {
      query {
        pool = GachaPool.new {
          this.name = data.name
        }
      }
    } else {
      // 有同名池子, 根据用户输入判断是否继续
      if (poolName == null) {
        subject.sendMessage("""
          同名池子: ${data.name} 已经存在, 请使用
          /gacha update2 $id 新池子名字
          来更新
        """.trimIndent())
        return
      }
      query {
        pool = GachaPool.new {
          this.name = poolName
        }
      }
    }
    val insertCharacter = kotlin.runCatching {
      insertCharacter(pool!!, data.character)
    }.onFailure {
      // 不知道为什么创建失败, 删除创建的池子
      query {
        GachaPoolTable.deleteWhere {
          GachaPoolTable.id eq pool!!.id
        }
      }
      subject.sendMessage("新池子创建失败, 请查看控制台日志")
      throw it
    }.getOrNull() ?: return
    val message = """
      新池子: ${pool!!.name} 已添加, id: ${pool!!.id}
      ${insertCharacter.joinToString(", ") { GachaUtil.mapStudentInfo(it) }}
      使用指令
      /gacha setpool ${pool!!.id}
      来切换到这个池子
    """.trimIndent()
    subject.sendMessage(message)
  }

  private fun insertCharacter(pool: GachaPool, list: List<GachaCharacter>): List<GC> {
    return query { _ ->
      list.map {
        var gc = GC.find {
          GachaCharacterTable.name eq it.name
        }.firstOrNull()
        // 存在添加不存在删除
        if (gc == null) {
          gc = GC.new {
            this.name = it.name
            this.star = it.star
            this.limit = it.limit == 1
          }
        }
        GachaPoolCharacter.new {
          this.poolId = pool.id
          this.characterId = gc.id
        }
        return@map gc
      }
    }!!
  }

  override val id: Int = 1
  override val name: String = "抽卡配置"
  override var enable: Boolean = true
  override fun init() {
    registerService()
    register()
  }

}