package com.example.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import com.example.ui.theme.*
import kotlin.math.roundToInt

/**
 * Constants identifying editable UI elements on the AI Teacher screen
 */
object TouchElementIds {
    const val BG_PHOTO = "elem_bg_photo"
    const val HEADER_BAR = "elem_header_bar"
    const val LOGO_BADGE = "elem_logo_badge"
    const val MITHILA_LOGO = "elem_mithila_logo"
    const val HEADER_TITLE = "elem_header_title"
    const val SESSIONS_BUTTON = "elem_sessions_btn"
    const val VOICE_SETTINGS_BUTTON = "elem_voice_settings_btn"
    const val SUBJECT_CHIPS = "elem_subject_chips"
    const val ACTIVE_SESSION_BANNER = "elem_active_session_banner"
    const val STATUS_CARD = "elem_status_card"
    const val WAVEFORM = "elem_waveform"
    const val SUBTITLE_TEXT = "elem_subtitle_text"
    const val QUICK_ACTIONS = "elem_quick_actions"
    const val YOUTUBE_BUTTON = "elem_youtube_btn"
    const val WHATSAPP_BUTTON = "elem_whatsapp_btn"
    const val MIC_BUTTON = "elem_mic_btn"
    const val MIC_AURA = "elem_mic_aura"
    const val INPUT_BAR = "elem_input_bar"
    const val CUSTOM_TEXT = "elem_custom_text"
    const val CUSTOM_STICKER = "elem_custom_sticker"
}

/**
 * State representing transformation, position, scale, rotation, and custom assets for an editable element.
 */
data class TouchElementTransform(
    val elementId: String,
    val displayName: String,
    val offsetX: Float = 0f,
    val offsetY: Float = 0f,
    val scale: Float = 1.0f,
    val rotation: Float = 0f,
    val customImageUri: String? = null,
    val isVisible: Boolean = true,
    val isImageElement: Boolean = false,
    val zIndex: Float = 1f
)

/**
 * Default initial transforms for all interactive elements
 */
fun getDefaultElementTransforms(): Map<String, TouchElementTransform> {
    return listOf(
        TouchElementTransform(TouchElementIds.BG_PHOTO, "AI Teacher Visual / Photo", isImageElement = true),
        TouchElementTransform(TouchElementIds.HEADER_BAR, "Top Header Bar"),
        TouchElementTransform(TouchElementIds.LOGO_BADGE, "SPA AI Teacher Badge", isImageElement = true),
        TouchElementTransform(TouchElementIds.MITHILA_LOGO, "Mithila Academy Logo", isImageElement = true),
        TouchElementTransform(TouchElementIds.HEADER_TITLE, "Header Title & Status"),
        TouchElementTransform(TouchElementIds.SESSIONS_BUTTON, "Chat History Button"),
        TouchElementTransform(TouchElementIds.VOICE_SETTINGS_BUTTON, "Voice Settings Button"),
        TouchElementTransform(TouchElementIds.SUBJECT_CHIPS, "Subject Category Chips"),
        TouchElementTransform(TouchElementIds.ACTIVE_SESSION_BANNER, "Active Session Doubt Banner"),
        TouchElementTransform(TouchElementIds.STATUS_CARD, "AI Live Status Card"),
        TouchElementTransform(TouchElementIds.WAVEFORM, "Audio Waveform Visualizer"),
        TouchElementTransform(TouchElementIds.SUBTITLE_TEXT, "AI Speech Subtitle"),
        TouchElementTransform(TouchElementIds.QUICK_ACTIONS, "Quick Actions Carousel"),
        TouchElementTransform(TouchElementIds.YOUTUBE_BUTTON, "YouTube Action Button"),
        TouchElementTransform(TouchElementIds.WHATSAPP_BUTTON, "WhatsApp Action Button"),
        TouchElementTransform(TouchElementIds.MIC_BUTTON, "Live Mic Voice Button"),
        TouchElementTransform(TouchElementIds.MIC_AURA, "Microphone Pulse Aura"),
        TouchElementTransform(TouchElementIds.INPUT_BAR, "Text Question Bar"),
        TouchElementTransform(TouchElementIds.CUSTOM_TEXT, "Custom Text / Academy Motto"),
        TouchElementTransform(TouchElementIds.CUSTOM_STICKER, "Custom Sticker / Image", isImageElement = true, isVisible = false)
    ).associateBy { it.elementId }
}

/**
 * Touch Editable Wrapper that empowers any UI element with touch drag, pinch-to-resize,
 * two-finger rotation, selection bounding boxes, and image replacement.
 */
