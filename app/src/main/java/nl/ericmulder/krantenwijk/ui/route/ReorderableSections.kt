package nl.ericmulder.krantenwijk.ui.route

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import nl.ericmulder.krantenwijk.R
import nl.ericmulder.krantenwijk.domain.model.Segment
import nl.ericmulder.krantenwijk.ui.common.PrimaryActionHeight
import nl.ericmulder.krantenwijk.ui.common.TargetSpacing
import nl.ericmulder.krantenwijk.ui.common.WalkTouchTarget
import nl.ericmulder.krantenwijk.ui.round.sideAndRange
import sh.calvin.reorderable.ReorderableColumn

/**
 * Street sections in walking order that can be reordered (ADR-05): drag the handle, or use the
 * "Move up" / "Move down" accessibility actions (TalkBack can't drag). [onReorder] is called once,
 * when a move is finished, with every section id in the new order.
 */
@Composable
fun ReorderableSections(
    segments: List<Segment>,
    detail: @Composable (Segment) -> String,
    onOpen: (Segment) -> Unit,
    onReorder: (List<Long>) -> Unit,
) {
    val haptics = LocalHapticFeedback.current
    // Local copy so the list follows the finger before the database confirms the new order.
    var order by remember(segments) { mutableStateOf(segments) }
    fun commit(newOrder: List<Segment>) {
        order = newOrder
        onReorder(newOrder.map { it.id })
    }
    fun move(from: Int, to: Int) {
        if (to !in order.indices || from == to) return
        commit(order.toMutableList().apply { add(to, removeAt(from)) })
    }

    ReorderableColumn(
        list = order,
        onSettle = { from, to -> move(from, to) },
        onMove = { haptics.performHapticFeedback(HapticFeedbackType.SegmentFrequentTick) },
        verticalArrangement = Arrangement.spacedBy(TargetSpacing),
    ) { index, segment, isDragging ->
        key(segment.id) {
            ReorderableItem {
                val elevation by animateDpAsState(if (isDragging) 8.dp else 0.dp, label = "drag elevation")
                val moveUp = stringResource(R.string.reorder_move_up)
                val moveDown = stringResource(R.string.reorder_move_down)
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    border = BorderStroke(
                        if (isDragging) 2.dp else 1.dp,
                        if (isDragging) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                    ),
                    shadowElevation = elevation,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = PrimaryActionHeight)
                        .semantics {
                            customActions = listOfNotNull(
                                CustomAccessibilityAction(moveUp) { move(index, index - 1); true }.takeIf { index > 0 },
                                CustomAccessibilityAction(moveDown) { move(index, index + 1); true }.takeIf { index < order.lastIndex },
                            )
                        },
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(
                            Modifier
                                .weight(1f)
                                .clickable { onOpen(segment) }
                                .padding(start = 16.dp, top = 12.dp, bottom = 12.dp),
                        ) {
                            Text("${index + 1}. ${segment.streetName}", style = MaterialTheme.typography.titleMedium)
                            Text(
                                segment.sideAndRange() + " · " + detail(segment),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(WalkTouchTarget)
                                .draggableHandle(
                                    onDragStarted = { haptics.performHapticFeedback(HapticFeedbackType.GestureThresholdActivate) },
                                    onDragStopped = { haptics.performHapticFeedback(HapticFeedbackType.GestureEnd) },
                                ),
                        ) {
                            Icon(
                                painterResource(R.drawable.ic_drag_handle),
                                contentDescription = stringResource(R.string.reorder_handle, segment.streetName),
                            )
                        }
                    }
                }
            }
        }
    }
}
