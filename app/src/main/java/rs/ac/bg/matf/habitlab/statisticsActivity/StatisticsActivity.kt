package rs.ac.bg.matf.habitlab.statisticsActivity

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.yml.charts.axis.AxisData
import co.yml.charts.common.model.PlotType
import co.yml.charts.common.model.Point
import co.yml.charts.ui.barchart.BarChart
import co.yml.charts.ui.barchart.models.BarChartData
import co.yml.charts.ui.barchart.models.BarData
import co.yml.charts.ui.piechart.charts.PieChart
import co.yml.charts.ui.piechart.models.PieChartConfig
import co.yml.charts.ui.piechart.models.PieChartData
import com.kizitonwose.calendar.compose.HeatMapCalendar
import com.kizitonwose.calendar.compose.heatmapcalendar.rememberHeatMapCalendarState
import com.kizitonwose.calendar.core.firstDayOfWeekFromLocale
import com.kizitonwose.calendar.core.yearMonth
import rs.ac.bg.matf.habitlab.data.AppDatabase
import rs.ac.bg.matf.habitlab.data.DataRepository
import rs.ac.bg.matf.habitlab.data.Habit
import rs.ac.bg.matf.habitlab.ui.theme.HabitLabTheme
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Calendar

class StatisticsActivity : ComponentActivity() {
    private lateinit var db: AppDatabase
    private lateinit var viewModel: StatisticsViewModel

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        db = AppDatabase.getInstance(applicationContext)
        @Suppress("DEPRECATION")
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
                    Scaffold(
                        topBar = {
                            TopAppBar(
                                title = { Text(text = viewModel.habit.name) },
                                navigationIcon = { ReturnButton() },
                                actions = { RemoveButton(viewModel) },
                            )
                        },
                    ){ innerPadding ->
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(innerPadding)
                                .padding(10.dp)
                        ) {
                            item {
                                ShowCalendar(viewModel)
                            }
                            if (viewModel.habit.isNumeric) {
                                item { ShowBar(viewModel) }
                            } else {
                                item { ShowPie(viewModel) }

                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReturnButton() {
    val activity = (LocalContext.current as? Activity)
    IconButton(
        onClick = { activity?.finish() },
        modifier = Modifier.size(60.dp)
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Unazad",
        )
    }
}

@Composable
fun DeleteDialog(viewModel: StatisticsViewModel, activity: Activity?) {
    AlertDialog(
        icon = {
            Icon(Icons.Default.Delete, contentDescription = "Delete Icon")
        },
        title = {
            Text(text = "Delete habit")
        },
        text = {
            Text(text = "Are you sure you want to delete habit ${viewModel.habit.name}?")
        },
        onDismissRequest = {
            viewModel.showDeleteDialog.value = false
        },
        confirmButton = {
            TextButton(
                onClick = {
                    viewModel.delete()
                    activity?.finish()
                }
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    viewModel.showDeleteDialog.value = false
                }
            ) {
                Text("Dismiss")
            }
        }
    )
}

@Composable
fun RemoveButton(viewModel: StatisticsViewModel) {
    val activity = (LocalContext.current as? Activity)
    IconButton(
        onClick = {
            viewModel.showDeleteDialog.value = true
        },
        modifier = Modifier
        //  .size(100.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Delete,
            contentDescription = "Obrisi",
        )
    }

    if (viewModel.showDeleteDialog.value) {
        DeleteDialog(viewModel = viewModel, activity = activity)
    }
}

@Composable
fun ShowCalendar(viewModel: StatisticsViewModel) {
    val endDate = viewModel.today
    val startDate = endDate.minusMonths(12)
    val data = viewModel.calendarData
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        val state = rememberHeatMapCalendarState(
            startMonth = startDate.yearMonth,
            endMonth = endDate.yearMonth,
            firstVisibleMonth = endDate.yearMonth,
            firstDayOfWeek = firstDayOfWeekFromLocale(),
        )
        HeatMapCalendar(
            modifier = Modifier.padding(vertical = 10.dp),
            state = state,
            contentPadding = PaddingValues(end = 6.dp),
            dayContent = { day, week ->
                Day(
                    day = day,
                    startDate = startDate,
                    endDate = endDate,
                    week = week,
                    level = data[day.date] ?: HeatLevel.Zero,
                )
            },
            weekHeader = { WeekHeader(it) },
            monthHeader = { MonthHeader(it, endDate) },
        )
        if (viewModel.habit.isNumeric) {
            CalendarInfo(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 44.dp),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerButton(viewModel: StatisticsViewModel) {
    var showDatePicker by remember { mutableStateOf(false) }
    val dateState = rememberDateRangePickerState(
        initialDisplayMode = DisplayMode.Input,
        initialSelectedStartDateMillis = viewModel.startDate.value.atStartOfDay()
            .toInstant(ZoneOffset.UTC).toEpochMilli(),
        initialSelectedEndDateMillis = viewModel.endDate.value.atStartOfDay()
            .toInstant(ZoneOffset.UTC).toEpochMilli(),
    )

    Button(
        onClick = {
            showDatePicker = true
        },
    ) {
        Text(text = "Pick range")
    }

    fun select() {
        if (dateState.selectedStartDateMillis != null && dateState.selectedEndDateMillis != null) {
            val selectedStartDate = Calendar.getInstance().apply {
                timeInMillis = dateState.selectedStartDateMillis!!
            }
            val selectedEndDate = Calendar.getInstance().apply {
                timeInMillis = dateState.selectedEndDateMillis!!
            }
            // from https://stackoverflow.com/questions/48983572/convert-calendar-to-localdate
            viewModel.startDate.value = LocalDateTime.ofInstant(
                selectedStartDate.toInstant(),
                selectedStartDate.timeZone.toZoneId()
            ).toLocalDate()
            viewModel.endDate.value = LocalDateTime.ofInstant(
                selectedEndDate.toInstant(),
                selectedEndDate.timeZone.toZoneId()
            ).toLocalDate()
            viewModel.launchRefreshCharts()
        }
    }

    // date picker component
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = {
                select()
                showDatePicker = false
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        select()
                        showDatePicker = false
                    },
                ) {
                    Text(text = "OK")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        select()
                        showDatePicker = false
                    },
                ) {
                    Text(text = "Cancel")
                }
            },
        )
        {
            DateRangePicker(state = dateState)
        }
    }
}

