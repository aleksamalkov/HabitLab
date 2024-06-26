package rs.ac.bg.matf.habitlab

import androidx.compose.material3.SnackbarHostState
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


class MainViewModel (private val dataRepository: DataRepository) : ViewModel() {
    // lista taskova
    val habits = mutableStateListOf<Habit>()
    // stanje checkboxova
    val checkedState = mutableStateMapOf<Habit, SnapshotStateList<Boolean>>()
    // stanje textfieldova za numericke taskove
    val numbersState = mutableStateMapOf<Habit, SnapshotStateList<Int>>()
    // string koji upisujemo u polje
    val textFieldString = mutableStateOf("")
    // string koji upisujemo u polje za goal
    val goalString = mutableStateOf("")
    // danasnji dan
    val today: LocalDate = LocalDate.now()

    val numberField = mutableStateOf("")

    val showHabitDialog = mutableStateOf(false)
    val snackbarHostState = SnackbarHostState()

    private val mutex = Mutex()

    init {
        refreshView()
    }

    // dodavanje navike u bazu i osvezavanje interfejsa
    fun addHabit(isNumeric: Boolean) {
        val taskName = textFieldString.value
        if (taskName.isBlank()) {
            viewModelScope.launch {
                snackbarHostState.showSnackbar("Invalid habit name")
            }
            textFieldString.value = ""
            goalString.value = ""
            return
        }

        var goal = 0
        val pattern = Regex("[0-9]+")
        if (isNumeric) {
            if (goalString.value.trim().matches(pattern) && goalString.value.trim().toInt() > 0) {
                goal = goalString.value.trim().toInt()
            } else {
                viewModelScope.launch {
                    snackbarHostState.showSnackbar("Invalid goal")
                }
                textFieldString.value = ""
                goalString.value = ""
                return
            }
        }
        viewModelScope.launch {
            if (isNumeric) {
                dataRepository.addNumericHabit(taskName, goal)
            } else {
                dataRepository.addBinaryHabit(taskName)
            }
            textFieldString.value = ""
            goalString.value = ""
            refresh()
        }
    }

    // akcija koja se desava kad uradimo ili "oduradimo" binarni zadatak
    fun onCheckedChange(habit: Habit, i: Int, value: Boolean) {
        val date = today.minusDays((6 - i).toLong())
        viewModelScope.launch {
            dataRepository.updateBinary(habit, date, value)
            refreshBinary(habit)
        }
    }

    fun changeNumber(habit: Habit, i: Int){
        val date = today.minusDays((6 - i).toLong())
        val pattern = Regex("[0-9]+\\s*")
        viewModelScope.launch {
            if(numberField.value == "" || !numberField.value.matches(pattern)){
                snackbarHostState.showSnackbar("Invalid number")
            }
            else{
                dataRepository.updateNumeric(habit, date, numberField.value.trim().toInt())
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
