package net.diyigemt.arona.db

import kotlinx.coroutines.Dispatchers
import net.diyigemt.arona.Arona
import net.diyigemt.arona.interfaces.BaseFunctionProvider
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SqlLogger
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.statements.StatementContext
import org.jetbrains.exposed.sql.statements.expandArgs
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

object DataBaseProvider: BaseFunctionProvider() {

  override val tag: String = "arona data base"

  sealed class ConnectionStatus {
    object CONNECTED : ConnectionStatus()
    object DISCONNECTED : ConnectionStatus()
  }

  private lateinit var db: Database
  private var connectionStatus: ConnectionStatus = ConnectionStatus.DISCONNECTED

  override suspend fun main() {
    try {
      db = Database.connect("jdbc:sqlite:${Arona.dataFolder}/${DBConstant.GLOBAL_DB_NAME}", "org.sqlite.JDBC")
      connectionStatus = ConnectionStatus.CONNECTED
      initDataBase()
    } catch (e: Exception) {
      e.printStackTrace()
      Arona.error("Database initialization failed. Any operation that requires database support will not be performed.")
    }
  }

  private fun initDataBase() {
    query {
      it.addLogger(object: SqlLogger {
        override fun log(context: StatementContext, transaction: Transaction) {
          Arona.verbose { "SQL: ${context.expandArgs(transaction)}" }
        }
      })
      Arona.info("arona database init success.")
      BaseDataBase.init()
    }
  }

  fun isConnected() = connectionStatus == ConnectionStatus.CONNECTED

  fun <T> query(block: (Transaction) -> T) : T? = if(connectionStatus == ConnectionStatus.DISCONNECTED) {
    Arona.error { "Database is disconnected, Any operation that requires database support cannot be performed." }
    null
  } else transaction(db) { block(this) }

  suspend fun <T> suspendQuery(block: suspend (Transaction) -> T) : T? = if(connectionStatus == ConnectionStatus.DISCONNECTED) {
    Arona.error { "Database is disconnected, Any operation that requires database support cannot be performed." }
    null
  } else newSuspendedTransaction(context = Dispatchers.IO, db = db) { block(this) }

}