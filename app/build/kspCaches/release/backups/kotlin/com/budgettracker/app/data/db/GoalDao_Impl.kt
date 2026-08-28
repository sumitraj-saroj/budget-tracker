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
public class GoalDao_Impl(
  __db: RoomDatabase,
) : GoalDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfGoalEntity: EntityInsertAdapter<GoalEntity>

  private val __deleteAdapterOfGoalEntity: EntityDeleteOrUpdateAdapter<GoalEntity>

  private val __updateAdapterOfGoalEntity: EntityDeleteOrUpdateAdapter<GoalEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfGoalEntity = object : EntityInsertAdapter<GoalEntity>() {
      protected override fun createQuery(): String =
          "INSERT OR ABORT INTO `goals` (`id`,`name`,`emoji`,`colorArgb`,`targetMinor`,`savedMinor`,`targetDate`,`note`,`createdAt`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: GoalEntity) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.name)
        statement.bindText(3, entity.emoji)
        statement.bindLong(4, entity.colorArgb)
        statement.bindLong(5, entity.targetMinor)
        statement.bindLong(6, entity.savedMinor)
        val _tmpTargetDate: Long? = entity.targetDate
        if (_tmpTargetDate == null) {
          statement.bindNull(7)
        } else {
          statement.bindLong(7, _tmpTargetDate)
        }
        statement.bindText(8, entity.note)
        statement.bindLong(9, entity.createdAt)
      }
    }
    this.__deleteAdapterOfGoalEntity = object : EntityDeleteOrUpdateAdapter<GoalEntity>() {
      protected override fun createQuery(): String = "DELETE FROM `goals` WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: GoalEntity) {
        statement.bindLong(1, entity.id)
      }
    }
    this.__updateAdapterOfGoalEntity = object : EntityDeleteOrUpdateAdapter<GoalEntity>() {
      protected override fun createQuery(): String =
          "UPDATE OR ABORT `goals` SET `id` = ?,`name` = ?,`emoji` = ?,`colorArgb` = ?,`targetMinor` = ?,`savedMinor` = ?,`targetDate` = ?,`note` = ?,`createdAt` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: GoalEntity) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.name)
        statement.bindText(3, entity.emoji)
        statement.bindLong(4, entity.colorArgb)
        statement.bindLong(5, entity.targetMinor)
        statement.bindLong(6, entity.savedMinor)
        val _tmpTargetDate: Long? = entity.targetDate
        if (_tmpTargetDate == null) {
          statement.bindNull(7)
        } else {
          statement.bindLong(7, _tmpTargetDate)
        }
        statement.bindText(8, entity.note)
        statement.bindLong(9, entity.createdAt)
        statement.bindLong(10, entity.id)
      }
    }
  }

  public override suspend fun insert(goal: GoalEntity): Long = performSuspending(__db, false, true)
      { _connection ->
    val _result: Long = __insertAdapterOfGoalEntity.insertAndReturnId(_connection, goal)
    _result
  }

  public override suspend fun insertAll(goals: List<GoalEntity>): Unit = performSuspending(__db,
      false, true) { _connection ->
    __insertAdapterOfGoalEntity.insert(_connection, goals)
  }

  public override suspend fun delete(goal: GoalEntity): Unit = performSuspending(__db, false, true)
      { _connection ->
    __deleteAdapterOfGoalEntity.handle(_connection, goal)
  }

  public override suspend fun update(goal: GoalEntity): Unit = performSuspending(__db, false, true)
      { _connection ->
    __updateAdapterOfGoalEntity.handle(_connection, goal)
  }

  public override fun observeAll(): Flow<List<GoalEntity>> {
    val _sql: String = "SELECT * FROM goals ORDER BY id"
    return createFlow(__db, false, arrayOf("goals")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfEmoji: Int = getColumnIndexOrThrow(_stmt, "emoji")
        val _columnIndexOfColorArgb: Int = getColumnIndexOrThrow(_stmt, "colorArgb")
        val _columnIndexOfTargetMinor: Int = getColumnIndexOrThrow(_stmt, "targetMinor")
        val _columnIndexOfSavedMinor: Int = getColumnIndexOrThrow(_stmt, "savedMinor")
        val _columnIndexOfTargetDate: Int = getColumnIndexOrThrow(_stmt, "targetDate")
        val _columnIndexOfNote: Int = getColumnIndexOrThrow(_stmt, "note")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _result: MutableList<GoalEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: GoalEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpEmoji: String
          _tmpEmoji = _stmt.getText(_columnIndexOfEmoji)
          val _tmpColorArgb: Long
          _tmpColorArgb = _stmt.getLong(_columnIndexOfColorArgb)
          val _tmpTargetMinor: Long
          _tmpTargetMinor = _stmt.getLong(_columnIndexOfTargetMinor)
          val _tmpSavedMinor: Long
          _tmpSavedMinor = _stmt.getLong(_columnIndexOfSavedMinor)
          val _tmpTargetDate: Long?
          if (_stmt.isNull(_columnIndexOfTargetDate)) {
            _tmpTargetDate = null
          } else {
            _tmpTargetDate = _stmt.getLong(_columnIndexOfTargetDate)
          }
          val _tmpNote: String
          _tmpNote = _stmt.getText(_columnIndexOfNote)
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          _item =
              GoalEntity(_tmpId,_tmpName,_tmpEmoji,_tmpColorArgb,_tmpTargetMinor,_tmpSavedMinor,_tmpTargetDate,_tmpNote,_tmpCreatedAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun byId(id: Long): GoalEntity? {
    val _sql: String = "SELECT * FROM goals WHERE id = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, id)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfEmoji: Int = getColumnIndexOrThrow(_stmt, "emoji")
        val _columnIndexOfColorArgb: Int = getColumnIndexOrThrow(_stmt, "colorArgb")
        val _columnIndexOfTargetMinor: Int = getColumnIndexOrThrow(_stmt, "targetMinor")
        val _columnIndexOfSavedMinor: Int = getColumnIndexOrThrow(_stmt, "savedMinor")
        val _columnIndexOfTargetDate: Int = getColumnIndexOrThrow(_stmt, "targetDate")
        val _columnIndexOfNote: Int = getColumnIndexOrThrow(_stmt, "note")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _result: GoalEntity?
        if (_stmt.step()) {
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpEmoji: String
          _tmpEmoji = _stmt.getText(_columnIndexOfEmoji)
          val _tmpColorArgb: Long
          _tmpColorArgb = _stmt.getLong(_columnIndexOfColorArgb)
          val _tmpTargetMinor: Long
          _tmpTargetMinor = _stmt.getLong(_columnIndexOfTargetMinor)
          val _tmpSavedMinor: Long
          _tmpSavedMinor = _stmt.getLong(_columnIndexOfSavedMinor)
          val _tmpTargetDate: Long?
          if (_stmt.isNull(_columnIndexOfTargetDate)) {
            _tmpTargetDate = null
          } else {
            _tmpTargetDate = _stmt.getLong(_columnIndexOfTargetDate)
          }
          val _tmpNote: String
          _tmpNote = _stmt.getText(_columnIndexOfNote)
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          _result =
              GoalEntity(_tmpId,_tmpName,_tmpEmoji,_tmpColorArgb,_tmpTargetMinor,_tmpSavedMinor,_tmpTargetDate,_tmpNote,_tmpCreatedAt)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun snapshot(): List<GoalEntity> {
    val _sql: String = "SELECT * FROM goals"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfEmoji: Int = getColumnIndexOrThrow(_stmt, "emoji")
        val _columnIndexOfColorArgb: Int = getColumnIndexOrThrow(_stmt, "colorArgb")
        val _columnIndexOfTargetMinor: Int = getColumnIndexOrThrow(_stmt, "targetMinor")
        val _columnIndexOfSavedMinor: Int = getColumnIndexOrThrow(_stmt, "savedMinor")
        val _columnIndexOfTargetDate: Int = getColumnIndexOrThrow(_stmt, "targetDate")
        val _columnIndexOfNote: Int = getColumnIndexOrThrow(_stmt, "note")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _result: MutableList<GoalEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: GoalEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpEmoji: String
          _tmpEmoji = _stmt.getText(_columnIndexOfEmoji)
          val _tmpColorArgb: Long
          _tmpColorArgb = _stmt.getLong(_columnIndexOfColorArgb)
          val _tmpTargetMinor: Long
          _tmpTargetMinor = _stmt.getLong(_columnIndexOfTargetMinor)
          val _tmpSavedMinor: Long
          _tmpSavedMinor = _stmt.getLong(_columnIndexOfSavedMinor)
          val _tmpTargetDate: Long?
          if (_stmt.isNull(_columnIndexOfTargetDate)) {
            _tmpTargetDate = null
          } else {
            _tmpTargetDate = _stmt.getLong(_columnIndexOfTargetDate)
          }
          val _tmpNote: String
          _tmpNote = _stmt.getText(_columnIndexOfNote)
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          _item =
              GoalEntity(_tmpId,_tmpName,_tmpEmoji,_tmpColorArgb,_tmpTargetMinor,_tmpSavedMinor,_tmpTargetDate,_tmpNote,_tmpCreatedAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteAll() {
    val _sql: String = "DELETE FROM goals"
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
