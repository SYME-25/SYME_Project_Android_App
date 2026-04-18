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
import androidx.compose.ui.graphics.toArgb
import com.syme.R
import com.syme.domain.mapper.toTariffConfig
import com.syme.domain.model.Bill
import java.io.File
import java.io.FileOutputStream
import kotlin.math.roundToInt

/**
 * Générateur de factures PDF — style rapport financier.
 *
 * Palette : charbon foncé (#1C1C1E) + ivoire chaud (#F7F4EF)
 *           + cuivre accent (#B87333) + gris neutre (#6B6B6B)
 * Mise en page : grille stricte, lignes droites, typographie serrée.
 */
object BillPdfGenerator {

    // ── Palette manuelle (pas de dépendances thème pour le PDF) ──────────────
    private val C_CHARCOAL      = Color.parseColor("#1C1C1E")   // noir charbon
    private val C_CHARCOAL_SOFT = Color.parseColor("#2E2E30")   // charbon adouci
    private val C_COPPER        = Color.parseColor("#B87333")   // cuivre accent
    private val C_COPPER_LIGHT  = Color.parseColor("#D4956A")   // cuivre clair
    private val C_IVORY         = Color.parseColor("#F7F4EF")   // fond ivoire
    private val C_IVORY_DARK    = Color.parseColor("#ECEAE4")   // ivoire foncé (zébrage)
    private val C_RULE          = Color.parseColor("#D8D4CC")   // filet séparateur
    private val C_TEXT_BODY     = Color.parseColor("#2A2A2A")   // texte corps
    private val C_TEXT_MUTED    = Color.parseColor("#6B6B6B")   // texte secondaire
    private val C_TEXT_FAINT    = Color.parseColor("#A0A0A0")   // texte discret
    private val C_WHITE         = Color.WHITE
    private val C_SUCCESS       = Color.parseColor("#2D7A4F")   // vert sobre

    // ── Constantes de mise en page ────────────────────────────────────────────
    private const val PAGE_W          = 595f
    private const val PAGE_H          = 842f
    private const val MARGIN_L        = 52f    // marge gauche (plus large)
    private const val MARGIN_R        = 44f    // marge droite
    private const val CONTENT_W       = PAGE_W - MARGIN_L - MARGIN_R
    private const val BOTTOM_RESERVED = 120f
    private const val LAST_Y          = PAGE_H - BOTTOM_RESERVED - 8f

    // Bande latérale gauche
    private const val STRIPE_W        = 4f

    // ── État interne ──────────────────────────────────────────────────────────
    private var currentPage:   PdfDocument.Page? = null
    private var currentCanvas: Canvas?            = null
    private var pageNumber = 1

    private fun ts(millis: Long) = TimeUtils.formatDate(millis, "dd/MM/yyyy HH:mm:ss")

