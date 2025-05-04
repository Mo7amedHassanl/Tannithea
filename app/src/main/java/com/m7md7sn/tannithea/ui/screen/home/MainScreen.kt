package com.m7md7sn.tannithea.ui.screen.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Sensors
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.m7md7sn.tannithea.R
import com.m7md7sn.tannithea.data.model.SensorReading
import com.m7md7sn.tannithea.data.model.SystemPart
import com.m7md7sn.tannithea.ui.theme.TannitheaTheme
import androidx.hilt.navigation.compose.hiltViewModel
import com.m7md7sn.tannithea.ui.screen.monitoring.phColor

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
    onSensorCardClick: (Int) -> Unit = {},
    onSystemPartClick: (Int) -> Unit = {},
) {
    val sensorReadings by viewModel.sensorReadings.collectAsState()
    val systemParts by viewModel.systemParts.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (isLoading && sensorReadings.isEmpty()) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (error != null && sensorReadings.isEmpty()) {
                ErrorView(
                    errorMessage = error ?: "Unknown error",
                    onRetry = { viewModel.refresh() }
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    SectionHeader(title = stringResource(R.string.section_water_quality))
                    Spacer(modifier = Modifier.height(12.dp))
                    SensorGridModern(readings = sensorReadings, onSensorCardClick = onSensorCardClick)
                    Spacer(modifier = Modifier.height(32.dp))
                    SectionHeader(title = stringResource(R.string.section_system_parts))
                    Spacer(modifier = Modifier.height(12.dp))
                    SystemPartsListVertical(parts = systemParts, onSystemPartClick = onSystemPartClick)
                    Spacer(modifier = Modifier.height(24.dp))
                }
                
                // Show refresh indicator if there's an ongoing refresh
                if (isLoading && sensorReadings.isNotEmpty()) {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.TopCenter)
                    )
                }
            }
        }
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

@Composable
fun SectionHeader(title: String) {
    Column(Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
        )
        HorizontalDivider(
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
            thickness = 2.dp,
            modifier = Modifier.padding(bottom = 8.dp)
        )
    }
}

@Composable
fun SensorGridModern(
    readings: List<SensorReading>,
    onSensorCardClick: (Int) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        readings.chunked(2).forEachIndexed { rowIndex, rowItems ->
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                rowItems.forEachIndexed { colIndex, reading ->
                    SensorCardModern(
                        reading = reading,
                        modifier = Modifier.weight(1f),
                        onCardClick = { onSensorCardClick(rowIndex * 2 + colIndex) }
                    )
                }
                if (rowItems.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun SensorCardModern(
    reading: SensorReading,
    onCardClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val overlayColor = when (reading.label.lowercase()) {
        "ph" -> phColor(reading.value.toFloatOrNull() ?: 7f)
        else -> {
            val value = reading.value.toFloatOrNull() ?: 0f
            val state = when (reading.label.lowercase()) {
                "tds" -> if (value < 500f) "Normal" else "High"
                "turbidity" -> if (value < 50f) "Normal" else "High"
                "temperature" -> if (value < 20f || value > 35f) "High" else "Normal"
                else -> "Normal"
            }
            if (state == "Normal") {
                Brush.horizontalGradient(listOf(Color(0xFF4CAF50), Color(0xFF81C784)))
            } else {
                Brush.horizontalGradient(listOf(Color(0xFFF44336), Color(0xFFE57373)))
            }
        }
    }

    Card(
        modifier = modifier.height(120.dp),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest),
        onClick = onCardClick,
    ) {
        Box(
            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(20.dp))
        ) {
            Box(
                modifier = Modifier.matchParentSize().let {
                    when (overlayColor) {
                        is Color -> it.background(overlayColor.copy(alpha = 0.5f))
                        is Brush -> it.background(overlayColor, alpha = 0.5f)
                        else -> it
                    }
                }
            )
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = reading.label,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (reading.unit != null) "${reading.value} ${reading.unit}" else reading.value,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun SystemPartsListVertical(parts: List<SystemPart>, onSystemPartClick: (Int) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        parts.forEachIndexed { idx, part ->
            SystemPartLargeCard(part = part, onClick = { onSystemPartClick(idx) })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SystemPartLargeCard(part: SystemPart, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Icon(
                imageVector = part.icon,
                contentDescription = part.name,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp)
            )
            Text(
                text = part.name,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    // For preview, use sample data instead of ViewModel
    TannitheaTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                SectionHeader(title = "Water Quality")
                Spacer(modifier = Modifier.height(12.dp))
                
                // Sample readings for preview
                val previewReadings = listOf(
                    SensorReading("TDS", "12.0", "ppm", Icons.Outlined.Sensors),
                    SensorReading("pH", "7", null, Icons.Outlined.Sensors),
                    SensorReading("Turbidity", "3.5", "NTU", Icons.Outlined.Sensors),
                    SensorReading("Temperature", "18", "°C", Icons.Outlined.Bolt)
                )
                SensorGridModern(readings = previewReadings, onSensorCardClick = {})
                
                Spacer(modifier = Modifier.height(32.dp))
                SectionHeader(title = "System Parts")
                Spacer(modifier = Modifier.height(12.dp))
                
                // Sample system parts for preview
                val previewParts = listOf(
                    SystemPart("Pumps", Icons.Outlined.Sync),
                    SystemPart("Sensors", Icons.Outlined.Sensors)
                )
                SystemPartsListVertical(parts = previewParts, onSystemPartClick = {})
                
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}