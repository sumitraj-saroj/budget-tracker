package com.budgettracker.app.`data`.db

import androidx.room.EntityDeleteOrUpdateAdapter
import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import com.budgettracker.app.util.BudgetPeriodType
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
public class BudgetDao_Impl(
  __db: RoomDatabase,
) : BudgetDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfBudgetEntity: EntityInsertAdapter<BudgetEntity>

  private val __converters: Converters = Converters()

  private val __deleteAdapterOfBudgetEntity: EntityDeleteOrUpdateAdapter<BudgetEntity>

  private val __updateAdapterOfBudgetEntity: EntityDeleteOrUpdateAdapter<BudgetEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfBudgetEntity = object : EntityInsertAdapter<BudgetEntity>() {
      protected override fun createQuery(): String =
          "INSERT OR ABORT INTO `budgets` (`id`,`name`,`amountMinor`,`periodType`,`customStart`,`customLengthDays`,`categoryIds`,`createdAt`) VALUES (nullif(?, 0),?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: BudgetEntity) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.name)
        statement.bindLong(3, entity.amountMinor)
        val _tmp: String = __converters.budgetPeriodTypeToString(entity.periodType)
        statement.bindText(4, _tmp)
        val _tmpCustomStart: Long? = entity.customStart
        if (_tmpCustomStart == null) {
          statement.bindNull(5)
        } else {
          statement.bindLong(5, _tmpCustomStart)
        }
        val _tmpCustomLengthDays: Int? = entity.customLengthDays
        if (_tmpCustomLengthDays == null) {
          statement.bindNull(6)
        } else {
          statement.bindLong(6, _tmpCustomLengthDays.toLong())
        }
        val _tmp_1: String = __converters.longListToString(entity.categoryIds)
        statement.bindText(7, _tmp_1)
        statement.bindLong(8, entity.createdAt)
      }
    }
    this.__deleteAdapterOfBudgetEntity = object : EntityDeleteOrUpdateAdapter<BudgetEntity>() {
      protected override fun createQuery(): String = "DELETE FROM `budgets` WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: BudgetEntity) {
        statement.bindLong(1, entity.id)
      }
    }
    this.__updateAdapterOfBudgetEntity = object : EntityDeleteOrUpdateAdapter<BudgetEntity>() {
      protected override fun createQuery(): String =
          "UPDATE OR ABORT `budgets` SET `id` = ?,`name` = ?,`amountMinor` = ?,`periodType` = ?,`customStart` = ?,`customLengthDays` = ?,`categoryIds` = ?,`createdAt` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: BudgetEntity) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.name)
        statement.bindLong(3, entity.amountMinor)
        val _tmp: String = __converters.budgetPeriodTypeToString(entity.periodType)
        statement.bindText(4, _tmp)
        val _tmpCustomStart: Long? = entity.customStart
        if (_tmpCustomStart == null) {
          statement.bindNull(5)
        } else {
          statement.bindLong(5, _tmpCustomStart)
        }
        val _tmpCustomLengthDays: Int? = entity.customLengthDays
        if (_tmpCustomLengthDays == null) {
          statement.bindNull(6)
        } else {
          statement.bindLong(6, _tmpCustomLengthDays.toLong())
        }
        val _tmp_1: String = __converters.longListToString(entity.categoryIds)
        statement.bindText(7, _tmp_1)
        statement.bindLong(8, entity.createdAt)
        statement.bindLong(9, entity.id)
      }
    }
  }

  public override suspend fun insert(budget: BudgetEntity): Long = performSuspending(__db, false,
      true) { _connection ->
    val _result: Long = __insertAdapterOfBudgetEntity.insertAndReturnId(_connection, budget)
    _result
  }

  public override suspend fun insertAll(budgets: List<BudgetEntity>): Unit = performSuspending(__db,
      false, true) { _connection ->
    __insertAdapterOfBudgetEntity.insert(_connection, budgets)
  }

  public override suspend fun delete(budget: BudgetEntity): Unit = performSuspending(__db, false,
      true) { _connection ->
    __deleteAdapterOfBudgetEntity.handle(_connection, budget)
  }

  public override suspend fun update(budget: BudgetEntity): Unit = performSuspending(__db, false,
      true) { _connection ->
    __updateAdapterOfBudgetEntity.handle(_connection, budget)
  }

  public override fun observeAll(): Flow<List<BudgetEntity>> {
    val _sql: String = "SELECT * FROM budgets ORDER BY id"
    return createFlow(__db, false, arrayOf("budgets")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfAmountMinor: Int = getColumnIndexOrThrow(_stmt, "amountMinor")
        val _columnIndexOfPeriodType: Int = getColumnIndexOrThrow(_stmt, "periodType")
        val _columnIndexOfCustomStart: Int = getColumnIndexOrThrow(_stmt, "customStart")
        val _columnIndexOfCustomLengthDays: Int = getColumnIndexOrThrow(_stmt, "customLengthDays")
        val _columnIndexOfCategoryIds: Int = getColumnIndexOrThrow(_stmt, "categoryIds")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _result: MutableList<BudgetEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: BudgetEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpAmountMinor: Long
          _tmpAmountMinor = _stmt.getLong(_columnIndexOfAmountMinor)
          val _tmpPeriodType: BudgetPeriodType
          val _tmp: String
          _tmp = _stmt.getText(_columnIndexOfPeriodType)
          _tmpPeriodType = __converters.stringToBudgetPeriodType(_tmp)
          val _tmpCustomStart: Long?
          if (_stmt.isNull(_columnIndexOfCustomStart)) {
            _tmpCustomStart = null
          } else {
            _tmpCustomStart = _stmt.getLong(_columnIndexOfCustomStart)
          }
          val _tmpCustomLengthDays: Int?
          if (_stmt.isNull(_columnIndexOfCustomLengthDays)) {
            _tmpCustomLengthDays = null
          } else {
            _tmpCustomLengthDays = _stmt.getLong(_columnIndexOfCustomLengthDays).toInt()
          }
          val _tmpCategoryIds: List<Long>
          val _tmp_1: String
          _tmp_1 = _stmt.getText(_columnIndexOfCategoryIds)
          _tmpCategoryIds = __converters.stringToLongList(_tmp_1)
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          _item =
              BudgetEntity(_tmpId,_tmpName,_tmpAmountMinor,_tmpPeriodType,_tmpCustomStart,_tmpCustomLengthDays,_tmpCategoryIds,_tmpCreatedAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun byId(id: Long): BudgetEntity? {
    val _sql: String = "SELECT * FROM budgets WHERE id = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, id)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfAmountMinor: Int = getColumnIndexOrThrow(_stmt, "amountMinor")
        val _columnIndexOfPeriodType: Int = getColumnIndexOrThrow(_stmt, "periodType")
        val _columnIndexOfCustomStart: Int = getColumnIndexOrThrow(_stmt, "customStart")
        val _columnIndexOfCustomLengthDays: Int = getColumnIndexOrThrow(_stmt, "customLengthDays")
        val _columnIndexOfCategoryIds: Int = getColumnIndexOrThrow(_stmt, "categoryIds")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _result: BudgetEntity?
        if (_stmt.step()) {
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpAmountMinor: Long
          _tmpAmountMinor = _stmt.getLong(_columnIndexOfAmountMinor)
          val _tmpPeriodType: BudgetPeriodType
          val _tmp: String
          _tmp = _stmt.getText(_columnIndexOfPeriodType)
          _tmpPeriodType = __converters.stringToBudgetPeriodType(_tmp)
          val _tmpCustomStart: Long?
          if (_stmt.isNull(_columnIndexOfCustomStart)) {
            _tmpCustomStart = null
          } else {
            _tmpCustomStart = _stmt.getLong(_columnIndexOfCustomStart)
          }
          val _tmpCustomLengthDays: Int?
          if (_stmt.isNull(_columnIndexOfCustomLengthDays)) {
            _tmpCustomLengthDays = null
          } else {
            _tmpCustomLengthDays = _stmt.getLong(_columnIndexOfCustomLengthDays).toInt()
          }
          val _tmpCategoryIds: List<Long>
          val _tmp_1: String
          _tmp_1 = _stmt.getText(_columnIndexOfCategoryIds)
          _tmpCategoryIds = __converters.stringToLongList(_tmp_1)
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          _result =
              BudgetEntity(_tmpId,_tmpName,_tmpAmountMinor,_tmpPeriodType,_tmpCustomStart,_tmpCustomLengthDays,_tmpCategoryIds,_tmpCreatedAt)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun snapshot(): List<BudgetEntity> {
    val _sql: String = "SELECT * FROM budgets"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfAmountMinor: Int = getColumnIndexOrThrow(_stmt, "amountMinor")
        val _columnIndexOfPeriodType: Int = getColumnIndexOrThrow(_stmt, "periodType")
        val _columnIndexOfCustomStart: Int = getColumnIndexOrThrow(_stmt, "customStart")
        val _columnIndexOfCustomLengthDays: Int = getColumnIndexOrThrow(_stmt, "customLengthDays")
        val _columnIndexOfCategoryIds: Int = getColumnIndexOrThrow(_stmt, "categoryIds")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _result: MutableList<BudgetEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: BudgetEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpAmountMinor: Long
          _tmpAmountMinor = _stmt.getLong(_columnIndexOfAmountMinor)
          val _tmpPeriodType: BudgetPeriodType
          val _tmp: String
          _tmp = _stmt.getText(_columnIndexOfPeriodType)
          _tmpPeriodType = __converters.stringToBudgetPeriodType(_tmp)
          val _tmpCustomStart: Long?
          if (_stmt.isNull(_columnIndexOfCustomStart)) {
            _tmpCustomStart = null
          } else {
            _tmpCustomStart = _stmt.getLong(_columnIndexOfCustomStart)
          }
          val _tmpCustomLengthDays: Int?
          if (_stmt.isNull(_columnIndexOfCustomLengthDays)) {
            _tmpCustomLengthDays = null
          } else {
            _tmpCustomLengthDays = _stmt.getLong(_columnIndexOfCustomLengthDays).toInt()
          }
          val _tmpCategoryIds: List<Long>
          val _tmp_1: String
          _tmp_1 = _stmt.getText(_columnIndexOfCategoryIds)
          _tmpCategoryIds = __converters.stringToLongList(_tmp_1)
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          _item =
              BudgetEntity(_tmpId,_tmpName,_tmpAmountMinor,_tmpPeriodType,_tmpCustomStart,_tmpCustomLengthDays,_tmpCategoryIds,_tmpCreatedAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteAll() {
    val _sql: String = "DELETE FROM budgets"
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