    // =========================================================================
    fun generate(context: Context, bill: Bill): File {
        val generatedAt = System.currentTimeMillis()
        val dir = File(context.cacheDir, "invoices")
        if (!dir.exists() && !dir.mkdirs())
            throw IllegalStateException("Cannot create invoices cache directory")
        dir.listFiles()?.forEach { it.delete() }

        val file     = File(dir, "bill_SYME_${bill.billId}.pdf")
        val document = PdfDocument()
        val p        = Paint(Paint.ANTI_ALIAS_FLAG)

        pageNumber = 1
        startPage(document)

        var y = drawHeader(context, currentCanvas!!, p, bill)
        y = drawPeriodBanner(context, currentCanvas!!, p, bill, y)
        y = drawMetaStrip(context, currentCanvas!!, p, bill, y)
        y += 28f

        y = drawSection(context, document, p,
            title       = context.getString(R.string.section_consumption),
            startY      = y,
            minHeight   = 80f
        ) { cv, paint, sy ->
            var iy = sy
            iy = row(cv, paint, context.getString(R.string.label_energy_kwh),
                "${(bill.energyWh / 1000).roundToInt()} kWh", iy, false)
            iy = row(cv, paint, context.getString(R.string.label_peak_power_kw),
                "${(bill.peakPowerW / 1000).roundToInt()} kW", iy, true)
            iy = row(cv, paint, context.getString(R.string.label_duration_h),
                "${bill.hours.roundToInt()} h", iy, false)
            iy
        }

        val tariff = (bill.metadata?.tariffSnapshot as? Map<String, Any>)?.toTariffConfig()
        if (tariff != null) {
            y = ensureSpace(document, p, y, 220f)
            y = drawSection(context, document, p,
                title     = context.getString(R.string.section_tariff),
                startY    = y,
                minHeight = 200f
            ) { cv, paint, sy ->
                var iy = sy; var alt = false
                fun r(lbl: String, v: String) { iy = row(cv, paint, lbl, v, iy, alt.also { alt = !alt }) }
                r(context.getString(R.string.label_price_per_kwh),   "${tariff.pricePerKwh.roundToInt()} ${bill.currency}/kWh")
                r(context.getString(R.string.label_penalty_per_kwh), "${tariff.penaltyPricePerKwh.roundToInt()} ${bill.currency}/kWh")
                r(context.getString(R.string.label_vat_rate),        "${(tariff.vatRate * 100).roundToInt()}%")
                r(context.getString(R.string.label_other_taxes),     "${(tariff.otherTaxesRate * 100).roundToInt()}%")
                r(context.getString(R.string.label_bonus_rate),      "${(tariff.bonusRate * 100).roundToInt()}%")
                r(context.getString(R.string.label_social_discount), "${(tariff.socialDiscountRate * 100).roundToInt()}%")
                r(context.getString(R.string.label_network_factor),  "${tariff.networkBalancingFactor}")
                iy
            }
        }

        y = ensureSpace(document, p, y, 80f)
        y = drawTraceSection(context, document, p, bill, y)

        y = ensureSpace(document, p, y, BOTTOM_RESERVED)
        drawFooterArea(context, currentCanvas!!, p, bill, generatedAt)

        document.finishPage(currentPage!!)
        document.writeTo(FileOutputStream(file))
        document.close()
        return file
    }

    // =========================================================================
    // Gestion des pages
    // =========================================================================

    private fun startPage(document: PdfDocument) {
        val info    = PdfDocument.PageInfo.Builder(595, 842, pageNumber).create()
        currentPage = document.startPage(info)
        currentCanvas = currentPage!!.canvas

        // Fond ivoire chaud sur toute la page
        val p = Paint()
        p.color = C_IVORY
        currentCanvas!!.drawRect(0f, 0f, PAGE_W, PAGE_H, p)
    }

    private fun newPage(document: PdfDocument, p: Paint): Float {
        document.finishPage(currentPage!!)
        pageNumber++
        startPage(document)

        // Bandeau continuité en haut — simple règle et numéro de page
        p.color  = C_RULE
        p.strokeWidth = 0.5f
        currentCanvas!!.drawLine(MARGIN_L, 36f, PAGE_W - MARGIN_R, 36f, p)

        p.color    = C_TEXT_FAINT
        p.typeface = Typeface.create("serif", Typeface.NORMAL)
        p.textSize = 8f
        p.textAlign = Paint.Align.RIGHT
        currentCanvas!!.drawText("— $pageNumber —", PAGE_W - MARGIN_R, 32f, p)
        p.textAlign = Paint.Align.LEFT

        return 52f
    }

    private fun ensureSpace(document: PdfDocument, p: Paint, y: Float, need: Float): Float =
        if (y + need > LAST_Y) newPage(document, p) else y

    // =========================================================================
    // En-tête
    // =========================================================================