@Composable
fun TouchEditableWrapper(
    elementId: String,
    transform: TouchElementTransform,
    isEditMode: Boolean,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onTransformChange: (offsetX: Float, offsetY: Float, scale: Float, rotation: Float) -> Unit,
    displayName: String = "",
    onReplaceImageRequest: (() -> Unit)? = null,
    onResetElement: (() -> Unit)? = null,
    onToggleVisibility: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    isImage: Boolean = false,
    content: @Composable () -> Unit
) {
    if (!transform.isVisible && !isEditMode) {
        return // Hidden in normal mode
    }

    if (!isEditMode) {
        // Fast path for normal UI mode: zero overhead, zero gesture interceptors, instant button responsiveness
        val hasTransform = transform.offsetX != 0f || transform.offsetY != 0f || transform.scale != 1.0f || transform.rotation != 0f
        if (hasTransform) {
            Box(
                modifier = modifier
                    .graphicsLayer {
                        translationX = transform.offsetX
                        translationY = transform.offsetY
                        scaleX = transform.scale
                        scaleY = transform.scale
                        rotationZ = transform.rotation
                    }
                    .zIndex(transform.zIndex)
                    .testTag("touch_editable_$elementId")
            ) {
                if (isImage && !transform.customImageUri.isNullOrBlank()) {
                    AsyncImage(
                        model = Uri.parse(transform.customImageUri),
                        contentDescription = displayName,
                        modifier = Modifier.wrapContentSize()
                    )
                } else {
                    content()
                }
            }
        } else {
            Box(
                modifier = modifier
                    .zIndex(transform.zIndex)
                    .testTag("touch_editable_$elementId")
            ) {
                if (isImage && !transform.customImageUri.isNullOrBlank()) {
                    AsyncImage(
                        model = Uri.parse(transform.customImageUri),
                        contentDescription = displayName,
                        modifier = Modifier.wrapContentSize()
                    )
                } else {
                    content()
                }
            }
        }
        return
    }

    var currentOffsetX by remember(transform.offsetX) { mutableFloatStateOf(transform.offsetX) }
    var currentOffsetY by remember(transform.offsetY) { mutableFloatStateOf(transform.offsetY) }
    var currentScale by remember(transform.scale) { mutableFloatStateOf(transform.scale) }
    var currentRotation by remember(transform.rotation) { mutableFloatStateOf(transform.rotation) }

    val editGestureModifier = Modifier
        .pointerInput(elementId, isSelected) {
            detectTapGestures(
                onTap = {
                    onSelect()
                }
            )
        }
        .pointerInput(elementId, isSelected) {
            detectTransformGestures(panZoomLock = false) { _, pan, zoom, rotation ->
                if (!isSelected) {
                    onSelect()
                }
                val newOffsetX = currentOffsetX + pan.x
                val newOffsetY = currentOffsetY + pan.y
                val newScale = (currentScale * zoom).coerceIn(0.25f, 4.0f)
                val newRotation = (currentRotation + rotation) % 360f

                currentOffsetX = newOffsetX
                currentOffsetY = newOffsetY
                currentScale = newScale
                currentRotation = newRotation

                onTransformChange(newOffsetX, newOffsetY, newScale, newRotation)
            }
        }

    Box(
        modifier = modifier
            .graphicsLayer {
                translationX = currentOffsetX
                translationY = currentOffsetY
                scaleX = currentScale
                scaleY = currentScale
                rotationZ = currentRotation
                alpha = if (!transform.isVisible && isEditMode) 0.35f else 1.0f
            }
            .zIndex(if (isSelected) 100f else transform.zIndex)
            .then(editGestureModifier)
            .testTag("touch_editable_$elementId")
    ) {
        // Render custom replaced image if available, else original content
        if (isImage && !transform.customImageUri.isNullOrBlank()) {
            AsyncImage(
                model = Uri.parse(transform.customImageUri),
                contentDescription = displayName,
                modifier = Modifier.wrapContentSize()
            )
        } else {
            content()
        }

        // Selection Bounding Box & Handles (Only in Edit Mode when selected)
        if (isSelected) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .border(
                        BorderStroke(2.dp, AmberAccent),
                        shape = RoundedCornerShape(8.dp)
                    )
            ) {
                // Top-Left Handle
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .offset(x = (-6).dp, y = (-6).dp)
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(AmberAccent)
                        .border(1.5.dp, Color.Black, CircleShape)
                )

                // Top-Right Handle
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 6.dp, y = (-6).dp)
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(AmberAccent)
                        .border(1.5.dp, Color.Black, CircleShape)
                )

                // Bottom-Left Handle
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .offset(x = (-6).dp, y = 6.dp)
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(AmberAccent)
                        .border(1.5.dp, Color.Black, CircleShape)
                )

                // Bottom-Right Handle
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = 6.dp, y = 6.dp)
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(AmberAccent)
                        .border(1.5.dp, Color.Black, CircleShape)
                )

                // Top Rotation Badge with Degree Indicator
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF0F172A),
                    border = BorderStroke(1.dp, AmberAccent),
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .offset(y = (-28).dp)
                        .shadow(6.dp, RoundedCornerShape(12.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.RotateRight,
                            contentDescription = null,
                            tint = AmberAccent,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "${currentRotation.roundToInt()}° • %.1fx".format(currentScale),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        )
                    }
                }
            }
        } else if (isEditMode && !isSelected) {
            // Subtle dotted/semi-transparent highlight to show element is touchable
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .border(
                        BorderStroke(1.dp, BlueSecondary.copy(alpha = 0.35f)),
                        shape = RoundedCornerShape(6.dp)
                    )
            )
        }
    }
}

