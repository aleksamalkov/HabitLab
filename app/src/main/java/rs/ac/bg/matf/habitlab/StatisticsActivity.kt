package rs.ac.bg.matf.habitlab

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import co.yml.charts.axis.AxisData
import co.yml.charts.axis.DataCategoryOptions
import co.yml.charts.common.model.PlotType
import co.yml.charts.common.utils.DataUtils
import co.yml.charts.ui.barchart.BarChart
import co.yml.charts.ui.barchart.models.BarChartData
import co.yml.charts.ui.barchart.models.BarChartType
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
import rs.ac.bg.matf.habitlab.ui.theme.NewPink
import rs.ac.bg.matf.habitlab.ui.theme.Pink40
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
                    color = NewPink
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                    ){
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        item {
                            Box(modifier = Modifier.height(60.dp))
                            {
                                Row {
                                    ReturnButton()
                                    Text(
                                        text = viewModel.habit.name,
                                        textAlign = TextAlign.Center,
                                        fontSize = 50.sp,
                                        modifier = Modifier
                                            .weight(1f) // This will make the Text take up all the available horizontal space
                                            .padding(horizontal = 8.dp)
                                    )
                                    RemoveButton()
                                }
                            }
                        }
                                    item {
                                        Box(modifier = Modifier.height(400.dp)) {
                                            ShowCalendar(
                                                selectedDate = viewModel.doneDates
                                            )
                                        }
                                    }
                                    if (viewModel.habit.isNumeric) {
                                        item { ShowBar() }
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
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Unazad",
                            modifier = Modifier.size(50.dp)
                        )
                    }
                }


                // TODO treba zapravo implementirati
                @Composable
                fun RemoveButton() {
                    val activity = (LocalContext.current as? Activity)
                    IconButton(
                        onClick = { },
                        modifier = Modifier
                        //  .size(100.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Obrisi",
                            modifier = Modifier
                                .size(80.dp)
                        )

                    }
                }

                @OptIn(ExperimentalMaterial3Api::class)
                @Composable
                fun ShowCalendar(selectedDate: SnapshotStateList<LocalDate>) {
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
                    )
                }

                @Composable
                fun ShowPie(viewModel: StatisticsViewModel) {
                    val showDialog = remember { mutableStateOf(false) }

                    Column {
                        //TODO da se napravi funkcikonalnost ovom textfieldu
                        Row() {
                            Spacer(modifier = Modifier.width(20.dp))
                            TextField(
                                value = "",
                                onValueChange = { },
                                placeholder = {
                                    Text(
                                        text = "poslednjih x dana",
                                        fontSize = 20.sp
                                    )
                                },
                                modifier = Modifier
                                    //.background(Color.LightGray, shape = RoundedCornerShape(50.dp))
                                    .width(200.dp)
                            )
                            Spacer(modifier = Modifier.width(20.dp))

                            Button(
                                onClick = { showDialog.value = true },
                                colors = ButtonDefaults.buttonColors(containerColor = Pink40)
                            ) {
                                Text("Prikazi pie")
                            }
                        }
                    }
                    ShowPieChart(viewModel)
                    if (showDialog.value) {
                        ShowPieChart(viewModel)
                    }
                }

                // TODO da se napravi da prima listu kad je uradjeno i na osnovu nje napravi pie chart
                @Composable
                fun ShowPieChart(viewModel: StatisticsViewModel) {
                    val pieChartData = PieChartData(
                        slices = listOf(
                            PieChartData.Slice(
                                "Uradjeno",
                                viewModel.pieRatio.floatValue * 100,
                                Color(0xFF333333)
                            ),
                            PieChartData.Slice(
                                "Nije",
                                (1 - viewModel.pieRatio.floatValue) * 100,
                                Color(0xFF666a86)
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
                        backgroundColor = NewPink
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
                fun ShowBar() {
                    val showDialog = remember { mutableStateOf(false) }

                    Column {
                        //TODO da se napravi funkcikonalnost ovom textfieldu
                        Row {
                            Spacer(modifier = Modifier.width(20.dp))
                            TextField(
                                value = "",
                                onValueChange = { },
                                placeholder = {
                                    Text(
                                        text = "poslednjih x dana",
                                        fontSize = 20.sp
                                    )
                                },
                                modifier = Modifier
                                    .width(200.dp)
                            )
                            Spacer(modifier = Modifier.width(20.dp))
                            Button(
                                onClick = { showDialog.value = true },
                                colors = ButtonDefaults.buttonColors(containerColor = Pink40)
                            ) {
                                Text("Prikazi bar")
                                  //  color = MaterialTheme.colorScheme.tertiary)
                            }

                        }
                        ShowBarChart()
                        if (showDialog.value) {
                            ShowBarChart()
                        }
                    }
                }
                @Composable
                fun ShowBarChart(){
                    val stepSize = 5
                    val barsData = DataUtils.getBarChartData(
                        listSize = 8,
                        maxRange = 8,
                        barChartType = BarChartType.VERTICAL,
                        dataCategoryOptions = DataCategoryOptions()
                    )

                    val xAxisData = AxisData.Builder()
                        .axisStepSize(30.dp)
                        .steps(barsData.size - 1)
                        .bottomPadding(40.dp)
                        .axisLabelAngle(20f)
                        .labelData { index -> barsData[index].label }
                        .backgroundColor(NewPink)
                        .build()

                    val yAxisData = AxisData.Builder()
                        .steps(stepSize)
                        .labelAndAxisLinePadding(20.dp)
                        .axisOffset(20.dp)
                        .labelData { index -> (index * (100 / stepSize)).toString() }
                        .backgroundColor(NewPink)
                        .build()

                    val barChartData = BarChartData(
                        chartData = barsData,
                        xAxisData = xAxisData,
                        yAxisData = yAxisData,
                        paddingEnd = 0.dp,
                        backgroundColor = NewPink
                    )

                    BarChart(modifier = Modifier.height(350.dp)
                        .fillMaxWidth(), barChartData = barChartData)
                }