    private fun drawHeader(context: Context, cv: Canvas, p: Paint, bill: Bill): Float {
        // Fond sombre en-tête
        p.color = C_CHARCOAL
        cv.drawRect(0f, 0f, PAGE_W, 118f, p)

        // Bande cuivre gauche
        p.color = C_COPPER
        cv.drawRect(0f, 0f, STRIPE_W, 118f, p)

        // Logo
        val logoResId = context.resources
            .getIdentifier("ic_syme_logo", "drawable", context.packageName)
            .takeIf { it != 0 }
            ?: context.resources
                .getIdentifier("ic_launcher", "mipmap", context.packageName)
                .takeIf { it != 0 }

        val logoSize = 52f
        val logoTop  = (118f - logoSize) / 2f
        if (logoResId != null) {
            val bmp = BitmapFactory.decodeResource(context.resources, logoResId)
            if (bmp != null) {
                cv.drawBitmap(
                    Bitmap.createScaledBitmap(bmp, logoSize.toInt(), logoSize.toInt(), true),
                    MARGIN_L, logoTop, p
                )
            } else drawLogoMark(cv, p, MARGIN_L, logoTop, logoSize)
        } else drawLogoMark(cv, p, MARGIN_L, logoTop, logoSize)

        // Séparateur vertical léger entre logo et texte
        p.color       = Color.argb(60, 255, 255, 255)
        p.strokeWidth = 0.5f
        cv.drawLine(MARGIN_L + logoSize + 18f, logoTop + 4f,
            MARGIN_L + logoSize + 18f, logoTop + logoSize - 4f, p)

        // Nom société
        val tx = MARGIN_L + logoSize + 30f
        p.color    = C_WHITE
        p.typeface = Typeface.create("serif", Typeface.BOLD)
        p.textSize = 19f
        cv.drawText(context.getString(R.string.company_name), tx, logoTop + 22f, p)

        // Sous-titre
        p.color    = C_COPPER_LIGHT
        p.typeface = Typeface.create("serif", Typeface.NORMAL)
        p.textSize = 8.5f
        cv.drawText(
            context.getString(R.string.invoice_tagline).uppercase(),
            tx, logoTop + 36f, p
        )

        // Ligne de séparation fine entre nom et tagline
        p.color       = Color.argb(50, 184, 115, 51)
        p.strokeWidth = 0.5f
        cv.drawLine(tx, logoTop + 40f, tx + 120f, logoTop + 40f, p)

        // FACTURE / Invoice type (droite)
        p.color     = C_WHITE
        p.typeface  = Typeface.create("serif", Typeface.BOLD)
        p.textSize  = 22f
        p.textAlign = Paint.Align.RIGHT
        cv.drawText(
            context.getString(R.string.invoice_label).uppercase(),
            PAGE_W - MARGIN_R, logoTop + 26f, p
        )

        // N° facture
        p.color    = C_COPPER_LIGHT
        p.typeface = Typeface.create("serif", Typeface.NORMAL)
        p.textSize = 9f
        cv.drawText("#${bill.billId}", PAGE_W - MARGIN_R, logoTop + 42f, p)

        p.textAlign = Paint.Align.LEFT
        return 140f
    }

    // =========================================================================
    // Bandeau période (affiché juste sous l'en-tête sombre)
    // =========================================================================

    private fun drawPeriodBanner(
        context: Context, cv: Canvas, p: Paint, bill: Bill, startY: Float
    ): Float {
        val h = 46f

        // Fond blanc pur pour contraster avec l'ivoire du corps
        p.color = C_WHITE
        cv.drawRect(0f, startY, PAGE_W, startY + h, p)

        // Filet cuivre en bas du bandeau
        p.color       = C_COPPER
        p.strokeWidth = 0.8f
        cv.drawLine(0f, startY + h, PAGE_W, startY + h, p)

        // Bande latérale cuivre (continuité avec header)
        p.color = C_COPPER
        cv.drawRect(0f, startY, STRIPE_W, startY + h, p)

        val dateStart = TimeUtils.formatDate(bill.periodStart, "dd MMM yyyy")
        val dateEnd   = TimeUtils.formatDate(bill.periodEnd,   "dd MMM yyyy")

        // Label gauche
        val lx = MARGIN_L
        p.color    = C_TEXT_MUTED
        p.typeface = Typeface.DEFAULT
        p.textSize = 7.5f
        cv.drawText(
            context.getString(R.string.label_period).uppercase(),
            lx, startY + 16f, p
        )

        // Période : "01 juin 2024  →  30 juin 2024"  (ou periodLabel si dispo)
        val periodText = if (bill.periodLabel.isNotBlank()) {
            bill.periodLabel
        } else {
            "$dateStart  →  $dateEnd"
        }
        p.color    = C_CHARCOAL
        p.typeface = Typeface.create("serif", Typeface.BOLD)
        p.textSize = 13f
        cv.drawText(periodText, lx, startY + 34f, p)

        // Dates individuelles en petit (start / end) si periodLabel utilisé
        if (bill.periodLabel.isNotBlank()) {
            p.color    = C_TEXT_MUTED
            p.typeface = Typeface.DEFAULT
            p.textSize = 8f
            cv.drawText("$dateStart  —  $dateEnd", lx + p.measureText(periodText) + 12f, startY + 34f, p)
        }

        // Échéance (dueDate) à droite si non nulle
        if (bill.dueDate > 0L) {
            val dueLabel = context.getString(R.string.label_due_date)
            val dueValue = TimeUtils.formatDate(bill.dueDate, "dd MMM yyyy")

            p.color     = C_TEXT_MUTED
            p.typeface  = Typeface.DEFAULT
            p.textSize  = 7.5f
            p.textAlign = Paint.Align.RIGHT
            cv.drawText(dueLabel.uppercase(), PAGE_W - MARGIN_R, startY + 16f, p)

            p.color    = C_COPPER
            p.typeface = Typeface.create("serif", Typeface.BOLD)
            p.textSize = 12f
            cv.drawText(dueValue, PAGE_W - MARGIN_R, startY + 34f, p)

            p.textAlign = Paint.Align.LEFT
        }

        return startY + h
    }

