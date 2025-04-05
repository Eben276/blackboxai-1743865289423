package com.realmoney

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.realmoney.ui.theme.RealMoneyTheme

class MainActivity : ComponentActivity() {
    private val notificationHelper by lazy { NotificationHelper(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RealMoneyTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TransactionScreen(notificationHelper)
                }
            }
        }
    }
}

@Composable
fun TransactionScreen(notificationHelper: NotificationHelper) {
    var senderName by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var currentBalance by remember { mutableStateOf("") }
    val transactionId by remember { mutableStateOf(generateTransactionId()) }

    val newBalance = remember { derivedStateOf { 
        val amt = amount.toDoubleOrNull() ?: 0.0
        val bal = currentBalance.toDoubleOrNull() ?: 0.0
        bal + amt
    } }

    val formattedAlertMessage = remember { derivedStateOf {
        "MobileMoney: Payment received for ₵${amount} from $senderName\n" +
        "Current Balance: ₵${newBalance.value}\n" +
        "Available Balance: ₵${newBalance.value}\n" +
        "Reference: 1.\n" +
        "Transaction ID: $transactionId\n" +
        "TRANSACTION FEE: 0.00"
    } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedTextField(
            value = senderName,
            onValueChange = { senderName = it },
            label = { Text("Sender Name") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = amount,
            onValueChange = { amount = it.filter { c -> c.isDigit() || c == '.' } },
            label = { Text("Amount (₵)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = currentBalance,
            onValueChange = { currentBalance = it.filter { c -> c.isDigit() || c == '.' } },
            label = { Text("Current Balance (₵)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Text(
            text = "New Balance: ₵${"%.2f".format(newBalance.value)}",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary
        )

        Button(
            onClick = {
                notificationHelper.sendTransactionAlert(formattedAlertMessage.value)
            },
            enabled = senderName.isNotBlank() && amount.isNotBlank() && currentBalance.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Generate Alert")
        }

        Card(
            modifier = Modifier.padding(top = 24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Text(
                text = formattedAlertMessage.value,
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

private fun generateTransactionId(): String {
    val currentTime = System.currentTimeMillis() % 1000000000L // 9 digits
    val randomSuffix = (0..99).random() // 2 digits
    return "${currentTime}${randomSuffix}".padStart(11, '0')
}