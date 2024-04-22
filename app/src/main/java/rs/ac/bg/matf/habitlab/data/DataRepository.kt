package rs.ac.bg.matf.habitlab.data

import java.time.Duration
import java.time.LocalDate

class DataRepository (private val habitDao: HabitDao, private val executionDao: OccurrenceDao) {
    suspend fun allHabits(): List<Habit> = habitDao.getAll()

    suspend fun addBinaryHabit(name: String) = habitDao.insert(Habit(name,false, null))

    // nije testirano al treba da bude korisno kad se doda u UI
    suspend fun addNumericHabit(name: String, goal: Int) = habitDao.insert(Habit(name,true, goal))

    suspend fun removeHabit(habit: Habit) = habitDao.delete(habit)

    suspend fun getNumericExecutions(habit: Habit, from: LocalDate, until: LocalDate): List<Int> {
        val executions = executionDao.getForHabit(habit.id, from, until)
        val result = mutableListOf<Int>()
        var date = from
        for (e in executions) {
            while (date.isBefore(e.date)) {
                result.add(0)
                date = date.plusDays(1)
            }
            result.add(e.count ?: 0)
            date = date.plusDays(1)
        }
        assert(result.size.toLong() == Duration.between(from, until).toDays() + 1)
        return result;
    }

    suspend fun getBinaryExecutions(habit: Habit, from: LocalDate, until: LocalDate): List<Boolean> {
        val executions = executionDao.getForHabit(habit.id, from, until)
        val result = mutableListOf<Boolean>()
        var date = from
        for (e in executions) {
            while (date.isBefore(e.date)) {
                result.add(false)
                date = date.plusDays(1)
            }
            result.add(true)
            date = date.plusDays(1)
        }
        assert(result.size.toLong() == Duration.between(from, until).toDays() + 1)
        return result;
    }
}
