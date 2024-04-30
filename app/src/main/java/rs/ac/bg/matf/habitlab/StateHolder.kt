package rs.ac.bg.matf.habitlab

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import rs.ac.bg.matf.habitlab.data.DataRepository
import rs.ac.bg.matf.habitlab.data.Habit
import java.time.LocalDate


class StateHolder (private val dataRepository: DataRepository) : ViewModel() {
    // lista taskova
    val habits = mutableStateListOf<Habit>()
    // stanje checkboxova TODO dodati slicno za numericke
    val checkedState = mutableStateMapOf<Habit, SnapshotStateList<Boolean>>()
    // stanje textfieldova za numericke taskove
    val numbersState = mutableStateMapOf<Habit, SnapshotStateList<Int>>()
    // string koji upisujemo u polje
    val textFieldString = mutableStateOf("")
    // string koji upisujemo u polje za goal
    val goalString = mutableStateOf("")
    // danasnji dan
    val today: LocalDate = LocalDate.now()

    val showHabitDialog = mutableStateOf(false)

    private val mutex = Mutex()

    init {
        refreshView()
    }

    // dodavanje navike u bazu i osvezavanje interfejsa
    fun addHabit(isNumeric: Boolean) {
        val taskName = textFieldString.value
        var goal = 0
        val pattern = Regex("[0-9]+")
        if (isNumeric && goalString.value.trim().matches(pattern))
            goal = goalString.value.trim().toInt()
        if (taskName.isNotBlank()) {
            textFieldString.value = ""
            goalString.value = ""
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

    fun onValueChange(habit: Habit, i: Int, value: String){
        val date = today.minusDays((6 - i).toLong())
        val pattern = Regex("[0-9]+\\s*")
        viewModelScope.launch {
            if(value == "" || !value.matches(pattern)){
                dataRepository.updateNumeric(habit, date, 0)
            }
            else{
                dataRepository.updateNumeric(habit, date, value.trim().toInt())
            }
            refreshNumeric(habit)
        }
    }

    fun refreshView() {
        viewModelScope.launch {
            refresh()
        }
    }

    // osvezavanje liste navika
    // poziva se kada se otvra activity i kada se dodaje/uklanja navika
    // TODO dodati osvezavanje numerickih navika kada se implementira
    private suspend fun refresh() {
        val newHabits = dataRepository.allHabits()
        for (habit in newHabits) {
            if (habit.isNumeric)
                refreshNumeric(habit)
            else
                refreshBinary(habit)
        }
        mutex.withLock {
            habits.clear()
            habits.addAll(newHabits)
        }
    }

    // osvezavanje interfejsa kada se uradi neki binarni zadatak za binarnu naviku 'habit'
    // TODO isto treba napraviti i za numericke
    private suspend fun refreshBinary(habit: Habit) {
        val execs = mutableStateListOf<Boolean>()
        val newExecs = dataRepository.getBinaryExecutions(habit, today.minusDays(6), today)
        execs.addAll(newExecs)
        checkedState[habit] = execs
    }

    private suspend fun refreshNumeric(habit: Habit){
        val execs = mutableStateListOf<Int>()
        val newExecs = dataRepository.getNumericExecutions(habit, today.minusDays(6), today)
        execs.addAll(newExecs)
        numbersState[habit] = execs
    }
}