    private fun drawLogoMark(cv: Canvas, p: Paint, x: Float, y: Float, size: Float) {
        // Carré géométrique simple comme marque
        p.color = Color.argb(40, 255, 255, 255)
        cv.drawRect(x, y, x + size, y + size, p)
        p.color       = C_COPPER
        p.style       = Paint.Style.STROKE
        p.strokeWidth = 1.5f
        cv.drawRect(x + 3f, y + 3f, x + size - 3f, y + size - 3f, p)
        p.style     = Paint.Style.FILL
        p.color     = C_WHITE
        p.typeface  = Typeface.create("serif", Typeface.BOLD)
        p.textSize  = 24f
        p.textAlign = Paint.Align.CENTER
        cv.drawText("S", x + size / 2f, y + size / 2f - (p.descent() + p.ascent()) / 2f, p)
        p.textAlign = Paint.Align.LEFT
    }

    // =========================================================================
    // Bande méta (IDs, devise, etc.)
    // =========================================================================

    private fun drawMetaStrip(
        context: Context, cv: Canvas, p: Paint, bill: Bill, startY: Float
    ): Float {
        val h = 58f

        // Fond légèrement plus foncé que l'ivoire
        p.color = C_IVORY_DARK
        cv.drawRect(0f, startY, PAGE_W, startY + h, p)

        // Filet supérieur cuivre très fin
        p.color       = C_COPPER
        p.strokeWidth = 0.8f
        cv.drawLine(0f, startY, PAGE_W, startY, p)

        // Filet inférieur neutre
        p.color       = C_RULE
        p.strokeWidth = 0.5f
        cv.drawLine(0f, startY + h, PAGE_W, startY + h, p)

        val cols = floatArrayOf(MARGIN_L, PAGE_W * 0.32f, PAGE_W * 0.58f, PAGE_W * 0.78f)
        val labels = arrayOf(
            context.getString(R.string.label_owner_id),
            context.getString(R.string.label_installation_id),
            context.getString(R.string.label_bill_id),
            context.getString(R.string.label_currency)
        )
        val values = arrayOf(bill.ownerId, bill.installationId, bill.billId, bill.currency)

        labels.forEachIndexed { i, lbl ->
            val cx = cols[i]
            p.color    = C_TEXT_MUTED
            p.typeface = Typeface.DEFAULT
            p.textSize = 7.5f
            cv.drawText(lbl.uppercase(), cx, startY + 18f, p)

            p.color    = C_TEXT_BODY
            p.typeface = Typeface.create("serif", Typeface.BOLD)
            p.textSize = 11f
            cv.drawText(values[i], cx, startY + 36f, p)
        }

        // Séparateurs verticaux discrets entre colonnes
        p.color       = C_RULE
        p.strokeWidth = 0.4f
        for (i in 1 until cols.size) {
            cv.drawLine(cols[i] - 10f, startY + 10f, cols[i] - 10f, startY + h - 10f, p)
        }

        return startY + h
    }

    // =========================================================================
    // Section générique avec callback
    // =========================================================================

