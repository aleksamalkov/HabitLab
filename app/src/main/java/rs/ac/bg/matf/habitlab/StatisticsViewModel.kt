package rs.ac.bg.matf.habitlab

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import rs.ac.bg.matf.habitlab.data.DataRepository
import rs.ac.bg.matf.habitlab.data.Habit

class StatisticsViewModel (private val dataRepository: DataRepository, val habit: Habit) : ViewModel() {

}