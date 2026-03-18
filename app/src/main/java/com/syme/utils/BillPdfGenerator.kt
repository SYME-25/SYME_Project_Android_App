package com.syme.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import com.syme.R
import com.syme.domain.mapper.toTariffConfig
import com.syme.domain.model.Bill
import java.io.File
import java.io.FileOutputStream
import kotlin.math.roundToInt

object BillPdfGenerator {

    fun generate(context: Context, bill: Bill): File {

        val dir = File(context.cacheDir, "invoices")

        if (!dir.exists() && !dir.mkdirs()) {
            throw IllegalStateException("Cannot create invoices cache directory")
        }

        // 🔥 Nettoyage du cache PDF
        dir.listFiles()?.forEach { file ->
            file.delete()
        }

        val file = File(dir, "bill_${bill.billId}.pdf")
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas
        val paint = Paint()
        var y = 60f

        // 🔷 HEADER
        paint.textSize = 28f
        paint.isFakeBoldText = true
        canvas.drawText(context.getString(R.string.bill_title), 60f, y, paint)

        y += 20
        paint.strokeWidth = 2f
        canvas.drawLine(60f, y, 535f, y, paint)
        y += 40

        // 🔷 IMAGE
        val bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.rapport_financier)
        val scaled = Bitmap.createScaledBitmap(bitmap, 120, 120, false)
        canvas.drawBitmap(scaled, 420f, 80f, paint)

        paint.textSize = 16f
        paint.isFakeBoldText = false

        // 🔷 BILL BASIC INFO
        canvas.drawText(context.getString(R.string.bill_id, bill.billId), 60f, y, paint)
        y += 20
        canvas.drawText(context.getString(R.string.bill_owner_id, bill.ownerId), 60f, y, paint)
        y += 20
        canvas.drawText(context.getString(R.string.bill_installation_id, bill.installationId), 60f, y, paint)
        y += 40

        // 🔷 CONSUMPTION
        paint.textSize = 18f
        paint.isFakeBoldText = true
        canvas.drawText(context.getString(R.string.bill_calculation_trace), 60f, y, paint)

        paint.textSize = 15f
        paint.isFakeBoldText = false
        y += 25
        canvas.drawText(context.getString(R.string.bill_energy_value, (bill.energyWh / 1000).roundToInt()), 60f, y, paint)
        y += 20
        canvas.drawText(context.getString(R.string.bill_peak_power_value, (bill.peakPowerW / 1000).roundToInt()), 60f, y, paint)
        y += 20
        canvas.drawText(context.getString(R.string.bill_duration_value, bill.hours.roundToInt()), 60f, y, paint)
        y += 40

        // 🔷 TARIFF / TAXES / BONUS
        val tariff = (bill.metadata?.tariffSnapshot as? Map<String, Any>)?.toTariffConfig()
        if (tariff != null) {
            paint.textSize = 18f
            paint.isFakeBoldText = true
            canvas.drawText(context.getString(R.string.bill_tariff_section), 60f, y, paint)
            paint.textSize = 14f
            paint.isFakeBoldText = false
            y += 25

            canvas.drawText(context.getString(R.string.bill_price_per_kwh, tariff.pricePerKwh.roundToInt(), bill.currency), 60f, y, paint); y += 18
            canvas.drawText(context.getString(R.string.bill_penalty_price_per_kwh, tariff.penaltyPricePerKwh.roundToInt(), bill.currency), 60f, y, paint); y += 18
            canvas.drawText(context.getString(R.string.bill_vat_rate, (tariff.vatRate * 100).roundToInt()), 60f, y, paint); y += 18
            canvas.drawText(context.getString(R.string.bill_other_taxes_rate, (tariff.otherTaxesRate * 100).roundToInt()), 60f, y, paint); y += 18
            canvas.drawText(context.getString(R.string.bill_bonus_rate, (tariff.bonusRate * 100).roundToInt()), 60f, y, paint); y += 18
            canvas.drawText(context.getString(R.string.bill_social_discount_rate, (tariff.socialDiscountRate * 100).roundToInt()), 60f, y, paint); y += 18
            canvas.drawText(context.getString(R.string.bill_network_factor, tariff.networkBalancingFactor), 60f, y, paint); y += 40
        }

        // 🔷 TRACE
        paint.textSize = 18f
        paint.isFakeBoldText = true
        canvas.drawText(context.getString(R.string.bill_calculation_trace), 60f, y, paint)
        paint.textSize = 14f
        paint.isFakeBoldText = false
        y += 25

        val trace = bill.metadata?.trace ?: emptyList()
        if (trace.isEmpty()) {
            canvas.drawText(context.getString(R.string.bill_no_trace_available), 60f, y, paint)
            y += 18
        } else {
            trace.forEach { step ->
                canvas.drawText(context.getString(R.string.bill_trace_item, step), 60f, y, paint)
                y += 18
            }
        }
        y += 40

        // 🔷 TOTAL
        paint.textSize = 24f
        paint.isFakeBoldText = true
        canvas.drawText(context.getString(R.string.bill_total_value, bill.amountToPay.roundToInt(), bill.currency), 60f, y, paint)

        document.finishPage(page)
        document.writeTo(FileOutputStream(file))
        document.close()

        return file
    }
}