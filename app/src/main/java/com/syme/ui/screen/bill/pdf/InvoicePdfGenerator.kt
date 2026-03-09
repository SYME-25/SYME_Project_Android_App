package com.syme.ui.screen.bill.pdf

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.syme.R
import com.syme.domain.model.Installation
import com.syme.domain.model.Invoice
import com.syme.domain.model.enumeration.InvoiceStatus
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Generates a professional invoice PDF using Android's native PdfDocument + Canvas API.
 * No external dependencies required.
 *
 * Layout:
 * ┌─────────────────────────────────────────────┐
 * │  [COLOR BAR]  SYME Energy          FACTURE  │  ← Header
 * │  address / email           N° INV-001       │
 * │─────────────────────────────────────────────│
 * │  FACTURER À                PÉRIODE          │  ← Meta
 * │─────────────────────────────────────────────│
 * │  DÉTAIL DES COÛTS                           │  ← Cost table
 * │  Puissance subscription    XXXX FCFA        │
 * │  Puissance demandes        XXXX FCFA        │
 * │  Énergie consommée         XXXX FCFA        │
 * │─────────────────────────────────────────────│
 * │  SOUS-TOTAL                XXXX FCFA        │  ← Adjustments
 * │  TVA 18%                   + XXX FCFA       │
 * │  Bonus réduction           - XXX FCFA       │
 * │═════════════════════════════════════════════│
 * │  TOTAL À RÉGLER            XXXX FCFA        │  ← Total
 * │─────────────────────────────────────────────│
 * │  Demandes actives (tableau)                 │  ← Demand lines
 * │─────────────────────────────────────────────│
 * │  Footer: SYME · Merci · Page 1/1            │
 * └─────────────────────────────────────────────┘
 */
