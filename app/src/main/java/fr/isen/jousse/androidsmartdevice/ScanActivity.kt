package fr.isen.jousse.androidsmartdevice

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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

    private val uniqueMacAddresses = mutableSetOf<String>()
    private val uniqueDeviceNames = mutableSetOf<String>()

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
    private var isScanning by mutableStateOf(false)

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
                    Column(
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        // Button to start and stop scan
                        StartStopScanButton(
                            isScanning = isScanning,
                            onClick = {
                                if (isScanning) {
                                    stopScan()
                                } else {
                                    startScan()
                                }
                                isScanning = !isScanning
                            }
                        )
                        // List of devices found during scan
                        DeviceList(devicesList)
                    }
                }
            }

            // Afficher la fenêtre de dialogue si Bluetooth n'est pas activé
            if (showBluetoothDialog) {
                BluetoothEnableDialog(
                    onConfirm = {
                        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                        //startActivityForResult(enableBtIntent, 1)
                        showBluetoothDialog = false
                    },
                    onDismiss = {
                        showBluetoothDialog = false
                    }
                )
            }
        }
    }

    private fun checkAndRequestPermissions() {
        val permissions = getRequiredPermissions()
        val permissionsToRequest = permissions.filter { permission ->
            ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED
        }
        if (permissionsToRequest.isNotEmpty()) {
            requestPermissionLauncher.launch(permissionsToRequest.toTypedArray())
        } else {
            Toast.makeText(this, "Toutes les permissions sont déjà accordées", Toast.LENGTH_SHORT).show()
            startScan()
        }
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

    private fun startScan() {
        bluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner
        devicesList.clear()
        bluetoothLeScanner.startScan(scanCallback)
        Log.d("BluetoothScan", "Scan démarré")
    }

    private fun stopScan() {
        bluetoothLeScanner.stopScan(scanCallback)
        Log.d("BluetoothScan", "Scan arrêté")
    }

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val device = result.device
            val deviceAddress = device.address
            val deviceName = device.name ?: "Appareil inconnu"
            if (devicesList.none { it.device.address == result.device.address }) {
                if (uniqueMacAddresses.contains(deviceAddress) || uniqueDeviceNames.contains(deviceName)) {
                    return
                }
                uniqueMacAddresses.add(deviceAddress)
                uniqueDeviceNames.add(deviceName)
                devicesList.add(result)
                Log.d("BluetoothDevice", "Appareil détecté : ${result.device.name ?: "Inconnu"} - ${result.device.address}")
            }
        }

        override fun onScanFailed(errorCode: Int) {
            Toast.makeText(applicationContext, "Échec du scan : $errorCode", Toast.LENGTH_SHORT).show()
        }
    }

    @Composable
    fun StartStopScanButton(
        isScanning: Boolean,
        onClick: () -> Unit
    ) {
        Button(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .height(60.dp),
            shape = MaterialTheme.shapes.small.copy(CornerSize(8.dp)),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Blue
            )
        ) {
            Text(
                text = if (isScanning) "Arrêter le Scan" else "Démarrer le Scan",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }
    }

    @Composable
    fun DeviceList(devices: List<ScanResult>) {
        Column(modifier = Modifier.fillMaxWidth()) {
            devices.forEachIndexed { index, device ->
                DeviceItem(deviceName = device.device.name ?: "Appareil inconnu", deviceNumber = "Adresse: ${device.device.address}")
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
        val context = LocalContext.current
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(80.dp)
                .clickable {
                    // Création d'un Intent pour lancer l'activité DeviceInteractionActivity
                    val intent = Intent(context, ConnexionActivity::class.java).apply {
                        putExtra("DEVICE_ADDRESS", deviceNumber) // Passer l'adresse de l'appareil
                    }
                    context.startActivity(intent)
                },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = deviceName,
                color = Color.DarkGray,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Text(
                text = deviceNumber,
                color = Color.Gray,
                fontSize = 18.sp
            )
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
}