    private fun drawSection(
        context: Context,
        document: PdfDocument,
        p: Paint,
        title: String,
        startY: Float,
        minHeight: Float,
        content: (Canvas, Paint, Float) -> Float
    ): Float {
        var y = ensureSpace(document, p, startY, minHeight)

        // Titre de section
        p.color    = C_CHARCOAL
        p.typeface = Typeface.create("serif", Typeface.BOLD)
        p.textSize = 11.5f
        currentCanvas!!.drawText(title.uppercase(), MARGIN_L, y, p)

        // Filet sous le titre
        y += 6f
        p.color       = C_COPPER
        p.strokeWidth = 0.8f
        currentCanvas!!.drawLine(MARGIN_L, y, PAGE_W - MARGIN_R, y, p)
        y += 14f

        y = content(currentCanvas!!, p, y)

        // Espace après section
        return y + 20f
    }

    private fun drawSectionHeader(cv: Canvas, p: Paint, title: String, y: Float): Float {
        p.color    = C_CHARCOAL
        p.typeface = Typeface.create("serif", Typeface.BOLD)
        p.textSize = 11.5f
        cv.drawText(title.uppercase(), MARGIN_L, y, p)

        val ry = y + 6f
        p.color       = C_COPPER
        p.strokeWidth = 0.8f
        cv.drawLine(MARGIN_L, ry, PAGE_W - MARGIN_R, ry, p)
        return ry + 14f
    }

    // =========================================================================
    // Ligne de données
    // =========================================================================

    private fun row(
        cv: Canvas, p: Paint,
        label: String, value: String,
        y: Float, alt: Boolean
    ): Float {
        val rowH = 21f
        val top  = y - 13f

        if (alt) {
            p.color = C_IVORY_DARK
            cv.drawRect(MARGIN_L - 4f, top, PAGE_W - MARGIN_R + 4f, top + rowH, p)
        }

        // Filet horizontal discret
        p.color       = C_RULE
        p.strokeWidth = 0.3f
        cv.drawLine(MARGIN_L, top + rowH, PAGE_W - MARGIN_R, top + rowH, p)

        p.color    = C_TEXT_MUTED
        p.typeface = Typeface.DEFAULT
        p.textSize = 10.5f
        cv.drawText(label, MARGIN_L + 4f, y, p)

        p.color     = C_TEXT_BODY
        p.typeface  = Typeface.create("serif", Typeface.BOLD)
        p.textSize  = 10.5f
        p.textAlign = Paint.Align.RIGHT
        cv.drawText(value, PAGE_W - MARGIN_R - 4f, y, p)
        p.textAlign = Paint.Align.LEFT

        return y + rowH
    }

    // =========================================================================
    // Section trace
    // =========================================================================

