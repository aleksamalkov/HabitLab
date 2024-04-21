package rs.ac.bg.matf.habitlab

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import androidx.room.RoomDatabase
import rs.ac.bg.matf.habitlab.data.AppDatabase
import rs.ac.bg.matf.habitlab.data.DataRepository
import rs.ac.bg.matf.habitlab.ui.theme.HabitLabTheme

class MainActivity : ComponentActivity() {

    lateinit var stateHolder: StateHolder
    lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        db = AppDatabase.getInstance(applicationContext)
        stateHolder = StateHolder(db.habitDao())

        setContent {
            HabitLabTheme {
                // ovo je vljd za dizajn, nisam sigurna
                Surface(
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column {
                        stateHolder.habits.forEach { task ->
                            BinaryTask(task.name)
                        }

                        Row {
                            AddTaskButton {
                                stateHolder.addBinaryTask()
                            }

                            TextField(value = stateHolder.textFieldString.value, onValueChange = {stateHolder.textFieldString.value = it})
                        }
                    }
                }
            }
        }
    }
}

// ovo mi je delovalo kao jedina opcija da imam listu taskova
data class BinaryTaskModel(val name: String)

@Composable
fun BinaryTask(name: String) {
    Column {
        Text(text = name)
        Row {
            repeat(7) {
                val checkedState = remember { mutableStateOf(false) }
                Checkbox(
                    checked = checkedState.value,
                    onCheckedChange = { checkedState.value = it }
                )
            }
        }
    }
}

@Preview
@Composable
fun BinaryTaskPreview(){
    BinaryTask("Task za preview")
}

@Composable
fun AddTaskButton(onClick: () -> Unit) {
    Button(onClick = onClick) {
        Text(text = "+ Binarni Task")
    }
}