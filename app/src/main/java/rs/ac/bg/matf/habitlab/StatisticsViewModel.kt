package rs.ac.bg.matf.habitlab

import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import rs.ac.bg.matf.habitlab.data.DataRepository
import rs.ac.bg.matf.habitlab.data.Habit
import java.time.LocalDate
import java.time.YearMonth

class StatisticsViewModel (private val dataRepository: DataRepository, val habit: Habit) : ViewModel() {
    val today: LocalDate = LocalDate.now()
    var month: YearMonth = YearMonth.of(today.year, today.month)
    val doneDates = mutableStateListOf<LocalDate>()
    val pieRatio = mutableFloatStateOf(0.0F)
    val barData = mutableStateListOf<Int>()

    var startDate = mutableStateOf(today.minusDays(6))
    var endDate = mutableStateOf(today)

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
        val newDates = dataRepository.getDoneDates(
            habit,
            month.atDay(1),
            month.atEndOfMonth(),
        )
        doneDates.clear()
        doneDates.addAll(newDates)
    }

    fun refreshChartsSynchronously() {
        viewModelScope.launch {
            refreshCharts()
        }
    }

    private suspend fun refreshCharts() {
        if (habit.isNumeric) {
            refreshBarChart()
        } else {
            refreshPieChart()
        }
    }

    private suspend fun refreshPieChart() {
        pieRatio.floatValue = dataRepository.getDoneRatio(habit, startDate.value, endDate.value)
    }

    private suspend fun refreshBarChart() {
        val newBarData = dataRepository.getNumericExecutions(habit, startDate.value, endDate.value)
        barData.clear()
        barData.addAll(newBarData)
    }
}