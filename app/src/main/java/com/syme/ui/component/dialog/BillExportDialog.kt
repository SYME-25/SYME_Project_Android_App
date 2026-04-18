package com.syme.ui.component.dialog

import android.util.Patterns
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.graphics.Color
import com.syme.R
import com.syme.domain.mapper.toTariffConfig
import com.syme.domain.model.Bill
import com.syme.ui.component.field.EmailField
import com.syme.ui.theme.Accent200
import com.syme.ui.theme.Accent400
import com.syme.ui.theme.SemanticError500
import com.syme.ui.theme.SemanticSuccess500
import kotlin.math.roundToInt

@Composable
fun BillExportDialog(
    bill: Bill,
    onDismiss: () -> Unit,
    onExportClick: (String) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var emailTouched by remember { mutableStateOf(false) }
    val isEmailValid = Patterns.EMAIL_ADDRESS.matcher(email).matches()
    val emailError =
        if (emailTouched && !isEmailValid) stringResource(R.string.error_invalid_email) else ""

    val traceSteps = bill.metadata?.trace ?: emptyList()
    val tariff = (bill.metadata?.tariffSnapshot as? Map<String, Any>)?.toTariffConfig()
    val energy = (bill.energyWh / 1000).roundToInt()
    val peak = (bill.peakPowerW / 1000).roundToInt()
    val duration = bill.hours.roundToInt()
    val total = bill.amountToPay.roundToInt()

    val colorScheme = MaterialTheme.colorScheme

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        containerColor = colorScheme.surface,
        confirmButton = {
            Button(
                onClick = { onExportClick(email) },
                enabled = isEmailValid,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorScheme.primary,
                    disabledContainerColor = colorScheme.primary.copy(alpha = 0.35f)
                ),
                modifier = Modifier.height(44.dp)
            ) {
                Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(stringResource(R.string.export_pdf), fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.height(44.dp)
            ) {
                Text(stringResource(R.string.cancel))
            }
        },
        title = null,
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 560.dp)
                    .verticalScroll(rememberScrollState())
            ) {

                // HEADER
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(colorScheme.primary, colorScheme.secondary)
                            )
                        )
                        .padding(vertical = 20.dp, horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {

                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(colorScheme.onPrimary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(R.drawable.rapport_financier),
                                contentDescription = null,
                                modifier = Modifier.size(48.dp)
                            )
                        }

                        Spacer(Modifier.height(10.dp))

                        Text(
                            text = stringResource(R.string.bill_details),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = colorScheme.onPrimary
                        )

                        Spacer(Modifier.height(4.dp))

                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = colorScheme.onPrimary.copy(alpha = 0.18f)
                        ) {
                            Text(
                                text = "#${bill.billId}",
                                fontSize = 11.sp,
                                color = colorScheme.onPrimary.copy(alpha = 0.9f),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                BillSectionTitle(stringResource(R.string.bill_identification))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = colorScheme.surfaceVariant
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IdRow(stringResource(R.string.label_bill_id), bill.billId)
                        IdRow(stringResource(R.string.label_installation_id), bill.installationId)
                        IdRow(stringResource(R.string.label_owner_id), bill.ownerId)
                    }
                }

                Spacer(Modifier.height(16.dp))

                BillSectionTitle(stringResource(R.string.bill_consumption))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 1. Énergie (Bleu Brand)
                    ConsumptionChip(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Bolt,
                        label = stringResource(R.string.energy),
                        value = "$energy ${stringResource(R.string.unit_kwh)}",
                        containerColor = colorScheme.primaryContainer,
                        contentColor = colorScheme.onPrimaryContainer
                    )

                    // 2. Crête (Orange/Warning)
                    ConsumptionChip(
                        modifier = Modifier.weight(1f),
                        icon = Icons.AutoMirrored.Filled.TrendingUp,
                        label = stringResource(R.string.peak),
                        value = "$peak ${stringResource(R.string.unit_kw)}",
                        containerColor = colorScheme.tertiaryContainer,
                        contentColor = colorScheme.onTertiaryContainer
                    )

                    // 3. Durée (Teal Accent)
                    ConsumptionChip(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Timer,
                        label = stringResource(R.string.duration),
                        value = "$duration ${stringResource(R.string.unit_hour)}",
                        containerColor = colorScheme.secondaryContainer,
                        contentColor = colorScheme.onSecondaryContainer
                    )
                }

                Spacer(Modifier.height(16.dp))

                if (tariff != null) {
                    BillSectionTitle(stringResource(R.string.bill_tariff_section))

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = colorScheme.surfaceVariant
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            TariffRow(stringResource(R.string.label_price_per_kwh),
                                "${tariff.pricePerKwh.roundToInt()} ${bill.currency}/${stringResource(R.string.unit_kwh)}")

                            TariffRow(stringResource(R.string.label_penalty_per_kwh),
                                "${tariff.penaltyPricePerKwh.roundToInt()} ${bill.currency}/${stringResource(R.string.unit_kwh)}")

                            TariffRow(stringResource(R.string.label_vat_rate),
                                "${(tariff.vatRate * 100).roundToInt()}%")

                            TariffRow(stringResource(R.string.label_other_taxes),
                                "${(tariff.otherTaxesRate * 100).roundToInt()}%")

                            TariffRow(stringResource(R.string.label_bonus_rate),
                                "−${(tariff.bonusRate * 100).roundToInt()}%")

                            TariffRow(stringResource(R.string.label_social_discount),
                                "−${(tariff.socialDiscountRate * 100).roundToInt()}%")

                            TariffRow(stringResource(R.string.label_network_factor),
                                "× ${tariff.networkBalancingFactor}")
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                }

                BillSectionTitle(stringResource(R.string.bill_calculation_trace))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = colorScheme.surfaceVariant
                ) {
                    if (traceSteps.isEmpty()) {
                        Text(
                            text = stringResource(R.string.bill_no_trace_available),
                            color = colorScheme.onSurfaceVariant,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(12.dp)
                        )
                    } else {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            traceSteps.forEachIndexed { index, step ->
                                Row(verticalAlignment = Alignment.Top) {

                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clip(CircleShape)
                                            .background(colorScheme.secondary),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "${index + 1}",
                                            color = colorScheme.onSecondary,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Spacer(Modifier.width(8.dp))

                                    Text(
                                        text = step,
                                        fontSize = 13.sp,
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(colorScheme.primary, colorScheme.secondary)
                            )
                        )
                        .padding(horizontal = 20.dp, vertical = 14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = stringResource(R.string.label_total_due).uppercase(),
                                color = colorScheme.onPrimary.copy(alpha = 0.7f),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = "$total ${bill.currency}",
                                color = colorScheme.onPrimary,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }

                        Icon(
                            Icons.Default.Payments,
                            contentDescription = null,
                            // Utiliser l'accent pour faire briller l'icône de paiement
                            tint = if (isSystemInDarkTheme()) Accent400 else Accent200,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))

                HorizontalDivider(color = colorScheme.outlineVariant)

                Spacer(Modifier.height(14.dp))

                Text(
                    text = stringResource(R.string.bill_export_indication),
                    fontSize = 13.sp,
                    color = colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(10.dp))

                EmailField(
                    value = email,
                    onValueChange = { email = it; emailTouched = true },
                    label = stringResource(R.string.enter_email),
                    error = emailError
                )

                Spacer(Modifier.height(4.dp))
            }
        }
    )
}

