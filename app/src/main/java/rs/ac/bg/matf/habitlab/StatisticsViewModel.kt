package rs.ac.bg.matf.habitlab

import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
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
        if (habit.isNumeric) {
            refreshBarChart()
        } else {
            refreshPieChart()
        }
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

    private suspend fun refreshPieChart() {
        pieRatio.floatValue = dataRepository.getDoneRatio(habit, today)
    }

    private suspend fun refreshBarChart() {
        val newBarData = dataRepository.getNumericExecutions(habit, month.atDay(1), month.atEndOfMonth())
        barData.clear()
        barData.addAll(newBarData)
    }
}