package com.budgettracker.app.util

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols

data class CurrencyInfo(
    val code: String,
    val name: String,
    val symbol: String,
    val usdRate: Double,
    val minorDigits: Int = 2,
)

/**
 * Offline currency catalogue. [CurrencyInfo.usdRate] = value of 1 USD in this
 * currency (approximate snapshot, editable in future). All cross-rates are
 * derived through USD.
 */
object Currencies {
    val USD = CurrencyInfo("USD", "US Dollar", "$", 1.0)
    val DEFAULT = USD

    val all: List<CurrencyInfo> = listOf(
        CurrencyInfo("USD", "US Dollar", "$", 1.0),
        CurrencyInfo("EUR", "Euro", "€", 0.93),
        CurrencyInfo("GBP", "British Pound", "£", 0.79),
        CurrencyInfo("JPY", "Japanese Yen", "¥", 152.0, 0),
        CurrencyInfo("CNY", "Chinese Yuan", "¥", 7.25),
        CurrencyInfo("INR", "Indian Rupee", "₹", 84.0),
        CurrencyInfo("NPR", "Nepalese Rupee", "Rs", 135.0),
        CurrencyInfo("AUD", "Australian Dollar", "A$", 1.52),
        CurrencyInfo("CAD", "Canadian Dollar", "C$", 1.36),
        CurrencyInfo("CHF", "Swiss Franc", "CHF", 0.88),
        CurrencyInfo("SEK", "Swedish Krona", "kr", 10.5),
        CurrencyInfo("NOK", "Norwegian Krone", "kr", 10.8),
        CurrencyInfo("DKK", "Danish Krone", "kr", 6.9),
        CurrencyInfo("NZD", "New Zealand Dollar", "NZ$", 1.65),
        CurrencyInfo("SGD", "Singapore Dollar", "S$", 1.34),
        CurrencyInfo("HKD", "Hong Kong Dollar", "HK$", 7.8),
        CurrencyInfo("KRW", "South Korean Won", "₩", 1380.0, 0),
        CurrencyInfo("THB", "Thai Baht", "฿", 34.0),
        CurrencyInfo("MYR", "Malaysian Ringgit", "RM", 4.5),
        CurrencyInfo("IDR", "Indonesian Rupiah", "Rp", 15800.0, 0),
        CurrencyInfo("PHP", "Philippine Peso", "₱", 58.0),
        CurrencyInfo("VND", "Vietnamese Dong", "₫", 25400.0, 0),
        CurrencyInfo("AED", "UAE Dirham", "د.إ", 3.67),
        CurrencyInfo("SAR", "Saudi Riyal", "﷼", 3.75),
        CurrencyInfo("PKR", "Pakistani Rupee", "Rs", 278.0),
        CurrencyInfo("BDT", "Bangladeshi Taka", "৳", 118.0),
        CurrencyInfo("LKR", "Sri Lankan Rupee", "Rs", 300.0),
        CurrencyInfo("BRL", "Brazilian Real", "R$", 5.8),
        CurrencyInfo("MXN", "Mexican Peso", "MX$", 20.0),
        CurrencyInfo("ZAR", "South African Rand", "R", 18.0),
        CurrencyInfo("TRY", "Turkish Lira", "₺", 34.0),
        CurrencyInfo("PLN", "Polish Zloty", "zł", 4.0),
        CurrencyInfo("CZK", "Czech Koruna", "Kč", 23.0),
        CurrencyInfo("HUF", "Hungarian Forint", "Ft", 360.0, 0),
        CurrencyInfo("ILS", "Israeli Shekel", "₪", 3.7),
        CurrencyInfo("EGP", "Egyptian Pound", "E£", 48.0),
        CurrencyInfo("NGN", "Nigerian Naira", "₦", 1650.0),
        CurrencyInfo("KES", "Kenyan Shilling", "KSh", 129.0),
        CurrencyInfo("UAH", "Ukrainian Hryvnia", "₴", 41.0),
    )

    private val byCode = all.associateBy { it.code }

    fun byCode(code: String): CurrencyInfo = byCode[code] ?: DEFAULT
}

/** Convert a minor-unit amount between currencies using USD cross rates. */
fun convertMinor(amountMinor: Long, from: CurrencyInfo, to: CurrencyInfo): Long {
    if (from.code == to.code || amountMinor == 0L) return amountMinor
    val scaleFrom = BigDecimal.TEN.pow(from.minorDigits)
    val scaleTo = BigDecimal.TEN.pow(to.minorDigits)
    val major = BigDecimal(amountMinor).divide(scaleFrom, 12, RoundingMode.HALF_UP)
    val converted = major.multiply(BigDecimal(to.usdRate)).divide(BigDecimal(from.usdRate), 12, RoundingMode.HALF_UP)
    return converted.multiply(scaleTo).setScale(0, RoundingMode.HALF_UP).toLong()
}

/** Format a minor-unit amount as a display string, e.g. "$1,234.56". */
fun formatMoney(
    amountMinor: Long,
    currency: CurrencyInfo,
    showSymbol: Boolean = true,
    signed: Boolean = false,
): String {
    val major = BigDecimal(amountMinor).divide(BigDecimal.TEN.pow(currency.minorDigits), currency.minorDigits, RoundingMode.HALF_UP)
    val symbols = DecimalFormatSymbols().apply {
        groupingSeparator = ','
        decimalSeparator = '.'
    }
    val pattern = if (currency.minorDigits == 0) "#,##0" else "#,##0." + "0".repeat(currency.minorDigits)
    val formatted = DecimalFormat(pattern, symbols).format(major.abs())
    val sign = when {
        amountMinor < 0 -> "-"
        signed && amountMinor > 0 -> "+"
        else -> ""
    }
    val body = if (showSymbol) "${currency.symbol}$formatted" else formatted
    return "$sign$body"
}

/** Parse user-typed amount text into minor units. Returns null when invalid. */
fun parseAmountMinor(input: String, currency: CurrencyInfo): Long? {
    val cleaned = input.replace(",", ".").replace(Regex("[^0-9.]"), "").trim()
    if (cleaned.isEmpty() || cleaned == ".") return null
    val parts = cleaned.split(".")
    if (parts.size > 2) return null
    val whole = parts[0].ifEmpty { "0" }
    if (whole.length > 12) return null
    val frac = parts.getOrNull(1).orEmpty().take(currency.minorDigits)
    val paddedFrac = frac.padEnd(currency.minorDigits, '0')
    val value = (whole + paddedFrac).toLongOrNull() ?: return null
    return if (value <= 0) null else value
}
