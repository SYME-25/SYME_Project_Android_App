package com.syme.ui.component.card

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.syme.R
import com.syme.domain.model.Bill
import com.syme.ui.component.text.TextWithBackground
import com.syme.ui.theme.SemanticError500
import com.syme.ui.theme.SemanticSuccess500
import kotlin.math.roundToInt

@Composable
fun BillCard(
    bill: Bill,
    imageModel: Any,
    compact: Boolean,
    large: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {

    val paidColor = if (bill.isPaid) SemanticSuccess500 else SemanticError500

    val paidLabel = if (bill.isPaid)
        stringResource(id = R.string.bill_paid)
    else
        stringResource(id = R.string.bill_unpaid)

    val cardHeight = when {
        large -> 180.dp
        compact -> 135.dp
        else -> 160.dp
    }

    val imageWidth = when {
        large -> 140.dp
        compact -> 80.dp
        else -> 110.dp
    }

    val titleSize = when {
        compact -> 12.sp
        large -> 18.sp
        else -> 16.sp
    }

    val amountSize = when {
        compact -> 16.sp
        large -> 24.sp
        else -> 20.sp
    }

    Card(
        modifier = modifier
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .fillMaxWidth()
            .height(cardHeight),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        onClick = onClick
    ) {

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {

            Surface(
                shape = RoundedCornerShape(14.dp),
                tonalElevation = 2.dp
            ) {
                Box(
                    modifier = Modifier
                        .width(imageWidth)
                        .fillMaxHeight()
                        .background(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = imageModel,
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(if (compact) 6.dp else 10.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(if (compact) 8.dp else 14.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {

                Column {

                    Text(
                        text = stringResource(
                            id = R.string.bill_number,
                            bill.billId
                        ),
                        fontWeight = FontWeight.Bold,
                        fontSize = titleSize,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = bill.periodLabel,
                        fontSize = if (compact) 11.sp else 13.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Column {

                    Text(
                        text = stringResource(
                            id = R.string.bill_energy,
                            (bill.energyWh / 1000).roundToInt()
                        ),
                        fontSize = if (compact) 11.sp else 13.sp
                    )

                    Text(
                        text = stringResource(
                            id = R.string.bill_hours,
                            bill.hours.roundToInt()
                        ),
                        fontSize = if (compact) 11.sp else 13.sp
                    )

                    if (!compact) {
                        Text(
                            text = stringResource(
                                id = R.string.bill_peak,
                                (bill.peakPowerW / 1000).roundToInt()
                            ),
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(
                modifier = Modifier.fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.End
            ) {

                Text(
                    text = stringResource(
                        id = R.string.bill_amount,
                        bill.amountToPay.roundToInt(),
                        bill.currency
                    ),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = amountSize,
                    color = MaterialTheme.colorScheme.primary
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .size(
                            width = if (compact) 70.dp else 100.dp,
                            height = 28.dp
                        )
                ) {
                    TextWithBackground(
                        text = paidLabel,
                        color = paidColor
                    )
                }
            }
        }
    }
}

@Composable
fun BillCardAdaptive(
    bill: Bill,
    onBillClick: (Bill) -> Unit
) {
    SubcomposeLayout { constraints ->
        val widthDp = constraints.maxWidth / density
        val compact = widthDp < 360f
        val large = widthDp > 600f

        val measurables = subcompose(Unit) {
            BillCard(
                bill = bill,
                imageModel = R.drawable.rapport_financier,
                compact = compact,
                large = large,
                onClick = { onBillClick(bill) }
            )
        }

        val placeables = measurables.map { it.measure(constraints) }
        layout(
            width = placeables.maxOfOrNull { it.width } ?: constraints.maxWidth,
            height = placeables.maxOfOrNull { it.height } ?: 0
        ) {
            placeables.forEach { it.placeRelative(0, 0) }
        }
    }
}
@Preview(showBackground = true, widthDp = 420, heightDp = 700)
@Composable
fun BillGridPreview() {

    val fakeBills = listOf(
        Bill("F001", periodLabel = "Jan-Fev 2026", energyWh = 120000.0, hours = 210.0, peakPowerW = 800.0, amountToPay = 5400.0, currency = "FCFA", isPaid = true),
        Bill("F002", periodLabel = "Mars-Avr 2026", energyWh = 300000.0, hours = 150.0, peakPowerW = 9200.0, amountToPay = 15000.0, currency = "FCFA", isPaid = false),
        Bill("F003", periodLabel = "Mai-Juin 2026", energyWh = 520000.0, hours = 95.0, peakPowerW = 12000.0, amountToPay = 28100.0, currency = "FCFA", isPaid = false),
        Bill("F004", periodLabel = "Juil-Août 2026", energyWh = 210000.0, hours = 180.0, peakPowerW = 3000.0, amountToPay = 9800.0, currency = "FCFA", isPaid = true),
        Bill("F005", periodLabel = "Sept-Oct 2026", energyWh = 410000.0, hours = 110.0, peakPowerW = 10000.0, amountToPay = 22000.0, currency = "FCFA", isPaid = false),
    )

    for (bill in fakeBills) {
        BillCardAdaptive(bill = bill, onBillClick = {})
    }
}