package com.example.sensormonitor

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.sensormonitor.ui.theme.SensorMonitorTheme
import kotlin.math.abs

class MainActivity : ComponentActivity() {
    private lateinit var sensorManager: SensorManager
    private lateinit var locationManager: LocationManager
    private val sensorListeners = mutableListOf<SensorEventListener>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        setContent {
            SensorMonitorTheme {
                SensorApp(
                    activity = this,
                    sensorManager = sensorManager,
                    locationManager = locationManager,
                    sensorListeners = sensorListeners
                )
            }
        }
    }

    override fun onPause() {
        super.onPause()
        cleanupSensors()
    }

    private fun cleanupSensors() {
        sensorListeners.forEach { listener ->
            sensorManager.unregisterListener(listener)
        }
        sensorListeners.clear()
        try {
            locationManager.removeUpdates(locationListener)
        } catch (e: Exception) {
            // Ignore if no updates were registered
        }
    }

    private val locationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {}
    }
}

@Composable
fun SensorApp(
    activity: MainActivity,
    sensorManager: SensorManager,
    locationManager: LocationManager,
    sensorListeners: MutableList<SensorEventListener>
) {
    Surface(modifier = Modifier.fillMaxSize()) {
        SensorScreen(
            activity = activity,
            sensorManager = sensorManager,
            locationManager = locationManager,
            sensorListeners = sensorListeners
        )
    }
}

@Composable
fun SensorScreen(
    activity: MainActivity,
    sensorManager: SensorManager,
    locationManager: LocationManager,
    sensorListeners: MutableList<SensorEventListener>
) {
    val context = LocalContext.current
    var gpsText by remember { mutableStateOf("GPS: Esperando datos...") }
    var accelerometerText by remember { mutableStateOf("Acelerómetro: Esperando datos...") }
    var gyroscopeText by remember { mutableStateOf("Giroscopio: Esperando datos...") }
    var lightText by remember { mutableStateOf("Luz: Esperando datos...") }
    var proximityText by remember { mutableStateOf("Proximidad: Esperando datos...") }
    var hasGyroscope by remember { mutableStateOf(false) }

    val requestPermissionLauncher = remember {
        activity.activityResultRegistry.register(
            "location_permission",
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                setupSensors(
                    context = context,
                    sensorManager = sensorManager,
                    locationManager = locationManager,
                    listenersList = sensorListeners,
                    onGpsUpdate = { location ->
                        gpsText = buildString {
                            append("GPS:\n")
                            append("Lat: ${"%.6f".format(location.latitude)}\n")
                            append("Lon: ${"%.6f".format(location.longitude)}\n")
                            append("Precisión: ${location.accuracy.toInt()}m")
                        }
                    },
                    onAccelerometerUpdate = { x, y, z ->
                        accelerometerText = buildString {
                            append("Acelerómetro:\n")
                            append("X: ${"%.2f".format(x)} m/s²\n")
                            append("Y: ${"%.2f".format(y)} m/s²\n")
                            append("Z: ${"%.2f".format(z)} m/s²")
                        }
                    },
                    onGyroscopeUpdate = { x, y, z ->
                        gyroscopeText = buildString {
                            append("Giroscopio:\n")
                            append("X: ${"%.2f".format(x)} rad/s\n")
                            append("Y: ${"%.2f".format(y)} rad/s\n")
                            append("Z: ${"%.2f".format(z)} rad/s")
                        }
                    },
                    onLightUpdate = { value ->
                        lightText = "Luz: ${"%.2f".format(value)} lux"
                    },
                    onProximityUpdate = { value ->
                        proximityText = "Proximidad: ${"%.2f".format(value)} cm"
                    },
                    onGyroscopeAvailable = { available ->
                        hasGyroscope = available
                        if (!available) {
                            gyroscopeText = "Giroscopio no disponible"
                        }
                    }
                )
            }
        }
    }

    LaunchedEffect(Unit) {
        when {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                setupSensors(
                    context = context,
                    sensorManager = sensorManager,
                    locationManager = locationManager,
                    listenersList = sensorListeners,
                    onGpsUpdate = { location ->
                        gpsText = buildString {
                            append("GPS:\n")
                            append("Lat: ${"%.6f".format(location.latitude)}\n")
                            append("Lon: ${"%.6f".format(location.longitude)}\n")
                            append("Precisión: ${location.accuracy.toInt()}m")
                        }
                    },
                    onAccelerometerUpdate = { x, y, z ->
                        accelerometerText = buildString {
                            append("Acelerómetro:\n")
                            append("X: ${"%.2f".format(x)} m/s²\n")
                            append("Y: ${"%.2f".format(y)} m/s²\n")
                            append("Z: ${"%.2f".format(z)} m/s²")
                        }
                    },
                    onGyroscopeUpdate = { x, y, z ->
                        gyroscopeText = buildString {
                            append("Giroscopio:\n")
                            append("X: ${"%.2f".format(x)} rad/s\n")
                            append("Y: ${"%.2f".format(y)} rad/s\n")
                            append("Z: ${"%.2f".format(z)} rad/s")
                        }
                    },
                    onLightUpdate = { value ->
                        lightText = "Luz: ${"%.2f".format(value)} lux"
                    },
                    onProximityUpdate = { value ->
                        proximityText = "Proximidad: ${"%.2f".format(value)} cm"
                    },
                    onGyroscopeAvailable = { available ->
                        hasGyroscope = available
                        if (!available) {
                            gyroscopeText = "Giroscopio no disponible"
                        }
                    }
                )
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Monitor de Sensores",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        SensorDataCard(title = gpsText)
        SensorDataCard(title = accelerometerText)
        if (hasGyroscope) {
            SensorDataCard(title = gyroscopeText)
        } else {
            SensorDataCard(title = "Giroscopio no disponible en este dispositivo")
        }
        SensorDataCard(title = lightText)
        SensorDataCard(title = proximityText)
    }
}

