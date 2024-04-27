package rs.ac.bg.matf.habitlab.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity
data class Habit (
    val name: String,
    val isNumeric: Boolean,
    val goal: Int?,
) : Serializable {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
}
