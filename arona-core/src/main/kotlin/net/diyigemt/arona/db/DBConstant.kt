package net.diyigemt.arona.db

object DBConstant {

  private const val GLOBAL_DB_NAME: String = "arona.db"
  private const val SCHALE_DB_NAME: String = "schale.db"

  val dbNameList = mutableListOf(GLOBAL_DB_NAME, SCHALE_DB_NAME)
}

enum class DB{
  DEFAULT,
  DATA
}