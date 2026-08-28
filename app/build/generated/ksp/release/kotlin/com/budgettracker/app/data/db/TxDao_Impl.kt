package com.budgettracker.app.`data`.db

import androidx.room.EntityDeleteOrUpdateAdapter
import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import javax.`annotation`.processing.Generated
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
public class TxDao_Impl(
  __db: RoomDatabase,
) : TxDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfTxEntity: EntityInsertAdapter<TxEntity>

  private val __converters: Converters = Converters()

  private val __deleteAdapterOfTxEntity: EntityDeleteOrUpdateAdapter<TxEntity>

  private val __updateAdapterOfTxEntity: EntityDeleteOrUpdateAdapter<TxEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfTxEntity = object : EntityInsertAdapter<TxEntity>() {
      protected override fun createQuery(): String =
          "INSERT OR ABORT INTO `transactions` (`id`,`amountMinor`,`type`,`accountId`,`toAccountId`,`toAmountMinor`,`categoryId`,`date`,`note`,`createdAt`,`updatedAt`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: TxEntity) {
        statement.bindLong(1, entity.id)
        statement.bindLong(2, entity.amountMinor)
        val _tmp: String = __converters.txTypeToString(entity.type)
        statement.bindText(3, _tmp)
        statement.bindLong(4, entity.accountId)
        val _tmpToAccountId: Long? = entity.toAccountId
        if (_tmpToAccountId == null) {
          statement.bindNull(5)
        } else {
          statement.bindLong(5, _tmpToAccountId)
        }
        val _tmpToAmountMinor: Long? = entity.toAmountMinor
        if (_tmpToAmountMinor == null) {
          statement.bindNull(6)
        } else {
          statement.bindLong(6, _tmpToAmountMinor)
        }
        val _tmpCategoryId: Long? = entity.categoryId
        if (_tmpCategoryId == null) {
          statement.bindNull(7)
        } else {
          statement.bindLong(7, _tmpCategoryId)
        }
        statement.bindLong(8, entity.date)
        statement.bindText(9, entity.note)
        statement.bindLong(10, entity.createdAt)
        statement.bindLong(11, entity.updatedAt)
      }
    }
    this.__deleteAdapterOfTxEntity = object : EntityDeleteOrUpdateAdapter<TxEntity>() {
      protected override fun createQuery(): String = "DELETE FROM `transactions` WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: TxEntity) {
        statement.bindLong(1, entity.id)
      }
    }
    this.__updateAdapterOfTxEntity = object : EntityDeleteOrUpdateAdapter<TxEntity>() {
      protected override fun createQuery(): String =
          "UPDATE OR ABORT `transactions` SET `id` = ?,`amountMinor` = ?,`type` = ?,`accountId` = ?,`toAccountId` = ?,`toAmountMinor` = ?,`categoryId` = ?,`date` = ?,`note` = ?,`createdAt` = ?,`updatedAt` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: TxEntity) {
        statement.bindLong(1, entity.id)
        statement.bindLong(2, entity.amountMinor)
        val _tmp: String = __converters.txTypeToString(entity.type)
        statement.bindText(3, _tmp)
        statement.bindLong(4, entity.accountId)
        val _tmpToAccountId: Long? = entity.toAccountId
        if (_tmpToAccountId == null) {
          statement.bindNull(5)
        } else {
          statement.bindLong(5, _tmpToAccountId)
        }
        val _tmpToAmountMinor: Long? = entity.toAmountMinor
        if (_tmpToAmountMinor == null) {
          statement.bindNull(6)
        } else {
          statement.bindLong(6, _tmpToAmountMinor)
        }
        val _tmpCategoryId: Long? = entity.categoryId
        if (_tmpCategoryId == null) {
          statement.bindNull(7)
        } else {
          statement.bindLong(7, _tmpCategoryId)
        }
        statement.bindLong(8, entity.date)
        statement.bindText(9, entity.note)
        statement.bindLong(10, entity.createdAt)
        statement.bindLong(11, entity.updatedAt)
        statement.bindLong(12, entity.id)
      }
    }
  }

  public override suspend fun insert(tx: TxEntity): Long = performSuspending(__db, false, true) {
      _connection ->
    val _result: Long = __insertAdapterOfTxEntity.insertAndReturnId(_connection, tx)
    _result
  }

  public override suspend fun insertAll(txs: List<TxEntity>): Unit = performSuspending(__db, false,
      true) { _connection ->
    __insertAdapterOfTxEntity.insert(_connection, txs)
  }

  public override suspend fun delete(tx: TxEntity): Unit = performSuspending(__db, false, true) {
      _connection ->
    __deleteAdapterOfTxEntity.handle(_connection, tx)
  }

  public override suspend fun update(tx: TxEntity): Unit = performSuspending(__db, false, true) {
      _connection ->
    __updateAdapterOfTxEntity.handle(_connection, tx)
  }

  public override fun observeAll(): Flow<List<TxEntity>> {
    val _sql: String = "SELECT * FROM transactions ORDER BY date DESC, id DESC"
    return createFlow(__db, false, arrayOf("transactions")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfAmountMinor: Int = getColumnIndexOrThrow(_stmt, "amountMinor")
        val _columnIndexOfType: Int = getColumnIndexOrThrow(_stmt, "type")
        val _columnIndexOfAccountId: Int = getColumnIndexOrThrow(_stmt, "accountId")
        val _columnIndexOfToAccountId: Int = getColumnIndexOrThrow(_stmt, "toAccountId")
        val _columnIndexOfToAmountMinor: Int = getColumnIndexOrThrow(_stmt, "toAmountMinor")
        val _columnIndexOfCategoryId: Int = getColumnIndexOrThrow(_stmt, "categoryId")
        val _columnIndexOfDate: Int = getColumnIndexOrThrow(_stmt, "date")
        val _columnIndexOfNote: Int = getColumnIndexOrThrow(_stmt, "note")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _result: MutableList<TxEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: TxEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpAmountMinor: Long
          _tmpAmountMinor = _stmt.getLong(_columnIndexOfAmountMinor)
          val _tmpType: TxType
          val _tmp: String
          _tmp = _stmt.getText(_columnIndexOfType)
          _tmpType = __converters.stringToTxType(_tmp)
          val _tmpAccountId: Long
          _tmpAccountId = _stmt.getLong(_columnIndexOfAccountId)
          val _tmpToAccountId: Long?
          if (_stmt.isNull(_columnIndexOfToAccountId)) {
            _tmpToAccountId = null
          } else {
            _tmpToAccountId = _stmt.getLong(_columnIndexOfToAccountId)
          }
          val _tmpToAmountMinor: Long?
          if (_stmt.isNull(_columnIndexOfToAmountMinor)) {
            _tmpToAmountMinor = null
          } else {
            _tmpToAmountMinor = _stmt.getLong(_columnIndexOfToAmountMinor)
          }
          val _tmpCategoryId: Long?
          if (_stmt.isNull(_columnIndexOfCategoryId)) {
            _tmpCategoryId = null
          } else {
            _tmpCategoryId = _stmt.getLong(_columnIndexOfCategoryId)
          }
          val _tmpDate: Long
          _tmpDate = _stmt.getLong(_columnIndexOfDate)
          val _tmpNote: String
          _tmpNote = _stmt.getText(_columnIndexOfNote)
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          val _tmpUpdatedAt: Long
          _tmpUpdatedAt = _stmt.getLong(_columnIndexOfUpdatedAt)
          _item =
              TxEntity(_tmpId,_tmpAmountMinor,_tmpType,_tmpAccountId,_tmpToAccountId,_tmpToAmountMinor,_tmpCategoryId,_tmpDate,_tmpNote,_tmpCreatedAt,_tmpUpdatedAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun byId(id: Long): TxEntity? {
    val _sql: String = "SELECT * FROM transactions WHERE id = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, id)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfAmountMinor: Int = getColumnIndexOrThrow(_stmt, "amountMinor")
        val _columnIndexOfType: Int = getColumnIndexOrThrow(_stmt, "type")
        val _columnIndexOfAccountId: Int = getColumnIndexOrThrow(_stmt, "accountId")
        val _columnIndexOfToAccountId: Int = getColumnIndexOrThrow(_stmt, "toAccountId")
        val _columnIndexOfToAmountMinor: Int = getColumnIndexOrThrow(_stmt, "toAmountMinor")
        val _columnIndexOfCategoryId: Int = getColumnIndexOrThrow(_stmt, "categoryId")
        val _columnIndexOfDate: Int = getColumnIndexOrThrow(_stmt, "date")
        val _columnIndexOfNote: Int = getColumnIndexOrThrow(_stmt, "note")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _result: TxEntity?
        if (_stmt.step()) {
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpAmountMinor: Long
          _tmpAmountMinor = _stmt.getLong(_columnIndexOfAmountMinor)
          val _tmpType: TxType
          val _tmp: String
          _tmp = _stmt.getText(_columnIndexOfType)
          _tmpType = __converters.stringToTxType(_tmp)
          val _tmpAccountId: Long
          _tmpAccountId = _stmt.getLong(_columnIndexOfAccountId)
          val _tmpToAccountId: Long?
          if (_stmt.isNull(_columnIndexOfToAccountId)) {
            _tmpToAccountId = null
          } else {
            _tmpToAccountId = _stmt.getLong(_columnIndexOfToAccountId)
          }
          val _tmpToAmountMinor: Long?
          if (_stmt.isNull(_columnIndexOfToAmountMinor)) {
            _tmpToAmountMinor = null
          } else {
            _tmpToAmountMinor = _stmt.getLong(_columnIndexOfToAmountMinor)
          }
          val _tmpCategoryId: Long?
          if (_stmt.isNull(_columnIndexOfCategoryId)) {
            _tmpCategoryId = null
          } else {
            _tmpCategoryId = _stmt.getLong(_columnIndexOfCategoryId)
          }
          val _tmpDate: Long
          _tmpDate = _stmt.getLong(_columnIndexOfDate)
          val _tmpNote: String
          _tmpNote = _stmt.getText(_columnIndexOfNote)
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          val _tmpUpdatedAt: Long
          _tmpUpdatedAt = _stmt.getLong(_columnIndexOfUpdatedAt)
          _result =
              TxEntity(_tmpId,_tmpAmountMinor,_tmpType,_tmpAccountId,_tmpToAccountId,_tmpToAmountMinor,_tmpCategoryId,_tmpDate,_tmpNote,_tmpCreatedAt,_tmpUpdatedAt)
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
    val _sql: String = "SELECT COUNT(*) FROM transactions"
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

  public override suspend fun countForAccount(accountId: Long): Int {
    val _sql: String = "SELECT COUNT(*) FROM transactions WHERE accountId = ? OR toAccountId = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, accountId)
        _argIndex = 2
        _stmt.bindLong(_argIndex, accountId)
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

  public override suspend fun snapshot(): List<TxEntity> {
    val _sql: String = "SELECT * FROM transactions"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfAmountMinor: Int = getColumnIndexOrThrow(_stmt, "amountMinor")
        val _columnIndexOfType: Int = getColumnIndexOrThrow(_stmt, "type")
        val _columnIndexOfAccountId: Int = getColumnIndexOrThrow(_stmt, "accountId")
        val _columnIndexOfToAccountId: Int = getColumnIndexOrThrow(_stmt, "toAccountId")
        val _columnIndexOfToAmountMinor: Int = getColumnIndexOrThrow(_stmt, "toAmountMinor")
        val _columnIndexOfCategoryId: Int = getColumnIndexOrThrow(_stmt, "categoryId")
        val _columnIndexOfDate: Int = getColumnIndexOrThrow(_stmt, "date")
        val _columnIndexOfNote: Int = getColumnIndexOrThrow(_stmt, "note")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _result: MutableList<TxEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: TxEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpAmountMinor: Long
          _tmpAmountMinor = _stmt.getLong(_columnIndexOfAmountMinor)
          val _tmpType: TxType
          val _tmp: String
          _tmp = _stmt.getText(_columnIndexOfType)
          _tmpType = __converters.stringToTxType(_tmp)
          val _tmpAccountId: Long
          _tmpAccountId = _stmt.getLong(_columnIndexOfAccountId)
          val _tmpToAccountId: Long?
          if (_stmt.isNull(_columnIndexOfToAccountId)) {
            _tmpToAccountId = null
          } else {
            _tmpToAccountId = _stmt.getLong(_columnIndexOfToAccountId)
          }
          val _tmpToAmountMinor: Long?
          if (_stmt.isNull(_columnIndexOfToAmountMinor)) {
            _tmpToAmountMinor = null
          } else {
            _tmpToAmountMinor = _stmt.getLong(_columnIndexOfToAmountMinor)
          }
          val _tmpCategoryId: Long?
          if (_stmt.isNull(_columnIndexOfCategoryId)) {
            _tmpCategoryId = null
          } else {
            _tmpCategoryId = _stmt.getLong(_columnIndexOfCategoryId)
          }
          val _tmpDate: Long
          _tmpDate = _stmt.getLong(_columnIndexOfDate)
          val _tmpNote: String
          _tmpNote = _stmt.getText(_columnIndexOfNote)
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          val _tmpUpdatedAt: Long
          _tmpUpdatedAt = _stmt.getLong(_columnIndexOfUpdatedAt)
          _item =
              TxEntity(_tmpId,_tmpAmountMinor,_tmpType,_tmpAccountId,_tmpToAccountId,_tmpToAmountMinor,_tmpCategoryId,_tmpDate,_tmpNote,_tmpCreatedAt,_tmpUpdatedAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteAll() {
    val _sql: String = "DELETE FROM transactions"
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
