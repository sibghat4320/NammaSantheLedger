package com.example.nammasantheledger.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.nammasantheledger.data.local.entity.DailySummaryEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for cached daily summaries.
 */
@Dao
interface DailySummaryDao {

    @Query("SELECT * FROM daily_summaries ORDER BY date DESC")
    fun getAllSummaries(): Flow<List<DailySummaryEntity>>

    @Query("SELECT * FROM daily_summaries WHERE date = :date")
    suspend fun getSummaryForDate(date: String): DailySummaryEntity?

    @Query("SELECT * FROM daily_summaries WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC")
    fun getSummariesBetween(startDate: String, endDate: String): Flow<List<DailySummaryEntity>>

    @Query("SELECT * FROM daily_summaries ORDER BY date DESC LIMIT :limit")
    fun getRecentSummaries(limit: Int = 30): Flow<List<DailySummaryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSummary(summary: DailySummaryEntity)

    @Query("DELETE FROM daily_summaries WHERE date = :date")
    suspend fun deleteSummaryForDate(date: String)
}
