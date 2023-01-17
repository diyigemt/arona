package net.diyigemt.arona.web.entity

import kotlinx.serialization.Serializable
import net.mamoe.mirai.contact.*

/**
 * bot和它的联系人们
 */
@Serializable
data class BotContact(
  val bot: Long,
  val friends: List<BotFriend>,
  val groups: List<BotGroup>
)

@Serializable
data class BotGroup (
  val id : Long,
  val name : String,
  val permission: MemberPermission
)

@Serializable
data class BotFriend (
  val id : Long,
  val name : String,
  val remark: String
)

@Serializable
data class BotGroupMember (
  val id : Long,
  val memberName : String,
  val permission: MemberPermission,
  val specialTitle: String,
  val joinTimestamp: Int,
  val lastSpeakTimestamp: Int,
  val muteTimeRemaining: Int,
  val group: BotGroup,
)

fun Group.toGroup(): BotGroup = BotGroup(this.id, this.name, this.botPermission)
fun Friend.toFriend(): BotFriend = BotFriend(this.id, this.nameCardOrNick, this.remarkOrNick)
fun NormalMember.toMember(): BotGroupMember = BotGroupMember(
  this.id,
  this.nameCardOrNick,
  this.permission,
  this.specialTitle,
  this.joinTimestamp,
  this.lastSpeakTimestamp,
  this.muteTimeRemaining,
  BotGroup(this.group.id, this.group.name, this.group.botPermission)
)