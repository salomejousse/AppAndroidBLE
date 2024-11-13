package fr.isen.jousse.androidsmartdevice

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.isen.jousse.androidsmartdevice.ui.theme.AndroidSmartDeviceTheme

class ConnexionActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val deviceAddress = intent.getStringExtra("DEVICE_ADDRESS") ?: "Adresse inconnue"

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
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Adresse :",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        Text(
                            text = deviceAddress,
                            fontSize = 16.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(bottom = 24.dp)
                        )

                        Divider(color = Color.LightGray, thickness = 1.dp)

                        Spacer(modifier = Modifier.height(16.dp))

                        Image(
                            painter = painterResource(id = R.drawable.led_img),
                            contentDescription = "Led Image",
                            modifier = Modifier.size(100.dp)
                        )

                        LedControlButton("Allumer LEDs") {
                            Toast.makeText(this@ConnexionActivity, "Commande LEDs envoyée", Toast.LENGTH_SHORT).show()
                        }

                        Divider(color = Color.LightGray, thickness = 1.dp, modifier = Modifier.padding(vertical = 16.dp))

                        ClickCounterButton(
                            label = "Consulter le nombre de clics du bouton principal",
                            onClick = {
                                // Logique pour obtenir le nombre de clics du bouton principal
                                Toast.makeText(this@ConnexionActivity, "Lecture des clics du bouton principal", Toast.LENGTH_SHORT).show()
                            }
                        )

                        Divider(color = Color.LightGray, thickness = 1.dp, modifier = Modifier.padding(vertical = 16.dp))

                        ClickCounterButton(
                            label = "Consulter le nombre de clics du troisième bouton",
                            onClick = {
                                // Logique pour obtenir le nombre de clics du troisième bouton
                                Toast.makeText(this@ConnexionActivity, "Lecture des clics du troisième bouton", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LedControlButton(label: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Blue
        )
    ) {
        Text(text = label, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun ClickCounterButton(label: String, onClick: () -> Unit) {
    var clickCount by remember { mutableStateOf(0) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
    ) {
        Button(
            onClick = {
                onClick()
                clickCount += 1 // Incrémenter le compteur à chaque clic
            },
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
        ) {
            Text(text = label, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.width(16.dp))
        Box(
            modifier = Modifier
                .size(50.dp)
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(text = clickCount.toString(), fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}
