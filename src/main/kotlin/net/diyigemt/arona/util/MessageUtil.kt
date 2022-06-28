package net.diyigemt.arona.util

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import net.diyigemt.arona.quartz.QuartzProvider
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.User
import net.mamoe.mirai.message.MessageReceipt
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.MessageChain
import org.quartz.Job
import org.quartz.JobExecutionContext
import java.util.Calendar

object MessageUtil {

  private const val MessageRecallJobKey = "MessageRecall"
  private const val JobDataMessageKey = "message"


  fun at(user: User, msg: MessageChain): MessageChain {
    return At(user).plus(msg)
  }

  fun at(user: User, msg: String): MessageChain {
    return At(user).plus(msg)
  }

  fun atAndCTRL(user: User, msg: MessageChain): MessageChain {
    return At(user).plus("\n").plus(msg)
  }

  fun atAndCTRL(user: User, msg: String): MessageChain {
    return At(user).plus("\n").plus(msg)
  }

  fun atMessageAndCTRL(user: User, inline: String, msg: String): MessageChain {
    return At(user).plus("$inline\n").plus(msg)
  }

  fun recall(target: MessageReceipt<Contact>, delay: Long = 1000 * 10) {
    val instance = Calendar.getInstance()
    instance.set(Calendar.MILLISECOND, (instance.get(Calendar.MILLISECOND) + delay).toInt())
    QuartzProvider.createSingleTask(
      MessageRecallJob::class.java,
      instance.time,
      MessageRecallJobKey,
      MessageRecallJobKey,
      mapOf(JobDataMessageKey to target)
    )
  }

  class MessageRecallJob: Job {
    override fun execute(context: JobExecutionContext?) {
      val message = context?.jobDetail?.jobDataMap?.get(JobDataMessageKey) ?: return
      runBlocking {
        withContext(coroutineContext) {
          (message as MessageReceipt<Contact>).recall()
        }
      }
    }
  }

}