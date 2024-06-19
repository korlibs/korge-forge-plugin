package korlibs.audio.mod

import korlibs.time.TimeSpan
import korlibs.time.seconds
import korlibs.memory.*
import korlibs.audio.format.AudioDecodingProps
import korlibs.audio.format.AudioFormat
import korlibs.audio.sound.AudioSamples
import korlibs.audio.sound.AudioStream
import korlibs.audio.sound.NativeSoundProvider
import korlibs.audio.sound.Sound
import korlibs.audio.sound.nativeSoundProvider
import korlibs.io.file.VfsFile
import korlibs.io.stream.AsyncStream
import korlibs.io.stream.readAll
import kotlin.math.min

abstract class BaseModuleTracker {
    abstract class Format(vararg exts: String) : AudioFormat(*exts) {
        abstract fun createTracker(): BaseModuleTracker
        open suspend fun fastValidate(data: AsyncStream): Boolean = true

        override suspend fun tryReadInfo(data: AsyncStream, props: AudioDecodingProps): Info? {
            try {
                if (!fastValidate(data)) return null
                val time: TimeSpan? = when (props.exactTimings) {
                    true -> {
                        val mod = createTracker()
                        if (!mod.parse(data.readAll().toNBufferUInt8())) return null
                        mod.totalLengthInSamples?.let { samples -> (samples.toDouble() / mod.samplerate.toDouble()).seconds }
                    }
                    else -> null
                }
                return Info(duration = time, channels = 2)
            } catch (e: Throwable) {
                e.printStackTrace()
                return null
            }
        }

        override suspend fun decodeStreamInternal(data: AsyncStream, props: AudioDecodingProps): AudioStream? {
            val mod = createTracker()
            if (!mod.parse(data.readAll().toNBufferUInt8())) return null
            return mod.createAudioStream()
        }
    }

    var samplerate = 44100
    var playing = false
    var endofsong = false

    abstract fun initialize()
    abstract fun parse(buffer: Uint8Buffer): Boolean
    abstract fun mix(bufs: Array<FloatArray>, buflen: Int = bufs[0].size)

    fun parseAndInit(buffer: Uint8Buffer) {
        parse(buffer)
        initialize()
        playing = true
    }

    var totalLengthInSamples: Long? = null

    suspend fun createSoundFromFile(file: VfsFile, soundProvider: NativeSoundProvider = nativeSoundProvider): Sound {
        parseAndInit(file.readBytes().toNBufferUInt8())
        return createSound(soundProvider)
    }

    suspend fun createSound(soundProvider: NativeSoundProvider = nativeSoundProvider): Sound {
        return soundProvider.createStreamingSound(createAudioStream())
    }

    fun createAudioStream(): AudioStream {
        playing = true
        var fch = Array(2) { FloatArray(1024) }
        return object : AudioStream(samplerate, 2) {
            override val finished: Boolean get() = endofsong

            // @TODO: we should figure out how to compute the length in samples/time
            override val totalLengthInSamples: Long?
                get() = this@BaseModuleTracker.totalLengthInSamples

            var _currentPositionInSamples: Long = 0L

            private fun skipUntil(newPosition: Long) {
                while (_currentPositionInSamples < newPosition) {
                    val available = newPosition - _currentPositionInSamples
                    val skip = min(available.toInt(), fch[0].size)
                    mix(fch, skip)
                    _currentPositionInSamples += skip
                }
            }

            override var currentPositionInSamples: Long
                get() = _currentPositionInSamples
                set(value) {
                    if (_currentPositionInSamples == value) return
                    if (value > _currentPositionInSamples) {
                        skipUntil(value)
                    } else {
                        //if (value != 0L) error("only supported rewind in MOD value=$value")
                        _currentPositionInSamples = 0L
                        initialize()
                        if (value != 0L) {
                            println("SLOW SEEK")
                            skipUntil(value)
                        }
                    }
                }

            override suspend fun read(out: AudioSamples, offset: Int, length: Int): Int {
                if (fch[0].size < length) fch = Array(2) { FloatArray(length) }
                mix(fch, length)
                _currentPositionInSamples += length
                val l = fch[0]
                val r = fch[1]
                for (n in 0 until length) out.setFloatStereo(offset + n, l[n], r[n])
                return length
            }

            override suspend fun clone(): AudioStream {
                return createAudioStream()
            }

        }
    }
}
