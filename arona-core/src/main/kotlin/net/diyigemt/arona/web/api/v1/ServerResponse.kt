package net.diyigemt.arona.web.api.v1

import kotlinx.serialization.Serializable

/**
 *@Author hjn
 *@Create 2022/10/16
 */

@Serializable
data class ServerResponse<T>(
    val code : Int,
    val message : String,
    val data : T? = null
)