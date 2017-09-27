package com.quaffle.voiceitt.quaffle

import android.Manifest
import android.media.AudioFormat
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import kotlinx.android.synthetic.main.activity_main.*
import android.media.AudioFormat.ENCODING_PCM_16BIT
import android.media.AudioFormat.CHANNEL_IN_MONO
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.AsyncTask
import android.util.Log
import android.media.AudioRecord.READ_BLOCKING
import java.lang.Math.abs


class MainActivity : AppCompatActivity() {

    private val SAMPLE_RATE = 16000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Example of a call to a native method
        sample_text.text = stringFromJNI()

        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 1 )

        val bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT)

        val record = AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT,
                bufferSize)

        val realRate = record.sampleRate
        Log.i("SAMPLE RATE", "" + realRate)

        if (realRate != SAMPLE_RATE) {
            throw java.lang.RuntimeException("this is bad: sample rate is " + record.sampleRate)
        }

        record.startRecording()
        AsyncBuffer(record).execute()
        println("Recording")
    }

    class AsyncBuffer : AsyncTask<Void, Void, Void> {

        private var recorder: AudioRecord
        var stopped: Boolean = false

        constructor (recorder: AudioRecord ){
            this.recorder = recorder
        }

        override fun doInBackground(vararg params: Void): Void? {
            println("asynch task")

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
            return null
        }
    }
    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    external fun stringFromJNI(): String

    companion object {

        // Used to load the 'native-lib' library on application startup.
        init {
            System.loadLibrary("native-lib")
        }
    }
}
