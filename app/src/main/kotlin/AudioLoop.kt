package com.quaffle.voiceitt.quaffle

/**
 * Created by mary on 9/27/17.
 */

import android.media.AudioRecord
import android.util.Log
import android.media.AudioRecord.READ_BLOCKING
import java.lang.Math.abs
import android.media.MediaRecorder
import android.media.AudioFormat
import android.os.AsyncTask


public class AudioLoop {

    private val SAMPLE_RATE = 16000

    var recorder: AudioRecord
    var isStopped: Boolean = false

    init {
        val bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT)

        recorder = AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT,
                bufferSize)

        val realRate = recorder.sampleRate
        Log.i("SAMPLE RATE", "" + realRate)

        if (realRate != SAMPLE_RATE) {
            throw java.lang.RuntimeException("this is bad: sample rate is " + recorder.sampleRate)
        }
    }

    fun start() {
        recorder.startRecording()
        AsyncBuffer(recorder).run()
        println("Recording")
    }

    fun stop() {

        println("Stopped")
    }

    class AsyncBuffer : Runnable {

        private var recorder: AudioRecord
        var stopped: Boolean = false

        constructor (recorder: AudioRecord ){
            this.recorder = recorder
        }

        override fun run() {
            println("async task")

            val buffer = ShortArray(1600)

            while (!stopped) {
                val n = recorder.read(buffer, 0, buffer.size, READ_BLOCKING)

                var sum = 0.0
                for (i in 0 until n) {
                    val x = buffer[i] / -java.lang.Short.MIN_VALUE.toDouble()
                    sum += abs(x)
                }
                sum /= n.toDouble()
                Log.i("Energy: ", "" + sum)

            }
        }
    }
}