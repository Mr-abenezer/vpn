package com.bdnet.vpn.data.local.dao

import androidx.room.*
import com.bdnet.vpn.data.local.entity.TweakEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TweakDao {

    @Query("SELECT * FROM tweaks ORDER BY created_at DESC")
    fun getAllTweaks(): Flow<List<TweakEntity>>

    @Query("SELECT * FROM tweaks WHERE id = :id")
    suspend fun getTweakById(id: Long): TweakEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTweak(tweak: TweakEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTweaks(tweaks: List<TweakEntity>)

    @Update
    suspend fun updateTweak(tweak: TweakEntity)

    @Delete
    suspend fun deleteTweak(tweak: TweakEntity)

    @Query("DELETE FROM tweaks WHERE id = :id")
    suspend fun deleteTweakById(id: Long)

    @Query("DELETE FROM tweaks")
    suspend fun deleteAllTweaks()
}
