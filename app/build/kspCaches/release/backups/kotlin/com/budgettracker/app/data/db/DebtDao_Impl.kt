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
public class DebtDao_Impl(
  __db: RoomDatabase,
) : DebtDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfDebtEntity: EntityInsertAdapter<DebtEntity>

  private val __converters: Converters = Converters()

  private val __deleteAdapterOfDebtEntity: EntityDeleteOrUpdateAdapter<DebtEntity>

  private val __updateAdapterOfDebtEntity: EntityDeleteOrUpdateAdapter<DebtEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfDebtEntity = object : EntityInsertAdapter<DebtEntity>() {
      protected override fun createQuery(): String =
          "INSERT OR ABORT INTO `debts` (`id`,`direction`,`personName`,`amountMinor`,`note`,`dueDate`,`isSettled`,`settledAt`,`createdAt`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: DebtEntity) {
        statement.bindLong(1, entity.id)
        val _tmp: String = __converters.debtDirectionToString(entity.direction)
        statement.bindText(2, _tmp)
        statement.bindText(3, entity.personName)
        statement.bindLong(4, entity.amountMinor)
        statement.bindText(5, entity.note)
        val _tmpDueDate: Long? = entity.dueDate
        if (_tmpDueDate == null) {
          statement.bindNull(6)
        } else {
          statement.bindLong(6, _tmpDueDate)
        }
        val _tmp_1: Int = if (entity.isSettled) 1 else 0
        statement.bindLong(7, _tmp_1.toLong())
        val _tmpSettledAt: Long? = entity.settledAt
        if (_tmpSettledAt == null) {
          statement.bindNull(8)
        } else {
          statement.bindLong(8, _tmpSettledAt)
        }
        statement.bindLong(9, entity.createdAt)
      }
    }
    this.__deleteAdapterOfDebtEntity = object : EntityDeleteOrUpdateAdapter<DebtEntity>() {
      protected override fun createQuery(): String = "DELETE FROM `debts` WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: DebtEntity) {
        statement.bindLong(1, entity.id)
      }
    }
    this.__updateAdapterOfDebtEntity = object : EntityDeleteOrUpdateAdapter<DebtEntity>() {
      protected override fun createQuery(): String =
          "UPDATE OR ABORT `debts` SET `id` = ?,`direction` = ?,`personName` = ?,`amountMinor` = ?,`note` = ?,`dueDate` = ?,`isSettled` = ?,`settledAt` = ?,`createdAt` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: DebtEntity) {
        statement.bindLong(1, entity.id)
        val _tmp: String = __converters.debtDirectionToString(entity.direction)
        statement.bindText(2, _tmp)
        statement.bindText(3, entity.personName)
        statement.bindLong(4, entity.amountMinor)
        statement.bindText(5, entity.note)
        val _tmpDueDate: Long? = entity.dueDate
        if (_tmpDueDate == null) {
          statement.bindNull(6)
        } else {
          statement.bindLong(6, _tmpDueDate)
        }
        val _tmp_1: Int = if (entity.isSettled) 1 else 0
        statement.bindLong(7, _tmp_1.toLong())
        val _tmpSettledAt: Long? = entity.settledAt
        if (_tmpSettledAt == null) {
          statement.bindNull(8)
        } else {
          statement.bindLong(8, _tmpSettledAt)
        }
        statement.bindLong(9, entity.createdAt)
        statement.bindLong(10, entity.id)
      }
    }
  }

  public override suspend fun insert(debt: DebtEntity): Long = performSuspending(__db, false, true)
      { _connection ->
    val _result: Long = __insertAdapterOfDebtEntity.insertAndReturnId(_connection, debt)
    _result
  }

  public override suspend fun insertAll(debts: List<DebtEntity>): Unit = performSuspending(__db,
      false, true) { _connection ->
    __insertAdapterOfDebtEntity.insert(_connection, debts)
  }

  public override suspend fun delete(debt: DebtEntity): Unit = performSuspending(__db, false, true)
      { _connection ->
    __deleteAdapterOfDebtEntity.handle(_connection, debt)
  }

  public override suspend fun update(debt: DebtEntity): Unit = performSuspending(__db, false, true)
      { _connection ->
    __updateAdapterOfDebtEntity.handle(_connection, debt)
  }

  public override fun observeAll(): Flow<List<DebtEntity>> {
    val _sql: String = "SELECT * FROM debts ORDER BY isSettled, dueDate, id"
    return createFlow(__db, false, arrayOf("debts")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfDirection: Int = getColumnIndexOrThrow(_stmt, "direction")
        val _columnIndexOfPersonName: Int = getColumnIndexOrThrow(_stmt, "personName")
        val _columnIndexOfAmountMinor: Int = getColumnIndexOrThrow(_stmt, "amountMinor")
        val _columnIndexOfNote: Int = getColumnIndexOrThrow(_stmt, "note")
        val _columnIndexOfDueDate: Int = getColumnIndexOrThrow(_stmt, "dueDate")
        val _columnIndexOfIsSettled: Int = getColumnIndexOrThrow(_stmt, "isSettled")
        val _columnIndexOfSettledAt: Int = getColumnIndexOrThrow(_stmt, "settledAt")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _result: MutableList<DebtEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: DebtEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpDirection: DebtDirection
          val _tmp: String
          _tmp = _stmt.getText(_columnIndexOfDirection)
          _tmpDirection = __converters.stringToDebtDirection(_tmp)
          val _tmpPersonName: String
          _tmpPersonName = _stmt.getText(_columnIndexOfPersonName)
          val _tmpAmountMinor: Long
          _tmpAmountMinor = _stmt.getLong(_columnIndexOfAmountMinor)
          val _tmpNote: String
          _tmpNote = _stmt.getText(_columnIndexOfNote)
          val _tmpDueDate: Long?
          if (_stmt.isNull(_columnIndexOfDueDate)) {
            _tmpDueDate = null
          } else {
            _tmpDueDate = _stmt.getLong(_columnIndexOfDueDate)
          }
          val _tmpIsSettled: Boolean
          val _tmp_1: Int
          _tmp_1 = _stmt.getLong(_columnIndexOfIsSettled).toInt()
          _tmpIsSettled = _tmp_1 != 0
          val _tmpSettledAt: Long?
          if (_stmt.isNull(_columnIndexOfSettledAt)) {
            _tmpSettledAt = null
          } else {
            _tmpSettledAt = _stmt.getLong(_columnIndexOfSettledAt)
          }
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          _item =
              DebtEntity(_tmpId,_tmpDirection,_tmpPersonName,_tmpAmountMinor,_tmpNote,_tmpDueDate,_tmpIsSettled,_tmpSettledAt,_tmpCreatedAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun byId(id: Long): DebtEntity? {
    val _sql: String = "SELECT * FROM debts WHERE id = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, id)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfDirection: Int = getColumnIndexOrThrow(_stmt, "direction")
        val _columnIndexOfPersonName: Int = getColumnIndexOrThrow(_stmt, "personName")
        val _columnIndexOfAmountMinor: Int = getColumnIndexOrThrow(_stmt, "amountMinor")
        val _columnIndexOfNote: Int = getColumnIndexOrThrow(_stmt, "note")
        val _columnIndexOfDueDate: Int = getColumnIndexOrThrow(_stmt, "dueDate")
        val _columnIndexOfIsSettled: Int = getColumnIndexOrThrow(_stmt, "isSettled")
        val _columnIndexOfSettledAt: Int = getColumnIndexOrThrow(_stmt, "settledAt")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _result: DebtEntity?
        if (_stmt.step()) {
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpDirection: DebtDirection
          val _tmp: String
          _tmp = _stmt.getText(_columnIndexOfDirection)
          _tmpDirection = __converters.stringToDebtDirection(_tmp)
          val _tmpPersonName: String
          _tmpPersonName = _stmt.getText(_columnIndexOfPersonName)
          val _tmpAmountMinor: Long
          _tmpAmountMinor = _stmt.getLong(_columnIndexOfAmountMinor)
          val _tmpNote: String
          _tmpNote = _stmt.getText(_columnIndexOfNote)
          val _tmpDueDate: Long?
          if (_stmt.isNull(_columnIndexOfDueDate)) {
            _tmpDueDate = null
          } else {
            _tmpDueDate = _stmt.getLong(_columnIndexOfDueDate)
          }
          val _tmpIsSettled: Boolean
          val _tmp_1: Int
          _tmp_1 = _stmt.getLong(_columnIndexOfIsSettled).toInt()
          _tmpIsSettled = _tmp_1 != 0
          val _tmpSettledAt: Long?
          if (_stmt.isNull(_columnIndexOfSettledAt)) {
            _tmpSettledAt = null
          } else {
            _tmpSettledAt = _stmt.getLong(_columnIndexOfSettledAt)
          }
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          _result =
              DebtEntity(_tmpId,_tmpDirection,_tmpPersonName,_tmpAmountMinor,_tmpNote,_tmpDueDate,_tmpIsSettled,_tmpSettledAt,_tmpCreatedAt)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun snapshot(): List<DebtEntity> {
    val _sql: String = "SELECT * FROM debts"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfDirection: Int = getColumnIndexOrThrow(_stmt, "direction")
        val _columnIndexOfPersonName: Int = getColumnIndexOrThrow(_stmt, "personName")
        val _columnIndexOfAmountMinor: Int = getColumnIndexOrThrow(_stmt, "amountMinor")
        val _columnIndexOfNote: Int = getColumnIndexOrThrow(_stmt, "note")
        val _columnIndexOfDueDate: Int = getColumnIndexOrThrow(_stmt, "dueDate")
        val _columnIndexOfIsSettled: Int = getColumnIndexOrThrow(_stmt, "isSettled")
        val _columnIndexOfSettledAt: Int = getColumnIndexOrThrow(_stmt, "settledAt")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _result: MutableList<DebtEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: DebtEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpDirection: DebtDirection
          val _tmp: String
          _tmp = _stmt.getText(_columnIndexOfDirection)
          _tmpDirection = __converters.stringToDebtDirection(_tmp)
          val _tmpPersonName: String
          _tmpPersonName = _stmt.getText(_columnIndexOfPersonName)
          val _tmpAmountMinor: Long
          _tmpAmountMinor = _stmt.getLong(_columnIndexOfAmountMinor)
          val _tmpNote: String
          _tmpNote = _stmt.getText(_columnIndexOfNote)
          val _tmpDueDate: Long?
          if (_stmt.isNull(_columnIndexOfDueDate)) {
            _tmpDueDate = null
          } else {
            _tmpDueDate = _stmt.getLong(_columnIndexOfDueDate)
          }
          val _tmpIsSettled: Boolean
          val _tmp_1: Int
          _tmp_1 = _stmt.getLong(_columnIndexOfIsSettled).toInt()
          _tmpIsSettled = _tmp_1 != 0
          val _tmpSettledAt: Long?
          if (_stmt.isNull(_columnIndexOfSettledAt)) {
            _tmpSettledAt = null
          } else {
            _tmpSettledAt = _stmt.getLong(_columnIndexOfSettledAt)
          }
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          _item =
              DebtEntity(_tmpId,_tmpDirection,_tmpPersonName,_tmpAmountMinor,_tmpNote,_tmpDueDate,_tmpIsSettled,_tmpSettledAt,_tmpCreatedAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteAll() {
    val _sql: String = "DELETE FROM debts"
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
