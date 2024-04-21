package rs.ac.bg.matf.habitlab.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface HabitDao {
    @Query("SELECT * FROM habit ORDER BY id")
    suspend fun getAll(): List<Habit>

    @Insert
    suspend fun insert(habit: Habit)

    @Delete
    suspend fun delete(habit: Habit)
}