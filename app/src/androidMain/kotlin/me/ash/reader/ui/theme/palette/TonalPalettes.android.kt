package me.ash.reader.ui.theme.palette

import android.content.Context
import android.os.Build
import androidx.annotation.ColorRes
import androidx.annotation.DoNotInline
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color

@RequiresApi(Build.VERSION_CODES.S)
private fun primarySystem(context: Context, tone: TonalValue): Color = when (tone) {
    0 -> ColorResourceHelper.getColor(context, android.R.color.system_accent1_1000)
    10 -> ColorResourceHelper.getColor(context, android.R.color.system_accent1_900)
    20 -> ColorResourceHelper.getColor(context, android.R.color.system_accent1_800)
    30 -> ColorResourceHelper.getColor(context, android.R.color.system_accent1_700)
    40 -> ColorResourceHelper.getColor(context, android.R.color.system_accent1_600)
    50 -> ColorResourceHelper.getColor(context, android.R.color.system_accent1_500)
    60 -> ColorResourceHelper.getColor(context, android.R.color.system_accent1_400)
    70 -> ColorResourceHelper.getColor(context, android.R.color.system_accent1_300)
    80 -> ColorResourceHelper.getColor(context, android.R.color.system_accent1_200)
    90 -> ColorResourceHelper.getColor(context, android.R.color.system_accent1_100)
    95 -> ColorResourceHelper.getColor(context, android.R.color.system_accent1_50)
    99 -> ColorResourceHelper.getColor(context, android.R.color.system_accent1_10)
    100 -> ColorResourceHelper.getColor(context, android.R.color.system_accent1_0)
    else -> throw IllegalArgumentException("Unknown primary tone: $tone")
}

@RequiresApi(Build.VERSION_CODES.S)
private fun secondarySystem(context: Context, tone: TonalValue): Color = when (tone) {
    0 -> ColorResourceHelper.getColor(context, android.R.color.system_accent2_1000)
    10 -> ColorResourceHelper.getColor(context, android.R.color.system_accent2_900)
    20 -> ColorResourceHelper.getColor(context, android.R.color.system_accent2_800)
    30 -> ColorResourceHelper.getColor(context, android.R.color.system_accent2_700)
    40 -> ColorResourceHelper.getColor(context, android.R.color.system_accent2_600)
    50 -> ColorResourceHelper.getColor(context, android.R.color.system_accent2_500)
    60 -> ColorResourceHelper.getColor(context, android.R.color.system_accent2_400)
    70 -> ColorResourceHelper.getColor(context, android.R.color.system_accent2_300)
    80 -> ColorResourceHelper.getColor(context, android.R.color.system_accent2_200)
    90 -> ColorResourceHelper.getColor(context, android.R.color.system_accent2_100)
    95 -> ColorResourceHelper.getColor(context, android.R.color.system_accent2_50)
    99 -> ColorResourceHelper.getColor(context, android.R.color.system_accent2_10)
    100 -> ColorResourceHelper.getColor(context, android.R.color.system_accent2_0)
    else -> throw IllegalArgumentException("Unknown secondary tone: $tone")
}

@RequiresApi(Build.VERSION_CODES.S)
private fun tertiarySystem(context: Context, tone: TonalValue): Color = when (tone) {
    0 -> ColorResourceHelper.getColor(context, android.R.color.system_accent3_1000)
    10 -> ColorResourceHelper.getColor(context, android.R.color.system_accent3_900)
    20 -> ColorResourceHelper.getColor(context, android.R.color.system_accent3_800)
    30 -> ColorResourceHelper.getColor(context, android.R.color.system_accent3_700)
    40 -> ColorResourceHelper.getColor(context, android.R.color.system_accent3_600)
    50 -> ColorResourceHelper.getColor(context, android.R.color.system_accent3_500)
    60 -> ColorResourceHelper.getColor(context, android.R.color.system_accent3_400)
    70 -> ColorResourceHelper.getColor(context, android.R.color.system_accent3_300)
    80 -> ColorResourceHelper.getColor(context, android.R.color.system_accent3_200)
    90 -> ColorResourceHelper.getColor(context, android.R.color.system_accent3_100)
    95 -> ColorResourceHelper.getColor(context, android.R.color.system_accent3_50)
    99 -> ColorResourceHelper.getColor(context, android.R.color.system_accent3_10)
    100 -> ColorResourceHelper.getColor(context, android.R.color.system_accent3_0)
    else -> throw IllegalArgumentException("Unknown tertiary tone: $tone")
}

