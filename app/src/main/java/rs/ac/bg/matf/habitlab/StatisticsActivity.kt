package rs.ac.bg.matf.habitlab

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import co.yml.charts.common.model.PlotType
import co.yml.charts.ui.piechart.charts.PieChart
import co.yml.charts.ui.piechart.models.PieChartConfig
import co.yml.charts.ui.piechart.models.PieChartData
import com.maxkeppeker.sheets.core.models.base.rememberUseCaseState
import com.maxkeppeler.sheets.calendar.CalendarView
import com.maxkeppeler.sheets.calendar.models.CalendarConfig
import com.maxkeppeler.sheets.calendar.models.CalendarSelection
import com.maxkeppeler.sheets.calendar.models.CalendarStyle
import rs.ac.bg.matf.habitlab.data.AppDatabase
import rs.ac.bg.matf.habitlab.data.DataRepository
import rs.ac.bg.matf.habitlab.data.Habit
import rs.ac.bg.matf.habitlab.ui.theme.HabitLabTheme
import java.time.LocalDate

class StatisticsActivity : ComponentActivity() {
    private lateinit var db: AppDatabase
    private lateinit var viewModel: StatisticsViewModel
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        db = AppDatabase.getInstance(applicationContext)
        viewModel = StatisticsViewModel(
            DataRepository(db.habitDao(), db.occurrenceDao()),
            intent.getSerializableExtra("habit") as Habit
        )

        setContent {
            HabitLabTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column{
                        Text(
                            text = viewModel.habit.name,
                            fontSize = 50.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentSize(Alignment.Center)
                        )

                        ShowCalendar(selectedDate = viewModel.doneDates)
                        ShowPie(viewModel)

                        Row{
                            ReturnButton()
                            RemoveButton()
                        }
                    }

                }
            }

        }
    }

}

@Composable
fun ReturnButton(){
    val activity = (LocalContext.current as? Activity)
    Button(onClick = { activity?.finish()}) {
        Text("Return")
    }
}


// TODO treba zapravo implementirati
@Composable
fun RemoveButton(){
    val activity = (LocalContext.current as? Activity)
    Button(onClick = { }) {
        Text("Remove")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShowCalendar(selectedDate: SnapshotStateList<LocalDate> ){
    var mutableSelectedDates = selectedDate
    CalendarView(
        useCaseState = rememberUseCaseState(true),
        config = CalendarConfig(
            yearSelection = true,
            monthSelection = true,
            style = CalendarStyle.MONTH,
        ),
        selection = CalendarSelection.Dates(
            selectedDates = mutableSelectedDates.toList()
        ) { newDate ->
            mutableSelectedDates = newDate as SnapshotStateList<LocalDate>
        },
        //  header = Header.Default("Trcanje")
    )
}

@Composable
fun ShowPie(viewModel: StatisticsViewModel) {
    val showDialog = remember {mutableStateOf(false)}

    Column {
        //TODO da se napravi funkcikonalnost ovom textfieldu
        TextField(
            value = "",
            onValueChange = { },
            placeholder = {
                Text(
                    text = "poslednjih x dana",
                    fontSize = 20.sp
                )
            }
        )
        Button(
            onClick = { showDialog.value = true }
        ) {
            Text("Prikazi pie")
        }
    }

    if (showDialog.value) {
        ShowPieChart(viewModel)
    }
}

@Composable
fun ShowPieChart(viewModel: StatisticsViewModel){
    val pieChartData = PieChartData(
        slices = listOf(
            PieChartData.Slice("Uradjeno", viewModel.pieRatio.floatValue * 100, Color(0xFF333333)),
            PieChartData.Slice("Nije", (1 - viewModel.pieRatio.floatValue) * 100, Color(0xFF666a86))

        ),
        plotType = PlotType.Pie
    )
    val pieChartConfig = PieChartConfig(

        labelVisible = true,
        sliceLabelTextSize = 10.sp,
        isAnimationEnable = true,
        showSliceLabels = true,
        animationDuration = 1500
    )

    PieChart(
        modifier = Modifier
            .width(150.dp)
            .height(150.dp),
        pieChartData,
        pieChartConfig
        )
   }

