package de.mo.bibliothek.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.View
import android.widget.RemoteViews
import de.mo.bibliothek.MainActivity
import de.mo.bibliothek.R
import de.mo.bibliothek.data.QuoteStore
import de.mo.bibliothek.data.pickQuote
import java.util.Locale

/**
 * Startbildschirm-Widget mit belegten Zitaten aus der eigenen Bibliothek.
 * Wechsel ca. alle 3 Stunden (System-Takt) und beim Antippen des Zitats; Titel öffnet das Buch in Warraq.
 */
class QuoteWidget : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) = render(context, manager, ids)

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_NEXT) updateAll(context)
    }

    companion object {
        const val ACTION_NEXT = "de.mo.bibliothek.widget.NEXT_QUOTE"
        const val EXTRA_BOOK = "de.mo.bibliothek.OPEN_BOOK"

        fun ids(context: Context): IntArray =
            AppWidgetManager.getInstance(context).getAppWidgetIds(ComponentName(context, QuoteWidget::class.java))

        fun updateAll(context: Context) {
            val ids = ids(context)
            if (ids.isNotEmpty()) render(context, AppWidgetManager.getInstance(context), ids)
        }

        private fun arabic(context: Context): Boolean =
            when (context.getSharedPreferences("bookie", Context.MODE_PRIVATE).getString("language", "system")) {
                "ar" -> true
                "de" -> false
                else -> Locale.getDefault().language == "ar"
            }

        private fun render(context: Context, manager: AppWidgetManager, ids: IntArray) {
            val prefs = context.getSharedPreferences("widget", Context.MODE_PRIVATE)
            val quote = pickQuote(QuoteStore(context).quotes(), prefs.getString("last", null))
            prefs.edit().putString("last", quote?.text).apply()
            val ar = arabic(context)
            val next = PendingIntent.getBroadcast(context, 0,
                Intent(context, QuoteWidget::class.java).setAction(ACTION_NEXT),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
            for (id in ids) {
                val views = RemoteViews(context.packageName, R.layout.quote_widget)
                if (quote == null) {
                    views.setTextViewText(R.id.quote_text, if (ar) "ستظهر هنا اقتباسات موثقة من كتبك عندما تفتحها في ورّاق."
                        else "Hier erscheinen belegte Zitate aus deinen Büchern, sobald du sie in Warraq öffnest.")
                    views.setViewVisibility(R.id.quote_book, View.GONE)
                    views.setViewVisibility(R.id.quote_source, View.GONE)
                    views.setOnClickPendingIntent(R.id.quote_root, PendingIntent.getActivity(context, 1,
                        Intent(context, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT))
                } else {
                    views.setTextViewText(R.id.quote_text, "„${quote.text}“".let { if (quote.source.endsWith("AR")) "«${quote.text}»" else it })
                    views.setViewVisibility(R.id.quote_book, View.VISIBLE)
                    views.setTextViewText(R.id.quote_book, listOf(quote.title, quote.author).filter { it.isNotBlank() }.joinToString(" · "))
                    views.setViewVisibility(R.id.quote_source, View.VISIBLE)
                    views.setTextViewText(R.id.quote_source, quote.source + if (ar) " · المس للتبديل" else " · Tippen für ein neues Zitat")
                    views.setOnClickPendingIntent(R.id.quote_root, next)
                    val open = Intent(context, MainActivity::class.java)
                        .setAction(Intent.ACTION_VIEW)
                        .setData(Uri.parse("warraq://book/${Uri.encode(quote.bookId)}"))
                        .putExtra(EXTRA_BOOK, quote.bookId)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    views.setOnClickPendingIntent(R.id.quote_book, PendingIntent.getActivity(context, 2, open,
                        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT))
                }
                manager.updateAppWidget(id, views)
            }
        }
    }
}
