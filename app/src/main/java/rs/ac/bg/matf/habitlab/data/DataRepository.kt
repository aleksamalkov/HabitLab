package rs.ac.bg.matf.habitlab.data

class DataRepository (private val habitDao: HabitDao) {
    suspend fun allHabits(): List<Habit> = habitDao.getAll()

    suspend fun addBinaryHabit(name: String) = habitDao.insert(Habit(name,false, null))

    // nije testirano al treba da bude korisno kad se doda u UI
    suspend fun addNumericHabit(name: String, goal: Int) = habitDao.insert(Habit(name,true, goal))

    suspend fun removeHabit(habit: Habit) = habitDao.delete(habit)
}
