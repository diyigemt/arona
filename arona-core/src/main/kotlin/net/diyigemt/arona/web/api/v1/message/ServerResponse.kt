package net.diyigemt.arona.web.api.v1.message

import com.squareup.moshi.JsonClass
import kotlinx.serialization.Serializable

/**
 *@Author hjn
 *@Create 2022/10/16
 */

@Serializable
@JsonClass(generateAdapter = true)
data class ServerResponse<T>(
    val code : Int,
    val message : String,
    val data : T? = null
)