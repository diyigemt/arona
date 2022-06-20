package net.diyigemt.arona.threadpool

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import net.diyigemt.arona.Arona
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.message.MessageReceipt
import java.util.Timer
import java.util.TimerTask

object RecallTimer {

  private val timer = Timer()

  fun recall(target: MessageReceipt<Contact>, delay: Long = 1000 * 10) {
    timer.schedule(RecallTimerTask(target), delay)
  }

  class RecallTimerTask(private val target: MessageReceipt<Contact>): TimerTask() {
    override fun run() {
      runBlocking {
        withContext(Arona.coroutineContext) {
          target.recall()
        }
      }
    }
  }

}
