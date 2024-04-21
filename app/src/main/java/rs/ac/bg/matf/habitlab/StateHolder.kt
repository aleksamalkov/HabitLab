package rs.ac.bg.matf.habitlab

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import rs.ac.bg.matf.habitlab.data.DataRepository
import rs.ac.bg.matf.habitlab.data.Habit
import rs.ac.bg.matf.habitlab.data.HabitDao

class StateHolder (dao: HabitDao) : ViewModel() {

    private val dataRepository = DataRepository(dao)
    // lista taskova
    val habits = mutableStateListOf<Habit>()
    // string koji upisujemo u polje
    val textFieldString = mutableStateOf<String>("")

    init {
        viewModelScope.launch {
            refresh()
        }
    }

    private suspend fun refresh() {
        val newHabits = dataRepository.allHabits()
        habits.clear()
        habits.addAll(newHabits)
    }

    fun addBinaryTask() {
        val taskName = textFieldString.value
        if (taskName.isNotBlank()) {
            textFieldString.value = ""
            viewModelScope.launch {
                dataRepository.addBinaryHabit(taskName)
                refresh()
            }
        }
    }
}