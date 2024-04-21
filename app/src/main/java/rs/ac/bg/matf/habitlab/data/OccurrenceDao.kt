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
    fun getForHabit(habitId: Int, from: LocalDate, until: LocalDate): List<Occurrence>

    @Query(
        "SELECT * " +
        "FROM occurrence " +
        "WHERE habitId = :habitId " +
        "ORDER BY date ")
    fun getForHabit(habitId: Int): List<Occurrence>

    @Upsert
    fun upsert(occurrence: Occurrence)

    @Delete
    fun delete(occurrence: Occurrence)
}