@Composable
fun SensorDataCard(title: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    ) {
        Text(
            text = title,
            modifier = Modifier.padding(16.dp),
            fontSize = 16.sp,
            lineHeight = 24.sp
        )
    }
}

fun setupSensors(
    context: Context,
    sensorManager: SensorManager,
    locationManager: LocationManager,
    listenersList: MutableList<SensorEventListener>,
    onGpsUpdate: (Location) -> Unit,
    onAccelerometerUpdate: (x: Float, y: Float, z: Float) -> Unit,
    onGyroscopeUpdate: (x: Float, y: Float, z: Float) -> Unit,
    onLightUpdate: (value: Float) -> Unit,
    onProximityUpdate: (value: Float) -> Unit,
    onGyroscopeAvailable: (Boolean) -> Unit
) {
    // Verificar disponibilidad del giroscopio
    val hasGyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE) != null
    onGyroscopeAvailable(hasGyroscope)

    // GPS
    if (ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    ) {
        try {
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                1000L,
                1f,
                object : LocationListener {
                    override fun onLocationChanged(location: Location) {
                        onGpsUpdate(location)
                    }
                }
            )
        } catch (e: SecurityException) {
            // Log the error or show a message to the user
        }
    }

    // Acelerómetro
    sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.let { sensor ->
        val listener = object : SensorEventListener {
            private var lastUpdate = System.currentTimeMillis()

            override fun onSensorChanged(event: SensorEvent) {
                val now = System.currentTimeMillis()
                if (now - lastUpdate < 100) return
                lastUpdate = now

                onAccelerometerUpdate(event.values[0], event.values[1], event.values[2])
            }

            override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
        }
        sensorManager.registerListener(
            listener,
            sensor,
            SensorManager.SENSOR_DELAY_GAME
        )
        listenersList.add(listener)
    }

    // Giroscopio (solo si está disponible)
    if (hasGyroscope) {
        sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)?.let { sensor ->
            val listener = object : SensorEventListener {
                private var lastUpdate = System.currentTimeMillis()
                private val noiseThreshold = 0.01f

                override fun onSensorChanged(event: SensorEvent) {
                    val now = System.currentTimeMillis()
                    if (now - lastUpdate < 100) return
                    lastUpdate = now

                    val x = if (abs(event.values[0]) > noiseThreshold) event.values[0] else 0f
                    val y = if (abs(event.values[1]) > noiseThreshold) event.values[1] else 0f
                    val z = if (abs(event.values[2]) > noiseThreshold) event.values[2] else 0f

                    onGyroscopeUpdate(x, y, z)
                }

                override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
            }
            sensorManager.registerListener(
                listener,
                sensor,
                SensorManager.SENSOR_DELAY_GAME
            )
            listenersList.add(listener)
        }
    }

    // Luz
    sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)?.let { sensor ->
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                onLightUpdate(event.values[0])
            }

            override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
        }
        sensorManager.registerListener(
            listener,
            sensor,
            SensorManager.SENSOR_DELAY_NORMAL
        )
        listenersList.add(listener)
    }

    // Proximidad
    sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)?.let { sensor ->
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                onProximityUpdate(event.values[0])
            }

            override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
        }
        sensorManager.registerListener(
            listener,
            sensor,
            SensorManager.SENSOR_DELAY_NORMAL
        )
        listenersList.add(listener)
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewSensorScreen() {
    val context = LocalContext.current
    var gpsText by remember { mutableStateOf("GPS: Datos de ejemplo") }
    var accelerometerText by remember { mutableStateOf("Acelerómetro: Datos de ejemplo") }
    var gyroscopeText by remember { mutableStateOf("Giroscopio: Datos de ejemplo") }
    var lightText by remember { mutableStateOf("Luz: Datos de ejemplo") }
    var proximityText by remember { mutableStateOf("Proximidad: Datos de ejemplo") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Monitor de Sensores (Preview)", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))
        SensorDataCard(title = gpsText)
        SensorDataCard(title = accelerometerText)
        SensorDataCard(title = gyroscopeText)
        SensorDataCard(title = lightText)
        SensorDataCard(title = proximityText)
    }
}