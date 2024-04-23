package rs.ac.bg.matf.habitlab

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
import rs.ac.bg.matf.habitlab.data.AppDatabase
import rs.ac.bg.matf.habitlab.data.DataRepository
import rs.ac.bg.matf.habitlab.data.Habit
import rs.ac.bg.matf.habitlab.ui.theme.HabitLabTheme
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Switch
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.format.DateTimeFormatter


class MainActivity : ComponentActivity() {

    private lateinit var stateHolder: StateHolder
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        db = AppDatabase.getInstance(applicationContext)
        stateHolder = StateHolder(DataRepository(db.habitDao(), db.occurrenceDao()))

        setContent {
            HabitLabTheme {
                // ovo je vljd za dizajn, nisam sigurna
                Surface(
                    color = MaterialTheme.colorScheme.background,
                    modifier = Modifier.padding(10.dp)
                ) {
                    Column {
                        ShowDays()
                        stateHolder.habits.forEach { task ->
                            if (task.isNumeric) {
                                NumberTask(task.name)
                            }
                            else {
                                BinaryTask(stateHolder, task)
                            }
                        }

                        Row (modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween){
                            val switchState = remember { mutableStateOf(false) }
                            Switch(
                                checked = switchState.value,
                                onCheckedChange = { isChecked -> switchState.value = isChecked })
                            AddTaskButton(switchState.value) {
                                // TODO ova funkcija opciono prihvata vrednost cilja kao drugi argument
                                stateHolder.addHabit(!switchState.value)
                            }

                            TextField(value = stateHolder.textFieldString.value,
                                onValueChange = {stateHolder.textFieldString.value = it},
                                modifier = Modifier.width(120.dp).height(50.dp),
                                label = {Text("Name")})

                            TextField(value = stateHolder.goalString.value,
                                onValueChange = {stateHolder.goalString.value = it},
                                modifier = Modifier.width(80.dp).height(50.dp),
                                label = {Text("Goal")})
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BinaryTask(viewModel: StateHolder, habit: Habit) {
    Column {
        Text(text = habit.name)
        Row (modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween){
            repeat(7) { i ->
                Checkbox(
                    checked = viewModel.checkedState[habit]?.get(i) ?: false,
                    onCheckedChange = {
                        viewModel.onCheckedChange(habit, i, it)
                    }
                )
            }
        }
    }
}

//@Preview
//@Composable
//fun BinaryTaskPreview(){
//    BinaryTask("Task za preview")
//}

@Composable
fun AddTaskButton(isBinary: Boolean, onClick: () -> Unit) {
    Button(onClick = onClick,
        modifier = Modifier.width(120.dp)) {
        if (isBinary)
            Text(text = "+ Binary")
        else
            Text(text = "+ Numeric")
    }
}

// TODO ovo treba promeniti tako da se stanje cuva u StateHolder klasi i poziva funkcija odande
// TODO  za on value change, slicno kao kod BinaryTask
@Composable
fun NumberTask(name: String) {
    Column {
        Text(text = name)
        Row (modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween){
            repeat(7) {
                val textState = remember { mutableStateOf("") }

                TextField(
                    value = textState.value,
                    onValueChange = { textState.value = it },
                    textStyle = TextStyle(fontSize = 14.sp),
                    modifier = Modifier.width(50.dp)
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

@Composable
fun ShowDays() {
    val today = LocalDate.now()
    val formatter = DateTimeFormatter.ofPattern("dd.MM.")
    Row (modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween){
        repeat(7) {it ->
            Text(text = today.minusDays(it.toLong()).format(formatter))
        }
    }
}