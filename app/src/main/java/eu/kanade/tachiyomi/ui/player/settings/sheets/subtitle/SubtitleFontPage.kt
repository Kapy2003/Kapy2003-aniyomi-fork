package eu.kanade.tachiyomi.ui.player.settings.sheets.subtitle

import android.os.Environment
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.FormatBold
import androidx.compose.material.icons.outlined.FormatItalic
import androidx.compose.material.icons.outlined.FormatSize
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.yubyf.truetypeparser.TTFFile
import eu.kanade.presentation.components.DropdownMenu
import eu.kanade.presentation.components.OutlinedNumericChooser
import eu.kanade.presentation.util.collectAsState
import eu.kanade.tachiyomi.R
import eu.kanade.tachiyomi.ui.player.settings.PlayerPreferences
import eu.kanade.tachiyomi.ui.player.settings.PlayerSettingsScreenModel
import `is`.xyz.mpv.MPVLib
import tachiyomi.presentation.core.components.material.ReadItemAlpha
import tachiyomi.presentation.core.components.material.padding
import java.io.File

@Composable
fun SubtitleFontPage(screenModel: PlayerSettingsScreenModel) {
    screenModel.OverrideSubtitlesSwitch {
        SubtitleFont(screenModel = screenModel)
    }
}

@Composable
private fun SubtitleFont(
    screenModel: PlayerSettingsScreenModel,
) {
    val font by screenModel.preferences.subtitleFont().collectAsState()
    val boldSubtitles by screenModel.preferences.boldSubtitles().collectAsState()
    val italicSubtitles by screenModel.preferences.italicSubtitles().collectAsState()
    val subtitleFontSize by screenModel.preferences.subtitleFontSize().collectAsState()
    val textColor by screenModel.preferences.textColorSubtitles().collectAsState()
    val borderColor by screenModel.preferences.borderColorSubtitles().collectAsState()
    val backgroundColor by screenModel.preferences.backgroundColorSubtitles().collectAsState()

    val updateBold = {
        val toBold = if (boldSubtitles) "no" else "yes"
        screenModel.togglePreference(PlayerPreferences::boldSubtitles)
        MPVLib.setPropertyString("sub-bold", toBold)
    }

    val updateItalic = {
        val toItalicize = if (italicSubtitles) "no" else "yes"
        screenModel.togglePreference(PlayerPreferences::italicSubtitles)
        MPVLib.setPropertyString("sub-italic", toItalicize)
    }

    val onSizeChanged: (Int) -> Unit = {
        MPVLib.setPropertyInt("sub-font-size", it)
        screenModel.preferences.subtitleFontSize().set(it)
    }

    val updateFont: (String) -> Unit = {
        MPVLib.setPropertyString("sub-font", it)
        screenModel.preferences.subtitleFont().set(it)
    }

    val context = LocalContext.current
    val fontList by remember {
        derivedStateOf {
            val customFonts = File(
                Environment.getExternalStorageDirectory().absolutePath +
                    File.separator + context.getString(R.string.app_name) +
                    File.separator,
                "fonts",
            ).listFiles { file ->
                file.extension.equals("ttf", true) ||
                    file.extension.equals("otf", true)
            }?.associate {
                TTFFile.open(it).families.values.toTypedArray()[0] to it.absolutePath
            } ?: emptyMap()
            mapOf("Sans Serif" to ("" to null)) + customFonts
        }
    }
    var selectingFont by remember { mutableStateOf(false) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.padding.tiny),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth(),
        ) {
            IconButton(onClick = { selectingFont = true }) {
                Icon(
                    imageVector = Icons.Outlined.FormatSize,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                )
            }

            OutlinedNumericChooser(
                label = stringResource(id = R.string.player_font_size_text_field),
                placeholder = "55",
                suffix = "",
                value = subtitleFontSize,
                step = 1,
                min = 1,
                onValueChanged = onSizeChanged,
            )

            val boldAlpha = if (boldSubtitles) 1f else ReadItemAlpha
            Icon(
                imageVector = Icons.Outlined.FormatBold,
                contentDescription = null,
                modifier = Modifier
                    .alpha(boldAlpha)
                    .size(32.dp)
                    .clickable(onClick = updateBold),
            )

            val italicAlpha = if (italicSubtitles) 1f else ReadItemAlpha
            Icon(
                imageVector = Icons.Outlined.FormatItalic,
                contentDescription = null,
                modifier = Modifier
                    .alpha(italicAlpha)
                    .size(32.dp)
                    .clickable(onClick = updateItalic),
            )
        }

        DropdownMenu(expanded = selectingFont, onDismissRequest = { selectingFont = false }) {
            fontList.map {
                val fontName = it.key
                DropdownMenuItem(
                    text = { Text(fontName) },
                    onClick = { updateFont(fontName) },
                    trailingIcon = {
                        if (font == fontName) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                            )
                        }
                    },
                )
            }
        }

        SubtitlePreview(
            font = font,
            isBold = boldSubtitles,
            isItalic = italicSubtitles,
            textColor = Color(textColor),
            borderColor = Color(borderColor),
            backgroundColor = Color(backgroundColor),
        )
    }
}
