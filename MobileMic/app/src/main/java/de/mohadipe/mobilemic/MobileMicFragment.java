package de.mohadipe.mobilemic;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.media.audiofx.AcousticEchoCanceler;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import roboguice.fragment.RoboFragment;
import roboguice.inject.InjectView;

public class MobileMicFragment extends RoboFragment {

    @InjectView(R.id.micButton)
    Button micButton;

    private boolean micStatus;
    private boolean recordingSchleife = false;
    private AudioRecord recorder = null;
    private AudioTrack track = null;
    private MobileMicAppStatus mobileMicAppStatus = MobileMicAppStatus.AUS;

    public MobileMicFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_mobilemic, container, false);
        return rootView;
    }

    public void activateMic(View view) {
        if (micStatus) {
            deaktiviereMic();
        } else {
            aktiviereMic();
        }
    }

    private void aktiviereMic() {
        AudioManager audioManager = (AudioManager) getActivity().getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        audioManager.setBluetoothScoOn(true);
        if (audioManager.isBluetoothA2dpOn()) {
            startRecording();
        } else {
            Toast.makeText(getActivity().getApplicationContext(), R.string.aktivate_bluetooth, Toast.LENGTH_LONG).show();
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
//                    int tailLength = 0; //nicht von JAEC verwendet, fix 2000
                    int N = AudioRecord.getMinBufferSize(sampleRateInHz, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
                    int bufferSizeInBytes = N * 10;
                    recorder = new AudioRecord(MediaRecorder.AudioSource.VOICE_COMMUNICATION, sampleRateInHz, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSizeInBytes);
                    if (AcousticEchoCanceler.isAvailable()) {
                        AcousticEchoCanceler acousticEchoCanceler = AcousticEchoCanceler.create(recorder.getAudioSessionId());
                        acousticEchoCanceler.setEnabled(true);
                        mobileMicAppStatus = MobileMicAppStatus.AN_RECORDING;
                        Log.i("ACE-Ready", "Device implements ACE");

                        track = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRateInHz,
                                AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT,
                                bufferSizeInBytes, AudioTrack.MODE_STREAM);
                        recorder.startRecording();
                        track.play();
                        /*
                         * Loops until something outside of this thread stops it.
                         * Reads the data from the recorder and writes it to the audio track for playback.
                         */
                        while (!recordingSchleife) {
                            short[] buffer = buffers[ix++ % buffers.length];
                            recorder.read(buffer, 0, buffer.length);
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
            Toast.makeText(getActivity().getApplicationContext(), R.string.no_ace, Toast.LENGTH_LONG).show();
        }
        if (mobileMicAppStatus.equals(MobileMicAppStatus.AN_RECORDING)) {
            Toast.makeText(getActivity().getApplicationContext(), R.string.mic_on, Toast.LENGTH_LONG).show();
        }

    }

    @Override
    public void onPause() {
        super.onPause();
        deaktiviereMic();
    }

    private void deaktiviereMic() {
        if (micStatus) {
            Toast.makeText(getActivity().getApplicationContext(), R.string.mic_off, Toast.LENGTH_LONG).show();
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
