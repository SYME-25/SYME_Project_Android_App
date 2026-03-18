package com.syme.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.syme.R
import com.syme.domain.mapper.toTariffConfig
import com.syme.domain.model.Bill
import java.io.File
import java.io.FileOutputStream
import kotlin.math.roundToInt

object BillPdfGenerator {

    // ── Palette ───────────────────────────────────────────────────────────────
    private val COLOR_PRIMARY  = Color.parseColor("#1A237E")
    private val COLOR_ACCENT   = Color.parseColor("#283593")
    private val COLOR_LIGHT_BG = Color.parseColor("#F5F7FF")
    private val COLOR_ROW_ALT  = Color.parseColor("#ECEFFE")
    private val COLOR_DIVIDER  = Color.parseColor("#C5CAE9")
    private val COLOR_TEXT_DARK = Color.parseColor("#1A1A2E")
    private val COLOR_TEXT_GRAY = Color.parseColor("#5C6BC0")
    private val COLOR_WHITE    = Color.WHITE
    private val COLOR_SUCCESS  = Color.parseColor("#2E7D32")
    private val COLOR_TOTAL_BG = Color.parseColor("#1A237E")

    // ── Page constants ────────────────────────────────────────────────────────
    private const val PAGE_W   = 595f
    private const val PAGE_H   = 842f
    private const val MARGIN   = 48f
    private const val CONTENT_W = PAGE_W - MARGIN * 2

    // Zone réservée en bas pour le bloc total + footer (page finale uniquement)
    private const val BOTTOM_RESERVED = 130f
    // Limite de contenu sur les pages intermédiaires (sans bloc total)
    private const val CONTENT_MAX_Y   = PAGE_H - 40f
    // Limite de contenu sur la dernière page (laisse place au bloc total)
    private const val LAST_PAGE_MAX_Y = PAGE_H - BOTTOM_RESERVED - 10f

    // ── State partagé entre pages ─────────────────────────────────────────────
    private var currentPage: PdfDocument.Page? = null
    private var currentCanvas: Canvas? = null
    private var pageNumber = 1

    fun generate(context: Context, bill: Bill): File {
        val dir = File(context.cacheDir, "invoices")
        if (!dir.exists() && !dir.mkdirs())
            throw IllegalStateException("Cannot create invoices cache directory")
        dir.listFiles()?.forEach { it.delete() }

        val file     = File(dir, "bill_${bill.billId}.pdf")
        val document = PdfDocument()
        val paint    = Paint(Paint.ANTI_ALIAS_FLAG)

        pageNumber = 1
        startPage(document, paint)

        var y = drawHeader(context, currentCanvas!!, paint, bill)
        y = drawInfoBlock(context, currentCanvas!!, paint, bill, y)
        y = ensureSpace(document, paint, y, 80f)
        y = drawConsumptionSection(context, document, paint, bill, y)

        val tariff = (bill.metadata?.tariffSnapshot as? Map<String, Any>)?.toTariffConfig()
        if (tariff != null) {
            y = ensureSpace(document, paint, y, 220f)
            y = drawTariffSection(context, document, paint, bill, tariff, y)
        }

        y = ensureSpace(document, paint, y, 80f)
        y = drawTraceSection(context, document, paint, bill, y)

        // S'assurer qu'il y a assez de place pour le bloc total
        y = ensureSpace(document, paint, y, BOTTOM_RESERVED)
        drawBottomArea(context, currentCanvas!!, paint, bill)

        document.finishPage(currentPage!!)
        document.writeTo(FileOutputStream(file))
        document.close()
        return file
    }

