package rs.ac.bg.matf.habitlab

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import rs.ac.bg.matf.habitlab.data.DataRepository
import rs.ac.bg.matf.habitlab.data.Habit
import rs.ac.bg.matf.habitlab.data.HabitDao
import rs.ac.bg.matf.habitlab.data.Occurrence
import java.time.LocalDate

class StateHolder (private val dataRepository: DataRepository) : ViewModel() {
    // lista taskova
    val habits = mutableStateListOf<Habit>()
    // stanje checkboxova TODO dodati slicno za numericke
    val checkedState = mutableStateMapOf<Habit, SnapshotStateList<Boolean>>()
    // string koji upisujemo u polje
    val textFieldString = mutableStateOf<String>("")
    // danasnji dan
    val today: LocalDate = LocalDate.now()

    init {
        viewModelScope.launch {
            refresh()
        }
    }

    // dodavanje navike u bazu i osvezavanje interfejsa
    fun addHabit(isNumeric: Boolean, goal: Int = 0) {
        val taskName = textFieldString.value
        if (taskName.isNotBlank()) {
            textFieldString.value = ""
            viewModelScope.launch {
                if (isNumeric) {
                    dataRepository.addNumericHabit(taskName, goal)
                } else {
                    dataRepository.addBinaryHabit(taskName)
                }
                refresh()
            }
        }
    }

    // akcija koja se desava kad uradimo ili "oduradimo" binarni zadatak
    // TODO isto treba napraviti i za numericke
    fun onCheckedChange(habit: Habit, i: Int, value: Boolean) {
        val date = today.minusDays((6 - i).toLong())
        viewModelScope.launch {
            dataRepository.updateBinary(habit, date, value)
            refreshBinary(habit)
        }
    }

    // osvezavanje liste navika
    // poziva se kada se otvra activity i kada se dodaje/uklanja navika
    // TODO dodati osvezavanje numerickih navika kada se implementira
    private suspend fun refresh() {
        val newHabits = dataRepository.allHabits()
        for (habit in newHabits) {
            refreshBinary(habit)
        }
        habits.clear()
        habits.addAll(newHabits)
    }

    // osvezavanje interfejsa kada se uradi neki binarni zadatak za binarnu naviku 'habit'
    // TODO isto treba napraviti i za numericke
    private suspend fun refreshBinary(habit: Habit) {
        val execs = mutableStateListOf<Boolean>()
        val newExecs = dataRepository.getBinaryExecutions(habit, today.minusDays(6), today)
        execs.addAll(newExecs)
        checkedState[habit] = execs
    }
}
