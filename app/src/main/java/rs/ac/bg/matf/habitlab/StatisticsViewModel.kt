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
    var month: YearMonth = YearMonth.now()
    val doneDates = mutableStateListOf<LocalDate>()
    val pieRatio = mutableFloatStateOf(0.0F)

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
        refreshPie()
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

    private suspend fun refreshPie() {
        pieRatio.floatValue = dataRepository.getDoneRatio(habit, today)
    }
}