// SUB COMPONENTS

@Composable
private fun IdRow(label: String, value: String) {
    val colorScheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Neutral 600 pour le label (discret)
        Text(label, fontSize = 11.sp, color = colorScheme.onSurfaceVariant)
        // Darker text pour la valeur
        Text(value, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = colorScheme.onSurface)
    }
}

@Composable
private fun ConsumptionChip(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    value: String,
    containerColor: Color,
    contentColor: Color
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = containerColor
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, null, tint = contentColor, modifier = Modifier.size(20.dp))
            Spacer(Modifier.height(4.dp))
            Text(
                text = value,
                fontWeight = FontWeight.Bold,
                color = contentColor,
                fontSize = 14.sp
            )
            Text(
                text = label,
                fontSize = 10.sp,
                color = contentColor.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun TariffRow(label: String, value: String, isDiscount: Boolean = false, isTax: Boolean = false) {
    val colorScheme = MaterialTheme.colorScheme
    val valueColor = when {
        isDiscount -> SemanticSuccess500 // Vert pour les bonus/remises
        isTax -> SemanticError500      // Rouge pour les taxes/pénalités
        else -> colorScheme.onSurface
    }

    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 12.sp, color = colorScheme.onSurfaceVariant)
        Text(value, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = valueColor)
    }
}
@Composable
fun BillSectionTitle(text: String) {
    val colorScheme = MaterialTheme.colorScheme

    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(18.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(colorScheme.primary)
        )
        Spacer(Modifier.width(8.dp))
        Text(text, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = colorScheme.primary)
    }
}