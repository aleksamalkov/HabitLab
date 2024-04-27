package rs.ac.bg.matf.habitlab

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import rs.ac.bg.matf.habitlab.ui.theme.HabitLabTheme
import java.time.LocalDate

class StatisticsNumberActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HabitLabTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column{
                        Text(
                            text = "Trcanje",
                            fontSize = 50.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentSize(Alignment.Center)
                        )

                        //ovo treba da bude lista dana kad je odradjena navika
                        var selectedDates = remember {
                            mutableStateListOf<LocalDate>(
                                LocalDate.now().minusDays(3),
                                LocalDate.now().minusDays(2),
                                LocalDate.now().minusDays(10),
                                LocalDate.now().minusDays(9),
                                LocalDate.now().minusDays(7)
                            )
                        }
                        ShowCalendar(selectedDate = selectedDates)
                        ShowBar()

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
fun ShowBar() {
    val showDialog = remember { mutableStateOf(false) }

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
            Text("Prikazi bar")
        }
    }

    if (showDialog.value) {
        ShowBarChart()
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
        .build()

    val yAxisData = AxisData.Builder()
        .steps(stepSize)
        .labelAndAxisLinePadding(20.dp)
        .axisOffset(20.dp)
        .labelData { index -> (index * (100 / stepSize)).toString() }
        .build()

    val barChartData = BarChartData(
        chartData = barsData,
        xAxisData = xAxisData,
        yAxisData = yAxisData,
      //  paddingBetweenBars = 20.dp,
      //  barWidth = 25.dp
    )

    BarChart(modifier = Modifier.height(350.dp), barChartData = barChartData)
}