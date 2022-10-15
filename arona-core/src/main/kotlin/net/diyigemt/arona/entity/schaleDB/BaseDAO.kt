package net.diyigemt.arona.entity.schaleDB

/**
 *@Author hjn
 *@Create 2022/8/29
 */
interface BaseDAO {
  fun sendToDataBase()

  fun <T : BaseDAO> toModel(dao : T) : T

  fun getServer(type : Boolean) : String = if(type) "JPN" else "GLB"
}