    // ── Gestion des pages ─────────────────────────────────────────────────────
    private fun startPage(document: PdfDocument, paint: Paint) {
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, pageNumber).create()
        currentPage   = document.startPage(pageInfo)
        currentCanvas = currentPage!!.canvas
    }

    private fun newPage(document: PdfDocument, paint: Paint): Float {
        document.finishPage(currentPage!!)
        pageNumber++
        startPage(document, paint)
        // Mini header de continuation
        paint.color = COLOR_PRIMARY
        currentCanvas!!.drawRect(0f, 0f, PAGE_W, 32f, paint)
        paint.color = COLOR_ACCENT
        currentCanvas!!.drawRect(0f, 0f, 6f, 32f, paint)
        paint.color = Color.parseColor("#9FA8DA")
        paint.typeface = Typeface.DEFAULT
        paint.textSize = 9f
        paint.textAlign = Paint.Align.RIGHT
        currentCanvas!!.drawText("Page $pageNumber", PAGE_W - MARGIN, 21f, paint)
        paint.textAlign = Paint.Align.LEFT
        return 50f // y de départ sur nouvelle page
    }

    /**
     * Vérifie si [neededHeight] tient encore sur la page courante.
     * Si non, crée une nouvelle page et retourne le nouveau y.
     */
    private fun ensureSpace(document: PdfDocument, paint: Paint, y: Float, neededHeight: Float): Float {
        return if (y + neededHeight > LAST_PAGE_MAX_Y) newPage(document, paint) else y
    }

    // ── HEADER (page 1 uniquement) ────────────────────────────────────────────
    private fun drawHeader(context: Context, canvas: Canvas, paint: Paint, bill: Bill): Float {
        paint.color = COLOR_PRIMARY
        canvas.drawRect(0f, 0f, PAGE_W, 140f, paint)

        paint.color = COLOR_ACCENT
        canvas.drawRect(0f, 0f, 6f, 140f, paint)

        // ── Logo (gauche) ─────────────────────────────────────────────────────
        val logoSize = 70f
        val logoLeft = MARGIN
        val logoTop  = 35f

        val logoResId = context.resources.getIdentifier("ic_syme_logo", "drawable", context.packageName)
            .takeIf { it != 0 }
            ?: context.resources.getIdentifier("ic_launcher", "mipmap", context.packageName)
                .takeIf { it != 0 }

        if (logoResId != null) {
            val bitmap = BitmapFactory.decodeResource(context.resources, logoResId)
            if (bitmap != null) {
                val scaled = Bitmap.createScaledBitmap(bitmap, logoSize.toInt(), logoSize.toInt(), true)
                canvas.drawBitmap(scaled, logoLeft, logoTop, paint)
            } else {
                drawLogoPlaceholder(canvas, paint, logoLeft, logoTop, logoLeft + logoSize, logoTop + logoSize)
            }
        } else {
            drawLogoPlaceholder(canvas, paint, logoLeft, logoTop, logoLeft + logoSize, logoTop + logoSize)
        }

        // ── Textes centre ─────────────────────────────────────────────────────
        paint.color = Color.parseColor("#9FA8DA")
        paint.typeface = Typeface.DEFAULT
        paint.textSize = 9f
        canvas.drawText(context.getString(R.string.invoice_tagline).uppercase(), MARGIN + 86f, 52f, paint)

        paint.color = COLOR_WHITE
        paint.typeface = Typeface.DEFAULT_BOLD
        paint.textSize = 22f
        canvas.drawText(context.getString(R.string.company_name), MARGIN + 86f, 76f, paint)

        paint.color = Color.parseColor("#7986CB")
        paint.strokeWidth = 1f
        canvas.drawLine(MARGIN + 86f, 82f, MARGIN + 86f + 160f, 82f, paint)

        paint.color = Color.parseColor("#9FA8DA")
        paint.typeface = Typeface.DEFAULT
        paint.textSize = 10f
        canvas.drawText(context.getString(R.string.bill_title), MARGIN + 86f, 98f, paint)

        // ── Image rapport_financier (droite) ──────────────────────────────────
        val imgSize  = 70f
        val imgRight = PAGE_W - MARGIN
        val imgLeft  = imgRight - imgSize
        val imgTop   = 35f

        val rapportBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.rapport_financier)
        if (rapportBitmap != null) {
            val scaledRapport = Bitmap.createScaledBitmap(rapportBitmap, imgSize.toInt(), imgSize.toInt(), true)
            canvas.drawBitmap(scaledRapport, imgLeft, imgTop, paint)
        }

        // ── INVOICE label + bill ID (sous l'image) ────────────────────────────
        paint.color = COLOR_WHITE
        paint.typeface = Typeface.DEFAULT_BOLD
        paint.textSize = 13f
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText(context.getString(R.string.invoice_label), PAGE_W - MARGIN, 118f, paint)

        paint.typeface = Typeface.DEFAULT
        paint.textSize = 10f
        paint.color = Color.parseColor("#9FA8DA")
        canvas.drawText("#${bill.billId}", PAGE_W - MARGIN, 132f, paint)

        paint.textAlign = Paint.Align.LEFT
        return 158f
    }

    // ── LOGO PLACEHOLDER ──────────────────────────────────────────────────────
    private fun drawLogoPlaceholder(
        canvas: Canvas, paint: Paint,
        left: Float, top: Float, right: Float, bottom: Float
    ) {
        val cx = (left + right) / 2f
        val cy = (top + bottom) / 2f
        val r  = (right - left) / 2f

        // Cercle de fond semi-transparent
        paint.color = Color.parseColor("#33FFFFFF")
        canvas.drawCircle(cx, cy, r, paint)

        // Anneau (stroke)
        paint.color = Color.parseColor("#7986CB")
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2f
        canvas.drawCircle(cx, cy, r - 2f, paint)
        paint.style = Paint.Style.FILL

        // Initiale "S" centrée
        paint.color = COLOR_WHITE
        paint.typeface = Typeface.DEFAULT_BOLD
        paint.textSize = 28f
        paint.textAlign = Paint.Align.CENTER
        val textY = cy - (paint.descent() + paint.ascent()) / 2f
        canvas.drawText("S", cx, textY, paint)
        paint.textAlign = Paint.Align.LEFT
    }

    // ── INFO BLOCK ────────────────────────────────────────────────────────────
    private fun drawInfoBlock(context: Context, canvas: Canvas, paint: Paint, bill: Bill, startY: Float): Float {
        val y = startY
        paint.color = COLOR_LIGHT_BG
        canvas.drawRoundRect(RectF(MARGIN, y, PAGE_W - MARGIN, y + 80f), 8f, 8f, paint)

        paint.color = COLOR_TEXT_GRAY
        paint.typeface = Typeface.DEFAULT
        paint.textSize = 9f
        val leftX  = MARGIN + 12f
        val rightX = PAGE_W / 2 + 12f

        canvas.drawText(context.getString(R.string.label_owner_id).uppercase(),        leftX,  y + 18f, paint)
        canvas.drawText(context.getString(R.string.label_installation_id).uppercase(), leftX,  y + 44f, paint)
        canvas.drawText(context.getString(R.string.label_bill_id).uppercase(),         rightX, y + 18f, paint)
        canvas.drawText(context.getString(R.string.label_currency).uppercase(),        rightX, y + 44f, paint)

        paint.color = COLOR_TEXT_DARK
        paint.typeface = Typeface.DEFAULT_BOLD
        paint.textSize = 12f
        canvas.drawText(bill.ownerId,        leftX,  y + 32f, paint)
        canvas.drawText(bill.installationId, leftX,  y + 58f, paint)
        canvas.drawText(bill.billId,         rightX, y + 32f, paint)
        canvas.drawText(bill.currency,       rightX, y + 58f, paint)

        paint.color = COLOR_DIVIDER
        paint.strokeWidth = 1f
        canvas.drawLine(PAGE_W / 2, y + 10f, PAGE_W / 2, y + 70f, paint)

        return y + 100f
    }

    // ── SECTION HEADER ────────────────────────────────────────────────────────
    private fun drawSectionHeader(canvas: Canvas, paint: Paint, title: String, y: Float): Float {
        paint.color = COLOR_ACCENT
        canvas.drawRect(MARGIN, y, MARGIN + 4f, y + 22f, paint)

        paint.color = COLOR_TEXT_DARK
        paint.typeface = Typeface.DEFAULT_BOLD
        paint.textSize = 13f
        canvas.drawText(title, MARGIN + 12f, y + 15f, paint)

        paint.color = COLOR_DIVIDER
        paint.strokeWidth = 0.8f
        canvas.drawLine(MARGIN + 12f, y + 24f, PAGE_W - MARGIN, y + 24f, paint)

        return y + 36f
    }

    // ── ROW helper ────────────────────────────────────────────────────────────
    private fun drawRow(canvas: Canvas, paint: Paint, label: String, value: String, y: Float, alt: Boolean): Float {
        if (alt) {
            paint.color = COLOR_ROW_ALT
            canvas.drawRect(MARGIN, y - 14f, PAGE_W - MARGIN, y + 6f, paint)
        }
        paint.color = COLOR_TEXT_GRAY
        paint.typeface = Typeface.DEFAULT
        paint.textSize = 11f
        canvas.drawText(label, MARGIN + 8f, y, paint)

        paint.color = COLOR_TEXT_DARK
        paint.typeface = Typeface.DEFAULT_BOLD
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText(value, PAGE_W - MARGIN - 8f, y, paint)
        paint.textAlign = Paint.Align.LEFT

        return y + 20f
    }

    // ── CONSUMPTION SECTION ───────────────────────────────────────────────────
    private fun drawConsumptionSection(
        context: Context, document: PdfDocument, paint: Paint, bill: Bill, startY: Float
    ): Float {
        var y = drawSectionHeader(currentCanvas!!, paint, context.getString(R.string.section_consumption), startY)
        y = ensureSpace(document, paint, y, 20f)
        y = drawRow(currentCanvas!!, paint, context.getString(R.string.label_energy_kwh),   "${(bill.energyWh / 1000).roundToInt()} kWh", y, false)
        y = ensureSpace(document, paint, y, 20f)
        y = drawRow(currentCanvas!!, paint, context.getString(R.string.label_peak_power_kw),"${(bill.peakPowerW / 1000).roundToInt()} kW",  y, true)
        y = ensureSpace(document, paint, y, 20f)
        y = drawRow(currentCanvas!!, paint, context.getString(R.string.label_duration_h),   "${bill.hours.roundToInt()} h",                y, false)
        return y + 16f
    }

    // ── TARIFF SECTION ────────────────────────────────────────────────────────
    private fun drawTariffSection(
        context: Context, document: PdfDocument, paint: Paint,
        bill: Bill, tariff: com.syme.domain.model.TariffConfig, startY: Float
    ): Float {
        var y = drawSectionHeader(currentCanvas!!, paint, context.getString(R.string.section_tariff), startY)
        var alt = false

        fun row(label: String, value: String) {
            y = ensureSpace(document, paint, y, 20f)
            y = drawRow(currentCanvas!!, paint, label, value, y, alt.also { alt = !alt })
        }

        row(context.getString(R.string.label_price_per_kwh),   "${tariff.pricePerKwh.roundToInt()} ${bill.currency}/kWh")
        row(context.getString(R.string.label_penalty_per_kwh), "${tariff.penaltyPricePerKwh.roundToInt()} ${bill.currency}/kWh")
        row(context.getString(R.string.label_vat_rate),        "${(tariff.vatRate * 100).roundToInt()}%")
        row(context.getString(R.string.label_other_taxes),     "${(tariff.otherTaxesRate * 100).roundToInt()}%")
        row(context.getString(R.string.label_bonus_rate),      "${(tariff.bonusRate * 100).roundToInt()}%")
        row(context.getString(R.string.label_social_discount), "${(tariff.socialDiscountRate * 100).roundToInt()}%")
        row(context.getString(R.string.label_network_factor),  "${tariff.networkBalancingFactor}")

        return y + 16f
    }

    // ── TRACE SECTION ─────────────────────────────────────────────────────────
    private fun drawTraceSection(
        context: Context, document: PdfDocument, paint: Paint, bill: Bill, startY: Float
    ): Float {
        var y = drawSectionHeader(currentCanvas!!, paint, context.getString(R.string.section_trace), startY)
        val trace = bill.metadata?.trace ?: emptyList()

        if (trace.isEmpty()) {
            y += 6f  // petit espace entre la ligne du header et le texte
            paint.color = COLOR_TEXT_GRAY
            paint.typeface = Typeface.DEFAULT
            paint.textSize = 11f
            currentCanvas!!.drawText(context.getString(R.string.bill_no_trace_available), MARGIN + 8f, y, paint)
            y += 18f
        } else {
            y += 6f  // espace entre la ligne du header et le premier item
            trace.forEachIndexed { index, step ->
                y = ensureSpace(document, paint, y, 26f)
                val cv = currentCanvas!!

                // Bulle numérotée
                paint.color = COLOR_ACCENT
                paint.style = Paint.Style.FILL
                cv.drawCircle(MARGIN + 10f, y - 4f, 9f, paint)

                paint.color = COLOR_WHITE
                paint.typeface = Typeface.DEFAULT_BOLD
                paint.textSize = 8f
                paint.textAlign = Paint.Align.CENTER
                cv.drawText("${index + 1}", MARGIN + 10f, y - 4f - (paint.descent() + paint.ascent()) / 2f, paint)
                paint.textAlign = Paint.Align.LEFT

                // Texte du step
                paint.color = COLOR_TEXT_DARK
                paint.typeface = Typeface.DEFAULT
                paint.textSize = 11f
                cv.drawText(step, MARGIN + 26f, y, paint)

                y += 22f
            }
        }
        return y + 14f
    }

    // ── BOTTOM AREA (total + footer, ancrés en bas de la page courante) ───────
    private fun drawBottomArea(context: Context, canvas: Canvas, paint: Paint, bill: Bill) {
        val footerH  = 50f
        val blockH   = 64f
        val gap      = 12f
        val blockTop = PAGE_H - footerH - gap - blockH

        // ── Total block ───────────────────────────────────────────────────────
        // Shadow
        paint.color = Color.parseColor("#1A1A2E22")
        canvas.drawRoundRect(RectF(MARGIN + 3f, blockTop + 3f, PAGE_W - MARGIN + 3f, blockTop + blockH + 3f), 10f, 10f, paint)

        // Background
        paint.color = COLOR_TOTAL_BG
        canvas.drawRoundRect(RectF(MARGIN, blockTop, PAGE_W - MARGIN, blockTop + blockH), 10f, 10f, paint)

        // Accent stripe
        paint.color = Color.parseColor("#7986CB")
        canvas.drawRoundRect(RectF(MARGIN, blockTop, MARGIN + 8f, blockTop + blockH), 10f, 10f, paint)
        canvas.drawRect(MARGIN + 4f, blockTop, MARGIN + 8f, blockTop + blockH, paint)

        // Label
        paint.color = Color.parseColor("#9FA8DA")
        paint.typeface = Typeface.DEFAULT
        paint.textSize = 10f
        canvas.drawText(context.getString(R.string.label_total_due).uppercase(), MARGIN + 20f, blockTop + 22f, paint)

        // Amount — repli automatique si trop long
        val amountText = "${bill.amountToPay.roundToInt()} ${bill.currency}"
        paint.color = COLOR_WHITE
        paint.typeface = Typeface.DEFAULT_BOLD
        paint.textSize = 20f
        if (paint.measureText(amountText) > CONTENT_W - 120f) paint.textSize = 16f
        canvas.drawText(amountText, MARGIN + 20f, blockTop + 48f, paint)

        // Status
        paint.color = COLOR_SUCCESS
        paint.typeface = Typeface.DEFAULT_BOLD
        paint.textSize = 10f
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText(context.getString(R.string.label_status_generated), PAGE_W - MARGIN - 12f, blockTop + blockH / 2 + 4f, paint)
        paint.textAlign = Paint.Align.LEFT

        // ── Footer ────────────────────────────────────────────────────────────
        val footerTop = PAGE_H - footerH
        paint.color = COLOR_DIVIDER
        paint.strokeWidth = 0.6f
        canvas.drawLine(MARGIN, footerTop, PAGE_W - MARGIN, footerTop, paint)

        paint.color = COLOR_TEXT_GRAY
        paint.typeface = Typeface.DEFAULT
        paint.textSize = 8f
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText(
            "Generated by SYME · Confidential document · Do not distribute",
            PAGE_W / 2, footerTop + 20f, paint
        )
        paint.textAlign = Paint.Align.LEFT
    }
}