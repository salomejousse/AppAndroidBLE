package fr.isen.jousse.androidsmartdevice

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import fr.isen.jousse.androidsmartdevice.ui.theme.AndroidSmartDeviceTheme

class ScanActivity : ComponentActivity() {

    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var bluetoothLeScanner: android.bluetooth.le.BluetoothLeScanner
    private val devicesList = mutableStateListOf<ScanResult>()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.all { it.value }) {
            Toast.makeText(this, "Permissions Bluetooth accordées", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Permissions Bluetooth requises", Toast.LENGTH_SHORT).show()
        }
    }

    private var isBluetoothEnabled by mutableStateOf(false)
    private var showBluetoothDialog by mutableStateOf(false)

    @SuppressLint("MissingPermission")
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter

        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
            showBluetoothDialog = true
        } else {
            isBluetoothEnabled = true
        }

        checkAndRequestPermissions()

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
                    MainContentComponent(
                        modifier = Modifier.padding(innerPadding),
                        onStartScan = { startBluetoothScan() }
                    )
                }
            }

            // Afficher la fenêtre de dialogue si Bluetooth n'est pas activé
            if (showBluetoothDialog) {
                BluetoothEnableDialog(
                    onConfirm = {
                        // Lancer l'intention pour activer le Bluetooth
                        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                        startActivityForResult(enableBtIntent, 1)
                        showBluetoothDialog = false
                    },
                    onDismiss = {
                        // Fermer la fenêtre de dialogue sans rien faire
                        showBluetoothDialog = false
                    }
                )
            }
        }
    }

    private fun checkAndRequestPermissions() {
        val permissions = getRequiredPermissions()
    // Vérifiez si toutes les permissions sont déjà accordées
        val permissionsToRequest = permissions.filter { permission ->
            ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED
        }
        if (permissionsToRequest.isNotEmpty()) {        // Si des permissions sont manquantes, les demander
            requestPermissionLauncher.launch(permissionsToRequest.toTypedArray())
        } else {        // Si toutes les permissions sont déjà accordées
            Toast.makeText(this, "Toutes les permissions sont déjà accordées", Toast.LENGTH_SHORT).show()        // Commencer le scan BLE ou toute autre action
            //startScan()
        }
    }

    private fun startBluetoothScan() {
        // Logique pour démarrer le scan Bluetooth
        Toast.makeText(this, "Démarrage du scan BLE...", Toast.LENGTH_SHORT).show()
    }

    private fun getRequiredPermissions(): List<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            listOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        } else {
            listOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
        }
    }
}

@Composable
fun BluetoothEnableDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Activer le Bluetooth") },
        text = { Text(text = "Le Bluetooth n'est pas activé. Voulez-vous l'activer ?") },
        confirmButton = {
            Button(
                onClick = onConfirm,
                modifier = Modifier.padding(8.dp)
            ) {
                Text("Oui")
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                modifier = Modifier.padding(8.dp)
            ) {
                Text("Non")
            }
        }
    )
}

@Composable
fun MainContentComponent(
    modifier: Modifier = Modifier,
    onStartScan: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(top = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (isPressed) {
            TriangleButtonClicked(onClick = { isPressed = false }, onStartScan = onStartScan)
        } else {
            TriangleButtonNotClicked(onClick = {
                isPressed = true
                onStartScan()
            })
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
                fontWeight = FontWeight.Bold,
                text = "Lancer le Scan BLE",
                color = Color.Gray,
                fontSize = 22.sp,
                modifier = Modifier.padding(end = 8.dp)
            )
            Image(
                painter = painterResource(id = R.drawable.img_play),
                contentDescription = "Play",
                modifier = Modifier.size(60.dp)
            )
        }
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .background(Color.Blue)
                .align(Alignment.Start)
                .padding(top = 8.dp)
        )
    }
}

@Composable
fun TriangleButtonClicked(onClick: () -> Unit, onStartScan: () -> Unit) {
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
                fontWeight = FontWeight.Bold,
                text = "Scan BLE en cours ...",
                color = Color.Gray,
                fontSize = 22.sp,
                modifier = Modifier.padding(end = 8.dp)
            )
            Image(
                painter = painterResource(id = R.drawable.img_pause),
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
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(80.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = deviceName,
            color = Color.Gray,
            fontSize = 18.sp
        )
        Text(
            text = deviceNumber,
            color = Color.Gray,
            fontSize = 18.sp
        )
    }
}
