package rs.ac.bg.matf.habitlab.data

class DataRepository (private val habitDao: HabitDao) {
    suspend fun allHabits(): List<Habit> = habitDao.getAll()

    suspend fun addBinaryHabit(name: String) = habitDao.insert(Habit(name,false, null))
    suspend fun removeHabit(habit: Habit) = habitDao.delete(habit)
}
