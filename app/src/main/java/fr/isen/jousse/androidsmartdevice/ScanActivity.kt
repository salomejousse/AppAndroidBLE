package fr.isen.jousse.androidsmartdevice

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
        val allPermissionsGranted = permissions.entries.all { it.value }
        if (allPermissionsGranted) {
            Toast.makeText(this, "Permissions Bluetooth accordées", Toast.LENGTH_SHORT).show()
            if (bluetoothAdapter.isEnabled) {
                startScan()
            } else {
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(enableBtIntent, PERMISSION_REQUEST_CODE)
            }
        } else {
            Toast.makeText(this, "Permissions Bluetooth requises", Toast.LENGTH_SHORT).show()
        }
    }


    private var isBluetoothEnabled by mutableStateOf(false)
    private var showBluetoothDialog by mutableStateOf(false)
    private var isScanning by mutableStateOf(false)

    @SuppressLint("MissingPermission")
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter

        //if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
            //showBluetoothDialog = true
        //} else {
            //isBluetoothEnabled = true
        //}

        if (bluetoothAdapter != null && !bluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, PERMISSION_REQUEST_CODE)
        } else {
            isBluetoothEnabled = bluetoothAdapter.isEnabled == true
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
        if (permissions.isNotEmpty()) {
            requestPermissionLauncher.launch(permissions.toTypedArray())
        } else if(!bluetoothAdapter.isEnabled) {
            val enableBIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBIntent, PERMISSION_REQUEST_CODE)
        } else {
            Toast.makeText(this, "Toutes les permissions sont déjà accordées", Toast.LENGTH_SHORT).show()
            startScan()
        }
    }

    private fun getRequiredPermissions(): List<String> {
        return when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                listOf(
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_ADMIN,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                listOf(
                    Manifest.permission.BLUETOOTH_ADMIN,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                )
            }
            else -> {
                listOf(
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_ADMIN,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            }
        }
    }

    companion object {
        const val PERMISSION_REQUEST_CODE = 1
    }

    //override fun onRequestPermissionsResult(
        //requestCode: Int,
        //permissions: Array<out String>,
        //grantResults: IntArray
    //) {
        //super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        //if (requestCode == PERMISSION_REQUEST_CODE) {
            //if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                //Toast.makeText(this, "Toutes les permissions sont accordées", Toast.LENGTH_SHORT).show()
            //} else {
                //Toast.makeText(this, "Permissions Bluetooth manquantes", Toast.LENGTH_SHORT).show()
            //}
        //}
    //}

    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                if (bluetoothAdapter.isEnabled) {
                    startScan()
                } else {
                    val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                    startActivityForResult(enableBtIntent, PERMISSION_REQUEST_CODE)
                }
            } else {
                Toast.makeText(this, "Permissions Bluetooth manquantes", Toast.LENGTH_SHORT).show()
            }
        }
    }


    @SuppressLint("MissingPermission")
    private fun startScan() {
        bluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner
        devicesList.clear()
        bluetoothLeScanner.startScan(scanCallback)
        Log.d("BluetoothScan", "Scan démarré")
    }

    @SuppressLint("MissingPermission")
    private fun stopScan() {
        bluetoothLeScanner.stopScan(scanCallback)
        Log.d("BluetoothScan", "Scan arrêté")
    }

    @SuppressLint("MissingPermission")
    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            // Vérifier si le scan est en cours
            if (!isScanning) return

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
        val context = LocalContext.current
        Button(
            onClick = {
                val permissions = getRequiredPermissions()
                val permissionGranted = permissions.all {
                    ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
                }
                if (permissionGranted){
                    onClick()
                } else {
                    Toast.makeText(context, "Veuillez accorder les permissions Bluetooth", Toast.LENGTH_SHORT).show()
                }
            },
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
                DeviceItem(device = device.device)
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

    @SuppressLint("MissingPermission")
    @Composable
    fun DeviceItem(device: BluetoothDevice) {
        val context = LocalContext.current
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(80.dp)
                .clickable {
                    // Création d'un Intent pour lancer l'activité DeviceInteractionActivity
                    val intent = Intent(context, ConnexionActivity::class.java).apply {
                        putExtra("DEVICE", device)
                    }
                    stopScan()
                    context.startActivity(intent)
                },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = device.name ?: "Inconnu",
                color = Color.DarkGray,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Text(
                text = device.address,
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
