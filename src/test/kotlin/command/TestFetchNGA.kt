package org.example.mirai.plugin.command

import net.diyigemt.arona.entity.Activity
import org.jsoup.Jsoup
import org.junit.jupiter.api.Test
import java.util.Calendar
import java.util.Random

class TestFetchNGA {

  val imageSrcBaseAddr = "https://img.nga.178.com/attachments/"
  @Test
  fun testFetchNGA() {
    val tid = "42164110"
    val cid = "Z8ghnljttk4qes3j9tg9bl4c0rampo98ecpdjdsl"
    val cookies = mutableMapOf(
      "ngaPassportUid" to tid,
      "ngaPassportCid" to cid,
      "lastvisit" to "",
      "lastpath" to ""
    )
    val random = Calendar.getInstance().time.time - Random().nextInt(25) + 5
    cookies["lastvisit"] = random.toString()
    cookies["lastpath"] = "/read.php?tid=$tid"
    val res = Jsoup.connect("https://ngabbs.com/read.php?tid=30843163")
      .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/102.0.0.0 Safari/537.36")
      .ignoreContentType(true)
      .cookies(cookies)
      .get()
      .body()
    println(res)
  }

}