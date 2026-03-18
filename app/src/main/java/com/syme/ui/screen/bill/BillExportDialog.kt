package com.syme.ui.screen.bill

import android.util.Patterns
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.syme.R
import com.syme.domain.mapper.toTariffConfig
import com.syme.domain.model.Bill
import com.syme.ui.component.field.EmailField
import kotlin.math.roundToInt

@Composable
fun BillExportDialog(
    bill: Bill,
    onDismiss: () -> Unit,
    onExportClick: (String) -> Unit
) {

    var email by remember { mutableStateOf("") }
    var emailTouched by remember { mutableStateOf(false) }

    val isEmailValid =
        Patterns.EMAIL_ADDRESS.matcher(email).matches()

    val emailError =
        if (emailTouched && !isEmailValid)
            stringResource(R.string.error_invalid_email)
        else ""

    val traceSteps =
        bill.metadata?.trace ?: emptyList()

    val tariff =
        (bill.metadata?.tariffSnapshot as? Map<String, Any>)
            ?.toTariffConfig()

    val energy =
        (bill.energyWh / 1000).roundToInt()

    val peak =
        (bill.peakPowerW / 1000).roundToInt()

    val duration =
        bill.hours.roundToInt()

    val total =
        bill.amountToPay.roundToInt()

    AlertDialog(

        onDismissRequest = onDismiss,

        confirmButton = {

            Button(
                onClick = { onExportClick(email) },
                enabled = isEmailValid
            ) {
                Text(stringResource(R.string.export_pdf))
            }
        },

        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        },

        title = {
            Text(
                text = stringResource(R.string.bill_details),
                fontWeight = FontWeight.Bold
            )
        },

        text = {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 520.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // IMAGE EN HAUT
                Image(
                    painter = painterResource(R.drawable.rapport_financier),
                    contentDescription = null,
                    modifier = Modifier
                        .size(120.dp)
                        .padding(bottom = 12.dp)
                )

                HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)

                Spacer(Modifier.height(12.dp))

                // IDENTIFICATION
                BillSectionTitle(
                    stringResource(R.string.bill_identification)
                )

                Text(stringResource(R.string.bill_id, bill.billId))
                Text(stringResource(R.string.bill_installation_id, bill.installationId))
                Text(stringResource(R.string.bill_owner_id, bill.ownerId))

                Spacer(Modifier.height(14.dp))

                // CONSUMPTION
                BillSectionTitle(
                    stringResource(R.string.bill_consumption)
                )

                BillInfoRow(
                    Icons.Default.Bolt,
                    stringResource(R.string.bill_energy_value, energy)
                )

                BillInfoRow(
                    Icons.AutoMirrored.Filled.TrendingUp,
                    stringResource(R.string.bill_peak_power_value, peak)
                )

                BillInfoRow(
                    Icons.Default.Timer,
                    stringResource(R.string.bill_duration_value, duration)
                )

                Spacer(Modifier.height(10.dp))

                BillInfoRow(
                    Icons.Default.Payments,
                    stringResource(
                        R.string.bill_total_value,
                        total,
                        bill.currency
                    )
                )

                // TARIFF
                if (tariff != null) {

                    Spacer(Modifier.height(16.dp))

                    BillSectionTitle(
                        stringResource(R.string.bill_tariff_section)
                    )

                    BillInfoRow(
                        Icons.Default.Payments,
                        stringResource(
                            R.string.bill_price_per_kwh,
                            tariff.pricePerKwh.roundToInt(),
                            bill.currency
                        )
                    )

                    BillInfoRow(
                        Icons.Default.Payments,
                        stringResource(
                            R.string.bill_penalty_price_per_kwh,
                            tariff.penaltyPricePerKwh.roundToInt(),
                            bill.currency
                        )
                    )

                    BillInfoRow(
                        Icons.Default.Percent,
                        stringResource(
                            R.string.bill_vat_rate,
                            (tariff.vatRate * 100).roundToInt()
                        )
                    )

                    BillInfoRow(
                        Icons.Default.Percent,
                        stringResource(
                            R.string.bill_other_taxes_rate,
                            (tariff.otherTaxesRate * 100).roundToInt()
                        )
                    )
                }

                Spacer(Modifier.height(16.dp))

                // TRACE
                BillSectionTitle(
                    stringResource(R.string.bill_calculation_trace)
                )

                if (traceSteps.isEmpty()) {

                    Text(
                        text = stringResource(
                            R.string.bill_no_trace_available
                        ),
                        color = MaterialTheme
                            .colorScheme
                            .onSurface
                            .copy(alpha = 0.6f)
                    )

                } else {

                    traceSteps.forEach {

                        Row(
                            modifier = Modifier.padding(vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Icon(
                                Icons.Default.Info,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )

                            Spacer(Modifier.width(6.dp))

                            Text(
                                text = it,
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)

                Spacer(Modifier.height(12.dp))

                Text(
                    text = stringResource(R.string.bill_export_indication),
                    fontSize = 14.sp
                )

                Spacer(Modifier.height(12.dp))

                // EMAIL
                EmailField(
                    value = email,
                    onValueChange = {
                        email = it
                        emailTouched = true
                    },
                    label = stringResource(R.string.enter_email),
                    error = emailError
                )

                Spacer(Modifier.height(6.dp))
            }
        }
    )
}

@Composable
fun BillSectionTitle(text: String) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {

        Text(
            text = text,
            fontWeight = FontWeight.Bold,
            fontSize = 17.sp,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(Modifier.height(4.dp))

        HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)
    }
}

@Composable
fun BillInfoRow(
    icon: ImageVector,
    text: String
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )

        Spacer(Modifier.width(10.dp))

        Text(
            text = text,
            fontSize = 15.sp
        )
    }
}