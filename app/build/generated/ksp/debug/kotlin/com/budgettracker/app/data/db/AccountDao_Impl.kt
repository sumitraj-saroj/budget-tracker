package com.budgettracker.app.`data`.db

import androidx.room.EntityDeleteOrUpdateAdapter
import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import javax.`annotation`.processing.Generated
import kotlin.Boolean
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.List
import kotlin.collections.MutableList
import kotlin.collections.mutableListOf
import kotlin.reflect.KClass
import kotlinx.coroutines.flow.Flow

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class AccountDao_Impl(
  __db: RoomDatabase,
) : AccountDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfAccountEntity: EntityInsertAdapter<AccountEntity>

  private val __converters: Converters = Converters()

  private val __deleteAdapterOfAccountEntity: EntityDeleteOrUpdateAdapter<AccountEntity>

  private val __updateAdapterOfAccountEntity: EntityDeleteOrUpdateAdapter<AccountEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfAccountEntity = object : EntityInsertAdapter<AccountEntity>() {
      protected override fun createQuery(): String =
          "INSERT OR ABORT INTO `accounts` (`id`,`name`,`emoji`,`colorArgb`,`type`,`currencyCode`,`startingBalanceMinor`,`isArchived`,`includeInTotals`,`sortOrder`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: AccountEntity) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.name)
        statement.bindText(3, entity.emoji)
        statement.bindLong(4, entity.colorArgb)
        val _tmp: String = __converters.accountTypeToString(entity.type)
        statement.bindText(5, _tmp)
        statement.bindText(6, entity.currencyCode)
        statement.bindLong(7, entity.startingBalanceMinor)
        val _tmp_1: Int = if (entity.isArchived) 1 else 0
        statement.bindLong(8, _tmp_1.toLong())
        val _tmp_2: Int = if (entity.includeInTotals) 1 else 0
        statement.bindLong(9, _tmp_2.toLong())
        statement.bindLong(10, entity.sortOrder.toLong())
      }
    }
    this.__deleteAdapterOfAccountEntity = object : EntityDeleteOrUpdateAdapter<AccountEntity>() {
      protected override fun createQuery(): String = "DELETE FROM `accounts` WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: AccountEntity) {
        statement.bindLong(1, entity.id)
      }
    }
    this.__updateAdapterOfAccountEntity = object : EntityDeleteOrUpdateAdapter<AccountEntity>() {
      protected override fun createQuery(): String =
          "UPDATE OR ABORT `accounts` SET `id` = ?,`name` = ?,`emoji` = ?,`colorArgb` = ?,`type` = ?,`currencyCode` = ?,`startingBalanceMinor` = ?,`isArchived` = ?,`includeInTotals` = ?,`sortOrder` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: AccountEntity) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.name)
        statement.bindText(3, entity.emoji)
        statement.bindLong(4, entity.colorArgb)
        val _tmp: String = __converters.accountTypeToString(entity.type)
        statement.bindText(5, _tmp)
        statement.bindText(6, entity.currencyCode)
        statement.bindLong(7, entity.startingBalanceMinor)
        val _tmp_1: Int = if (entity.isArchived) 1 else 0
        statement.bindLong(8, _tmp_1.toLong())
        val _tmp_2: Int = if (entity.includeInTotals) 1 else 0
        statement.bindLong(9, _tmp_2.toLong())
        statement.bindLong(10, entity.sortOrder.toLong())
        statement.bindLong(11, entity.id)
      }
    }
  }

  public override suspend fun insert(account: AccountEntity): Long = performSuspending(__db, false,
      true) { _connection ->
    val _result: Long = __insertAdapterOfAccountEntity.insertAndReturnId(_connection, account)
    _result
  }

  public override suspend fun insertAll(accounts: List<AccountEntity>): Unit =
      performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfAccountEntity.insert(_connection, accounts)
  }

  public override suspend fun delete(account: AccountEntity): Unit = performSuspending(__db, false,
      true) { _connection ->
    __deleteAdapterOfAccountEntity.handle(_connection, account)
  }

  public override suspend fun update(account: AccountEntity): Unit = performSuspending(__db, false,
      true) { _connection ->
    __updateAdapterOfAccountEntity.handle(_connection, account)
  }

  public override fun observeAll(): Flow<List<AccountEntity>> {
    val _sql: String = "SELECT * FROM accounts ORDER BY isArchived, sortOrder, id"
    return createFlow(__db, false, arrayOf("accounts")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfEmoji: Int = getColumnIndexOrThrow(_stmt, "emoji")
        val _columnIndexOfColorArgb: Int = getColumnIndexOrThrow(_stmt, "colorArgb")
        val _columnIndexOfType: Int = getColumnIndexOrThrow(_stmt, "type")
        val _columnIndexOfCurrencyCode: Int = getColumnIndexOrThrow(_stmt, "currencyCode")
        val _columnIndexOfStartingBalanceMinor: Int = getColumnIndexOrThrow(_stmt,
            "startingBalanceMinor")
        val _columnIndexOfIsArchived: Int = getColumnIndexOrThrow(_stmt, "isArchived")
        val _columnIndexOfIncludeInTotals: Int = getColumnIndexOrThrow(_stmt, "includeInTotals")
        val _columnIndexOfSortOrder: Int = getColumnIndexOrThrow(_stmt, "sortOrder")
        val _result: MutableList<AccountEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: AccountEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpEmoji: String
          _tmpEmoji = _stmt.getText(_columnIndexOfEmoji)
          val _tmpColorArgb: Long
          _tmpColorArgb = _stmt.getLong(_columnIndexOfColorArgb)
          val _tmpType: AccountType
          val _tmp: String
          _tmp = _stmt.getText(_columnIndexOfType)
          _tmpType = __converters.stringToAccountType(_tmp)
          val _tmpCurrencyCode: String
          _tmpCurrencyCode = _stmt.getText(_columnIndexOfCurrencyCode)
          val _tmpStartingBalanceMinor: Long
          _tmpStartingBalanceMinor = _stmt.getLong(_columnIndexOfStartingBalanceMinor)
          val _tmpIsArchived: Boolean
          val _tmp_1: Int
          _tmp_1 = _stmt.getLong(_columnIndexOfIsArchived).toInt()
          _tmpIsArchived = _tmp_1 != 0
          val _tmpIncludeInTotals: Boolean
          val _tmp_2: Int
          _tmp_2 = _stmt.getLong(_columnIndexOfIncludeInTotals).toInt()
          _tmpIncludeInTotals = _tmp_2 != 0
          val _tmpSortOrder: Int
          _tmpSortOrder = _stmt.getLong(_columnIndexOfSortOrder).toInt()
          _item =
              AccountEntity(_tmpId,_tmpName,_tmpEmoji,_tmpColorArgb,_tmpType,_tmpCurrencyCode,_tmpStartingBalanceMinor,_tmpIsArchived,_tmpIncludeInTotals,_tmpSortOrder)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun byId(id: Long): AccountEntity? {
    val _sql: String = "SELECT * FROM accounts WHERE id = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, id)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfEmoji: Int = getColumnIndexOrThrow(_stmt, "emoji")
        val _columnIndexOfColorArgb: Int = getColumnIndexOrThrow(_stmt, "colorArgb")
        val _columnIndexOfType: Int = getColumnIndexOrThrow(_stmt, "type")
        val _columnIndexOfCurrencyCode: Int = getColumnIndexOrThrow(_stmt, "currencyCode")
        val _columnIndexOfStartingBalanceMinor: Int = getColumnIndexOrThrow(_stmt,
            "startingBalanceMinor")
        val _columnIndexOfIsArchived: Int = getColumnIndexOrThrow(_stmt, "isArchived")
        val _columnIndexOfIncludeInTotals: Int = getColumnIndexOrThrow(_stmt, "includeInTotals")
        val _columnIndexOfSortOrder: Int = getColumnIndexOrThrow(_stmt, "sortOrder")
        val _result: AccountEntity?
        if (_stmt.step()) {
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpEmoji: String
          _tmpEmoji = _stmt.getText(_columnIndexOfEmoji)
          val _tmpColorArgb: Long
          _tmpColorArgb = _stmt.getLong(_columnIndexOfColorArgb)
          val _tmpType: AccountType
          val _tmp: String
          _tmp = _stmt.getText(_columnIndexOfType)
          _tmpType = __converters.stringToAccountType(_tmp)
          val _tmpCurrencyCode: String
          _tmpCurrencyCode = _stmt.getText(_columnIndexOfCurrencyCode)
          val _tmpStartingBalanceMinor: Long
          _tmpStartingBalanceMinor = _stmt.getLong(_columnIndexOfStartingBalanceMinor)
          val _tmpIsArchived: Boolean
          val _tmp_1: Int
          _tmp_1 = _stmt.getLong(_columnIndexOfIsArchived).toInt()
          _tmpIsArchived = _tmp_1 != 0
          val _tmpIncludeInTotals: Boolean
          val _tmp_2: Int
          _tmp_2 = _stmt.getLong(_columnIndexOfIncludeInTotals).toInt()
          _tmpIncludeInTotals = _tmp_2 != 0
          val _tmpSortOrder: Int
          _tmpSortOrder = _stmt.getLong(_columnIndexOfSortOrder).toInt()
          _result =
              AccountEntity(_tmpId,_tmpName,_tmpEmoji,_tmpColorArgb,_tmpType,_tmpCurrencyCode,_tmpStartingBalanceMinor,_tmpIsArchived,_tmpIncludeInTotals,_tmpSortOrder)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun count(): Int {
    val _sql: String = "SELECT COUNT(*) FROM accounts"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _result: Int
        if (_stmt.step()) {
          val _tmp: Int
          _tmp = _stmt.getLong(0).toInt()
          _result = _tmp
        } else {
          _result = 0
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun snapshot(): List<AccountEntity> {
    val _sql: String = "SELECT * FROM accounts"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfEmoji: Int = getColumnIndexOrThrow(_stmt, "emoji")
        val _columnIndexOfColorArgb: Int = getColumnIndexOrThrow(_stmt, "colorArgb")
        val _columnIndexOfType: Int = getColumnIndexOrThrow(_stmt, "type")
        val _columnIndexOfCurrencyCode: Int = getColumnIndexOrThrow(_stmt, "currencyCode")
        val _columnIndexOfStartingBalanceMinor: Int = getColumnIndexOrThrow(_stmt,
            "startingBalanceMinor")
        val _columnIndexOfIsArchived: Int = getColumnIndexOrThrow(_stmt, "isArchived")
        val _columnIndexOfIncludeInTotals: Int = getColumnIndexOrThrow(_stmt, "includeInTotals")
        val _columnIndexOfSortOrder: Int = getColumnIndexOrThrow(_stmt, "sortOrder")
        val _result: MutableList<AccountEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: AccountEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpEmoji: String
          _tmpEmoji = _stmt.getText(_columnIndexOfEmoji)
          val _tmpColorArgb: Long
          _tmpColorArgb = _stmt.getLong(_columnIndexOfColorArgb)
          val _tmpType: AccountType
          val _tmp: String
          _tmp = _stmt.getText(_columnIndexOfType)
          _tmpType = __converters.stringToAccountType(_tmp)
          val _tmpCurrencyCode: String
          _tmpCurrencyCode = _stmt.getText(_columnIndexOfCurrencyCode)
          val _tmpStartingBalanceMinor: Long
          _tmpStartingBalanceMinor = _stmt.getLong(_columnIndexOfStartingBalanceMinor)
          val _tmpIsArchived: Boolean
          val _tmp_1: Int
          _tmp_1 = _stmt.getLong(_columnIndexOfIsArchived).toInt()
          _tmpIsArchived = _tmp_1 != 0
          val _tmpIncludeInTotals: Boolean
          val _tmp_2: Int
          _tmp_2 = _stmt.getLong(_columnIndexOfIncludeInTotals).toInt()
          _tmpIncludeInTotals = _tmp_2 != 0
          val _tmpSortOrder: Int
          _tmpSortOrder = _stmt.getLong(_columnIndexOfSortOrder).toInt()
          _item =
              AccountEntity(_tmpId,_tmpName,_tmpEmoji,_tmpColorArgb,_tmpType,_tmpCurrencyCode,_tmpStartingBalanceMinor,_tmpIsArchived,_tmpIncludeInTotals,_tmpSortOrder)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteAll() {
    val _sql: String = "DELETE FROM accounts"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public companion object {
    public fun getRequiredConverters(): List<KClass<*>> = emptyList()
  }
}
