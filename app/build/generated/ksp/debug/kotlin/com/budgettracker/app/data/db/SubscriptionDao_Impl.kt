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
public class SubscriptionDao_Impl(
  __db: RoomDatabase,
) : SubscriptionDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfSubscriptionEntity: EntityInsertAdapter<SubscriptionEntity>

  private val __converters: Converters = Converters()

  private val __deleteAdapterOfSubscriptionEntity: EntityDeleteOrUpdateAdapter<SubscriptionEntity>

  private val __updateAdapterOfSubscriptionEntity: EntityDeleteOrUpdateAdapter<SubscriptionEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfSubscriptionEntity = object : EntityInsertAdapter<SubscriptionEntity>() {
      protected override fun createQuery(): String =
          "INSERT OR ABORT INTO `subscriptions` (`id`,`name`,`emoji`,`amountMinor`,`accountId`,`categoryId`,`cycle`,`nextDue`,`isActive`,`note`,`createdAt`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: SubscriptionEntity) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.name)
        statement.bindText(3, entity.emoji)
        statement.bindLong(4, entity.amountMinor)
        statement.bindLong(5, entity.accountId)
        val _tmpCategoryId: Long? = entity.categoryId
        if (_tmpCategoryId == null) {
          statement.bindNull(6)
        } else {
          statement.bindLong(6, _tmpCategoryId)
        }
        val _tmp: String = __converters.cycleToString(entity.cycle)
        statement.bindText(7, _tmp)
        statement.bindLong(8, entity.nextDue)
        val _tmp_1: Int = if (entity.isActive) 1 else 0
        statement.bindLong(9, _tmp_1.toLong())
        statement.bindText(10, entity.note)
        statement.bindLong(11, entity.createdAt)
      }
    }
    this.__deleteAdapterOfSubscriptionEntity = object :
        EntityDeleteOrUpdateAdapter<SubscriptionEntity>() {
      protected override fun createQuery(): String = "DELETE FROM `subscriptions` WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: SubscriptionEntity) {
        statement.bindLong(1, entity.id)
      }
    }
    this.__updateAdapterOfSubscriptionEntity = object :
        EntityDeleteOrUpdateAdapter<SubscriptionEntity>() {
      protected override fun createQuery(): String =
          "UPDATE OR ABORT `subscriptions` SET `id` = ?,`name` = ?,`emoji` = ?,`amountMinor` = ?,`accountId` = ?,`categoryId` = ?,`cycle` = ?,`nextDue` = ?,`isActive` = ?,`note` = ?,`createdAt` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: SubscriptionEntity) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.name)
        statement.bindText(3, entity.emoji)
        statement.bindLong(4, entity.amountMinor)
        statement.bindLong(5, entity.accountId)
        val _tmpCategoryId: Long? = entity.categoryId
        if (_tmpCategoryId == null) {
          statement.bindNull(6)
        } else {
          statement.bindLong(6, _tmpCategoryId)
        }
        val _tmp: String = __converters.cycleToString(entity.cycle)
        statement.bindText(7, _tmp)
        statement.bindLong(8, entity.nextDue)
        val _tmp_1: Int = if (entity.isActive) 1 else 0
        statement.bindLong(9, _tmp_1.toLong())
        statement.bindText(10, entity.note)
        statement.bindLong(11, entity.createdAt)
        statement.bindLong(12, entity.id)
      }
    }
  }

  public override suspend fun insert(sub: SubscriptionEntity): Long = performSuspending(__db, false,
      true) { _connection ->
    val _result: Long = __insertAdapterOfSubscriptionEntity.insertAndReturnId(_connection, sub)
    _result
  }

  public override suspend fun insertAll(subs: List<SubscriptionEntity>): Unit =
      performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfSubscriptionEntity.insert(_connection, subs)
  }

  public override suspend fun delete(sub: SubscriptionEntity): Unit = performSuspending(__db, false,
      true) { _connection ->
    __deleteAdapterOfSubscriptionEntity.handle(_connection, sub)
  }

  public override suspend fun update(sub: SubscriptionEntity): Unit = performSuspending(__db, false,
      true) { _connection ->
    __updateAdapterOfSubscriptionEntity.handle(_connection, sub)
  }

  public override fun observeAll(): Flow<List<SubscriptionEntity>> {
    val _sql: String = "SELECT * FROM subscriptions ORDER BY isActive DESC, nextDue, id"
    return createFlow(__db, false, arrayOf("subscriptions")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfEmoji: Int = getColumnIndexOrThrow(_stmt, "emoji")
        val _columnIndexOfAmountMinor: Int = getColumnIndexOrThrow(_stmt, "amountMinor")
        val _columnIndexOfAccountId: Int = getColumnIndexOrThrow(_stmt, "accountId")
        val _columnIndexOfCategoryId: Int = getColumnIndexOrThrow(_stmt, "categoryId")
        val _columnIndexOfCycle: Int = getColumnIndexOrThrow(_stmt, "cycle")
        val _columnIndexOfNextDue: Int = getColumnIndexOrThrow(_stmt, "nextDue")
        val _columnIndexOfIsActive: Int = getColumnIndexOrThrow(_stmt, "isActive")
        val _columnIndexOfNote: Int = getColumnIndexOrThrow(_stmt, "note")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _result: MutableList<SubscriptionEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: SubscriptionEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpEmoji: String
          _tmpEmoji = _stmt.getText(_columnIndexOfEmoji)
          val _tmpAmountMinor: Long
          _tmpAmountMinor = _stmt.getLong(_columnIndexOfAmountMinor)
          val _tmpAccountId: Long
          _tmpAccountId = _stmt.getLong(_columnIndexOfAccountId)
          val _tmpCategoryId: Long?
          if (_stmt.isNull(_columnIndexOfCategoryId)) {
            _tmpCategoryId = null
          } else {
            _tmpCategoryId = _stmt.getLong(_columnIndexOfCategoryId)
          }
          val _tmpCycle: Cycle
          val _tmp: String
          _tmp = _stmt.getText(_columnIndexOfCycle)
          _tmpCycle = __converters.stringToCycle(_tmp)
          val _tmpNextDue: Long
          _tmpNextDue = _stmt.getLong(_columnIndexOfNextDue)
          val _tmpIsActive: Boolean
          val _tmp_1: Int
          _tmp_1 = _stmt.getLong(_columnIndexOfIsActive).toInt()
          _tmpIsActive = _tmp_1 != 0
          val _tmpNote: String
          _tmpNote = _stmt.getText(_columnIndexOfNote)
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          _item =
              SubscriptionEntity(_tmpId,_tmpName,_tmpEmoji,_tmpAmountMinor,_tmpAccountId,_tmpCategoryId,_tmpCycle,_tmpNextDue,_tmpIsActive,_tmpNote,_tmpCreatedAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun byId(id: Long): SubscriptionEntity? {
    val _sql: String = "SELECT * FROM subscriptions WHERE id = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, id)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfEmoji: Int = getColumnIndexOrThrow(_stmt, "emoji")
        val _columnIndexOfAmountMinor: Int = getColumnIndexOrThrow(_stmt, "amountMinor")
        val _columnIndexOfAccountId: Int = getColumnIndexOrThrow(_stmt, "accountId")
        val _columnIndexOfCategoryId: Int = getColumnIndexOrThrow(_stmt, "categoryId")
        val _columnIndexOfCycle: Int = getColumnIndexOrThrow(_stmt, "cycle")
        val _columnIndexOfNextDue: Int = getColumnIndexOrThrow(_stmt, "nextDue")
        val _columnIndexOfIsActive: Int = getColumnIndexOrThrow(_stmt, "isActive")
        val _columnIndexOfNote: Int = getColumnIndexOrThrow(_stmt, "note")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _result: SubscriptionEntity?
        if (_stmt.step()) {
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpEmoji: String
          _tmpEmoji = _stmt.getText(_columnIndexOfEmoji)
          val _tmpAmountMinor: Long
          _tmpAmountMinor = _stmt.getLong(_columnIndexOfAmountMinor)
          val _tmpAccountId: Long
          _tmpAccountId = _stmt.getLong(_columnIndexOfAccountId)
          val _tmpCategoryId: Long?
          if (_stmt.isNull(_columnIndexOfCategoryId)) {
            _tmpCategoryId = null
          } else {
            _tmpCategoryId = _stmt.getLong(_columnIndexOfCategoryId)
          }
          val _tmpCycle: Cycle
          val _tmp: String
          _tmp = _stmt.getText(_columnIndexOfCycle)
          _tmpCycle = __converters.stringToCycle(_tmp)
          val _tmpNextDue: Long
          _tmpNextDue = _stmt.getLong(_columnIndexOfNextDue)
          val _tmpIsActive: Boolean
          val _tmp_1: Int
          _tmp_1 = _stmt.getLong(_columnIndexOfIsActive).toInt()
          _tmpIsActive = _tmp_1 != 0
          val _tmpNote: String
          _tmpNote = _stmt.getText(_columnIndexOfNote)
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          _result =
              SubscriptionEntity(_tmpId,_tmpName,_tmpEmoji,_tmpAmountMinor,_tmpAccountId,_tmpCategoryId,_tmpCycle,_tmpNextDue,_tmpIsActive,_tmpNote,_tmpCreatedAt)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun snapshot(): List<SubscriptionEntity> {
    val _sql: String = "SELECT * FROM subscriptions"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfEmoji: Int = getColumnIndexOrThrow(_stmt, "emoji")
        val _columnIndexOfAmountMinor: Int = getColumnIndexOrThrow(_stmt, "amountMinor")
        val _columnIndexOfAccountId: Int = getColumnIndexOrThrow(_stmt, "accountId")
        val _columnIndexOfCategoryId: Int = getColumnIndexOrThrow(_stmt, "categoryId")
        val _columnIndexOfCycle: Int = getColumnIndexOrThrow(_stmt, "cycle")
        val _columnIndexOfNextDue: Int = getColumnIndexOrThrow(_stmt, "nextDue")
        val _columnIndexOfIsActive: Int = getColumnIndexOrThrow(_stmt, "isActive")
        val _columnIndexOfNote: Int = getColumnIndexOrThrow(_stmt, "note")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _result: MutableList<SubscriptionEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: SubscriptionEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpEmoji: String
          _tmpEmoji = _stmt.getText(_columnIndexOfEmoji)
          val _tmpAmountMinor: Long
          _tmpAmountMinor = _stmt.getLong(_columnIndexOfAmountMinor)
          val _tmpAccountId: Long
          _tmpAccountId = _stmt.getLong(_columnIndexOfAccountId)
          val _tmpCategoryId: Long?
          if (_stmt.isNull(_columnIndexOfCategoryId)) {
            _tmpCategoryId = null
          } else {
            _tmpCategoryId = _stmt.getLong(_columnIndexOfCategoryId)
          }
          val _tmpCycle: Cycle
          val _tmp: String
          _tmp = _stmt.getText(_columnIndexOfCycle)
          _tmpCycle = __converters.stringToCycle(_tmp)
          val _tmpNextDue: Long
          _tmpNextDue = _stmt.getLong(_columnIndexOfNextDue)
          val _tmpIsActive: Boolean
          val _tmp_1: Int
          _tmp_1 = _stmt.getLong(_columnIndexOfIsActive).toInt()
          _tmpIsActive = _tmp_1 != 0
          val _tmpNote: String
          _tmpNote = _stmt.getText(_columnIndexOfNote)
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          _item =
              SubscriptionEntity(_tmpId,_tmpName,_tmpEmoji,_tmpAmountMinor,_tmpAccountId,_tmpCategoryId,_tmpCycle,_tmpNextDue,_tmpIsActive,_tmpNote,_tmpCreatedAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteAll() {
    val _sql: String = "DELETE FROM subscriptions"
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
