package com.bansify.ai.core.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bansify.ai.core.designsystem.BansifyRadius
import com.bansify.ai.core.designsystem.BansifyTextStyles
import com.bansify.ai.core.designsystem.BansifyThemeTokens
import com.bansify.ai.core.designsystem.Space

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SheetScaffold(
    onDismiss: () -> Unit,
    title: String? = null,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false),
    content: @Composable ColumnScope.() -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = BansifyRadius.Sheet,
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        scrimColor = BansifyThemeTokens.extendedColors.scrim,
        dragHandle = {
            BottomSheetDefaults.DragHandle(
                color = MaterialTheme.colorScheme.outline
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = Space.md)
        ) {
            if (title != null) {
                Text(
                    text = title,
                    style = BansifyTextStyles.H2,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(Space.sm))
            }
            content()
            Spacer(Modifier.height(Space.lg))
        }
    }
}