    private fun drawTraceSection(
        context: Context, document: PdfDocument, p: Paint, bill: Bill, startY: Float
    ): Float {
        var y = ensureSpace(document, p, startY, 80f)
        y = drawSectionHeader(currentCanvas!!, p, context.getString(R.string.section_trace), y)

        val trace = bill.trace

        // ── Bloc dates ───────────────────────────────────────────────────────
        val bTop = y
        val bH   = 62f

        // Fond bloc
        p.color = C_IVORY_DARK
        currentCanvas!!.drawRect(MARGIN_L - 4f, bTop, PAGE_W - MARGIN_R + 4f, bTop + bH, p)

        // Bande cuivre gauche très fine
        p.color = C_COPPER
        currentCanvas!!.drawRect(MARGIN_L - 4f, bTop, MARGIN_L - 4f + 3f, bTop + bH, p)

        val bx = MARGIN_L + 8f

        // Colonne gauche — créé le
        p.color    = C_TEXT_MUTED
        p.typeface = Typeface.DEFAULT
        p.textSize = 8f
        currentCanvas!!.drawText(
            context.getString(R.string.label_trace_created_at).uppercase(),
            bx, bTop + 16f, p
        )
        p.color    = C_TEXT_BODY
        p.typeface = Typeface.create("serif", Typeface.BOLD)
        p.textSize = 10.5f
        currentCanvas!!.drawText(ts(trace.createdAt), bx, bTop + 30f, p)

        // Colonne gauche — mis à jour le
        p.color    = C_TEXT_MUTED
        p.typeface = Typeface.DEFAULT
        p.textSize = 8f
        currentCanvas!!.drawText(
            context.getString(R.string.label_trace_updated_at).uppercase(),
            bx, bTop + 46f, p
        )
        p.color    = C_TEXT_BODY
        p.typeface = Typeface.create("serif", Typeface.BOLD)
        p.textSize = 10.5f
        currentCanvas!!.drawText(ts(trace.updatedAt), bx, bTop + 60f, p)

        // Colonne droite — version et statut
        p.color     = C_TEXT_MUTED
        p.typeface  = Typeface.DEFAULT
        p.textSize  = 8f
        p.textAlign = Paint.Align.RIGHT
        currentCanvas!!.drawText(
            context.getString(R.string.label_trace_version).uppercase(),
            PAGE_W - MARGIN_R - 8f, bTop + 16f, p
        )
        p.color    = C_TEXT_BODY
        p.typeface = Typeface.create("serif", Typeface.BOLD)
        p.textSize = 10.5f
        currentCanvas!!.drawText("v${trace.version}", PAGE_W - MARGIN_R - 8f, bTop + 30f, p)

        val statusLabel = if (trace.active)
            context.getString(R.string.label_trace_active)
        else
            context.getString(R.string.label_trace_inactive)

        p.color    = if (trace.active) C_SUCCESS else C_TEXT_FAINT
        p.typeface = Typeface.DEFAULT
        p.textSize = 8.5f
        currentCanvas!!.drawText(statusLabel, PAGE_W - MARGIN_R - 8f, bTop + 56f, p)
        p.textAlign = Paint.Align.LEFT

        y = bTop + bH + 18f

        // ── Acteurs ───────────────────────────────────────────────────────────
        if (trace.createdById.isNotBlank() || trace.updatedById.isNotBlank()) {
            y = ensureSpace(document, p, y, 50f)

            val lx = MARGIN_L
            val rx = PAGE_W / 2 + 8f

            if (trace.createdById.isNotBlank()) {
                p.color    = C_TEXT_MUTED
                p.typeface = Typeface.DEFAULT
                p.textSize = 8f
                currentCanvas!!.drawText(
                    context.getString(R.string.label_trace_created_by).uppercase(),
                    lx, y, p
                )
                p.color    = C_TEXT_BODY
                p.typeface = Typeface.create("serif", Typeface.BOLD)
                p.textSize = 10.5f
                currentCanvas!!.drawText(trace.createdById, lx, y + 14f, p)
                if (trace.createdByRole.isNotBlank()) {
                    p.color    = C_TEXT_FAINT
                    p.typeface = Typeface.DEFAULT
                    p.textSize = 8.5f
                    currentCanvas!!.drawText(trace.createdByRole, lx, y + 26f, p)
                }
            }

            if (trace.updatedById.isNotBlank()) {
                p.color    = C_TEXT_MUTED
                p.typeface = Typeface.DEFAULT
                p.textSize = 8f
                currentCanvas!!.drawText(
                    context.getString(R.string.label_trace_updated_by).uppercase(),
                    rx, y, p
                )
                p.color    = C_TEXT_BODY
                p.typeface = Typeface.create("serif", Typeface.BOLD)
                p.textSize = 10.5f
                currentCanvas!!.drawText(trace.updatedById, rx, y + 14f, p)
                if (trace.updatedByRole.isNotBlank()) {
                    p.color    = C_TEXT_FAINT
                    p.typeface = Typeface.DEFAULT
                    p.textSize = 8.5f
                    currentCanvas!!.drawText(trace.updatedByRole, rx, y + 26f, p)
                }
            }

            // Filet séparateur entre colonnes
            p.color       = C_RULE
            p.strokeWidth = 0.4f
            currentCanvas!!.drawLine(PAGE_W / 2 - 2f, y - 4f, PAGE_W / 2 - 2f, y + 34f, p)

            y += 44f
        }

        // ── Étapes (steps) ────────────────────────────────────────────────────
        val steps = bill.metadata?.trace ?: emptyList()
        if (steps.isNotEmpty()) {
            y += 10f
            steps.forEachIndexed { idx, step ->
                y = ensureSpace(document, p, y, 24f)

                val cv = currentCanvas!!

                // Numéro dans carré cuivre
                p.color = C_COPPER
                p.style = Paint.Style.FILL
                cv.drawRect(MARGIN_L, y - 12f, MARGIN_L + 14f, y + 2f, p)

                p.color     = C_WHITE
                p.typeface  = Typeface.DEFAULT_BOLD
                p.textSize  = 7.5f
                p.textAlign = Paint.Align.CENTER
                cv.drawText(
                    "${idx + 1}",
                    MARGIN_L + 7f,
                    y - 12f + 7f - (p.descent() + p.ascent()) / 2f,
                    p
                )
                p.textAlign = Paint.Align.LEFT

                p.color    = C_TEXT_BODY
                p.typeface = Typeface.DEFAULT
                p.textSize = 10f
                cv.drawText(step, MARGIN_L + 22f, y, p)

                // Filet fin sous chaque étape
                p.color       = C_RULE
                p.strokeWidth = 0.3f
                cv.drawLine(MARGIN_L + 22f, y + 4f, PAGE_W - MARGIN_R, y + 4f, p)

                y += 22f
            }
        }

        return y + 16f
    }

