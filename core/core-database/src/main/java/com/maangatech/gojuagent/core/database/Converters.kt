package com.maangatech.gojuagent.core.database

import androidx.room.TypeConverter
import com.maangatech.gojuagent.core.database.entity.SyncEntityType
import com.maangatech.gojuagent.core.database.entity.TransactionStatus
import com.maangatech.gojuagent.core.database.entity.TransactionSyncStatus

class Converters {
    @TypeConverter
    fun toTransactionStatus(value: String) = enumValueOf<TransactionStatus>(value)

    @TypeConverter
    fun fromTransactionStatus(value: TransactionStatus) = value.name

    @TypeConverter
    fun toSyncStatus(value: String) = enumValueOf<TransactionSyncStatus>(value)

    @TypeConverter
    fun fromSyncStatus(value: TransactionSyncStatus) = value.name

    @TypeConverter
    fun toSyncEntityType(value: String) = enumValueOf<SyncEntityType>(value)

    @TypeConverter
    fun fromSyncEntityType(value: SyncEntityType) = value.name
}
