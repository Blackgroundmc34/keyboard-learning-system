package com.privatecloset.app.ui.practice

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.privatecloset.app.data.model.PracticeExercise
import com.privatecloset.app.ui.common.StateScaffold
import com.privatecloset.app.ui.common.UiState
import com.privatecloset.app.ui.theme.BgCard
import com.privatecloset.app.ui.theme.BgStroke
import com.privatecloset.app.ui.theme.Gold
import com.privatecloset.app.ui.theme.GoldLight
import com.privatecloset.app.ui.theme.Purple
import com.privatecloset.app.ui.theme.PurpleLight
import com.privatecloset.app.ui.theme.ScreenBackgroundGradient
import com.privatecloset.app.ui.theme.TextPrimary
import com.privatecloset.app.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// 64-Key definition data structure
data class PianoKeyData(
    val midiNote: Int,
    val noteName: String,
    val isBlack: Boolean,
    val octave: Int,
)

// Generate full 64 Keys from C1 (MIDI 24) to D#6 (MIDI 87)
private val FULL_64_KEYS: List<PianoKeyData> = run {
    val noteNames = listOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")
    val blackSet = setOf(1, 3, 6, 8, 10)
    val list = mutableListOf<PianoKeyData>()
    for (midi in 24..87) {
        val semitone = (midi - 24) % 12
        val octave = (midi / 12) - 1
        val name = "${noteNames[semitone]}$octave"
        list.add(
            PianoKeyData(
                midiNote = midi,
                noteName = name,
                isBlack = semitone in blackSet,
                octave = octave,
            )
        )
    }
    list
}

