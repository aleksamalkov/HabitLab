package rs.ac.bg.matf.habitlab.statisticsActivity

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import rs.ac.bg.matf.habitlab.data.DataRepository
import rs.ac.bg.matf.habitlab.data.Habit
import java.time.LocalDate
import kotlin.math.max

class StatisticsViewModel (private val dataRepository: DataRepository, val habit: Habit) : ViewModel() {
    val today: LocalDate = LocalDate.now()
    val pieRatio = mutableFloatStateOf(0.0F)
    val goalRatio = mutableFloatStateOf(0.0F)
    val barData = mutableStateListOf<Int>()
    val startDate = mutableStateOf(today.minusDays(6))
    val endDate = mutableStateOf(today)
    val scorePerDate = mutableStateMapOf<LocalDate, Int>()
    val calendarData = mutableStateMapOf<LocalDate, HeatLevel>()
    val showDeleteDialog = mutableStateOf(false)
    val selectedDate = mutableStateOf<LocalDate?>(null)
    val snackbarHostState = SnackbarHostState()

    private val mutex = Mutex()

    init {
        viewModelScope.launch {
            refresh()
        }
    }

    fun delete() {
        viewModelScope.launch {
            dataRepository.removeHabit(habit)
        }
    }

    private suspend fun refresh() {
        refreshDates()
        refreshCharts()
    }

    private suspend fun refreshDates() {
        val newDates = dataRepository.getScorePerDate(
            habit,
            today.minusYears(1),
            today
        )


        val maxValue = newDates.maxOfOrNull { it.second } ?: 0
        val step = max(maxValue / 4, 1)

        mutex.withLock {
            scorePerDate.clear()
            calendarData.clear()
            if (habit.isNumeric) {
                for (pair in newDates) {
                    val category = (step + pair.second - 1) / step
                    scorePerDate[pair.first] = pair.second
                    calendarData[pair.first] = when (category) {
                        0 -> HeatLevel.Zero
                        1 -> HeatLevel.One
                        2 -> HeatLevel.Two
                        3 -> HeatLevel.Three
                        else -> HeatLevel.Four
                    }
                }
            }
            else {
                for (pair in newDates) {
                    scorePerDate[pair.first] = pair.second
                    calendarData[pair.first] = if (pair.second == 1) HeatLevel.Four else HeatLevel.Zero
                }
            }
        }
    }

    fun launchRefreshCharts() {
        viewModelScope.launch {
            refreshCharts()
        }
    }

    private suspend fun refreshCharts() {
        if (habit.isNumeric) {
            refreshBarChart()
            refreshNumPieChart()
        } else {
            refreshPieChart()
        }
    }

    private suspend fun refreshPieChart() {
        pieRatio.floatValue = dataRepository.getDoneRatio(habit, startDate.value, endDate.value)
    }
    private suspend fun refreshNumPieChart() {
        goalRatio.floatValue = dataRepository.getGoalRatio(habit, startDate.value, endDate.value)
    }

    private suspend fun refreshBarChart() {
        val newBarData = dataRepository.getNumericExecutions(habit, startDate.value, endDate.value)
        mutex.withLock {
            barData.clear()
            barData.addAll(newBarData)
        }
    }

    fun updateBinary(date: LocalDate, value: Boolean) {
        viewModelScope.launch {
            dataRepository.updateBinary(habit, date, value)
            refresh()
        }
    }

    fun updateNumeric(date: LocalDate, numberField: String){
        val pattern = Regex("[0-9]+\\s*")
        viewModelScope.launch {
            if(numberField == "" || !numberField.matches(pattern)){
                snackbarHostState.showSnackbar("Invalid number")
            }
            else{
                dataRepository.updateNumeric(habit, date, numberField.trim().toInt())
            }
            refresh()
        }
    }
}