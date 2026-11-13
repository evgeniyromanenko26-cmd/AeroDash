package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

class AeroSoundEngine {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val sampleRate = 22050

    fun playJump(enabled: Boolean) {
        if (!enabled) return
        playToneSweep(startFreq = 360.0, endFreq = 780.0, durationMs = 95, volume = 0.22f)
    }

    fun playOrb(enabled: Boolean) {
        if (!enabled) return
        playToneSweep(startFreq = 680.0, endFreq = 1240.0, durationMs = 140, volume = 0.26f)
    }

    fun playPad(enabled: Boolean) {
        if (!enabled) return
        playToneSweep(startFreq = 440.0, endFreq = 1040.0, durationMs = 130, volume = 0.24f)
    }

    fun playCrash(enabled: Boolean) {
        if (!enabled) return
        playToneSweep(startFreq = 260.0, endFreq = 75.0, durationMs = 180, volume = 0.28f)
    }

    fun playVictory(enabled: Boolean) {
        if (!enabled) return
        scope.launch {
            playSingleNote(523.25, 110, 0.22f)
            playSingleNote(659.25, 110, 0.22f)
            playSingleNote(783.99, 130, 0.24f)
            playSingleNote(1046.5, 240, 0.26f)
        }
    }

    private fun playToneSweep(
        startFreq: Double,
        endFreq: Double,
        durationMs: Int,
        volume: Float,
    ) {
        scope.launch {
            runCatching {
                val numSamples = (sampleRate * durationMs) / 1000
                val buffer = ShortArray(numSamples)
                var phase = 0.0
                for (i in 0 until numSamples) {
                    val t = i.toDouble() / numSamples
                    val freq = startFreq + (endFreq - startFreq) * t
                    phase += 2.0 * PI * freq / sampleRate
                    val envelope = exp(-3.5 * t)
                    val sample = (sin(phase) * envelope * volume * Short.MAX_VALUE).toInt()
                        .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
                    buffer[i] = sample.toShort()
                }
                writePcm(buffer)
            }
        }
    }

    private fun playSingleNote(freq: Double, durationMs: Int, volume: Float) {
        runCatching {
            val numSamples = (sampleRate * durationMs) / 1000
            val buffer = ShortArray(numSamples)
            for (i in 0 until numSamples) {
                val t = i.toDouble() / numSamples
                val envelope = exp(-2.8 * t)
                val sample = (sin(2.0 * PI * freq * i / sampleRate) * envelope * volume * Short.MAX_VALUE).toInt()
                    .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
                buffer[i] = sample.toShort()
            }
            writePcm(buffer)
        }
    }

    private fun writePcm(buffer: ShortArray) {
        val minBytes = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
        )
        val byteSize = maxOf(minBytes, buffer.size * 2)
        val track = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(byteSize)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()

        track.write(buffer, 0, buffer.size)
        track.play()
        Thread.sleep(((buffer.size * 1000L) / sampleRate) + 25L)
        track.stop()
        track.release()
    }
}
