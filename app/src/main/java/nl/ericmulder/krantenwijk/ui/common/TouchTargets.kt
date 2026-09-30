package nl.ericmulder.krantenwijk.ui.common

import androidx.compose.ui.unit.dp

// Touch target sizes (DEC-023), based on Android/Material (48 dp, 8 dp apart), WCAG 2.2 and
// touch research (about 1 cm for adjacent targets), with extra margin for gloves outdoors.

/** Smallest size for anything tappable, anywhere in the app. */
val MinTouchTarget = 56.dp

/** Tiles, mailboxes and sheet options that are tapped while walking a round (about 12 mm). */
val WalkTouchTarget = 64.dp

/** Height of the most important actions: Previous/Next, Start round, Set sticker. */
val PrimaryActionHeight = 72.dp

/** Space between neighbouring tappable elements. */
val TargetSpacing = 8.dp
