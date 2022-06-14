package net.diyigemt.arona.threadpool

import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.message.MessageReceipt
import java.util.Timer
import java.util.TimerTask

object RecallTimer {

  private val timer = Timer()

  fun recall(target: MessageReceipt<Contact>) {
    timer.schedule(RecallTimerTask(target), 1000 * 10)
  }

  class RecallTimerTask(private val target: MessageReceipt<Contact>): TimerTask() {
    override fun run() {
      runBlocking {
        target.recall()
      }
    }
  }

}
