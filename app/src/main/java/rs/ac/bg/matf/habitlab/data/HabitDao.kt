package rs.ac.bg.matf.habitlab.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface HabitDao {
    @Query("SELECT * FROM habit ORDER BY id")
    suspend fun getAll(): List<Habit>

    @Insert
    suspend fun insert(habit: Habit)

    @Delete
    fun deleteHabit(habit: Habit)

    @Query(
        "DELETE " +
        "FROM occurrence " +
        "WHERE habitId = :id "
    )
    fun deleteOccurrences(id: Int)

    @Transaction
    suspend fun delete(habit: Habit) {
        deleteOccurrences(habit.id)
        deleteHabit(habit)
    }
}