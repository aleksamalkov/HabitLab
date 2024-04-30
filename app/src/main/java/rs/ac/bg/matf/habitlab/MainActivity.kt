package rs.ac.bg.matf.habitlab

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import rs.ac.bg.matf.habitlab.data.AppDatabase
import rs.ac.bg.matf.habitlab.data.DataRepository
import rs.ac.bg.matf.habitlab.data.Habit
import rs.ac.bg.matf.habitlab.statisticsActivity.StatisticsActivity
import rs.ac.bg.matf.habitlab.ui.theme.HabitLabTheme
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
                Surface(
                    color = MaterialTheme.colorScheme.background,
                ) {
                    Scaffold (
                        topBar = {ShowDays()},
                        floatingActionButton = {
                                FloatingActionButton(
                                    onClick = { stateHolder.showHabitDialog.value = true },
                                ) {
                                    Icon(Icons.Filled.Add, "Floating action button.")
                                    if (stateHolder.showHabitDialog.value) {
                                        HabitDialog(stateHolder)
                                    }
                                }
                        },
                        snackbarHost = { SnackbarHost(hostState = stateHolder.snackbarHostState) },
                    ) { innerPadding ->
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(innerPadding),
                            contentPadding = PaddingValues(horizontal = 10.dp)
                        ) {
                            items(stateHolder.habits) { task ->
                                if (task.isNumeric) {
                                    NumberTask(stateHolder, task)
                                } else {
                                    BinaryTask(stateHolder, task)
                                }
                                HorizontalDivider()
                            }
                            item { 
                                Spacer(modifier = Modifier.height(100.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        stateHolder.refreshView()
    }
}

@Composable
fun HabitDialog(stateHolder: StateHolder) {
    Dialog(onDismissRequest = { stateHolder.showHabitDialog.value = false }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
        ) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val switchState = remember { mutableStateOf(false) }

                Text(text = "New Habit")

                TextField(value = stateHolder.textFieldString.value,
                    onValueChange = { stateHolder.textFieldString.value = it },
                    modifier = Modifier
                        .padding(10.dp)
                        .width(200.dp),
                    label = { Text("Name") }
                )

                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(10.dp)) {
                    Switch(
                        checked = switchState.value,
                        onCheckedChange = { isChecked ->
                            switchState.value = isChecked
                        },
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Numeric")
                }

                if (switchState.value) {
                    TextField(
                        value = stateHolder.goalString.value,
                        onValueChange = {
                            val pattern = Regex("[0-9]+")
                            if (it.matches(pattern)) {
                                stateHolder.goalString.value = it
                            } else {
                                stateHolder.goalString.value = ""
                            }
                        },
                        enabled = switchState.value,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .width(200.dp)
                            .padding(10.dp),
                        label = { Text("Goal") },
                    )
                }

                Row (modifier = Modifier.padding(10.dp)) {
                    TextButton(onClick = { stateHolder.showHabitDialog.value = false }) {
                        Text("Cancel")
                    }
                    AddTaskButton(switchState.value) {
                        stateHolder.addHabit(switchState.value)
                        stateHolder.showHabitDialog.value = false
                    }
                }
            }
        }
    }
}

@Composable
fun BinaryTask(viewModel: StateHolder, habit: Habit) {
    Column {
        StatisticsButton(habit)
//        Text(text = habit.name)
        Row (modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween){
            repeat(7) { i ->
                Checkbox(
                    checked = viewModel.checkedState[habit]?.get(i) ?: false,
                    onCheckedChange = {
                        viewModel.onCheckedChange(habit, i, it)
                    },
                            colors = CheckboxDefaults.colors()//checkboxColors(containerColor = Pink40)
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
fun AddTaskButton(isNumeric: Boolean, onClick: () -> Unit) {
    Button(onClick = onClick,
        modifier = Modifier.width(120.dp),
        colors = ButtonDefaults.buttonColors()
    ) {
        if (isNumeric) {
            Text(text = "+ Numeric")

        }
        else {
            Text(text = "+ Binary")
        }
    }
}

@Composable
fun NumberTask(viewModel: StateHolder, habit: Habit) {
    Column {
        StatisticsButton(habit)
        Row (modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween){
            repeat(7) {i ->

                val showDialog = remember{ mutableStateOf(false) }
                if (showDialog.value) {
                    NumberDialog(viewModel = viewModel, habit = habit, i = i, showDialog)
                }

                TextButton(
                    onClick = {
                        viewModel.numberField.value = viewModel.numbersState[habit]?.get(i).toString()
                        showDialog.value = true
                    },
                    modifier = Modifier
                        .width(50.dp)
                        .padding(bottom = 7.dp),

                ) {
                    Text(text = viewModel.numbersState[habit]?.get(i).toString())
                }
            }
        }
    }
}

@Composable
fun NumberDialog(viewModel: StateHolder, habit: Habit, i: Int, showDialog: MutableState<Boolean>) {
    Dialog(onDismissRequest = {showDialog.value = false}) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column (
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(habit.name, modifier = Modifier.padding(10.dp))
                Text(viewModel.today.toString(), modifier = Modifier.padding(10.dp))
                TextField(
                    value = viewModel.numberField.value,
                    onValueChange = { viewModel.numberField.value = it },
                    textStyle = TextStyle(textAlign = TextAlign.Center),
                    modifier = Modifier
                        .width(200.dp)
                        .padding(10.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Row (modifier = Modifier.padding(10.dp)) {
                    TextButton(onClick = { showDialog.value = false }) {
                        Text("Cancel")
                    }
                    TextButton(
                        onClick = {
                            showDialog.value = false
                            viewModel.changeNumber(habit, i)
                        }
                    ) {
                        Text("OK")
                    }
                }
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
    val formatter = DateTimeFormatter.ofPattern("MM/dd")
    Row (modifier = Modifier.fillMaxWidth().padding(10.dp),
        horizontalArrangement = Arrangement.SpaceBetween){
        repeat(7) {
            Text(text = firstDayToShow.plusDays(it.toLong()).format(formatter))
        }
    }
}

@Composable
fun StatisticsButton(habit: Habit){
    val mContext = LocalContext.current
    TextButton(onClick = {
        mContext.startActivity(Intent(mContext, StatisticsActivity::class.java).apply {
            putExtra("habit", habit)
        })
    }) {
        Text(text = habit.name,
            style = TextStyle(fontSize = 20.sp),
        )
    }
}

