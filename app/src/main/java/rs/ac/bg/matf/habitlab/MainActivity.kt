package rs.ac.bg.matf.habitlab

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.room.RoomDatabase
import rs.ac.bg.matf.habitlab.data.AppDatabase
import rs.ac.bg.matf.habitlab.data.DataRepository
import rs.ac.bg.matf.habitlab.ui.theme.HabitLabTheme
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Switch
import androidx.compose.ui.unit.dp


class MainActivity : ComponentActivity() {

    lateinit var stateHolder: StateHolder
    lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        db = AppDatabase.getInstance(applicationContext)
        stateHolder = StateHolder(DataRepository(db.habitDao(), db.occurrenceDao()))

        setContent {
            HabitLabTheme {
                // ovo je vljd za dizajn, nisam sigurna
                Surface(
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column {
                        stateHolder.habits.forEach { task ->
                            BinaryTask(task.name)
                            NumberTask(task.name)
                            //svaki task treba da ima oznaku sta je
                            // i onda se prikazuje samo ono sto treba
                        }

                        Row {
                            var switchState = remember { mutableStateOf(false) }
                            Switch(
                                checked = switchState.value,
                                onCheckedChange = { isChecked -> switchState.value = isChecked })
                            AddTaskButton {
                                if (switchState.value){
                                    stateHolder.addBinaryTask()
                                }
                                else{
//                                  TODO stateHolder.addNumberTask()
                                }
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

@Composable
fun NumberTask(name: String) {
    Column {
        Text(text = name)
        Row {
            repeat(7) {
                val textState = remember { mutableStateOf("") }

                TextField(
                    value = textState.value,
                    onValueChange = { textState.value = it },
                    textStyle = TextStyle(fontSize = 16.sp),
                    modifier = Modifier.width(60.dp).padding(5.dp).height(50.dp)
                )
            }
        }
    }
}

@Preview
@Composable
fun NumberTaskPreview(){
    NumberTask("pokusaj")
}