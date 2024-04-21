package rs.ac.bg.matf.habitlab.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Habit (
    val name: String,
    val isNumeric: Boolean,
    val goal: Int?,
) {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
}
