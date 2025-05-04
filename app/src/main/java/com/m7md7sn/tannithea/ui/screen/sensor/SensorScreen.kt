package com.m7md7sn.tannithea.ui.screen.sensor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import com.m7md7sn.tannithea.data.model.TimedSensorReading
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.material3.HorizontalDivider

@Composable
fun SensorScreen(
    sensorName: String,
    currentValue: Float,
    unit: String,
    minValue: Float,
    maxValue: Float,
    normalRange: ClosedFloatingPointRange<Float>,
    viewModel: SensorViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    LaunchedEffect(sensorName) { viewModel.loadReadings(sensorName) }
    
    val readings by viewModel.readings.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    
    Box(modifier = modifier.fillMaxSize()) {
        if (isLoading && readings.isEmpty()) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
        } else if (error != null && readings.isEmpty()) {
            ErrorView(
                errorMessage = error ?: "Unknown error",
                onRetry = { viewModel.refresh(sensorName) }
            )
        } else {
            val scrollState = rememberScrollState()
            val latestValue = readings.lastOrNull()?.value ?: currentValue
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = sensorName,
                    style = MaterialTheme.typography.titleLarge.copy(fontSize = 32.sp),
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp,
                    modifier = Modifier
                        .padding(bottom = 20.dp)
                        .align(Alignment.Start)
                )
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    GradientCircularProgressBar(
                        value = latestValue,
                        minValue = minValue,
                        maxValue = maxValue,
                        normalRange = normalRange,
                        unit = unit,
                        modifier = Modifier.size(160.dp)
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                    thickness = 2.dp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                Text(
                    text = "Recent Readings",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                if (readings.isEmpty()) {
                    NoDataView()
                } else {
                    ReadingTable(readings)
                }
                Spacer(modifier = Modifier.height(24.dp))
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                    thickness = 2.dp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                Text(
                    text = "Reading Trend",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                if (readings.isEmpty()) {
                    NoDataView()
                } else {
                    VicoSensorLineChart(readings)
                }
            }
            
            // Show refresh indicator if there's an ongoing refresh
            if (isLoading && readings.isNotEmpty()) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                )
            }
        }
    }
}

@Composable
fun GradientCircularProgressBar(
    value: Float,
    minValue: Float,
    maxValue: Float,
    normalRange: ClosedFloatingPointRange<Float>,
    unit: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        val progress = ((value - minValue) / (maxValue - minValue)).coerceIn(0f, 1f)
        val color = when {
            value < normalRange.start -> Color(0xFF4CAF50)
            value > normalRange.endInclusive -> Color(0xFFF44336)
            else -> Color(0xFFFFEB3B)
        }
        CircularProgressIndicator(
            progress = { progress },
            strokeWidth = 18.dp,
            color = color,
            trackColor = Color.LightGray,
            modifier = Modifier.fillMaxSize()
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = value.toString(),
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = unit,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun ReadingTable(readings: List<TimedSensorReading>) {
    val maxRows = 5
    val columns = if (readings.isEmpty()) 1 else (readings.size + maxRows - 1) / maxRows
    val tableData = List(columns) { col ->
        readings.drop(col * maxRows).take(maxRows)
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large),
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Column(Modifier.padding(vertical = 12.dp, horizontal = 0.dp)) {
            // Header
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                    .padding(vertical = 10.dp, horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (col in 0 until columns) {
                    Row(Modifier.weight(1f), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(
                            "Reading",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp),
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            "Time",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
            Spacer(Modifier.height(4.dp))
            for (row in 0 until maxRows) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (col in 0 until columns) {
                        val reading = tableData.getOrNull(col)?.getOrNull(row)
                        if (reading != null) {
                            Box(
                                modifier = Modifier
                                    .background(
                                        color = if ((row + col) % 2 == 0) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                        shape = CircleShape
                                    )
                                    .padding(horizontal = 14.dp, vertical = 6.dp)
                                    .weight(1f)
                            ) {
                                Text(
                                    text = reading.value.toString(),
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = reading.time,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                                modifier = Modifier.weight(1f)
                            )
                        } else {
                            Spacer(Modifier.weight(2f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun VicoSensorLineChart(readings: List<TimedSensorReading>, modifier: Modifier = Modifier) {
    if (readings.size < 2) return

    val modelProducer = remember { CartesianChartModelProducer() }
    LaunchedEffect(readings) {
        modelProducer.runTransaction {
            lineSeries {
                series(*readings.map { it.value }.toTypedArray())
            }
        }
    }

    CartesianChartHost(
        chart = rememberCartesianChart(
            rememberLineCartesianLayer(),
            startAxis = VerticalAxis.rememberStart(),
            bottomAxis = HorizontalAxis.rememberBottom(),
        ),
        modelProducer = modelProducer,
        modifier = modifier
            .fillMaxWidth()
            .height(160.dp)
    )
}

@Composable
fun NoDataView() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "No data available",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

@Composable
fun ErrorView(
    errorMessage: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Error,
            contentDescription = "Error",
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = errorMessage,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(
                imageVector = Icons.Filled.Refresh,
                contentDescription = "Retry"
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Retry")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SensorScreenPreview() {
    // For preview, don't use the actual ViewModel
    // Instead render just the UI components directly
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = "pH",
            style = MaterialTheme.typography.titleLarge.copy(fontSize = 32.sp),
            fontWeight = FontWeight.Bold,
            fontSize = 32.sp,
            modifier = Modifier
                .padding(bottom = 20.dp)
                .align(Alignment.Start)
        )
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            GradientCircularProgressBar(
                value = 7.8f,
                minValue = 0f,
                maxValue = 14f,
                normalRange = 6.5f..8.5f,
                unit = "",
                modifier = Modifier.size(160.dp)
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        HorizontalDivider(
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
            thickness = 2.dp,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        Text(
            text = "Recent Readings",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        // Sample readings for preview
        val previewReadings = listOf(
            TimedSensorReading(7.5f, "10:00"),
            TimedSensorReading(7.8f, "10:05")
        )
        ReadingTable(previewReadings)
    }
} 