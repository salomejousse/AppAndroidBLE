package fr.isen.jousse.androidsmartdevice

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.isen.jousse.androidsmartdevice.ui.theme.AndroidSmartDeviceTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AndroidSmartDeviceTheme {
                val context = LocalContext.current
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        Button(
                            onClick = {
                                context.startActivity(Intent(context, ScanActivity::class.java))
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(text = "COMMENCER")
                        }
                    }
                ) { innerPadding ->
                    MainContentComponent(innerPadding)
                }
            }
        }
    }
}

@Composable
fun MainContentComponent(innerPadding: PaddingValues) {
    Column (
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ){

        Text(
            text = "Bienvenue sur AndoidSmartDevice",
            fontSize = 22.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = "Pour démarrer le scan des devices BLE, cliquer sur le bouton du dessous",
            fontSize = 16.sp,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        Image(
            painter = painterResource(id = R.drawable.bluetooth_image),
            contentDescription = "Bluetooth Image",
            modifier = Modifier.size(100.dp)
        )
        //Button(
            //onClick = {},
            //modifier = Modifier
                //.padding(top = 24.dp)
                //.clip(RoundedCornerShape(50))
                //.fillMaxWidth(0.7f)
        //)
        // {
            //Text(text = "COMMENCER")
        //}
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    AndroidSmartDeviceTheme {
        MainContentComponent(innerPadding = PaddingValues(0.dp))
    }
}

