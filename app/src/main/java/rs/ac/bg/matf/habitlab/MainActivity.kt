package rs.ac.bg.matf.habitlab

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.ui.unit.sp
import rs.ac.bg.matf.habitlab.data.AppDatabase
import rs.ac.bg.matf.habitlab.data.DataRepository
import rs.ac.bg.matf.habitlab.data.Habit
import rs.ac.bg.matf.habitlab.ui.theme.HabitLabTheme
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Switch
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import android.content.Intent
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.TextButton


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
                    Column (modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())){
                        ShowDays()
                        stateHolder.habits.forEach { task ->
                            if (task.isNumeric) {
                                NumberTask(stateHolder, task)
                            }
                            else {
                                BinaryTask(stateHolder, task)
                            }
                        }

                        Row (modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
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
                                modifier = Modifier
                                    .width(120.dp)
                                    .height(50.dp),
                                label = {Text("Name")})

                            TextField(value = stateHolder.goalString.value,
                                onValueChange = {stateHolder.goalString.value = it},
                                modifier = Modifier
                                    .width(80.dp)
                                    .height(50.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
        StatisticsBinaryButton(habit)
//        Text(text = habit.name)
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
fun NumberTask(viewModel: StateHolder, habit: Habit) {
    Column {
        StatisticsNumberButton(habitName = habit.name)
        Row (modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween){
            repeat(7) {i ->

                TextField(
                    value = if (viewModel.numbersState[habit]?.get(i).toString() == "0")  "" else viewModel.numbersState[habit]?.get(i).toString() ,
                    onValueChange = { viewModel.onValueChange(habit, i, it)},
                    textStyle = TextStyle(fontSize = 14.sp),
                    modifier = Modifier.width(50.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        }
    }
}

//@Preview
//@Composable
//fun NumberTaskPreview(){
//    NumberTask("pokusaj")
//}

@Composable
fun ShowDays() {
    val firstDayToShow = LocalDate.now().minusDays(6)
    val formatter = DateTimeFormatter.ofPattern("dd.MM.")
    Row (modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween){
        repeat(7) {it ->
            Text(text = firstDayToShow.plusDays(it.toLong()).format(formatter))
        }
    }
}

@Composable
fun StatisticsBinaryButton(habit: Habit){
    val mContext = LocalContext.current
    TextButton(onClick = {
        mContext.startActivity(Intent(mContext, StatisticsActivity::class.java).apply {
            putExtra("habit", habit)
        })
    }) {
        Text(text = habit.name,
            style = TextStyle(fontSize = 20.sp)
        )
    }
}

@Composable
fun StatisticsNumberButton(habitName: String){
    val mContext = LocalContext.current
    TextButton(onClick = {
        mContext.startActivity(Intent(mContext, StatisticsNumberActivity::class.java))
    }) {
        Text(text = habitName,
            style = TextStyle(fontSize = 20.sp)
        )
    }
}