@RequiresApi(Build.VERSION_CODES.S)
private fun neutralSystem(context: Context, tone: TonalValue): Color = when (tone) {
    0 -> ColorResourceHelper.getColor(context, android.R.color.system_neutral1_1000)
    10 -> ColorResourceHelper.getColor(context, android.R.color.system_neutral1_900)
    20 -> ColorResourceHelper.getColor(context, android.R.color.system_neutral1_800)
    30 -> ColorResourceHelper.getColor(context, android.R.color.system_neutral1_700)
    40 -> ColorResourceHelper.getColor(context, android.R.color.system_neutral1_600)
    50 -> ColorResourceHelper.getColor(context, android.R.color.system_neutral1_500)
    60 -> ColorResourceHelper.getColor(context, android.R.color.system_neutral1_400)
    70 -> ColorResourceHelper.getColor(context, android.R.color.system_neutral1_300)
    80 -> ColorResourceHelper.getColor(context, android.R.color.system_neutral1_200)
    90 -> ColorResourceHelper.getColor(context, android.R.color.system_neutral1_100)
    95 -> ColorResourceHelper.getColor(context, android.R.color.system_neutral1_50)
    99 -> ColorResourceHelper.getColor(context, android.R.color.system_neutral1_10)
    100 -> ColorResourceHelper.getColor(context, android.R.color.system_neutral1_0)
    else -> throw IllegalArgumentException("Unknown neutral tone: $tone")
}

@RequiresApi(Build.VERSION_CODES.S)
private fun neutralVariantSystem(context: Context, tone: TonalValue): Color = when (tone) {
    0 -> ColorResourceHelper.getColor(context, android.R.color.system_neutral2_1000)
    10 -> ColorResourceHelper.getColor(context, android.R.color.system_neutral2_900)
    20 -> ColorResourceHelper.getColor(context, android.R.color.system_neutral2_800)
    30 -> ColorResourceHelper.getColor(context, android.R.color.system_neutral2_700)
    40 -> ColorResourceHelper.getColor(context, android.R.color.system_neutral2_600)
    50 -> ColorResourceHelper.getColor(context, android.R.color.system_neutral2_500)
    60 -> ColorResourceHelper.getColor(context, android.R.color.system_neutral2_400)
    70 -> ColorResourceHelper.getColor(context, android.R.color.system_neutral2_300)
    80 -> ColorResourceHelper.getColor(context, android.R.color.system_neutral2_200)
    90 -> ColorResourceHelper.getColor(context, android.R.color.system_neutral2_100)
    95 -> ColorResourceHelper.getColor(context, android.R.color.system_neutral2_50)
    99 -> ColorResourceHelper.getColor(context, android.R.color.system_neutral2_10)
    100 -> ColorResourceHelper.getColor(context, android.R.color.system_neutral2_0)
    else -> throw IllegalArgumentException("Unknown neutral variant tone: $tone")
}

@RequiresApi(Build.VERSION_CODES.S)
@Composable
@Stable
fun Context.getSystemTonalPalettes(): TonalPalettes {
    val tonalPalettes = TonalPalettes(238.36, 15.0)
    tonalTokens.forEach {
        tonalPalettes.primary[it] = primarySystem(this, it)
    }
    tonalTokens.forEach {
        tonalPalettes.secondary[it] = secondarySystem(this, it)
    }
    tonalTokens.forEach {
        tonalPalettes.tertiary[it] = tertiarySystem(this, it)
    }
    tonalTokens.forEach {
        tonalPalettes.neutral[it] = neutralSystem(this, it)
    }
    tonalTokens.forEach {
        tonalPalettes.neutralVariant[it] = neutralVariantSystem(this, it)
    }
    tonalTokens.forEach {
        tonalPalettes error it
    }
    return tonalPalettes
}

@RequiresApi(23)
object ColorResourceHelper {

    @DoNotInline
    fun getColor(context: Context, @ColorRes id: Int): Color {
        return Color(context.resources.getColor(id, context.theme))
    }
}
