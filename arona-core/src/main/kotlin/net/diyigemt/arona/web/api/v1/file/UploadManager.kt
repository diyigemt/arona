package net.diyigemt.arona.web.api.v1.file

import com.squareup.moshi.Types
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.util.pipeline.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import net.diyigemt.arona.Arona
import net.diyigemt.arona.db.DataBaseProvider
import net.diyigemt.arona.db.reply.ContentItem
import net.diyigemt.arona.db.reply.MessageGroups
import net.diyigemt.arona.db.reply.MessageKind
import net.diyigemt.arona.util.GeneralUtils.toHex
import net.diyigemt.arona.util.MoshiUtil
import net.diyigemt.arona.web.api.v1.Worker
import net.diyigemt.arona.web.api.v1.message.ServerResponse
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import java.io.File
import java.net.URLDecoder
import java.util.*

/**
 *@Author hjn
 *@Create 2023/2/21
 */
object UploadManager:Worker {
  val mutex = Mutex()
  override suspend fun worker(context: PipelineContext<Unit, ApplicationCall>) {
    super.worker(context)
    when(context.call.request.httpMethod) {
      HttpMethod.Get -> kotlin.runCatching {
        val id = context.call.request.queryParameters["id"].let {
          if (it == null) {
            context.call.respond(ServerResponse(400, HttpStatusCode.BadRequest.description, "id can not be null"))
            return@runCatching
          } else return@let it
        }
        val fileType = kotlin.runCatching { FileType.valueOf(File(id).extension.uppercase()) }.getOrNull().let {
          if (it == null) {
            context.call.respond(ServerResponse(400, HttpStatusCode.BadRequest.description, "Unsupported file type"))
            return@runCatching
          } else return@let it
        }
        context.call.respondBytes { File("${Arona.dataFolder}\\${fileType.folder}\\$id").readBytes() }
      }.onFailure {
        it.printStackTrace()
        context.call.respond(ServerResponse(500, HttpStatusCode.InternalServerError.description, ""))
      }

      HttpMethod.Post -> kotlin.runCatching {
        val fileName = context.call.request.headers["arona-file-name"].let {
          if (it == null) {
            ServerResponse(400, HttpStatusCode.BadRequest.description, "Invalid fileName")
            return
          } else return@let File(URLDecoder.decode(it, "UTF-8")).nameWithoutExtension
        }
        var res = ""
        context.call.receiveMultipart().forEachPart {
          when(it) {
            is PartData.FileItem -> {
              val fileBytes = it.streamProvider().readBytes()
              FileType.identifyFileType(fileBytes).apply {
                if (this == null)
                  ServerResponse(400, HttpStatusCode.BadRequest.description, "Unsupported file magic")
                else {
                  val file = kotlin.run {
                    var uuid: String
                    do {
                      uuid = UUID.randomUUID().toString().split("-")[0]
                    } while (File("${Arona.dataFolder.path}\\${this.folder}\\$fileName-$uuid.${this.name.lowercase()}").exists())
                    Arona.info("${Arona.dataFolder.path}\\${this.folder}\\$fileName-$uuid.${this.name.lowercase()}")
                    File("${Arona.dataFolder.path}\\${this.folder}\\$fileName-$uuid.${this.name.lowercase()}")
                  }
                  file.createNewFile()
                  file.writeBytes(fileBytes)
                  res = file.name
                }
              }
            }
            else -> context.call.respond(
              ServerResponse(400, HttpStatusCode.BadRequest.description, "Unsupported form data disposal strategy")
            )
          }
        }
        return@runCatching res
      }.onFailure {
        it.printStackTrace()
        context.call.respond(ServerResponse(500, HttpStatusCode.InternalServerError.description, ""))
      }.onSuccess { context.call.respond(ServerResponse(200, "OK", it)) }
    }
  }

  interface MagicCompare {
    fun compareMagic(bytes: ByteArray): Boolean
  }

  enum class FileType(val folder: String): MagicCompare {
    PNG("image\\userdata") {
      override fun compareMagic(bytes: ByteArray): Boolean = bytes.copyOf(4).toHex() == "89504e47"
    },
    JPG("image\\userdata") {
      override fun compareMagic(bytes: ByteArray): Boolean {
        return bytes.copyOf(4).toHex() == "ffd8ffe0"
      }
    },
    GIF("image\\userdata") {
      override fun compareMagic(bytes: ByteArray): Boolean = bytes.copyOf(4).toHex() == "47494638"
    };

    companion object {
      fun identifyFileType(bytes: ByteArray): FileType? {
        FileType.values().forEach {
          if (it.compareMagic(bytes)) return it
        }
        return null
      }
    }
  }

  suspend fun removeUnusedFiles() = withContext(Dispatchers.IO) {
    val type = Types.newParameterizedType(List::class.java, ContentItem::class.java)
    mutex.withLock {
      val res: MutableMap<String, FileType> = mutableMapOf()
      DataBaseProvider.query {
        MessageGroups.slice(MessageGroups.content).selectAll().map { it[MessageGroups.content] }
      }?.forEach { contentList ->
        MoshiUtil.reflect.adapter<List<ContentItem>>(type).fromJson(contentList)?.forEach {
          if (it.type != MessageKind.String) kotlin.runCatching {
            res.put(it.content, (FileType.valueOf(File(it.content).extension.uppercase())))
          }
        }
      }
      kotlin.runCatching {
        File("${Arona.dataFolder}\\image\\userdata").listFiles()?.filter { it.name !in res.keys }?.forEach {
          it.delete()
        }
      }
    }
  }
}