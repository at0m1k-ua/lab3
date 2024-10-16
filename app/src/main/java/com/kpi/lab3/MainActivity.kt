package com.kpi.lab3

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.shape.RoundedCornerShape

import org.apache.commons.math3.distribution.NormalDistribution

class MainActivity : ComponentActivity() {
    private var inputsMap by mutableStateOf(mapOf(
        "power" to "",
        "deviation" to ""
    ))

    private var calculationResult by mutableStateOf("Показники ще не обчислено")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            InputsScreen(
                inputsMap = inputsMap,
                onValueChange = { key, value ->
                    inputsMap = inputsMap.toMutableMap().apply { this[key] = value }
                },
                calculationResult = calculationResult,
                onCalculate = { calculate() }
            )
        }
    }

    private fun calculate() {
        val powerAmt = inputsMap["power"]?.toDoubleOrNull() ?: .0
        val deviationAmt = inputsMap["deviation"]?.toDoubleOrNull() ?: .0
        val cost = 7.0

        val normalDist = NormalDistribution(powerAmt, deviationAmt)
        val maxDeviation = powerAmt * 0.05

        val lowerLimit = powerAmt - maxDeviation
        val upperLimit = powerAmt + maxDeviation

        val cdfLower = normalDist.cumulativeProbability(lowerLimit)
        val cdfUpper = normalDist.cumulativeProbability(upperLimit)

        val integralValue = cdfUpper - cdfLower

        val profit = cost*24*powerAmt*integralValue
        val loss = cost*24*powerAmt*(1 - integralValue)
        val total = profit - loss

        calculationResult =
            """
                Добовий дохід електростанції: %.2f тис. грн
            """.trimIndent().format(total)
    }
}

@Composable
fun InputsScreen(
    inputsMap: Map<String, String>,
    onValueChange: (String, String) -> Unit,
    calculationResult: String,
    onCalculate: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            Input(
                label = "Середньодобова потужність",
                units = "МВт",
                value = inputsMap["power"] ?: "",
                onValueChange = { onValueChange("power", it) }
            )
            Input(
                label = "Середньоквадратичне відхилення",
                units = "МВт",
                value = inputsMap["deviation"] ?: "",
                onValueChange = { onValueChange("deviation", it) }
            )
        }

        Text(
            calculationResult,
            modifier = Modifier.padding(8.dp)
        )

        Button(
            modifier = Modifier
                .padding(8.dp)
                .height(72.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            onClick = { onCalculate() }
        ) {
            Text(
                "Calculate",
                fontSize = 24.sp
            )
        }
    }
}

@Composable
fun Input(label: String, units: String, value: String, onValueChange: (String) -> Unit) {
    val regex = Regex("^\\d*\\.?\\d*\$")

    Column (
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("$label, $units")
        OutlinedTextField(
            value = value,
            onValueChange = {
                if (it.isEmpty() || it.matches(regex)) {
                    onValueChange(it)
                }
            },
            modifier = Modifier.height(64.dp).padding(8.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true
        )
    }
}