    // =========================================================================
    // Zone de pied de page (total + footer)
    // =========================================================================

    private fun drawFooterArea(
        context: Context, cv: Canvas, p: Paint, bill: Bill, generatedAt: Long
    ) {
        val totalH   = 52f
        val footerH  = 36f
        val totalTop = PAGE_H - footerH - totalH - 6f

        // ── Bloc total ────────────────────────────────────────────────────────
        // Fond charbon
        p.color = C_CHARCOAL
        cv.drawRect(0f, totalTop, PAGE_W, totalTop + totalH, p)

        // Bande cuivre gauche
        p.color = C_COPPER
        cv.drawRect(0f, totalTop, STRIPE_W, totalTop + totalH, p)

        // Filet cuivre supérieur
        p.color       = C_COPPER
        p.strokeWidth = 1f
        cv.drawLine(0f, totalTop, PAGE_W, totalTop, p)

        // Label total
        p.color    = C_COPPER_LIGHT
        p.typeface = Typeface.DEFAULT
        p.textSize = 8.5f
        cv.drawText(
            context.getString(R.string.label_total_due).uppercase(),
            MARGIN_L, totalTop + 18f, p
        )

        // Montant
        val amount = "${bill.amountToPay.roundToInt()} ${bill.currency}"
        p.color    = C_WHITE
        p.typeface = Typeface.create("serif", Typeface.BOLD)
        p.textSize = if (p.measureText(amount) > CONTENT_W - 130f) 17f else 22f
        cv.drawText(amount, MARGIN_L, totalTop + 40f, p)

        // Statut (droite)
        p.color     = C_SUCCESS
        p.typeface  = Typeface.DEFAULT
        p.textSize  = 9f
        p.textAlign = Paint.Align.RIGHT
        cv.drawText(
            context.getString(R.string.label_status_generated),
            PAGE_W - MARGIN_R, totalTop + totalH / 2 + 4f, p
        )
        p.textAlign = Paint.Align.LEFT

        // ── Footer ────────────────────────────────────────────────────────────
        val ftop = PAGE_H - footerH

        p.color       = C_RULE
        p.strokeWidth = 0.4f
        cv.drawLine(MARGIN_L, ftop + 4f, PAGE_W - MARGIN_R, ftop + 4f, p)

        p.color     = C_TEXT_MUTED
        p.typeface  = Typeface.DEFAULT
        p.textSize  = 7.5f
        p.textAlign = Paint.Align.CENTER
        cv.drawText(
            context.getString(R.string.pdf_footer_confidential),
            PAGE_W / 2f, ftop + 17f, p
        )

        p.color    = C_TEXT_FAINT
        p.textSize = 7f
        cv.drawText(
            context.getString(
                R.string.label_pdf_generated_at,
                TimeUtils.formatDate(generatedAt, "dd/MM/yyyy HH:mm:ss")
            ),
            PAGE_W / 2f, ftop + 28f, p
        )
        p.textAlign = Paint.Align.LEFT
    }
}