package net.diyigemt.arona.db

object DBConstant {

  private const val GLOBAL_DB_NAME: String = "arona.db"
  private const val DATA_DB_NAME: String = "data.db"

  val dbNameList = mutableListOf(GLOBAL_DB_NAME, DATA_DB_NAME)
}

enum class DB{
  DEFAULT,
  DATA
}