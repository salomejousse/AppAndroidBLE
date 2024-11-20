package fr.isen.jousse.androidsmartdevice

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat

// Définition des états possibles des LEDs
enum class LEDStateEnum(val value: Byte) {
    OFF(0x00),
    ON(0x01)
}

class ConnexionActivity : ComponentActivity() {

    private var bluetoothGatt: BluetoothGatt? = null
    private var bluetoothDevice: BluetoothDevice? = null
    private var ledCharacteristic: BluetoothGattCharacteristic? = null

    // Remplace cette constante par l'UUID réel de la caractéristique LED
    private val LED_UUID = "UUID_LED_CHARACTERISTIC"  // Exemple : "00002a56-0000-1000-8000-00805f9b34fb"

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Vérification de l'adresse Bluetooth avant de l'utiliser
        bluetoothDevice = intent.getParcelableExtra<BluetoothDevice>("DEVICE")

        setContent {
            DeviceScreen(deviceName = bluetoothDevice?.name ?: "", deviceAddress = bluetoothDevice?.address ?: "")
        }
    }

    @SuppressLint("MissingPermission")
    private fun connectToDevice() {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth non pris en charge", Toast.LENGTH_SHORT).show()
            return
        }


        if (bluetoothDevice == null) {
            Log.e("Bluetooth", "Aucun périphérique Bluetooth valide")
            return
        }

        bluetoothGatt = bluetoothDevice?.connectGatt(this, false, object : BluetoothGattCallback() {
            override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
                Log.d("BLE", "Connexion État: $status, Nouvel état: $newState")
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    Log.d("BLE", "Connecté au serveur GATT. Découverte des services...")
                    gatt.discoverServices()
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    Log.d("BLE", "Déconnecté du serveur GATT.")
                    Toast.makeText(this@ConnexionActivity, "Déconnexion réussie", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    val services = gatt.services
                    for (service in services) {
                        Log.d("BLE", "Service trouvé: ${service.uuid}")
                        // Vérifie les caractéristiques de chaque service
                        val characteristics = service.characteristics
                        for (characteristic in characteristics) {
                            Log.d("BLE", "Caractéristique trouvée: ${characteristic.uuid}")
                            // On recherche la caractéristique LED ici
                            if (characteristic.uuid.toString() == LED_UUID) {
                                ledCharacteristic = characteristic
                                break
                            }
                        }
                    }
                } else {
                    Log.e("BLE", "Échec de la découverte des services avec le statut $status")
                }
            }

            override fun onCharacteristicWrite(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    Log.d("BLE", "Caractéristique écrite avec succès: ${characteristic.uuid}")
                } else {
                    Log.e("BLE", "Échec de l'écriture sur la caractéristique: ${characteristic.uuid}")
                }
            }
        })
    }

    @SuppressLint("MissingPermission")
    private fun writeToLEDCharacteristic(state: LEDStateEnum) {
        if (ledCharacteristic != null) {
            ledCharacteristic?.value = byteArrayOf(state.value) // Conversion en ByteArray
            bluetoothGatt?.writeCharacteristic(ledCharacteristic)
            Log.d("BLE", "LED state set to: ${state.name}")
        } else {
            Log.e("BLE", "LED characteristic non trouvée.")
            Toast.makeText(this, "Caractéristique LED non trouvée", Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("MissingPermission")
    private fun disconnectFromDevice() {
        bluetoothGatt?.disconnect()
        bluetoothGatt?.close()
        bluetoothGatt = null
        Log.d("BLE", "Déconnecté du périphérique.")
        Toast.makeText(this, "Déconnecté du périphérique", Toast.LENGTH_SHORT).show()
    }

    @Composable
    fun DeviceScreen(deviceName: String?, deviceAddress: String?) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Device Info", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            Text("Name: ${deviceName ?: "Inconnu"}")
            Text("Address: ${deviceAddress ?: "Inconnu"}")
            Spacer(modifier = Modifier.height(32.dp))

            // Bouton pour se connecter au périphérique Bluetooth
            Button(
                onClick = {
                    Log.d("Bluetooth", "Tentative de connexion...")
                    connectToDevice()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Blue)
            ) {
                Text("Connecter au device", color = Color.White)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = R.drawable.led_img),
                    contentDescription = "LED ON",
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = { writeToLEDCharacteristic(LEDStateEnum.ON) },
                    modifier = Modifier.padding(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Blue)
                ) {
                    Text("Allumer LED 1", color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Bouton pour éteindre la LED avec l'image de la LED
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = R.drawable.img_led_off),
                    contentDescription = "LED OFF",
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = { writeToLEDCharacteristic(LEDStateEnum.OFF) },
                    modifier = Modifier.padding(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Blue)
                ) {
                    Text("Eteindre LED 1", color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Bouton pour se déconnecter du périphérique
            Button(
                onClick = { disconnectFromDevice() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Blue)
            ) {
                Text("Deconnecter le device", color = Color.White)
            }
        }
    }

    @SuppressLint("MissingPermission")
    override fun onDestroy() {
        super.onDestroy()
        disconnectFromDevice()
    }
}
