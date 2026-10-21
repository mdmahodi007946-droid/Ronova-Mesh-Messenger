package com.example.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        DeviceEntity::class,
        ConversationEntity::class,
        MessageEntity::class,
        RelayQueueEntity::class,
        PeerEntity::class,
        CallHistoryEntity::class,
        SecurityEventEntity::class,
        ApprovedContactEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun deviceDao(): DeviceDao
    abstract fun conversationDao(): ConversationDao
    abstract fun messageDao(): MessageDao
    abstract fun relayQueueDao(): RelayQueueDao
    abstract fun peerDao(): PeerDao
    abstract fun callHistoryDao(): CallHistoryDao
    abstract fun securityEventDao(): SecurityEventDao
    abstract fun approvedContactDao(): ApprovedContactDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `approved_contacts` (
                        `contactId` TEXT NOT NULL PRIMARY KEY,
                        `nickname` TEXT NOT NULL,
                        `authorizedAt` INTEGER NOT NULL,
                        `authorizationCredential` TEXT NOT NULL
                    )
                """.trimIndent())
                try {
                    db.execSQL("ALTER TABLE `messages` ADD COLUMN `transportType` TEXT NOT NULL DEFAULT 'BLUETOOTH'")
                } catch (_: Exception) {}
                try {
                    db.execSQL("ALTER TABLE `call_history` ADD COLUMN `transportType` TEXT NOT NULL DEFAULT 'BLUETOOTH'")
                } catch (_: Exception) {}
                try {
                    db.execSQL("ALTER TABLE `conversations` ADD COLUMN `lastTransportType` TEXT NOT NULL DEFAULT 'BLUETOOTH'")
                } catch (_: Exception) {}
                try {
                    db.execSQL("ALTER TABLE `devices` ADD COLUMN `isProfileLocked` INTEGER NOT NULL DEFAULT 0")
                } catch (_: Exception) {}
                try {
                    db.execSQL("ALTER TABLE `devices` ADD COLUMN `isInternetReachable` INTEGER NOT NULL DEFAULT 0")
                } catch (_: Exception) {}
                try {
                    db.execSQL("ALTER TABLE `peers` ADD COLUMN `isDirectBluetooth` INTEGER NOT NULL DEFAULT 1")
                } catch (_: Exception) {}
                try {
                    db.execSQL("ALTER TABLE `peers` ADD COLUMN `isProfileLocked` INTEGER NOT NULL DEFAULT 0")
                } catch (_: Exception) {}
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "ronova_mesh.db"
                )
                    .addMigrations(MIGRATION_2_3)
                    .fallbackToDestructiveMigration(false)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