/**
 * Top Editor Toolbar with Mode Indicator, Save, Reset, Add Image, and Exit.
 */
@Composable
fun TouchEditorTopToolbar(
    isEditMode: Boolean,
    onToggleEditMode: () -> Unit,
    onSaveLayout: () -> Unit,
    onResetAll: () -> Unit,
    onAddSticker: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (isEditMode) Color(0xF00A0F1D) else Color(0xB30F172A),
        border = BorderStroke(
            1.5.dp,
            if (isEditMode) AmberAccent else Color.White.copy(alpha = 0.2f)
        ),
        shadowElevation = if (isEditMode) 12.dp else 4.dp,
        modifier = modifier
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .testTag("touch_editor_top_toolbar")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Edit Mode Indicator & Switch
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.clickable { onToggleEditMode() }
            ) {
                Surface(
                    shape = CircleShape,
                    color = if (isEditMode) AmberAccent else Color.White.copy(alpha = 0.2f),
                    modifier = Modifier.size(24.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isEditMode) Icons.Default.TouchApp else Icons.Default.Edit,
                            contentDescription = null,
                            tint = if (isEditMode) Color.Black else Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                Column {
                    Text(
                        text = if (isEditMode) "TOUCH EDITING MODE" else "EDIT MODE",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isEditMode) AmberAccent else Color.White,
                            fontSize = 11.sp
                        )
                    )
                    Text(
                        text = if (isEditMode) "Drag, Pinch to resize, Rotate" else "Tap to customize layout",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 9.sp
                        )
                    )
                }
            }

            // Quick Actions when Edit Mode is active
            if (isEditMode) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Add Sticker / Image Button
                    IconButton(
                        onClick = onAddSticker,
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(BluePrimary.copy(alpha = 0.3f))
                            .testTag("touch_editor_add_sticker_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddPhotoAlternate,
                            contentDescription = "Add Sticker/Photo",
                            tint = BlueSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Reset Layout Button
                    IconButton(
                        onClick = onResetAll,
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.1f))
                            .testTag("touch_editor_reset_all_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.RestartAlt,
                            contentDescription = "Reset Layout",
                            tint = Color.White.copy(alpha = 0.9f),
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Save Layout Button
                    Button(
                        onClick = onSaveLayout,
                        colors = ButtonDefaults.buttonColors(containerColor = AmberAccent, contentColor = Color.Black),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier
                            .height(28.dp)
                            .testTag("touch_editor_save_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = "Save",
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "Save",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        )
                    }

                    // Exit / Done Button
                    IconButton(
                        onClick = onToggleEditMode,
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(RedLive.copy(alpha = 0.2f))
                            .testTag("touch_editor_exit_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Exit Edit Mode",
                            tint = RedLive,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            } else {
                // Simple Toggle Button to enter Edit Mode
                Button(
                    onClick = onToggleEditMode,
                    colors = ButtonDefaults.buttonColors(containerColor = AmberAccent.copy(alpha = 0.25f), contentColor = AmberAccent),
                    border = BorderStroke(1.dp, AmberAccent.copy(alpha = 0.6f)),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                    modifier = Modifier
                        .height(28.dp)
                        .testTag("toggle_edit_mode_btn")
                ) {
                    Text(
                        text = "Edit UI ✏️",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    )
                }
            }
        }
    }
}

/**
 * Floating Selected Element Inspector Toolbar (Bottom)
 */
