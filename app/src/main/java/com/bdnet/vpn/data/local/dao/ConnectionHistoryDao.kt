package com.bdnet.vpn.data.local.dao

import androidx.room.*
import com.bdnet.vpn.data.local.entity.ConnectionHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ConnectionHistoryDao {

    @Query("SELECT * FROM connection_history ORDER BY start_time DESC")
    fun getAllHistory(): Flow<List<ConnectionHistoryEntity>>

    @Query("SELECT * FROM connection_history WHERE server_id = :serverId ORDER BY start_time DESC")
    fun getHistoryByServer(serverId: Long): Flow<List<ConnectionHistoryEntity>>

    @Query("SELECT * FROM connection_history WHERE id = :id")
    suspend fun getHistoryById(id: Long): ConnectionHistoryEntity?

    @Query("SELECT * FROM connection_history ORDER BY start_time DESC LIMIT :limit")
    fun getRecentHistory(limit: Int = 50): Flow<List<ConnectionHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: ConnectionHistoryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistoryList(history: List<ConnectionHistoryEntity>)

    @Update
    suspend fun updateHistory(history: ConnectionHistoryEntity)

    @Delete
    suspend fun deleteHistory(history: ConnectionHistoryEntity)

    @Query("DELETE FROM connection_history WHERE server_id = :serverId")
    suspend fun deleteHistoryByServer(serverId: Long)

    @Query("DELETE FROM connection_history WHERE start_time < :timestamp")
    suspend fun deleteOldHistory(timestamp: Long)

    @Query("DELETE FROM connection_history")
    suspend fun deleteAllHistory()

    @Query("SELECT SUM(bytes_in) FROM connection_history")
    suspend fun getTotalBytesIn(): Long?

    @Query("SELECT SUM(bytes_out) FROM connection_history")
    suspend fun getTotalBytesOut(): Long?

    @Query("SELECT SUM(duration) FROM connection_history")
    suspend fun getTotalDuration(): Long?

    @Query("SELECT COUNT(*) FROM connection_history")
    fun getHistoryCount(): Flow<Int>
}
