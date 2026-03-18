package com.syme.utils

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File

object EmailSender {

    fun send(
        context: Context,
        file: File,
        email: String,
        billId: String
    ) {

        val uri =
            FileProvider.getUriForFile(
                context,
                context.packageName + ".provider",
                file
            )

        val intent = Intent(Intent.ACTION_SEND).apply {

            type = "application/pdf"

            putExtra(
                Intent.EXTRA_EMAIL,
                arrayOf(email)
            )

            putExtra(
                Intent.EXTRA_SUBJECT,
                "Electricity bill $billId"
            )

            putExtra(
                Intent.EXTRA_TEXT,
                "Please find attached your electricity bill."
            )

            putExtra(
                Intent.EXTRA_STREAM,
                uri
            )

            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(
            Intent.createChooser(
                intent,
                "Send bill via"
            )
        )
    }
}