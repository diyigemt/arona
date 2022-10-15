package net.diyigemt.arona.db

import kotlinx.coroutines.Dispatchers
import net.diyigemt.arona.Arona
import net.diyigemt.arona.db.data.schaledb.SchaleDataBase
import net.diyigemt.arona.interfaces.BaseFunctionProvider
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SqlLogger
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.statements.StatementContext
import org.jetbrains.exposed.sql.statements.expandArgs
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

object DataBaseProvider: BaseFunctionProvider() {

  override val tag: String = "arona data base"

  sealed class ConnectionStatus {
    object CONNECTED : ConnectionStatus()
    object DISCONNECTED : ConnectionStatus()
  }

  data class Connection(
    var db: Database?,
    var connectionStatus: ConnectionStatus = ConnectionStatus.DISCONNECTED
  )

  private val databaseConnectionList = mutableListOf<Connection>()

  override suspend fun main() {
    for(item in DBConstant.dbNameList){
      connectDataBase(item)
    }
    if (databaseConnectionList.size > 0) {
      TransactionManager.defaultDatabase = databaseConnectionList[0].db
    }
  }

  private fun connectDataBase(dataBaseName : String){
    var tmp = Connection(null, ConnectionStatus.DISCONNECTED)
    runCatching {
      tmp = Connection(
        Database.connect("jdbc:sqlite:${Arona.dataFolder}/${dataBaseName}", "org.sqlite.JDBC")
      )
    }.onFailure {
      databaseConnectionList.add(tmp) //失败也插入，否则ID就串行了
      databaseConnectionList.last().connectionStatus = ConnectionStatus.DISCONNECTED
      Arona.error("Database ($dataBaseName) initialization failed. Any operation that requires database support will not be performed.")
    }.onSuccess {
      databaseConnectionList.add(tmp)
      databaseConnectionList.last().connectionStatus = ConnectionStatus.CONNECTED
      initDataBase(databaseConnectionList.size - 1)
    }
  }

  private fun initDataBase(id : Int) {
    query(id) {
      when(id){
        DB.DEFAULT.ordinal -> BaseDataBase.init()
        DB.DATA.ordinal -> SchaleDataBase.init()
        else -> Arona.warning("Undefined database id : $id.")
      }

      it.addLogger(object: SqlLogger {
        override fun log(context: StatementContext, transaction: Transaction) {
          Arona.verbose { "SQL: ${context.expandArgs(transaction)}" }
        }
      })
    }
  }

  fun isConnected(id : Int = 0) = databaseConnectionList[id].connectionStatus == ConnectionStatus.CONNECTED

  fun <T> query(db : Int = 0, block: (Transaction) -> T) : T? =
    if(databaseConnectionList[db].connectionStatus == ConnectionStatus.DISCONNECTED) {
    Arona.error { "Database is disconnected, Any operation that requires database support cannot be performed." }
    null
  } else transaction(databaseConnectionList[db].db) { block(this) }

  suspend fun <T> suspendQuery(db : Int = 0, block: suspend (Transaction) -> T) : T? =
    if(databaseConnectionList[db].connectionStatus == ConnectionStatus.DISCONNECTED) {
    Arona.error { "Database is disconnected, Any operation that requires database support cannot be performed." }
    null
  } else newSuspendedTransaction(context = Dispatchers.IO, db = databaseConnectionList[db].db) { block(this) }
}