@Composable
fun ShowPie(viewModel: StatisticsViewModel) {
    Column {
        Row {
            Spacer(modifier = Modifier.width(20.dp))
            DatePickerButton(viewModel)
        }
        Text(text = "Since ${viewModel.startDate.value} , until ${viewModel.endDate.value}.")
        ShowPieChart(viewModel)
    }
}

@Composable
fun ShowPieChart(viewModel: StatisticsViewModel) {
    val pieChartData = PieChartData(
        slices = listOf(
            PieChartData.Slice(
                "Uradjeno",
                viewModel.pieRatio.floatValue * 100,
                color = MaterialTheme.colorScheme.primary
            ),
            PieChartData.Slice(
                "Nije",
                (1 - viewModel.pieRatio.floatValue) * 100,
                color = MaterialTheme.colorScheme.secondary,
            )

        ),
        plotType = PlotType.Pie
    )
    val pieChartConfig = PieChartConfig(

        labelVisible = true,
        sliceLabelTextSize = 10.sp,
        isAnimationEnable = true,
        showSliceLabels = true,
        animationDuration = 1500,
        backgroundColor = MaterialTheme.colorScheme.background,
    )

    PieChart(
        modifier = Modifier
            .width(250.dp)
            .height(250.dp),

        pieChartData,
        pieChartConfig
    )
}

@Composable
fun ShowBar(viewModel: StatisticsViewModel) {

    Column {
        Row {
            Spacer(modifier = Modifier.width(20.dp))
            DatePickerButton(viewModel)
        }
        Text(text = "Since ${viewModel.startDate.value} , until ${viewModel.endDate.value}.")
        ShowBarChart(viewModel)
    }
}

@Composable
fun ShowBarChart(viewModel: StatisticsViewModel){
    val data = viewModel.barData
    if (data.isEmpty()) {
        data.add(0)
    }

    val barsData = mutableListOf<BarData>()

    var date = viewModel.startDate.value
    var x = 0f
    val formatter = DateTimeFormatter.ofPattern("MM-dd")
    for (i in data) {
        barsData.add(BarData(
            Point(x, i.toFloat()),
            color = MaterialTheme.colorScheme.primary,
            label = date.format(formatter),
        ))
        x += 1f
        date = date.plusDays(1)
    }

    val xAxisData = AxisData.Builder()
        .axisStepSize(30.dp)
        .steps(barsData.size - 1)
        .bottomPadding(40.dp)
        .axisLabelAngle(20f)
        .labelData { index -> barsData[index].label }
        .backgroundColor(MaterialTheme.colorScheme.background)
        .axisLabelColor(MaterialTheme.colorScheme.onBackground)
        .axisLineColor(MaterialTheme.colorScheme.onBackground)
        .build()

    // TODO popraviti za veliko max
    val max = data.maxOrNull() ?: 1
    val yAxisData = AxisData.Builder()
        .steps(max)
        .labelAndAxisLinePadding(20.dp)
        .axisOffset(20.dp)
        .labelData { index -> (index).toString() }
        .backgroundColor(MaterialTheme.colorScheme.background)
        .axisLabelColor(MaterialTheme.colorScheme.onBackground)
        .axisLineColor(MaterialTheme.colorScheme.onBackground)
        .build()

    val barChartData = BarChartData(
        chartData = barsData,
        xAxisData = xAxisData,
        yAxisData = yAxisData,
        paddingEnd = 0.dp,
        backgroundColor = MaterialTheme.colorScheme.background,
    )

    BarChart(modifier = Modifier
        .height(350.dp)
        .fillMaxWidth(), barChartData = barChartData)
}
