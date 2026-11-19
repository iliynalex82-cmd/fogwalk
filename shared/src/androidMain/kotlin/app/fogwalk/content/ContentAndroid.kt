package app.fogwalk.content

import java.util.Locale

fun Content.Companion.of(locale: Locale): Content = of(locale.language)
