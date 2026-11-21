package com.bansify.ai.feature.chat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.bansify.ai.core.designsystem.BansifyBrandAvatar
import com.bansify.ai.core.designsystem.BansifyRadius
import com.bansify.ai.core.designsystem.BansifyTextStyles
import com.bansify.ai.core.designsystem.IconPill
import com.bansify.ai.core.designsystem.Space
import com.bansify.ai.core.ui.InlineErrorCard
import com.bansify.ai.data.mock.ChatMessage
import com.bansify.ai.data.mock.ChatMode
import com.bansify.ai.data.mock.GeneratedMedia
import com.bansify.ai.data.mock.MessageFeedback
import com.bansify.ai.data.mock.MockChats
import com.bansify.ai.data.mock.MockUser
import com.bansify.ai.data.mock.SuggestionCardItem
import com.bansify.ai.data.mock.UserProfile
import kotlinx.coroutines.launch

@Composable
fun MessageList(
    messages: List<ChatMessage>,
    userProfile: UserProfile,
    chatMode: ChatMode,
    listState: LazyListState,
    revealedActionsId: String?,
    inlineError: String?,
    onRetryError: () -> Unit,
    onSuggestionClick: (String) -> Unit,
    onTapAssistant: (String) -> Unit,
    onLongPressMessage: (ChatMessage) -> Unit,
    onCopyMessage: (ChatMessage) -> Unit,
    onRegenerateMessage: (ChatMessage) -> Unit,
    onFeedback: (String, MessageFeedback) -> Unit,
    onShareMessage: (ChatMessage) -> Unit,
    onCodeCopied: () -> Unit,
    onOpenMediaPreview: (GeneratedMedia) -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()

    val isAtBottom by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val lastVisible = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            layoutInfo.totalItemsCount == 0 || lastVisible >= layoutInfo.totalItemsCount - 2
        }
    }

    var showNewMessagesPill by remember { mutableStateOf(false) }
    val lastContentLen = messages.lastOrNull()?.content?.length ?: 0

    LaunchedEffect(messages.size, lastContentLen) {
        if (messages.isEmpty()) {
            showNewMessagesPill = false
        } else if (isAtBottom) {
            showNewMessagesPill = false
            listState.scrollToItem((messages.size - 1).coerceAtLeast(0))
        } else {
            showNewMessagesPill = true
        }
    }

    LaunchedEffect(isAtBottom) {
        if (isAtBottom) showNewMessagesPill = false
    }

    if (messages.isEmpty()) {
        ChatEmptyGreeting(
            userProfile = userProfile,
            chatMode = chatMode,
            inlineError = inlineError,
            onRetryError = onRetryError,
            onSuggestionClick = onSuggestionClick,
            modifier = modifier
        )
        return
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(
                start = Space.md,
                end = Space.md,
                top = Space.md,
                bottom = Space.lg
            ),
            verticalArrangement = Arrangement.spacedBy(Space.lg),
            modifier = Modifier
                .fillMaxSize()
                .testTag("chat_message_list")
        ) {
            itemsIndexed(
                items = messages,
                key = { _, item -> item.id }
            ) { index, message ->
                val showDate = index == 0 || messages[index - 1].dateGroup != message.dateGroup
                Column(verticalArrangement = Arrangement.spacedBy(Space.sm)) {
                    if (showDate) {
                        DateSeparator(label = message.dateGroup)
                    }
                    MessageItem(
                        message = message,
                        showActions = revealedActionsId == message.id,
                        onTapAssistant = { onTapAssistant(message.id) },
                        onLongPress = { onLongPressMessage(message) },
                        onCopy = { onCopyMessage(message) },
                        onRegenerate = { onRegenerateMessage(message) },
                        onFeedback = { fb -> onFeedback(message.id, fb) },
                        onShare = { onShareMessage(message) },
                        onCodeCopied = onCodeCopied,
                        onOpenMediaPreview = onOpenMediaPreview
                    )
                }
            }

            if (inlineError != null) {
                item(key = "inline_error") {
                    InlineErrorCard(
                        title = "Couldn't get a response",
                        message = inlineError,
                        onRetry = onRetryError
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = showNewMessagesPill,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = Space.sm)
        ) {
            IconPill(
                label = "New messages",
                leadingIcon = Icons.Rounded.ArrowDownward,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                onClick = {
                    scope.launch {
                        showNewMessagesPill = false
                        listState.animateScrollToItem((messages.size - 1).coerceAtLeast(0))
                    }
                },
                testTag = "scroll_to_bottom_pill"
            )
        }
    }
}

@Composable
private fun ChatEmptyGreeting(
    userProfile: UserProfile,
    chatMode: ChatMode,
    inlineError: String?,
    onRetryError: () -> Unit,
    onSuggestionClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val greeting = remember(userProfile, chatMode) {
        when (chatMode) {
            ChatMode.CHAT -> MockUser.greetingFor(userProfile)
            ChatMode.IMAGE -> "What image would you like to create?"
            ChatMode.VIDEO -> "What video would you like to create?"
        }
    }
    val suggestions = remember(userProfile, chatMode) {
        when (chatMode) {
            ChatMode.CHAT -> MockUser.suggestionsFor(userProfile)
            ChatMode.IMAGE -> MockChats.imageSuggestions
            ChatMode.VIDEO -> MockChats.videoSuggestions
        }
    }
    val helperCaption = when (chatMode) {
        ChatMode.CHAT -> "Or ask anything"
        ChatMode.IMAGE -> "Every prompt in this chat generates an image"
        ChatMode.VIDEO -> "Every prompt in this chat generates a video clip"
    }

    Column(
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Space.md, vertical = Space.lg)
            .testTag("chat_empty_greeting")
    ) {
        BansifyBrandAvatar(size = 40.dp)
        Spacer(Modifier.height(Space.md))
        Text(
            text = greeting,
            style = BansifyTextStyles.H1,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(Space.lg))

        // 2x2 personalized suggestion grid
        suggestions.chunked(2).forEach { rowItems ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(Space.sm),
                modifier = Modifier.fillMaxWidth()
            ) {
                rowItems.forEach { item ->
                    SuggestionCard(
                        item = item,
                        onClick = { onSuggestionClick(item.prompt) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            Spacer(Modifier.height(Space.sm))
        }

        Spacer(Modifier.height(Space.xs))
        Text(
            text = helperCaption,
            style = BansifyTextStyles.Caption,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (inlineError != null) {
            Spacer(Modifier.height(Space.md))
            InlineErrorCard(
                title = "Couldn't get a response",
                message = inlineError,
                onRetry = onRetryError
            )
        }
    }
}

@Composable
fun SuggestionCard(
    item: SuggestionCardItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = BansifyRadius.Card,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        tonalElevation = 0.dp,
        modifier = modifier
            .heightIn(min = 96.dp)
            .clip(BansifyRadius.Card)
            .clickable(onClick = onClick)
            .testTag("suggestion_${item.id}")
    ) {
        Column(
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.padding(Space.sm)
        ) {
            Text(
                text = item.title,
                style = BansifyTextStyles.BodyStrong,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(Space.xxs))
            Text(
                text = item.subtitle,
                style = BansifyTextStyles.Caption,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
