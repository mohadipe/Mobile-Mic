package de.mohadipe.mobilemic;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.media.audiofx.AcousticEchoCanceler;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import roboguice.activity.RoboActivity;
import roboguice.inject.InjectView;

public class MobileMicActivity extends RoboActivity {

    @InjectView(R.id.micButton)
    Button micButton;

    private boolean micStatus;
    private boolean recordingSchleife = false;
    private AudioRecord recorder = null;
    private AudioTrack track = null;
    private MobileMicAppStatus mobileMicAppStatus = MobileMicAppStatus.AUS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mobile_mic);
        micStatus = false;
    }

    public void activateMic(View view) {
        if (micStatus) {
            deaktiviereMic();
        } else {
            aktiviereMic();
        }
    }

    private void aktiviereMic() {
        AudioManager audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        audioManager.setBluetoothScoOn(true);
        if (audioManager.isBluetoothA2dpOn()) {
            startRecording();
        } else {
            Toast.makeText(getApplicationContext(), R.string.aktivate_bluetooth, 2000).show();
        }
    }

    private void startRecording() {
        micStatus = true;
        micButton.setText(R.string.button_off);
        recordingSchleife = false;
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.i("Audio", "Running Audio Thread");
                short[][] buffers = new short[256][160];
                int ix = 0;

                /*
                 * Initialize buffer to hold continuously recorded audio data, start recording, and start
                 * playback.
                 */
                try {
                    int sampleRateInHz = 8000;
                    int N = AudioRecord.getMinBufferSize(sampleRateInHz, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
                    int bufferSizeInBytes = N * 10;
                    recorder = new AudioRecord(MediaRecorder.AudioSource.MIC, sampleRateInHz, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSizeInBytes);
                    AcousticEchoCanceler.create(recorder.getAudioSessionId());
                    if (AcousticEchoCanceler.isAvailable()) {
                        mobileMicAppStatus = MobileMicAppStatus.AN_RECORDING;
                        Log.i("ACE-Ready", "Device implements ACE");

                        track = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRateInHz,
                                AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSizeInBytes, AudioTrack.MODE_STREAM);
                        recorder.startRecording();
                        track.play();
                        /*
                         * Loops until something outside of this thread stops it.
                         * Reads the data from the recorder and writes it to the audio track for playback.
                         */
                        while (!recordingSchleife) {
                            Log.i("Map", "Writing new data to buffer");
                            short[] buffer = buffers[ix++ % buffers.length];
                            N = recorder.read(buffer, 0, buffer.length);
                            track.write(buffer, 0, buffer.length);
                        }
                    } else {
                        mobileMicAppStatus = MobileMicAppStatus.AN_NO_ACE;
                    }
                } catch (Throwable x) {
                    Log.w("Audio", "Error reading voice audio", x);
                }
            }
        }).start();
        if (mobileMicAppStatus.equals(MobileMicAppStatus.AN_NO_ACE)) {
            Toast.makeText(getApplicationContext(), R.string.no_ace, 2000).show();
        }
        if (mobileMicAppStatus.equals(MobileMicAppStatus.AN_RECORDING)) {
            Toast.makeText(getApplicationContext(), R.string.mic_on, 2000).show();
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        deaktiviereMic();
    }

    private void deaktiviereMic() {
        if (micStatus) {
            Toast.makeText(getApplicationContext(), R.string.mic_off, 2000).show();
            micButton.setText(R.string.button_on);
            micStatus = false;
            try {
                if (recorder != null) {
                    recorder.stop();
                    recorder.release();
                    track.stop();
                    track.release();
                    recordingSchleife = true;
                    mobileMicAppStatus = MobileMicAppStatus.AUS;
                }
            } catch (IllegalStateException e) {
                Log.w("Audio", "Recorder & Track beenden: " + e.getMessage());
            }

        }
    }
}
