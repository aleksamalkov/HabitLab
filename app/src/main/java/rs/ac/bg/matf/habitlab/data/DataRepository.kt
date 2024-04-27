package rs.ac.bg.matf.habitlab.data

import java.time.Duration
import java.time.LocalDate

class DataRepository (private val habitDao: HabitDao, private val occurrenceDao: OccurrenceDao) {
    suspend fun allHabits(): List<Habit> = habitDao.getAll()

    suspend fun addBinaryHabit(name: String) = habitDao.insert(Habit(name,false, null))

    suspend fun addNumericHabit(name: String, goal: Int) = habitDao.insert(Habit(name,true, goal))

    suspend fun removeHabit(habit: Habit) = habitDao.delete(habit)

    // za naviku i vremenski period vraca niz int-ova koji pokazuju kojih dana je izvrsena i koliko
    suspend fun getNumericExecutions(habit: Habit, from: LocalDate, until: LocalDate): List<Int> {
        val executions = occurrenceDao.getForHabit(habit.id, from, until)
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
        while (!date.isAfter(until)) {
            result.add(0)
            date = date.plusDays(1)
        }

        assert(result.size.toLong() == Duration.between(from.atStartOfDay(), until.atStartOfDay()).toDays() + 1)
        return result
    }

    // za naviku i vremenski period vraca niz bool-ova koji pokazuju kojih dana je izvrsena
    suspend fun getBinaryExecutions(habit: Habit, from: LocalDate, until: LocalDate): List<Boolean> {
        val executions = occurrenceDao.getForHabit(habit.id, from, until)
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
        while (!date.isAfter(until)) {
            result.add(false)
            date = date.plusDays(1)
        }

        assert(result.size.toLong() == Duration.between(from.atStartOfDay(), until.atStartOfDay()).toDays() + 1)
        return result
    }

    suspend fun getDoneDates(habit: Habit, from: LocalDate, until: LocalDate): List<LocalDate> {
        return occurrenceDao.getForHabit(habit.id, from, until).map {occurrence -> occurrence.date }
    }

    suspend fun getDoneRatio(habit: Habit, from: LocalDate, until: LocalDate): Float {
        return (occurrenceDao.countOccurrences(habit.id, from, until).toFloat()
                / (Duration.between(from.atStartOfDay(), until.atStartOfDay()).toDays() + 1))
    }

    suspend fun getDoneRatio(habit: Habit, today: LocalDate): Float {
        val date = occurrenceDao.firstDate(habit.id)
        return if (date == null) {
            0f;
        } else {
            (occurrenceDao.countOccurrences(habit.id).toFloat()
                    / (Duration.between(date.atStartOfDay(), today.atStartOfDay()).toDays() + 1))
        }
    }

    // azurira da li je izvrsena odredjena binarna navika odredjenog dana
    suspend fun updateBinary(habit: Habit, date: LocalDate, value: Boolean) {
        val occurrence = Occurrence(habit.id, date, null)
        if (value) {
            occurrenceDao.upsert(occurrence)
        } else {
            occurrenceDao.delete(occurrence)
        }
    }

    // azurira broj izvrsavanja za odredjenu numericku naviku odredjenog dana
    suspend fun updateNumeric(habit: Habit, date: LocalDate, value: Int) {
        val occurrence = Occurrence(habit.id, date, value)
        if (value > 0) {
            occurrenceDao.upsert(occurrence)
        } else {
            occurrenceDao.delete(occurrence)
        }
    }
}
