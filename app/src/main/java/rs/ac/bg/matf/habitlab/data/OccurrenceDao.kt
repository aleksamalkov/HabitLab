package rs.ac.bg.matf.habitlab.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import java.time.LocalDate

@Dao
interface OccurrenceDao {
    @Query(
        "SELECT * " +
        "FROM occurrence " +
        "WHERE habitId = :habitId AND date BETWEEN :from AND :until " +
        "ORDER BY date ")
    suspend fun getForHabit(habitId: Int, from: LocalDate, until: LocalDate): List<Occurrence>

    @Upsert
    suspend fun upsert(occurrence: Occurrence)

    @Delete
    suspend fun delete(occurrence: Occurrence)
}