@Composable
fun PracticeScreen(uiState: UiState<PracticeExercise>) {
    val synthesizer = remember { PianoSoundSynthesizer() }
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    var isFull64KeyMode by remember { mutableStateOf(false) }
    var sustainEnabled by remember { mutableStateOf(false) }
    var selectedSoundPreset by remember { mutableStateOf("Grand Piano") }
    var lastPlayedNote by remember { mutableStateOf<String?>(null) }
    var currentOctaveIndex by remember { mutableIntStateOf(3) } // Octave 3 & 4 by default

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBackgroundGradient)
            .padding(horizontal = if (isLandscape) 12.dp else 18.dp, vertical = if (isLandscape) 8.dp else 16.dp),
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    text = "Interactive 64-Key Keyboard",
                    color = TextPrimary,
                    style = MaterialTheme.typography.titleLarge,
                    fontSize = if (isLandscape) 18.sp else 22.sp,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = if (isLandscape) "Landscape Mode · Full Keybed Visible" else "Tilt screen or toggle 64-Key Panoramic view",
                    color = GoldLight,
                    fontSize = 11.sp,
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                // View toggle
                FilterChip(
                    selected = isFull64KeyMode || isLandscape,
                    onClick = { isFull64KeyMode = !isFull64KeyMode },
                    label = { Text(if (isFull64KeyMode || isLandscape) "64-Key Panoramic" else "Octave Zoom", fontSize = 11.sp) },
                    leadingIcon = { Icon(Icons.Filled.ScreenRotation, contentDescription = null, modifier = Modifier.size(14.dp)) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PurpleLight.copy(alpha = 0.4f),
                        selectedLabelColor = GoldLight,
                    )
                )

                // Sustain pedal toggle
                FilterChip(
                    selected = sustainEnabled,
                    onClick = { sustainEnabled = !sustainEnabled },
                    label = { Text("Sustain", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Gold.copy(alpha = 0.3f),
                        selectedLabelColor = GoldLight,
                    )
                )
            }
        }

        Spacer(Modifier.height(if (isLandscape) 6.dp else 12.dp))

        StateScaffold(uiState) { exercise ->
            val scope = rememberCoroutineScope()

            // Exercise Title and Status
            if (!isLandscape) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(BgCard)
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text(exercise.title, color = TextSecondary, fontSize = 11.sp)
                        Text(exercise.chordLabel, color = GoldLight, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        exercise.highlightedNotes.forEach { note ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Purple.copy(alpha = 0.4f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                            ) {
                                Text(note, color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))
            }

            // Keyboard Container with responsive height and width calculation
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center,
            ) {
                Responsive64PianoKeybed(
                    keys = FULL_64_KEYS,
                    isFull64View = isFull64KeyMode || isLandscape,
                    activeOctave = currentOctaveIndex,
                    highlightedNotes = exercise.highlightedNotes.toSet(),
                    lastPlayedNote = lastPlayedNote,
                    onKeyTriggered = { key ->
                        lastPlayedNote = key.noteName
                        val freq = synthesizer.midiToFrequency(key.midiNote)
                        synthesizer.playNote(freq, sustain = sustainEnabled)
                    }
                )
            }

            Spacer(Modifier.height(8.dp))

            // Octave Jump & Preset Bar (when not in full 64-key view)
            if (!isFull64KeyMode && !isLandscape) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Octaves:", color = TextSecondary, fontSize = 11.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf(1 to "C1-B2", 3 to "C3-B4", 5 to "C5-D#6").forEach { (oct, label) ->
                            val isSelected = currentOctaveIndex == oct
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) PurpleLight else BgCard)
                                    .clickable { currentOctaveIndex = oct }
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                            ) {
                                Text(
                                    text = label,
                                    color = if (isSelected) Color.White else TextSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                        }
                    }
                    lastPlayedNote?.let {
                        Text("Note: $it", color = Gold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(Modifier.height(10.dp))
            }

            // Bottom Action Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RoundPracticeAction(
                    icon = Icons.Filled.Headphones,
                    label = "Listen Chord",
                    onClick = {
                        scope.launch {
                            exercise.highlightedNotes.forEach { n ->
                                val f = synthesizer.noteNameToFrequency("${n}4")
                                synthesizer.playNote(f, durationMs = 800, sustain = true)
                                delay(220)
                            }
                        }
                    }
                )

                // Large record/synth button
                Box(
                    modifier = Modifier
                        .size(if (isLandscape) 48.dp else 60.dp)
                        .clip(CircleShape)
                        .background(Purple)
                        .clickable {
                            val f = synthesizer.noteNameToFrequency("C4")
                            synthesizer.playNote(f, sustain = true)
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Filled.GraphicEq,
                        contentDescription = "Synth Feedback",
                        tint = Gold,
                        modifier = Modifier.size(if (isLandscape) 22.dp else 28.dp)
                    )
                }

                RoundPracticeAction(
                    icon = Icons.Filled.VolumeUp,
                    label = "Sound: $selectedSoundPreset",
                    onClick = {
                        selectedSoundPreset = when (selectedSoundPreset) {
                            "Grand Piano" -> "Rhodes EP"
                            "Rhodes EP" -> "Velvet Synth"
                            else -> "Grand Piano"
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun Responsive64PianoKeybed(
    keys: List<PianoKeyData>,
    isFull64View: Boolean,
    activeOctave: Int,
    highlightedNotes: Set<String>,
    lastPlayedNote: String?,
    onKeyTriggered: (PianoKeyData) -> Unit,
) {
    val scrollState = rememberScrollState()

    // Filter displayed keys: if full view, show all 64 keys; else show ~24 keys around activeOctave
    val visibleKeys = remember(isFull64View, activeOctave) {
        if (isFull64View) {
            keys
        } else {
            keys.filter { it.octave in activeOctave..(activeOctave + 1) }
        }
    }

    val whiteKeys = visibleKeys.filter { !it.isBlack }
    val blackKeys = visibleKeys.filter { it.isBlack }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .clip(RoundedCornerShape(16.dp))
            .background(BgCard)
            .border(1.dp, BgStroke, RoundedCornerShape(16.dp))
            .padding(top = 12.dp, bottom = 4.dp, start = 6.dp, end = 6.dp),
    ) {
        val totalWidth = maxWidth
        val containerHeight = maxHeight

        // Calculate individual key width based on screen width and mode
        val whiteKeyWidth: Dp = if (isFull64View) {
            // Distribute all 37 white keys evenly across available width, minimum 14.dp
            (totalWidth / whiteKeys.size.coerceAtLeast(1)).coerceAtLeast(13.dp)
        } else {
            (totalWidth / whiteKeys.size.coerceAtLeast(1)).coerceIn(34.dp, 56.dp)
        }

        val blackKeyWidth: Dp = whiteKeyWidth * 0.65f
        val blackKeyHeight: Dp = containerHeight * 0.58f

        Box(
            modifier = Modifier
                .fillMaxSize()
                .horizontalScroll(scrollState),
        ) {
            // 1. LAYER: White Keys
            Row(modifier = Modifier.fillMaxHeight()) {
                whiteKeys.forEach { key ->
                    val isHighlighted = highlightedNotes.any { key.noteName.startsWith(it) }
                    val isPressed = key.noteName == lastPlayedNote

                    WhitePianoKey(
                        key = key,
                        width = whiteKeyWidth,
                        isHighlighted = isHighlighted,
                        isPressed = isPressed,
                        onClick = { onKeyTriggered(key) },
                    )
                }
            }

            // 2. LAYER: Black Keys (Positioned over white keys)
            // Calculate pixel/dp offset of each black key relative to its preceding white key
            whiteKeys.forEachIndexed { index, whiteKey ->
                // Check if there is a black key following this white key
                val whiteLetter = whiteKey.noteName.substring(0, 1)
                val hasSharpAfter = whiteLetter in listOf("C", "D", "F", "G", "A")

                if (hasSharpAfter) {
                    val matchingBlack = blackKeys.find {
                        it.midiNote == whiteKey.midiNote + 1
                    }
                    if (matchingBlack != null) {
                        val isHighlighted = highlightedNotes.any { matchingBlack.noteName.startsWith(it) }
                        val isPressed = matchingBlack.noteName == lastPlayedNote
                        val xOffset = (whiteKeyWidth * (index + 1)) - (blackKeyWidth / 2)

                        BlackPianoKey(
                            key = matchingBlack,
                            width = blackKeyWidth,
                            height = blackKeyHeight,
                            offsetX = xOffset,
                            isHighlighted = isHighlighted,
                            isPressed = isPressed,
                            onClick = { onKeyTriggered(matchingBlack) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WhitePianoKey(
    key: PianoKeyData,
    width: Dp,
    isHighlighted: Boolean,
    isPressed: Boolean,
    onClick: () -> Unit,
) {
    val bgColor = when {
        isPressed -> Gold
        isHighlighted -> PurpleLight.copy(alpha = 0.45f)
        else -> Color(0xFFF8F7FA)
    }
    val textColor = when {
        isPressed -> Color.Black
        isHighlighted -> GoldLight
        else -> Color(0xFF281F38)
    }

    Box(
        modifier = Modifier
            .width(width)
            .fillMaxHeight()
            .padding(horizontal = 1.dp)
            .clip(RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp))
            .background(bgColor)
            .border(1.dp, Color(0xFFD4D0DC), RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.BottomCenter,
    ) {
        if (width >= 20.dp) {
            Text(
                text = key.noteName,
                color = textColor,
                fontSize = if (width < 30.dp) 8.sp else 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 6.dp),
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun BlackPianoKey(
    key: PianoKeyData,
    width: Dp,
    height: Dp,
    offsetX: Dp,
    isHighlighted: Boolean,
    isPressed: Boolean,
    onClick: () -> Unit,
) {
    val bgColor = when {
        isPressed -> Gold
        isHighlighted -> Purple
        else -> Color(0xFF140E20)
    }

    Box(
        modifier = Modifier
            .offset(x = offsetX)
            .width(width)
            .height(height)
            .zIndex(10f)
            .shadow(4.dp, RoundedCornerShape(bottomStart = 5.dp, bottomEnd = 5.dp))
            .clip(RoundedCornerShape(bottomStart = 5.dp, bottomEnd = 5.dp))
            .background(bgColor)
            .border(1.dp, if (isHighlighted) Gold else Color(0xFF322845), RoundedCornerShape(bottomStart = 5.dp, bottomEnd = 5.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.BottomCenter,
    ) {
        if (width >= 16.dp && (isPressed || isHighlighted)) {
            Text(
                text = key.noteName.take(2),
                color = if (isPressed) Color.Black else GoldLight,
                fontSize = 7.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 4.dp),
            )
        }
    }
}

@Composable
private fun RoundPracticeAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(BgCard)
                .border(1.dp, BgStroke, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = label, tint = GoldLight, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.height(4.dp))
        Text(label, color = TextSecondary, fontSize = 11.sp)
    }
}
