package com.bdnet.vpn.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.bdnet.vpn.data.local.dao.ConnectionHistoryDao
import com.bdnet.vpn.data.local.dao.ServerDao
import com.bdnet.vpn.data.local.dao.TweakDao
import com.bdnet.vpn.data.local.entity.ConnectionHistoryEntity
import com.bdnet.vpn.data.local.entity.ServerEntity
import com.bdnet.vpn.data.local.entity.TweakEntity

@Database(
    entities = [
        ServerEntity::class,
        TweakEntity::class,
        ConnectionHistoryEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun serverDao(): ServerDao
    abstract fun tweakDao(): TweakDao
    abstract fun connectionHistoryDao(): ConnectionHistoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "bdnet_vpn_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
