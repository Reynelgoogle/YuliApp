package com.example.ui.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin
import kotlin.math.cos
import kotlin.math.exp
import java.util.Random

object YuliAudioSynth {
    private const val SAMPLE_RATE = 22050
    private val scope = CoroutineScope(Dispatchers.Default)

    /**
     * Synthesizes a warm, resonant wooden marimba note.
     * Uses a fundamental frequency, a 4x overtone (2 octaves above, typical for marimba tuning),
     * and a quick mallet-strike transient with organic decay.
     */
    fun playMarimbaNote(freq: Double, durationMs: Int = 300) {
        val numSamples = (SAMPLE_RATE * (durationMs / 1000.0)).toInt()
        val samples = FloatArray(numSamples)
        
        val f0 = freq
        val f1 = f0 * 4.0  // 2nd mode (two octaves above)
        val f2 = f0 * 10.0 // 3rd mode (high metallic/wooden ring)
        
        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            
            // Fast exponential decay of the overtones
            val env0 = exp(-t * 12.0) // fundamental decays nicely
            val env1 = exp(-t * 24.0) // 2nd mode decays faster
            val env2 = exp(-t * 48.0) // 3rd mode decays very fast
            
            // Smooth attack to avoid hard clicks, simulating a soft wool mallet
            val attack = if (t < 0.005) t / 0.005 else 1.0
            
            val val0 = sin(2.0 * PI * f0 * t) * env0
            val val1 = sin(2.0 * PI * f1 * t) * env1 * 0.4
            val val2 = sin(2.0 * PI * f2 * t) * env2 * 0.15
            
            val totalSec = durationMs / 1000.0
            val release = if (t > totalSec - 0.03) (totalSec - t) / 0.03 else 1.0
            
            samples[i] = ((val0 + val1 + val2) * attack * release * 0.5).toFloat().coerceIn(-1.0f, 1.0f)
        }
        playBuffer(samples, durationMs)
    }

    /**
     * Glorious ascending, rapid wooden marimba cascade.
     * Played when a child launches a game from the home menu.
     */
    fun playForwardNavigation() {
        val durationMs = 700
        val numSamples = (SAMPLE_RATE * (durationMs / 1000.0)).toInt()
        val samples = FloatArray(numSamples)
        
        // Pentatonic major ascending cascade notes - sweet music box chimes
        val freqs = listOf(392.00, 440.00, 523.25, 587.33, 659.25, 783.99) // G4, A4, C5, D5, E5, G5
        val entryTimes = listOf(0.0, 0.05, 0.10, 0.15, 0.20, 0.25)
        
        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            var mixed = 0.0
            
            for (j in freqs.indices) {
                val entryT = entryTimes[j]
                if (t >= entryT) {
                    val noteT = t - entryT
                    val f0 = freqs[j]
                    val f1 = f0 * 2.0 // softer harmonic overtones
                    val f2 = f0 * 3.0
                    
                    val env0 = exp(-noteT * 6.5)  // slower decay for ringing chime resonance
                    val env1 = exp(-noteT * 18.0)
                    val env2 = exp(-noteT * 32.0)
                    val attack = if (noteT < 0.005) noteT / 0.005 else 1.0 // softer attack
                    
                    val val0 = sin(2.0 * PI * f0 * noteT) * env0
                    val val1 = sin(2.0 * PI * f1 * noteT) * env1 * 0.18
                    val val2 = sin(2.0 * PI * f2 * noteT) * env2 * 0.05
                    
                    mixed += (val0 + val1 + val2) * attack * 0.35
                }
            }
            
            val totalSec = durationMs / 1000.0
            val release = if (t > totalSec - 0.06) (totalSec - t) / 0.06 else 1.0
            samples[i] = (mixed * release * 0.6).toFloat().coerceIn(-1.0f, 1.0f)
        }
        playBuffer(samples, durationMs)
    }

    /**
     * Gentle descending pentatonic sequence representing going back or closing.
     */
    fun playBackNavigation() {
        val durationMs = 600
        val numSamples = (SAMPLE_RATE * (durationMs / 1000.0)).toInt()
        val samples = FloatArray(numSamples)
        
        // Pentatonic descending notes - sweet music box chimes
        val freqs = listOf(783.99, 659.25, 523.25, 392.00) // G5, E5, C5, G4
        val entryTimes = listOf(0.0, 0.06, 0.12, 0.18)
        
        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            var mixed = 0.0
            
            for (j in freqs.indices) {
                val entryT = entryTimes[j]
                if (t >= entryT) {
                    val noteT = t - entryT
                    val f0 = freqs[j]
                    val f1 = f0 * 2.0
                    val f2 = f0 * 3.0
                    
                    val env0 = exp(-noteT * 6.0)
                    val env1 = exp(-noteT * 16.0)
                    val env2 = exp(-noteT * 28.0)
                    val attack = if (noteT < 0.005) noteT / 0.005 else 1.0
                    
                    val val0 = sin(2.0 * PI * f0 * noteT) * env0
                    val val1 = sin(2.0 * PI * f1 * noteT) * env1 * 0.18
                    val val2 = sin(2.0 * PI * f2 * noteT) * env2 * 0.05
                    
                    mixed += (val0 + val1 + val2) * attack * 0.38
                }
            }
            
            val totalSec = durationMs / 1000.0
            val release = if (t > totalSec - 0.06) (totalSec - t) / 0.06 else 1.0
            samples[i] = (mixed * release * 0.55).toFloat().coerceIn(-1.0f, 1.0f)
        }
        playBuffer(samples, durationMs)
    }

    /**
     * Sophisticated multi-sensory tab sound design using marimba wood resonances.
     */
    fun playNavigationTo(screen: String) {
        when (screen) {
            "home" -> {
                // Warm, friendly "welcome home" music box chime double stroke (C5 -> E5 rolled)
                val durationMs = 500
                val numSamples = (SAMPLE_RATE * (durationMs / 1000.0)).toInt()
                val samples = FloatArray(numSamples)
                val freqs = listOf(523.25, 659.25) // C5 -> E5
                val entryTimes = listOf(0.0, 0.05)
                
                for (i in 0 until numSamples) {
                    val t = i.toDouble() / SAMPLE_RATE
                    var mixed = 0.0
                    for (j in freqs.indices) {
                        val entryT = entryTimes[j]
                        if (t >= entryT) {
                            val noteT = t - entryT
                            val f0 = freqs[j]
                            val f1 = f0 * 2.0
                            val f2 = f0 * 3.0
                            
                            val env0 = exp(-noteT * 6.5)
                            val env1 = exp(-noteT * 18.0)
                            val env2 = exp(-noteT * 32.0)
                            val attack = if (noteT < 0.005) noteT / 0.005 else 1.0
                            
                            val val0 = sin(2.0 * PI * f0 * noteT) * env0
                            val val1 = sin(2.0 * PI * f1 * noteT) * env1 * 0.18
                            val val2 = sin(2.0 * PI * f2 * noteT) * env2 * 0.05
                            
                            mixed += (val0 + val1 + val2) * attack * 0.45
                        }
                    }
                    samples[i] = (mixed * 0.55).toFloat().coerceIn(-1.0f, 1.0f)
                }
                playBuffer(samples, durationMs)
            }
            "colors" -> {
                // A bright, sparkling, fast ascending music box triad (C5, E5, G5, C6)
                val durationMs = 550
                val numSamples = (SAMPLE_RATE * (durationMs / 1000.0)).toInt()
                val samples = FloatArray(numSamples)
                val freqs = listOf(523.25, 659.25, 783.99, 1046.50) // C5 -> E5 -> G5 -> C6
                val entryTimes = listOf(0.0, 0.05, 0.10, 0.15)
                
                for (i in 0 until numSamples) {
                    val t = i.toDouble() / SAMPLE_RATE
                    var mixed = 0.0
                    for (j in freqs.indices) {
                        val entryT = entryTimes[j]
                        if (t >= entryT) {
                            val noteT = t - entryT
                            val f0 = freqs[j]
                            val f1 = f0 * 2.0
                            val f2 = f0 * 3.0
                            
                            val env0 = exp(-noteT * 7.5)
                            val env1 = exp(-noteT * 20.0)
                            val env2 = exp(-noteT * 36.0)
                            val attack = if (noteT < 0.005) noteT / 0.005 else 1.0
                            
                            val val0 = sin(2.0 * PI * f0 * noteT) * env0
                            val val1 = sin(2.0 * PI * f1 * noteT) * env1 * 0.18
                            val val2 = sin(2.0 * PI * f2 * noteT) * env2 * 0.05
                            
                            mixed += (val0 + val1 + val2) * attack * 0.35
                        }
                    }
                    samples[i] = (mixed * 0.5).toFloat().coerceIn(-1.0f, 1.0f)
                }
                playBuffer(samples, durationMs)
            }
            "animals" -> {
                // Playful bouncy chime double stroke (F4 -> A4)
                val durationMs = 400
                val numSamples = (SAMPLE_RATE * (durationMs / 1000.0)).toInt()
                val samples = FloatArray(numSamples)
                val freqs = listOf(349.23, 440.00) // F4 -> A4
                val entryTimes = listOf(0.0, 0.06)
                
                for (i in 0 until numSamples) {
                    val t = i.toDouble() / SAMPLE_RATE
                    var mixed = 0.0
                    for (j in freqs.indices) {
                        val entryT = entryTimes[j]
                        if (t >= entryT) {
                            val noteT = t - entryT
                            val f0 = freqs[j]
                            val f1 = f0 * 2.0
                            val f2 = f0 * 3.0
                            
                            val env0 = exp(-noteT * 8.0)
                            val env1 = exp(-noteT * 22.0)
                            val env2 = exp(-noteT * 40.0)
                            val attack = if (noteT < 0.005) noteT / 0.005 else 1.0
                            
                            val val0 = sin(2.0 * PI * f0 * noteT) * env0
                            val val1 = sin(2.0 * PI * f1 * noteT) * env1 * 0.18
                            val val2 = sin(2.0 * PI * f2 * noteT) * env2 * 0.05
                            
                            mixed += (val0 + val1 + val2) * attack * 0.45
                        }
                    }
                    samples[i] = (mixed * 0.55).toFloat().coerceIn(-1.0f, 1.0f)
                }
                playBuffer(samples, durationMs)
            }
            "piano" -> {
                // A single beautiful marimba key strike (C5)
                playMarimbaNote(523.25, 450)
            }
            "family" -> {
                // Soft, rolling music box major 7th chord (C4, E4, G4, B4)
                val durationMs = 1000
                val numSamples = (SAMPLE_RATE * (durationMs / 1000.0)).toInt()
                val samples = FloatArray(numSamples)
                val freqs = listOf(261.63, 329.63, 392.00, 493.88) // C4, E4, G4, B4
                val entryTimes = listOf(0.0, 0.05, 0.10, 0.15)
                
                for (i in 0 until numSamples) {
                    val t = i.toDouble() / SAMPLE_RATE
                    var mixed = 0.0
                    for (j in freqs.indices) {
                        val entryT = entryTimes[j]
                        if (t >= entryT) {
                            val noteT = t - entryT
                            val f0 = freqs[j]
                            val f1 = f0 * 2.0
                            val f2 = f0 * 3.0
                            
                            val env0 = exp(-noteT * 5.0)
                            val env1 = exp(-noteT * 14.0)
                            val env2 = exp(-noteT * 28.0)
                            val attack = if (noteT < 0.005) noteT / 0.005 else 1.0
                            
                            val val0 = sin(2.0 * PI * f0 * noteT) * env0
                            val val1 = sin(2.0 * PI * f1 * noteT) * env1 * 0.18
                            val val2 = sin(2.0 * PI * f2 * noteT) * env2 * 0.05
                            
                            mixed += (val0 + val1 + val2) * attack * 0.28
                        }
                    }
                    val totalSec = durationMs / 1000.0
                    val release = if (t > totalSec - 0.06) (totalSec - t) / 0.06 else 1.0
                    samples[i] = (mixed * release * 0.6).toFloat().coerceIn(-1.0f, 1.0f)
                }
                playBuffer(samples, durationMs)
            }
            else -> playPop()
        }
    }

    /**
     * Helper to launch AudioTrack and play a buffer of raw float samples statically.
     */
    private fun playBuffer(samples: FloatArray, durationMs: Int) {
        scope.launch {
            try {
                val audioTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_FLOAT)
                            .setSampleRate(SAMPLE_RATE)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(samples.size * 4)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                audioTrack.write(samples, 0, samples.size, AudioTrack.WRITE_NON_BLOCKING)
                audioTrack.play()

                // Delay until sound finishes, then clean up
                delay(durationMs.toLong() + 50)
                audioTrack.stop()
                audioTrack.release()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * General tone player with support for wave types, frequency sweeps, and tremolo.
     * Retained and improved to produce softer, warmer sound profiles.
     */
    fun playTone(
        frequency: Double,
        durationMs: Int,
        waveType: String = "sine",
        sweepTo: Double? = null,
        tremoloFreq: Double? = null,
        tremoloDepth: Double = 0.5,
        harmonicRatio: Double? = null,
        harmonicGain: Double = 0.3,
        gain: Double = 0.5
    ) {
        scope.launch {
            try {
                val numSamples = (SAMPLE_RATE * (durationMs / 1000.0)).toInt()
                val samples = FloatArray(numSamples)
                val random = Random()
                val totalDuration = durationMs / 1000.0

                for (i in 0 until numSamples) {
                    val t = i.toDouble() / SAMPLE_RATE

                    // Frequency sweep calculation
                    val currentFreq = if (sweepTo != null) {
                        val progress = t / totalDuration
                        frequency + (sweepTo - frequency) * progress
                    } else {
                        frequency
                    }

                    // Main angle
                    val angle = 2.0 * PI * currentFreq * t

                    // Retrieve base value with band-limiting to soften waves
                    var baseValue = when (waveType) {
                        "sine" -> sin(angle)
                        "square" -> {
                            var v = 0.0
                            for (k in listOf(1, 3, 5)) {
                                v += sin(angle * k) * (1.0 / k)
                            }
                            v
                        }
                        "triangle" -> {
                            var v = 0.0
                            for (k in listOf(1, 3, 5)) {
                                v += sin(angle * k) * (1.0 / (k * k))
                            }
                            v
                        }
                        "noise" -> random.nextFloat() * 2.0 - 1.0
                        else -> sin(angle)
                    }

                    // Add harmonic if requested
                    if (harmonicRatio != null) {
                        val hAngle = 2.0 * PI * (currentFreq * harmonicRatio) * t
                        val hValue = sin(hAngle)
                        baseValue = (baseValue + hValue * harmonicGain) / (1.0 + harmonicGain)
                    }

                    // Apply Tremolo
                    if (tremoloFreq != null) {
                        val tremoloMod = 1.0 - tremoloDepth + tremoloDepth * sin(2.0 * PI * tremoloFreq * t)
                        baseValue *= tremoloMod
                    }

                    // Apply ADSR envelope with organic exponential release
                    val envelope = when {
                        t < 0.015 -> t / 0.015 // soft 15ms attack
                        t > (totalDuration - 0.05) -> {
                            val remaining = totalDuration - t
                            remaining / 0.05 // soft 50ms release
                        }
                        else -> {
                            exp(-t * 2.2) // natural decay
                        }
                    }

                    samples[i] = (baseValue * envelope * gain * 0.55).toFloat().coerceIn(-1.0f, 1.0f)
                }

                playBuffer(samples, durationMs)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Beautiful wooden marimba / organic water droplet "bloop" pop sound.
     * Replaces the electronic laser-beep with a satisfying physical key strike.
     */
    fun playPop() {
        val durationMs = 150
        val numSamples = (SAMPLE_RATE * (durationMs / 1000.0)).toInt()
        val samples = FloatArray(numSamples)
        
        val f0 = 523.25 // Soft C5 note
        val f1 = f0 * 2.0
        val f2 = f0 * 3.0
        
        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            
            // Physical resonance decay rates (softer)
            val env0 = exp(-t * 22.0)
            val env1 = exp(-t * 80.0)
            val env2 = exp(-t * 150.0)
            
            // Softer linear attack to prevent sharp transient click
            val attack = if (t < 0.005) t / 0.005 else 1.0
            
            val val0 = sin(2.0 * PI * f0 * t) * env0
            val val1 = sin(2.0 * PI * f1 * t) * env1 * 0.15
            val val2 = sin(2.0 * PI * f2 * t) * env2 * 0.05
            
            samples[i] = ((val0 + val1 + val2) * attack * 0.38).toFloat().coerceIn(-1.0f, 1.0f)
        }
        playBuffer(samples, durationMs)
    }

    /**
     * Synthesized Cow (🐮 "Moooo")
     * Dual-formant vowel shift from bright nasal "Mee" to deep "Ooo".
     */
    fun playCow() {
        val durationMs = 1100
        val numSamples = (SAMPLE_RATE * (durationMs / 1000.0)).toInt()
        val samples = FloatArray(numSamples)
        
        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val totalSec = durationMs / 1000.0
            
            // Pitch sweeps down smoothly
            val freq = 115.0 - 23.0 * (t / totalSec)
            
            var mixed = 0.0
            val formantTransition = if (t < 0.25) t / 0.25 else 1.0
            
            for (k in 1..6) {
                val hFreq = freq * k
                val hAngle = 2.0 * PI * hFreq * t
                
                val hEnv = when (k) {
                    1 -> exp(-t * 1.5) // fundamental remains long
                    2 -> exp(-t * 2.0) * 0.7
                    3 -> exp(-t * 5.0) * (0.15 + 0.35 * (1.0 - formantTransition)) // vocal resonance peak for nasal "Mee"
                    4 -> exp(-t * 10.0) * 0.2
                    5 -> exp(-t * 14.0) * 0.15
                    6 -> exp(-t * 18.0) * 0.1
                    else -> 0.0
                }
                mixed += sin(hAngle) * hEnv
            }
            
            val attack = if (t < 0.08) t / 0.08 else 1.0
            val release = if (t > totalSec - 0.15) (totalSec - t) / 0.15 else 1.0
            
            samples[i] = (mixed * attack * release * 0.55).toFloat().coerceIn(-1.0f, 1.0f)
        }
        playBuffer(samples, durationMs)
    }

    /**
     * Synthesized Pig (🐷 "Oink-Oink")
     * Two short guttural snorts containing rapid vocal rattle (34Hz amplitude modulation)
     * mixed with light soft friction noise for organic breathiness.
     */
    fun playPig() {
        scope.launch {
            playSinglePigSnort(95)
            delay(280)
            playSinglePigSnort(85)
        }
    }
    
    private fun playSinglePigSnort(baseFreq: Int) {
        val durationMs = 240
        val numSamples = (SAMPLE_RATE * (durationMs / 1000.0)).toInt()
        val samples = FloatArray(numSamples)
        val random = Random()
        
        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val totalSec = durationMs / 1000.0
            
            val freq = baseFreq.toDouble() - 22.0 * (t / totalSec)
            
            var valSaw = 0.0
            for (k in 1..8) {
                valSaw += sin(2.0 * PI * (freq * k) * t) * (1.0 / k)
            }
            
            val rattle = 0.6 + 0.4 * sin(2.0 * PI * 34.0 * t)
            val noise = (random.nextFloat() * 2.0 - 1.0) * 0.16
            
            val env = exp(-t * 8.0)
            val attack = if (t < 0.02) t / 0.02 else 1.0
            val release = if (t > totalSec - 0.04) (totalSec - t) / 0.04 else 1.0
            
            val mixed = (valSaw * rattle + noise) * env * attack * release
            samples[i] = (mixed * 0.6).toFloat().coerceIn(-1.0f, 1.0f)
        }
        playBuffer(samples, durationMs)
    }

    /**
     * Synthesized Sheep (🐑 "Baaaaa")
     * Complete physical modeling with parallel 13Hz pitch vibrato and 13Hz amplitude tremolo,
     * producing a wonderful, realistic shivering cartoon bleat.
     */
    fun playSheep() {
        val durationMs = 900
        val numSamples = (SAMPLE_RATE * (durationMs / 1000.0)).toInt()
        val samples = FloatArray(numSamples)
        
        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val totalSec = durationMs / 1000.0
            
            val vibratoFactor = 1.0 + 0.032 * sin(2.0 * PI * 13.0 * t)
            val baseFreq = (260.0 - 30.0 * (t / totalSec)) * vibratoFactor
            
            var mixed = 0.0
            for (k in 1..5) {
                val hAngle = 2.0 * PI * (baseFreq * k) * t
                val hEnv = when (k) {
                    1 -> 0.8
                    2 -> 0.5
                    3 -> 0.4 * (1.0 + 0.25 * sin(2.0 * PI * 6.0 * t))
                    4 -> 0.2
                    5 -> 0.1
                    else -> 0.0
                }
                mixed += sin(hAngle) * hEnv
            }
            
            val tremolo = 0.72 + 0.28 * sin(2.0 * PI * 13.0 * t)
            
            val env = exp(-t * 2.5)
            val attack = if (t < 0.05) t / 0.05 else 1.0
            val release = if (t > totalSec - 0.1) (totalSec - t) / 0.1 else 1.0
            
            samples[i] = (mixed * tremolo * env * attack * release * 0.48).toFloat().coerceIn(-1.0f, 1.0f)
        }
        playBuffer(samples, durationMs)
    }

    /**
     * Synthesized Duck (🦆 "Quack-Quack")
     * Two fast, nasal brass/clarinet sweeps with odd-harmonics emphasized for authentic nasal beak resonance.
     */
    fun playDuck() {
        scope.launch {
            playSingleQuack(310.0)
            delay(240)
            playSingleQuack(290.0)
        }
    }
    
    private fun playSingleQuack(startFreq: Double) {
        val durationMs = 170
        val numSamples = (SAMPLE_RATE * (durationMs / 1000.0)).toInt()
        val samples = FloatArray(numSamples)
        
        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val totalSec = durationMs / 1000.0
            
            val freq = startFreq - 90.0 * (t / totalSec)
            
            var mixed = 0.0
            for (k in listOf(1, 3, 5, 7)) {
                val hAngle = 2.0 * PI * (freq * k) * t
                val weight = when (k) {
                    1 -> 0.7
                    3 -> 0.85 // Emphasize high nasal reed formant
                    5 -> 0.52
                    7 -> 0.3
                    else -> 0.0
                }
                mixed += sin(hAngle) * weight
            }
            
            val env = exp(-t * 9.0)
            val attack = if (t < 0.015) t / 0.015 else 1.0
            val release = if (t > totalSec - 0.03) (totalSec - t) / 0.03 else 1.0
            
            samples[i] = (mixed * env * attack * release * 0.5).toFloat().coerceIn(-1.0f, 1.0f)
        }
        playBuffer(samples, durationMs)
    }

    /**
     * Synthesized Lion (🦁 "Roarrr")
     * Low 50Hz fundamental square rumble modulated at 38Hz amplitude,
     * mixed with filtered brown friction air noise for a deep, rumbly, warm, safe roar.
     */
    fun playLion() {
        val durationMs = 1100
        val numSamples = (SAMPLE_RATE * (durationMs / 1000.0)).toInt()
        val samples = FloatArray(numSamples)
        val random = Random()
        
        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val totalSec = durationMs / 1000.0
            
            val freq = if (t < 0.3) {
                55.0 + 15.0 * (t / 0.3)
            } else {
                70.0 - 25.0 * ((t - 0.3) / (totalSec - 0.3))
            }
            
            var valSaw = 0.0
            for (k in 1..6) {
                valSaw += sin(2.0 * PI * (freq * k) * t) * (1.0 / k)
            }
            
            val rumbles = 0.5 + 0.5 * sin(2.0 * PI * 38.0 * t)
            val noise = (random.nextFloat() * 2.0 - 1.0) * 0.25
            
            val env = if (t < 0.25) {
                t / 0.25
            } else {
                exp(-(t - 0.25) * 2.0)
            }
            
            val mixed = (valSaw * rumbles + noise) * env
            samples[i] = (mixed * 0.65).toFloat().coerceIn(-1.0f, 1.0f)
        }
        playBuffer(samples, durationMs)
    }

    /**
     * Synthesized Frog (🐸 "Ribbit-Ribbit")
     * Dual pulse trains (14 quick, highly-resonant clicks spaced 18ms apart)
     * creating a magnificent, ultra-realistic vocal croak.
     */
    fun playFrog() {
        scope.launch {
            playSingleCroak()
            delay(350)
            playSingleCroak()
        }
    }
    
    private fun playSingleCroak() {
        val pulseCount = 14
        val pulseIntervalMs = 18
        val durationMs = pulseCount * pulseIntervalMs
        val numSamples = (SAMPLE_RATE * (durationMs / 1000.0)).toInt()
        val samples = FloatArray(numSamples)
        
        val f0 = 175.0
        val f1 = f0 * 2.3
        
        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val tMs = t * 1000.0
            
            val pulseIdx = (tMs / pulseIntervalMs).toInt()
            val timeInPulseMs = tMs - (pulseIdx * pulseIntervalMs)
            val timeInPulseSec = timeInPulseMs / 1000.0
            
            val pulseEnv = exp(-timeInPulseSec * 150.0)
            
            val val0 = sin(2.0 * PI * f0 * t)
            val val1 = sin(2.0 * PI * f1 * t) * 0.4
            
            val croakProgress = tMs / durationMs
            val croakEnv = sin(croakProgress * PI)
            
            samples[i] = ((val0 + val1) * pulseEnv * croakEnv * 0.55).toFloat().coerceIn(-1.0f, 1.0f)
        }
        playBuffer(samples, durationMs)
    }

    /**
     * Beautiful arpeggiated music-box arpeggio (C Major Triad) for the Family Screen.
     */
    fun playFamilySparkle() {
        val durationMs = 800
        val numSamples = (SAMPLE_RATE * (durationMs / 1000.0)).toInt()
        val samples = FloatArray(numSamples)
        
        val freqs = listOf(523.25, 659.25, 783.99) // C5, E5, G5
        val entryTimes = listOf(0.0, 0.08, 0.16) // Staggered arpeggio
        
        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            var mixed = 0.0
            
            for (j in 0 until 3) {
                val entryT = entryTimes[j]
                if (t >= entryT) {
                    val noteT = t - entryT
                    val f0 = freqs[j]
                    
                    val env = exp(-noteT * 4.5)
                    val attack = if (noteT < 0.004) noteT / 0.004 else 1.0
                    
                    val val0 = sin(2.0 * PI * f0 * noteT) * env * attack
                    val val1 = sin(2.0 * PI * (f0 * 2.0) * noteT) * env * attack * 0.3
                    
                    mixed += (val0 + val1) * 0.33
                }
            }
            
            val totalSec = durationMs / 1000.0
            val release = if (t > totalSec - 0.05) (totalSec - t) / 0.05 else 1.0
            
            samples[i] = (mixed * release * 0.5).toFloat().coerceIn(-1.0f, 1.0f)
        }
        playBuffer(samples, durationMs)
    }

    /**
     * Gorgeous music-box / physical toy-piano chime notes.
     * Contains five harmonic components and a rapid high-frequency metal impact "ping".
     */
    fun playNote(freq: Double, isBlackKey: Boolean = false) {
        val durationMs = 600
        val numSamples = (SAMPLE_RATE * (durationMs / 1000.0)).toInt()
        val samples = FloatArray(numSamples)
        
        val f0 = freq
        val f1 = f0 * 2.0
        val f2 = f0 * 3.0
        val f3 = f0 * 4.0
        val fChime = f0 * 5.4 // metallic chime ringing ratio
        
        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            
            // Physical vibration decays
            val env0 = exp(-t * 3.5)
            val env1 = exp(-t * 5.5)
            val env2 = exp(-t * 9.0)
            val env3 = exp(-t * 12.0)
            val envChime = exp(-t * 45.0) // high strike metal ping decays instantly
            
            val attack = if (t < 0.005) t / 0.005 else 1.0
            val totalSec = durationMs / 1000.0
            val release = if (t > totalSec - 0.05) (totalSec - t) / 0.05 else 1.0
            
            val val0 = sin(2.0 * PI * f0 * t) * env0
            val val1 = sin(2.0 * PI * f1 * t) * env1 * if (isBlackKey) 0.5 else 0.4
            val val2 = sin(2.0 * PI * f2 * t) * env2 * 0.15
            val val3 = sin(2.0 * PI * f3 * t) * env3 * 0.1
            val valChime = sin(2.0 * PI * fChime * t) * envChime * 0.35
            
            val mixed = val0 + val1 + val2 + val3 + valChime
            samples[i] = (mixed * attack * release * 0.48).toFloat().coerceIn(-1.0f, 1.0f)
        }
        playBuffer(samples, durationMs)
    }
}

