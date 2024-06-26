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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import rs.ac.bg.matf.habitlab.data.AppDatabase
import rs.ac.bg.matf.habitlab.data.DataRepository
import rs.ac.bg.matf.habitlab.data.Habit
import rs.ac.bg.matf.habitlab.statisticsActivity.StatisticsActivity
import rs.ac.bg.matf.habitlab.ui.theme.HabitLabTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale


class MainActivity : ComponentActivity() {

    private lateinit var viewModel: MainViewModel
    private lateinit var db: AppDatabase

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // temporary, so that date formatting is consistent
        Locale.setDefault(Locale.UK)

        db = AppDatabase.getInstance(applicationContext)
        viewModel = MainViewModel(DataRepository(db.habitDao(), db.occurrenceDao()))

        setContent {
            HabitLabTheme {
                Surface(
                    color = MaterialTheme.colorScheme.background,
                ) {
                    Scaffold (
                        topBar = {
                            Column {
                                TopAppBar(
                                    title = {
                                        Text(
                                            text = "HabitLab",
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary,
                                        )
                                    }
                                )
                                ShowDays()
                            }
                        },
                        floatingActionButton = {
                                FloatingActionButton(
                                    onClick = { viewModel.showHabitDialog.value = true },
                                ) {
                                    Icon(Icons.Filled.Add, "Floating action button.")
                                    if (viewModel.showHabitDialog.value) {
                                        HabitDialog(viewModel)
                                    }
                                }
                        },
                        snackbarHost = { SnackbarHost(hostState = viewModel.snackbarHostState) },
                    ) { innerPadding ->
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(innerPadding),
                            contentPadding = PaddingValues(horizontal = 10.dp)
                        ) {
                            items(viewModel.habits) { task ->
                                if (task.isNumeric) {
                                    NumberTask(viewModel, task)
                                } else {
                                    BinaryTask(viewModel, task)
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
        viewModel.refreshView()
    }
}

@Composable
fun HabitDialog(viewModel: MainViewModel) {
    val switchState = remember { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = { viewModel.showHabitDialog.value = false },
        confirmButton = {
            AddTaskButton(switchState.value) {
                viewModel.addHabit(switchState.value)
                viewModel.showHabitDialog.value = false
            }
        },
        dismissButton = {
            TextButton(onClick = { viewModel.showHabitDialog.value = false }) {
                Text("Cancel")
            }
        },
        icon = { Icon(Icons.Default.Add, contentDescription = "Add") },
        title = {
            Text(text = "New Habit")
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OutlinedTextField(value = viewModel.textFieldString.value,
                    onValueChange = { viewModel.textFieldString.value = it },
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
                    OutlinedTextField(
                        value = viewModel.goalString.value,
                        onValueChange = {
                            val pattern = Regex("[0-9]+")
                            if (it.matches(pattern)) {
                                viewModel.goalString.value = it
                            } else {
                                viewModel.goalString.value = ""
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
            }
        },
    )
}

@Composable
fun BinaryTask(viewModel: MainViewModel, habit: Habit) {
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
fun NumberTask(viewModel: MainViewModel, habit: Habit) {
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
                        .padding(bottom = 7.dp)
                        ,

                ) {
                    Text(text = viewModel.numbersState[habit]?.get(i).toString(), fontSize = 20.sp)
                }
            }
        }
    }
}

@Composable
fun NumberDialog(viewModel: MainViewModel, habit: Habit, i: Int, showDialog: MutableState<Boolean>) {
    AlertDialog(
        onDismissRequest = { showDialog.value = false },
        confirmButton = {
            TextButton(
                onClick = {
                    showDialog.value = false
                    viewModel.changeNumber(habit, i)
                }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = { showDialog.value = false }) {
                Text("Cancel")
            }
        },
        icon = { Icon(Icons.Default.Edit, contentDescription = "Edit entry") },
        title = { Text(habit.name) },
        text = {
            Column (
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val date = viewModel.today.minusDays((6 - i).toLong())
                val formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL)
                Text(
                    date.format(formatter),
                    modifier = Modifier.padding(10.dp)
                )
                TextField(
                    value = viewModel.numberField.value,
                    onValueChange = { viewModel.numberField.value = it },
                    textStyle = TextStyle(textAlign = TextAlign.Center),
                    modifier = Modifier
                        .width(200.dp)
                        .padding(10.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        },
    )
}

//@Preview
//@Composable
//fun NumberTaskPreview(){
//    NumberTask("pokusaj")
//}

@Composable
fun ShowDays() {
    val firstDayToShow = LocalDate.now().minusDays(6)
    val formatter = DateTimeFormatter.ofPattern("dd/MM")
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

