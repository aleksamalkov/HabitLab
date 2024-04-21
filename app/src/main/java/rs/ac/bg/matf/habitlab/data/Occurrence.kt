package rs.ac.bg.matf.habitlab.data

import androidx.room.Entity
import java.time.LocalDate

@Entity(primaryKeys = ["habitId", "date"])
data class Occurrence (
    val habitId: Int,
    val date: LocalDate,
    val count: Int?,
)