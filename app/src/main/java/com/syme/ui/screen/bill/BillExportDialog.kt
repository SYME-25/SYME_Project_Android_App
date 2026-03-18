package com.syme.ui.screen.bill

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
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
    val isEmailValid = Patterns.EMAIL_ADDRESS.matcher(email).matches()
    val emailError = if (emailTouched && !isEmailValid) stringResource(R.string.error_invalid_email) else ""

    val traceSteps = bill.metadata?.trace ?: emptyList()
    val tariff = (bill.metadata?.tariffSnapshot as? Map<String, Any>)?.toTariffConfig()
    val energy = (bill.energyWh / 1000).roundToInt()
    val peak = (bill.peakPowerW / 1000).roundToInt()
    val duration = bill.hours.roundToInt()
    val total = bill.amountToPay.roundToInt()

    // Color palette matching the PDF
    val colorPrimary = Color(0xFF1A237E)
    val colorAccent = Color(0xFF3949AB)
    val colorLightBg = Color(0xFFF5F7FF)
    val colorRowAlt = Color(0xFFECEFFE)
    val colorTextGray = Color(0xFF5C6BC0)

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        confirmButton = {
            Button(
                onClick = { onExportClick(email) },
                enabled = isEmailValid,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorPrimary,
                    disabledContainerColor = colorPrimary.copy(alpha = 0.35f)
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
                // ── HEADER CARD ───────────────────────────────────────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(colorPrimary, colorAccent)
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
                                .background(Color.White.copy(alpha = 0.15f)),
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
                            color = Color.White
                        )
                        Spacer(Modifier.height(4.dp))
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color.White.copy(alpha = 0.18f)
                        ) {
                            Text(
                                text = "#${bill.billId}",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.9f),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // ── IDENTIFICATION BLOCK ──────────────────────────────────
                BillSectionTitle(stringResource(R.string.bill_identification), colorPrimary)
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = colorLightBg
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        IdRow(label = stringResource(R.string.label_bill_id),         value = bill.billId,          colorAccent)
                        IdRow(label = stringResource(R.string.label_installation_id), value = bill.installationId,  colorAccent)
                        IdRow(label = stringResource(R.string.label_owner_id),        value = bill.ownerId,         colorAccent)
                    }
                }

                Spacer(Modifier.height(16.dp))

                // ── CONSUMPTION CHIPS ─────────────────────────────────────
                BillSectionTitle(stringResource(R.string.bill_consumption), colorPrimary)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ConsumptionChip(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Bolt,
                        label = "Energy",
                        value = "$energy kWh",
                        accentColor = Color(0xFF1565C0),
                        bgColor = Color(0xFFE3F2FD)
                    )
                    ConsumptionChip(
                        modifier = Modifier.weight(1f),
                        icon = Icons.AutoMirrored.Filled.TrendingUp,
                        label = "Peak",
                        value = "$peak kW",
                        accentColor = Color(0xFF6A1B9A),
                        bgColor = Color(0xFFF3E5F5)
                    )
                    ConsumptionChip(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Timer,
                        label = "Duration",
                        value = "$duration h",
                        accentColor = Color(0xFF00695C),
                        bgColor = Color(0xFFE0F2F1)
                    )
                }

                Spacer(Modifier.height(16.dp))

                // ── TARIFF SECTION ────────────────────────────────────────
                if (tariff != null) {
                    BillSectionTitle(stringResource(R.string.bill_tariff_section), colorPrimary)
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = colorLightBg
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            TariffRow(stringResource(R.string.label_price_per_kwh),   "${tariff.pricePerKwh.roundToInt()} ${bill.currency}/kWh", false, colorRowAlt)
                            TariffRow(stringResource(R.string.label_penalty_per_kwh), "${tariff.penaltyPricePerKwh.roundToInt()} ${bill.currency}/kWh", true, colorRowAlt)
                            TariffRow(stringResource(R.string.label_vat_rate),        "${(tariff.vatRate * 100).roundToInt()}%", false, colorRowAlt)
                            TariffRow(stringResource(R.string.label_other_taxes),     "${(tariff.otherTaxesRate * 100).roundToInt()}%", true, colorRowAlt)
                            TariffRow(stringResource(R.string.label_bonus_rate),      "−${(tariff.bonusRate * 100).roundToInt()}%", false, colorRowAlt)
                            TariffRow(stringResource(R.string.label_social_discount), "−${(tariff.socialDiscountRate * 100).roundToInt()}%", true, colorRowAlt)
                            TariffRow(stringResource(R.string.label_network_factor),  "× ${tariff.networkBalancingFactor}", false, colorRowAlt)
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }

                // ── TRACE SECTION ─────────────────────────────────────────
                BillSectionTitle(stringResource(R.string.bill_calculation_trace), colorPrimary)
                if (traceSteps.isEmpty()) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = colorLightBg
                    ) {
                        Text(
                            text = stringResource(R.string.bill_no_trace_available),
                            color = colorTextGray,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                } else {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = colorLightBg
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            traceSteps.forEachIndexed { index, step ->
                                Row(verticalAlignment = Alignment.Top) {
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clip(CircleShape)
                                            .background(colorAccent),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "${index + 1}",
                                            color = Color.White,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(Modifier.width(8.dp))
                                    Text(text = step, fontSize = 13.sp, modifier = Modifier.padding(top = 2.dp))
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // ── TOTAL BLOCK ───────────────────────────────────────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Brush.horizontalGradient(listOf(colorPrimary, colorAccent)))
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = stringResource(R.string.label_total_due).uppercase(),
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                letterSpacing = 1.sp
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = "$total ${bill.currency}",
                                color = Color.White,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                        Icon(
                            Icons.Default.Payments,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.4f),
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))

                // ── EMAIL EXPORT ──────────────────────────────────────────
                HorizontalDivider(color = Color(0xFFC5CAE9))
                Spacer(Modifier.height(14.dp))
                Text(
                    text = stringResource(R.string.bill_export_indication),
                    fontSize = 13.sp,
                    color = colorTextGray,
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

// ── Sub-components ────────────────────────────────────────────────────────────

@Composable
private fun IdRow(label: String, value: String, accentColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 11.sp, color = accentColor, fontWeight = FontWeight.Medium)
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun ConsumptionChip(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    value: String,
    accentColor: Color,
    bgColor: Color
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = bgColor
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(20.dp))
            Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = accentColor)
            Text(text = label, fontSize = 10.sp, color = accentColor.copy(alpha = 0.7f))
        }
    }
}

@Composable
private fun TariffRow(label: String, value: String, alt: Boolean, altColor: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (alt) Modifier.background(altColor, RoundedCornerShape(6.dp)) else Modifier)
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, fontSize = 12.sp, color = Color(0xFF5C6BC0))
            Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1A1A2E))
        }
    }
}

@Composable
fun BillSectionTitle(text: String, color: Color = Color(0xFF1A237E)) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(18.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(color)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = text,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = color
        )
    }
}

@Composable
fun BillInfoRow(icon: ImageVector, text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFF3949AB),
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(10.dp))
        Text(text = text, fontSize = 15.sp)
    }
}