@Composable
fun TouchEditorSelectedToolbar(
    selectedTransform: TouchElementTransform?,
    onUpdateTransform: (offsetX: Float, offsetY: Float, scale: Float, rotation: Float) -> Unit,
    onReplaceImage: () -> Unit,
    onResetElement: () -> Unit,
    onToggleVisibility: () -> Unit,
    onDeselect: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (selectedTransform == null) return

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color(0xF50F172A),
        border = BorderStroke(1.5.dp, AmberAccent.copy(alpha = 0.7f)),
        shadowElevation = 16.dp,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 4.dp)
            .testTag("touch_editor_inspector_toolbar")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Selected Element Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = AmberAccent.copy(alpha = 0.2f),
                        modifier = Modifier.size(24.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Layers,
                                contentDescription = null,
                                tint = AmberAccent,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }
                    Text(
                        text = selectedTransform.displayName,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 12.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Center Button
                    OutlinedButton(
                        onClick = {
                            onUpdateTransform(0f, 0f, selectedTransform.scale, selectedTransform.rotation)
                        },
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                        modifier = Modifier.height(26.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FilterCenterFocus,
                            contentDescription = "Center",
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("Center", fontSize = 10.sp, color = Color.White)
                    }

                    // Reset Element Button
                    OutlinedButton(
                        onClick = onResetElement,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                        modifier = Modifier.height(26.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reset",
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("Reset", fontSize = 10.sp, color = Color.White)
                    }

                    // Replace Image (if image element)
                    if (selectedTransform.isImageElement) {
                        Button(
                            onClick = onReplaceImage,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                            modifier = Modifier.height(26.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhotoCamera,
                                contentDescription = "Replace Image",
                                tint = Color.White,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("Replace", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Deselect
                    IconButton(
                        onClick = onDeselect,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Deselect",
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Transformation Sliders (Scale, Rotate, Position D-pad)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Scale Control
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Size: %.1fx".format(selectedTransform.scale), fontSize = 10.sp, color = Color.White.copy(alpha = 0.8f))
                        Text("Pinch/Slider", fontSize = 9.sp, color = AmberAccent)
                    }
                    Slider(
                        value = selectedTransform.scale,
                        onValueChange = { newScale ->
                            onUpdateTransform(selectedTransform.offsetX, selectedTransform.offsetY, newScale, selectedTransform.rotation)
                        },
                        valueRange = 0.3f..3.0f,
                        colors = SliderDefaults.colors(
                            thumbColor = AmberAccent,
                            activeTrackColor = AmberAccent,
                            inactiveTrackColor = Color.White.copy(alpha = 0.2f)
                        ),
                        modifier = Modifier.height(24.dp)
                    )
                }

                // Rotation Control
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Rotate: ${selectedTransform.rotation.roundToInt()}°", fontSize = 10.sp, color = Color.White.copy(alpha = 0.8f))
                        Text(
                            text = "0° Reset",
                            fontSize = 9.sp,
                            color = BlueSecondary,
                            modifier = Modifier.clickable {
                                onUpdateTransform(selectedTransform.offsetX, selectedTransform.offsetY, selectedTransform.scale, 0f)
                            }
                        )
                    }
                    Slider(
                        value = selectedTransform.rotation,
                        onValueChange = { newRot ->
                            onUpdateTransform(selectedTransform.offsetX, selectedTransform.offsetY, selectedTransform.scale, newRot)
                        },
                        valueRange = -180f..180f,
                        colors = SliderDefaults.colors(
                            thumbColor = BlueSecondary,
                            activeTrackColor = BluePrimary,
                            inactiveTrackColor = Color.White.copy(alpha = 0.2f)
                        ),
                        modifier = Modifier.height(24.dp)
                    )
                }

                // D-Pad Quick Move Stepper
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.wrapContentWidth()
                ) {
                    IconButton(
                        onClick = {
                            onUpdateTransform(selectedTransform.offsetX, selectedTransform.offsetY - 15f, selectedTransform.scale, selectedTransform.rotation)
                        },
                        modifier = Modifier.size(20.dp)
                    ) {
                        Icon(Icons.Default.ArrowDropUp, contentDescription = "Up", tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        IconButton(
                            onClick = {
                                onUpdateTransform(selectedTransform.offsetX - 15f, selectedTransform.offsetY, selectedTransform.scale, selectedTransform.rotation)
                            },
                            modifier = Modifier.size(20.dp)
                        ) {
                            Icon(Icons.Default.ArrowLeft, contentDescription = "Left", tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                        IconButton(
                            onClick = {
                                onUpdateTransform(selectedTransform.offsetX + 15f, selectedTransform.offsetY, selectedTransform.scale, selectedTransform.rotation)
                            },
                            modifier = Modifier.size(20.dp)
                        ) {
                            Icon(Icons.Default.ArrowRight, contentDescription = "Right", tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }
                    IconButton(
                        onClick = {
                            onUpdateTransform(selectedTransform.offsetX, selectedTransform.offsetY + 15f, selectedTransform.scale, selectedTransform.rotation)
                        },
                        modifier = Modifier.size(20.dp)
                    ) {
                        Icon(Icons.Default.ArrowDropDown, contentDescription = "Down", tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}
