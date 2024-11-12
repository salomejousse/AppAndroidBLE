package fr.isen.jousse.androidsmartdevice

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.InfiniteTransition
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
//import androidx.activity.enableEdgeToEdge
//import androidx.compose.foundation.Image
//import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
//import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.focusModifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
//import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.isen.jousse.androidsmartdevice.ui.theme.AndroidSmartDeviceTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection

class ScanActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AndroidSmartDeviceTheme {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text("AndroidSmartDevice") },
                            colors = TopAppBarDefaults.smallTopAppBarColors(
                                containerColor = Color.Blue,
                                titleContentColor = Color.White
                            )
                        )
                    }
                ) { innerPadding ->
                    MainContentComponent(Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun MainContentComponent(modifier: Modifier = Modifier) {
    var isPressed by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(top = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (isPressed) {
            TriangleButtonClicked(onClick = { isPressed = !isPressed })
        } else {
            TriangleButtonNotClicked(onClick = { isPressed = !isPressed })
        }
    }
}
@Composable
fun TriangleButtonNotClicked(onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .clickable { onClick() }
            .padding(16.dp)
            .fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Lancer le Scan BLE",
                color = Color.Gray,
                fontSize = 14.sp,
                modifier = Modifier.padding(end = 8.dp)
            )
            Image(
                painter = painterResource(id = R.drawable.play),
                contentDescription = "Play",
                modifier = Modifier.size(60.dp)
            )
        }
        Spacer(
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .height(2.dp)
                .background(Color.Blue)
                .align(Alignment.Start)
                .padding(top = 4.dp)
        )
    }
}

@Composable
fun TriangleButtonClicked(onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .clickable { onClick() }
            .padding(16.dp)
            .fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Scan BLE en cours ...",
                color = Color.Gray,
                fontSize = 14.sp,
                modifier = Modifier.padding(end = 8.dp)
            )
            Image(
                painter = painterResource(id = R.drawable.pause),
                contentDescription = "Pause",
                modifier = Modifier.size(60.dp)
            )
        }
        LinearProgressIndicator(
            color = Color.Blue,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
                .height(4.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))

        DeviceList(
            devices = listOf("Device 1", "Device 2", "Device 3")
        )
    }
}

@Composable
fun DeviceList(devices: List<String>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        devices.forEachIndexed { index, device ->
            DeviceItem(deviceName = device, deviceNumber = "Numéro ${index + 1}")
            if (index < devices.size - 1) {
                Spacer(modifier = Modifier.height(8.dp))
                Divider(
                    color = Color.Gray,
                    thickness = 1.dp,
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .align(Alignment.CenterHorizontally)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun DeviceItem(deviceName: String, deviceNumber: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(36.dp)
                .background(Color.Blue, shape = CircleShape)
        ) {
            Text(
                text = deviceNumber.split(" ").last(),
                color = Color.White,
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column {
            Text(
                text = deviceName,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Text(
                text = deviceNumber,
                fontSize = 14.sp,
                color = Color.Gray
            )
        }
    }
}
