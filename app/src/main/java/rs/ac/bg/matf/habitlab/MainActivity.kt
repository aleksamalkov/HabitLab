package rs.ac.bg.matf.habitlab

import android.os.Bundle
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
import rs.ac.bg.matf.habitlab.ui.theme.HabitLabTheme

class MainActivity : ComponentActivity() {
    // lista taskova
    private val binaryTasks = mutableStateListOf<BinaryTaskModel>()
    // string koji upisujemo u polje
    private val textFieldString = mutableStateOf<String>("")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HabitLabTheme {
                // ovo je vljd za dizajn, nisam sigurna
                Surface(
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column {
                        binaryTasks.forEach { task ->
                            BinaryTask(task.name)
                        }

                        Row {
                            AddTaskButton {
                                addBinaryTask(textFieldString.value)
                            }

                            TextField(value = textFieldString.value, onValueChange = {textFieldString.value = it})
                        }
                    }
                }
            }
        }
    }


    private fun addBinaryTask(taskName: String) {
        if (taskName.isNotBlank()) {
            binaryTasks.add(BinaryTaskModel(taskName))
            textFieldString.value = ""
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