object InvoicePdfGenerator {
    // ── Design tokens ─────────────────────────────────────────────────────────
    private val COLOR_PRIMARY    = Color.rgb(25, 118, 210)   // blue 700
    private val COLOR_ACCENT     = Color.rgb(2, 136, 209)    // light blue 700
    private val COLOR_SURFACE    = Color.rgb(245, 248, 255)  // near-white blue tint
    private val COLOR_TEXT_DARK  = Color.rgb(18, 18, 24)
    private val COLOR_TEXT_MID   = Color.rgb(80, 90, 110)
    private val COLOR_TEXT_LIGHT = Color.rgb(140, 150, 170)
    private val COLOR_GREEN      = Color.rgb(46, 125, 50)
    private val COLOR_DIVIDER    = Color.rgb(220, 225, 235)
    private val COLOR_TOTAL_BG   = Color.rgb(25, 118, 210)
    private val COLOR_WHITE      = Color.WHITE
    // A4 at 72dpi ≈ 595 × 842 pt — we work in points
    private const val PAGE_W = 595
    private const val PAGE_H = 842
    private const val MARGIN = 48f
    private const val CONTENT_W = PAGE_W - MARGIN * 2
    fun generate(
        context: Context,
        invoice: Invoice,
        installation: Installation?,
        companyName: String,
        companyAddress: String,
        companyEmail: String,
        moneyUnit: String
    ): File {
        val doc   = PdfDocument()
        val info  = PdfDocument.PageInfo.Builder(PAGE_W, PAGE_H, 1).create()
        val page  = doc.startPage(info)
        val c     = page.canvas
        val df    = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
        val dfShort = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        var y = 0f
        // ── 1. Header band ────────────────────────────────────────────────────
        y = drawHeader(context, c, invoice, companyName, companyAddress, companyEmail, df, dfShort, moneyUnit)
        y += 28f
        // ── 2. Meta: bill-to + period ─────────────────────────────────────────
        y = drawMeta(context, c, invoice, installation, df, y)
        y += 24f
        drawDivider(c, y)
        y += 18f
        // ── 3. Cost breakdown table ───────────────────────────────────────────
        y = drawCostSection(context, c, invoice, moneyUnit, y)
        y += 16f
        drawDivider(c, y)
        y += 16f
        // ── 4. Adjustments ────────────────────────────────────────────────────
        y = drawAdjustments(context, c, invoice, moneyUnit, y)
        y += 12f
        // ── 5. Total band ─────────────────────────────────────────────────────
        y = drawTotalBand(context, c, invoice, moneyUnit, y)
        y += 24f
        // ── 6. Demand lines table (if any) ────────────────────────────────────
        if (invoice.demandLines.isNotEmpty()) {
            drawDivider(c, y)
            y += 16f
            y = drawDemandLines(context, c, invoice, moneyUnit, dfShort, y)
            y += 16f
        }
        // ── 7. Footer ─────────────────────────────────────────────────────────
        drawFooter(context, c, companyName, invoice.invoiceId)
        doc.finishPage(page)
        // ── Write to cache ────────────────────────────────────────────────────
        val dir = File(context.cacheDir, "invoices").also { it.mkdirs() }
        val file = File(dir, "facture_${invoice.invoiceId}.pdf")
        FileOutputStream(file).use { doc.writeTo(it) }
        doc.close()
        return file
    }
    // ─────────────────────────────────────────────────────────────────────────
    // HEADER
    // ─────────────────────────────────────────────────────────────────────────
    private fun drawHeader(
        context: Context,
        c: Canvas, invoice: Invoice,
        companyName: String, companyAddress: String, companyEmail: String,
        df: SimpleDateFormat, dfShort: SimpleDateFormat,
        moneyUnit: String
    ): Float {
        // Top color bar
        val barPaint = Paint().apply { color = COLOR_PRIMARY; style = Paint.Style.FILL }
        c.drawRect(0f, 0f, PAGE_W.toFloat(), 6f, barPaint)
        // Header background
        val bgPaint = Paint().apply { color = COLOR_SURFACE; style = Paint.Style.FILL }
        c.drawRect(0f, 6f, PAGE_W.toFloat(), 130f, bgPaint)
        // Left: company name
        c.drawText(
            companyName,
            MARGIN, 48f,
            paint(24f, COLOR_PRIMARY, bold = true)
        )
        c.drawText(
            companyAddress,
            MARGIN, 66f,
            paint(10f, COLOR_TEXT_MID)
        )
        c.drawText(
            companyEmail,
            MARGIN, 80f,
            paint(10f, COLOR_TEXT_MID)
        )
        // Right: INVOICE label + invoice number
        val rightX = PAGE_W - MARGIN
        c.drawText(
            context.getString(R.string.invoice_label),
            rightX, 48f,
            paint(22f, COLOR_PRIMARY, bold = true, align = Paint.Align.RIGHT)
        )
        c.drawText(
            context.getString(R.string.invoice_number_prefix) + invoice.invoiceId,
            rightX, 65f,
            paint(11f, COLOR_TEXT_DARK, bold = true, align = Paint.Align.RIGHT)
        )

        // Issue date
        val issueDate = invoice.issuedAt?.let { df.format(Date(it)) }
            ?: df.format(Date())
        c.drawText(
            context.getString(R.string.invoice_issued_at, issueDate),
            rightX, 80f,
            paint(10f, COLOR_TEXT_MID, align = Paint.Align.RIGHT)
        )
        // Status badge
        val statusLabel = when (invoice.status) {
            InvoiceStatus.DRAFT     -> context.getString(R.string.invoice_status_draft)
            InvoiceStatus.ISSUED    -> context.getString(R.string.invoice_status_issued)
            InvoiceStatus.PAID      -> context.getString(R.string.invoice_status_paid)
            InvoiceStatus.OVERDUE   -> context.getString(R.string.invoice_status_overdue)
            InvoiceStatus.CANCELLED -> context.getString(R.string.invoice_status_cancelled)
        }
        val statusColor = when (invoice.status) {
            InvoiceStatus.PAID    -> COLOR_GREEN
            InvoiceStatus.OVERDUE -> Color.rgb(198, 40, 40)
            else                  -> COLOR_PRIMARY
        }
        val badgePaint = Paint().apply {
            color = statusColor
            style = Paint.Style.FILL
        }
        val badgeRect = RectF(rightX - 80f, 88f, rightX, 104f)
        c.drawRoundRect(badgeRect, 6f, 6f, badgePaint)
        c.drawText(
            statusLabel,
            rightX - 40f, 100f,
            paint(9f, COLOR_WHITE, bold = true, align = Paint.Align.CENTER)
        )
        // Bottom divider of header
        drawDivider(c, 118f, color = COLOR_DIVIDER)
        return 130f
    }
    // ─────────────────────────────────────────────────────────────────────────
    // META (bill-to + period)
    // ─────────────────────────────────────────────────────────────────────────
    private fun drawMeta(
        context: Context,
        c: Canvas, invoice: Invoice,
        installation: Installation?,
        df: SimpleDateFormat, y: Float
    ): Float {
        val mid = PAGE_W / 2f
        // Left column: Bill to
        c.drawText(context.getString(R.string.invoice_bill_to), MARGIN, y + 16f, paint(9f, COLOR_TEXT_LIGHT, bold = true))
        c.drawText(
            invoice.ownerId,
            MARGIN, y + 32f,
            paint(13f, COLOR_TEXT_DARK, bold = true)
        )
        c.drawText(
            context.getString(R.string.invoice_installation_prefix, invoice.installationId),
            MARGIN, y + 48f,
            paint(10f, COLOR_TEXT_MID)
        )
        if (installation != null) {
            c.drawText(
                installation.address.ifBlank { installation.name },
                MARGIN, y + 62f,
                paint(10f, COLOR_TEXT_MID)
            )
        }
        // Right column: Billing period
        c.drawText(context.getString(R.string.invoice_billing_period), mid, y + 16f, paint(9f, COLOR_TEXT_LIGHT, bold = true))
        c.drawText(
            "${df.format(Date(invoice.billingPeriod.periodStart))}",
            mid, y + 32f,
            paint(12f, COLOR_TEXT_DARK, bold = true)
        )
        c.drawText(
            context.getString(R.string.invoice_period_to, df.format(Date(invoice.billingPeriod.periodEnd))),
            mid, y + 48f,
            paint(12f, COLOR_TEXT_DARK)
        )
        c.drawText(
            context.getString(R.string.invoice_duration_hours, invoice.totalHours),
            mid, y + 62f,
            paint(10f, COLOR_TEXT_MID)
        )
        return y + 72f
    }
    // ─────────────────────────────────────────────────────────────────────────
    // COST SECTION
    // ─────────────────────────────────────────────────────────────────────────
    private fun drawCostSection(context: Context, c: Canvas, invoice: Invoice, moneyUnit: String, y: Float): Float {
        var cy = y
        c.drawText(context.getString(R.string.invoice_cost_details), MARGIN, cy, paint(10f, COLOR_TEXT_LIGHT, bold = true))
        cy += 18f
        // Table header
        val tableBg = Paint().apply { color = Color.rgb(235, 242, 255); style = Paint.Style.FILL }
        c.drawRect(MARGIN, cy, MARGIN + CONTENT_W, cy + 22f, tableBg)
        c.drawText(context.getString(R.string.invoice_col_description), MARGIN + 10f, cy + 15f, paint(9f, COLOR_PRIMARY, bold = true))
        c.drawText(context.getString(R.string.invoice_col_detail), MARGIN + 220f, cy + 15f, paint(9f, COLOR_PRIMARY, bold = true))
        c.drawText(context.getString(R.string.invoice_col_amount), MARGIN + CONTENT_W - 10f, cy + 15f,
            paint(9f, COLOR_PRIMARY, bold = true, align = Paint.Align.RIGHT))
        cy += 26f
        // Row 1: Subscription
        cy = drawCostRow(
            c, cy,
            label = context.getString(R.string.invoice_row_subscription_label),
            detail = context.getString(R.string.invoice_row_subscription_detail, invoice.totalSubscriptionHours, invoice.subscribedPowerKw),
            amount = invoice.subscriptionCost,
            unit = moneyUnit,
            shade = false
        )
        // Row 2: Demand
        cy = drawCostRow(
            c, cy,
            label = context.getString(R.string.invoice_row_demand_label),
            detail = context.getString(R.string.invoice_row_demand_detail, invoice.demandLines.size),
            amount = invoice.demandCost,
            unit = moneyUnit,
            shade = true
        )
        // Row 3: Energy
        cy = drawCostRow(
            c, cy,
            label = context.getString(R.string.invoice_row_energy_label),
            detail = context.getString(R.string.invoice_row_energy_detail, invoice.totalEnergyKwh),
            amount = invoice.energyCost,
            unit = moneyUnit,
            shade = false
        )
        return cy
    }
    private fun drawCostRow(
        c: Canvas, y: Float,
        label: String, detail: String, amount: Double,
        unit: String, shade: Boolean
    ): Float {
        if (shade) {
            val rowBg = Paint().apply { color = Color.rgb(248, 250, 255); style = Paint.Style.FILL }
            c.drawRect(MARGIN, y, MARGIN + CONTENT_W, y + 26f, rowBg)
        }
        c.drawText(label, MARGIN + 10f, y + 17f, paint(11f, COLOR_TEXT_DARK))
        c.drawText(detail, MARGIN + 220f, y + 17f, paint(10f, COLOR_TEXT_MID))
        c.drawText(
            "%.0f %s".format(amount, unit),
            MARGIN + CONTENT_W - 10f, y + 17f,
            paint(11f, COLOR_TEXT_DARK, bold = true, align = Paint.Align.RIGHT)
        )
        // Row bottom border
        val divPaint = Paint().apply { color = COLOR_DIVIDER; strokeWidth = 0.5f }
        c.drawLine(MARGIN, y + 26f, MARGIN + CONTENT_W, y + 26f, divPaint)
        return y + 28f
    }
    // ─────────────────────────────────────────────────────────────────────────
    // ADJUSTMENTS
    // ─────────────────────────────────────────────────────────────────────────
    private fun drawAdjustments(context: Context, c: Canvas, invoice: Invoice, moneyUnit: String, y: Float): Float {
        var cy = y
        c.drawText(context.getString(R.string.invoice_adjustments), MARGIN, cy, paint(10f, COLOR_TEXT_LIGHT, bold = true))
        cy += 16f
        // Sub-total line
        cy = drawAdjLine(c, cy, context.getString(R.string.invoice_subtotal), invoice.subTotal, moneyUnit, bold = true)
        if (invoice.vatAmount != 0.0)
            cy = drawAdjLine(c, cy, context.getString(R.string.invoice_vat, (invoice.tariffConfig.vatRate * 100).toInt()),
                invoice.vatAmount, moneyUnit, isAddition = true)
        if (invoice.otherTaxesAmount != 0.0)
            cy = drawAdjLine(c, cy, context.getString(R.string.invoice_other_taxes),
                invoice.otherTaxesAmount, moneyUnit, isAddition = true)
        if (invoice.bonusAmount != 0.0)
            cy = drawAdjLine(c, cy, context.getString(R.string.invoice_voluntary_bonus),
                invoice.bonusAmount, moneyUnit, isAddition = false, isBonus = true)
        if (invoice.socialDiscountAmount != 0.0)
            cy = drawAdjLine(c, cy, context.getString(R.string.invoice_social_discount),
                invoice.socialDiscountAmount, moneyUnit, isAddition = false, isBonus = true)
        if (invoice.networkBalancingAmount != 0.0)
            cy = drawAdjLine(c, cy, context.getString(R.string.invoice_network_balancing),
                invoice.networkBalancingAmount, moneyUnit, isAddition = true)
        return cy
    }
    private fun drawAdjLine(
        c: Canvas, y: Float,
        label: String, amount: Double,
        unit: String,
        bold: Boolean = false,
        isAddition: Boolean = true,
        isBonus: Boolean = false
    ): Float {
        val prefix = when {
            isBonus    -> "− "
            isAddition -> "+ "
            else       -> ""
        }
        val color = when {
            isBonus -> COLOR_GREEN
            bold    -> COLOR_TEXT_DARK
            else    -> COLOR_TEXT_MID
        }
        c.drawText(label, MARGIN + 10f, y + 13f, paint(11f, if (bold) COLOR_TEXT_DARK else COLOR_TEXT_MID, bold = bold))
        c.drawText(
            "$prefix%.0f %s".format(amount, unit),
            MARGIN + CONTENT_W - 10f, y + 13f,
            paint(11f, color, bold = bold, align = Paint.Align.RIGHT)
        )
        return y + 18f
    }
    // ─────────────────────────────────────────────────────────────────────────
    // TOTAL BAND
    // ─────────────────────────────────────────────────────────────────────────
    private fun drawTotalBand(context: Context, c: Canvas, invoice: Invoice, moneyUnit: String, y: Float): Float {
        val bandH = 52f
        val bandPaint = Paint().apply { color = COLOR_TOTAL_BG; style = Paint.Style.FILL }
        val rect = RectF(MARGIN, y, MARGIN + CONTENT_W, y + bandH)
        c.drawRoundRect(rect, 10f, 10f, bandPaint)
        c.drawText(
            context.getString(R.string.invoice_total_due),
            MARGIN + 18f, y + 32f,
            paint(13f, COLOR_WHITE, bold = true)
        )
        c.drawText(
            "%.0f %s".format(invoice.totalAmount, moneyUnit),
            MARGIN + CONTENT_W - 18f, y + 34f,
            paint(22f, COLOR_WHITE, bold = true, align = Paint.Align.RIGHT)
        )
        // Due date if available
        invoice.dueDate?.let { due ->
            val df = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            c.drawText(
                context.getString(R.string.invoice_due_date, df.format(Date(due))),
                MARGIN + 18f, y + 46f,
                paint(9f, Color.argb(180, 255, 255, 255))
            )
        }
        return y + bandH
    }
    // ─────────────────────────────────────────────────────────────────────────
    // DEMAND LINES TABLE
    // ─────────────────────────────────────────────────────────────────────────
    private fun drawDemandLines(
        context: Context,
        c: Canvas, invoice: Invoice,
        moneyUnit: String, df: SimpleDateFormat, y: Float
    ): Float {
        var cy = y
        c.drawText(
            context.getString(R.string.invoice_demand_details),
            MARGIN, cy,
            paint(10f, COLOR_TEXT_LIGHT, bold = true)
        )
        cy += 16f
        // Table header
        val hdrBg = Paint().apply { color = Color.rgb(235, 242, 255); style = Paint.Style.FILL }
        c.drawRect(MARGIN, cy, MARGIN + CONTENT_W, cy + 20f, hdrBg)
        val cols = listOf(
            MARGIN + 10f       to context.getString(R.string.invoice_col_period_start),
            MARGIN + 120f      to context.getString(R.string.invoice_col_period_end),
            MARGIN + 230f      to context.getString(R.string.invoice_col_duration_h),
            MARGIN + 310f      to context.getString(R.string.invoice_col_power_kw),
            MARGIN + CONTENT_W - 10f to context.getString(R.string.invoice_col_cost)
        )
        cols.forEach { (x, lbl) ->
            val align = if (x > MARGIN + CONTENT_W - 60f) Paint.Align.RIGHT else Paint.Align.LEFT
            c.drawText(lbl, x, cy + 14f, paint(8.5f, COLOR_PRIMARY, bold = true, align = align))
        }
        cy += 22f
        invoice.demandLines.forEachIndexed { idx, line ->
            if (idx % 2 == 1) {
                val rowBg = Paint().apply { color = Color.rgb(248, 250, 255); style = Paint.Style.FILL }
                c.drawRect(MARGIN, cy, MARGIN + CONTENT_W, cy + 22f, rowBg)
            }
            c.drawText(df.format(Date(line.effectiveStart)), MARGIN + 10f, cy + 15f, paint(9.5f, COLOR_TEXT_DARK))
            c.drawText(df.format(Date(line.effectiveEnd)), MARGIN + 120f, cy + 15f, paint(9.5f, COLOR_TEXT_DARK))
            c.drawText("%.1f".format(line.durationHours), MARGIN + 230f, cy + 15f, paint(9.5f, COLOR_TEXT_DARK))
            c.drawText("%.0f".format(line.requestedPowerKw), MARGIN + 310f, cy + 15f, paint(9.5f, COLOR_TEXT_DARK))
            c.drawText(
                "%.0f %s".format(line.cost, moneyUnit),
                MARGIN + CONTENT_W - 10f, cy + 15f,
                paint(9.5f, COLOR_TEXT_DARK, bold = true, align = Paint.Align.RIGHT)
            )
            val divPaint = Paint().apply { color = COLOR_DIVIDER; strokeWidth = 0.5f }
            c.drawLine(MARGIN, cy + 22f, MARGIN + CONTENT_W, cy + 22f, divPaint)
            cy += 24f
        }
        return cy
    }
    // ─────────────────────────────────────────────────────────────────────────
    // FOOTER
    // ─────────────────────────────────────────────────────────────────────────
    private fun drawFooter(context: Context, c: Canvas, companyName: String, invoiceId: String) {
        val fy = PAGE_H - 36f
        // Top line
        val linePaint = Paint().apply { color = COLOR_PRIMARY; strokeWidth = 1.5f }
        c.drawLine(MARGIN, fy - 10f, MARGIN + CONTENT_W, fy - 10f, linePaint)
        c.drawText(
            "$companyName  ·  ${context.getString(R.string.invoice_footer_thanks)}",
            MARGIN, fy + 8f,
            paint(9f, COLOR_TEXT_MID)
        )
        c.drawText(
            context.getString(R.string.invoice_footer_page, invoiceId),
            MARGIN + CONTENT_W, fy + 8f,
            paint(9f, COLOR_TEXT_LIGHT, align = Paint.Align.RIGHT)
        )
    }
    // ─────────────────────────────────────────────────────────────────────────
    // HELPERS
    // ─────────────────────────────────────────────────────────────────────────
    private fun drawDivider(c: Canvas, y: Float, color: Int = COLOR_DIVIDER) {
        val p = Paint().apply { this.color = color; strokeWidth = 1f }
        c.drawLine(MARGIN, y, MARGIN + CONTENT_W, y, p)
    }
    private fun paint(
        size: Float,
        color: Int,
        bold: Boolean = false,
        align: Paint.Align = Paint.Align.LEFT
    ) = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = size * 2.2f   // scale pt → px (approx at mdpi)
        this.color = color
        typeface = if (bold) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
        textAlign = align
    }
}