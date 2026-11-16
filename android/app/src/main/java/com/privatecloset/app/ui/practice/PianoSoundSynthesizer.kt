package com.privatecloset.app.ui.practice

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.pow
import kotlin.math.sin

/**
 * Real-time software synthesizer generating expressive piano and electric keyboard tones
 * across all octaves for interactive practice without needing external audio files.
 */
class PianoSoundSynthesizer {

    private val sampleRate = 22050
    private val scope = CoroutineScope(Dispatchers.Default)

    /**
     * Converts note name (e.g. "C3", "F#4", "Ab5") or MIDI number into frequency in Hz.
     */
    fun noteNameToFrequency(noteName: String): Double {
        val clean = noteName.trim().uppercase()
        val regex = Regex("""^([A-G][#B]?)(-?\d+)$""")
        val match = regex.matchEntire(clean) ?: return 440.0
        val pitch = match.groupValues[1]
        val octave = match.groupValues[2].toIntOrNull() ?: 4

        val semitoneOffset = when (pitch) {
            "C" -> 0
            "C#", "DB" -> 1
            "D" -> 2
            "D#", "EB" -> 3
            "E" -> 4
            "F" -> 5
            "F#", "GB" -> 6
            "G" -> 7
            "G#", "AB" -> 8
            "A" -> 9
            "A#", "BB" -> 10
            "B" -> 11
            else -> 0
        }
        val midiNumber = (octave + 1) * 12 + semitoneOffset
        return midiToFrequency(midiNumber)
    }

    fun midiToFrequency(midi: Int): Double {
        return 440.0 * 2.0.pow((midi - 69).toDouble() / 12.0)
    }

    /**
     * Plays a synthesized piano chord or note with realistic attack and exponential decay.
     */
    fun playNote(frequency: Double, durationMs: Int = 1200, sustain: Boolean = false) {
        scope.launch {
            try {
                val totalDuration = if (sustain) (durationMs * 1.8).toInt() else durationMs
                val numSamples = (sampleRate * (totalDuration / 1000.0)).toInt()
                val buffer = ShortArray(numSamples)

                val twoPiF = 2.0 * PI * frequency / sampleRate
                val decayRate = if (sustain) 1.8 else 3.5

                for (i in 0 until numSamples) {
                    val time = i.toDouble() / sampleRate
                    // Harmonic overtones for rich piano warmth (fundamental + 2nd + 3rd harmonic)
                    val sample = sin(twoPiF * i) * 0.65 +
                            sin(twoPiF * 2 * i) * 0.22 +
                            sin(twoPiF * 3 * i) * 0.10 +
                            sin(twoPiF * 4 * i) * 0.03

                    // Exponential decay envelope
                    val envelope = exp(-decayRate * time)
                    val finalVal = (sample * envelope * Short.MAX_VALUE * 0.75).toInt()
                    buffer[i] = finalVal.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                }

                val audioTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(buffer.size * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                audioTrack.write(buffer, 0, buffer.size)
                audioTrack.play()

                // Release track after playback completes
                kotlinx.coroutines.delay(totalDuration.toLong() + 100)
                audioTrack.stop()
                audioTrack.release()
            } catch (e: Exception) {
                // Graceful fallback if device audio buffer is constrained
            }
        }
    }
}
