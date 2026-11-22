package com.bansify.ai.feature.settings

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bansify.ai.core.designsystem.BansifyRadius
import com.bansify.ai.core.designsystem.BansifyTextButton
import com.bansify.ai.core.designsystem.BansifyTextStyles
import com.bansify.ai.core.designsystem.PrimaryButton
import com.bansify.ai.core.designsystem.SectionHeader
import com.bansify.ai.core.designsystem.Space
import com.bansify.ai.core.designsystem.TextFieldOutlined
import com.bansify.ai.core.ui.BansifyTopBar
import com.bansify.ai.data.ai.AiProviders
import com.bansify.ai.data.mock.ProviderGroup
import com.bansify.ai.data.repo.ModelRepository
import kotlinx.coroutines.launch
import androidx.compose.foundation.text.KeyboardOptions

/**
 * Bring-your-own-key screen. Every provider listed here has a card-free tier; the key is
 * stored on this device only and is sent only to that provider.
 */
@Composable
fun ProvidersScreen(
    modelRepo: ModelRepository,
    onBack: () -> Unit
) {
    val providers by modelRepo.providers.collectAsStateWithLifecycle()
    val keys by modelRepo.keys.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val drafts = remember { mutableStateMapOf<String, String>() }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { BansifyTopBar(title = "AI providers", onBack = onBack) }
    ) { innerPadding ->
        LazyColumn(
            contentPadding = androidx.compose.foundation.layout.PaddingValues(Space.md),
            verticalArrangement = Arrangement.spacedBy(Space.md),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("providers_screen")
        ) {
            item(key = "intro") {
                Text(
                    text = "Bansify Cloud is built in and needs no key. You can also add your own free key from any provider below; Auto mode uses Bansify Cloud first, then the first provider that has a key.",
                    style = BansifyTextStyles.Body,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            items(providers, key = { it.id }) { group ->
                ProviderCard(
                    group = group,
                    savedKey = keys[group.id].orEmpty(),
                    draft = drafts[group.id] ?: "",
                    onDraftChange = { drafts[group.id] = it },
                    onSave = {
                        val value = (drafts[group.id] ?: "").trim()
                        if (value.isNotEmpty()) {
                            scope.launch {
                                modelRepo.saveKey(group.id, value)
                                drafts[group.id] = ""
                            }
                        }
                    },
                    onRemove = { scope.launch { modelRepo.clearKey(group.id) } },
                    onOpenKeyPage = {
                        val provider = AiProviders.byId(group.id)
                        val url = provider?.keyUrl ?: return@ProviderCard
                        if (url.startsWith("http")) {
                            context.startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun ProviderCard(
    group: ProviderGroup,
    savedKey: String,
    draft: String,
    onDraftChange: (String) -> Unit,
    onSave: () -> Unit,
    onRemove: () -> Unit,
    onOpenKeyPage: () -> Unit
) {
    val provider = AiProviders.byId(group.id)
    SectionHeader(title = group.name)
    Surface(
        shape = BansifyRadius.Card,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        tonalElevation = 0.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(Space.md)) {
            Text(
                text = provider?.freeTier ?: group.freeTierSummary,
                style = BansifyTextStyles.Caption,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(Space.xs))
            Text(
                text = group.models.joinToString(" · ") { it.name },
                style = BansifyTextStyles.Caption,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(Space.sm))
            if (provider?.requiresKey == false) {
                Text(
                    text = "Built in. The Worker already holds the provider keys, so nothing to paste here.",
                    style = BansifyTextStyles.Caption,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                TextFieldOutlined(
                    value = draft,
                    onValueChange = onDraftChange,
                    placeholder = if (savedKey.isBlank()) {
                        provider?.keyLabel ?: "API key"
                    } else {
                        "Key saved (${savedKey.take(4)}…) — paste a new one to replace"
                    },
                    label = provider?.keyLabel ?: "API key",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    testTag = "provider_key_${group.id}"
                )
                Spacer(Modifier.height(Space.sm))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(Space.xs),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    PrimaryButton(
                        text = if (savedKey.isBlank()) "Save key" else "Replace key",
                        onClick = onSave,
                        enabled = draft.isNotBlank(),
                        modifier = Modifier.weight(1f)
                    )
                    if (savedKey.isNotBlank()) {
                        BansifyTextButton(text = "Remove", onClick = onRemove)
                    }
                }
            }
            if (provider != null) {
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.padding(vertical = Space.xs)
                )
                Text(
                    text = "Get a key: ${provider.keyUrl}",
                    style = BansifyTextStyles.Caption,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onOpenKeyPage)
                        .padding(vertical = Space.xxs)
                )
            }
        }
    }
}
