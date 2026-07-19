package com.maangatech.gojuagent.core.designsystem.component

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.maangatech.gojuagent.core.designsystem.theme.GojuErrorContainer
import com.maangatech.gojuagent.core.designsystem.theme.GojuPending
import com.maangatech.gojuagent.core.designsystem.theme.GojuPendingContainer
import com.maangatech.gojuagent.core.designsystem.theme.GojuSuccess
import com.maangatech.gojuagent.core.designsystem.theme.GojuSuccessContainer
import com.maangatech.gojuagent.core.designsystem.theme.GojuWarning
import com.maangatech.gojuagent.core.designsystem.theme.GojuWarningContainer

/** Mirrors transaction/sync lifecycle states used across history, execution, and sync screens. */
enum class StatusTone { SUCCESS, PENDING, FAILED, WARNING }

@Composable
fun StatusBadge(text: String, tone: StatusTone, modifier: Modifier = Modifier) {
    val (container, content) = when (tone) {
        StatusTone.SUCCESS -> GojuSuccessContainer to GojuSuccess
        StatusTone.PENDING -> GojuPendingContainer to GojuPending
        StatusTone.FAILED -> GojuErrorContainer to androidx.compose.ui.graphics.Color(0xFF8C1D18)
        StatusTone.WARNING -> GojuWarningContainer to GojuWarning
    }
    Surface(
        modifier = modifier,
        color = container,
        contentColor = content,
        shape = RoundedCornerShape(50),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
        )
    }
}
