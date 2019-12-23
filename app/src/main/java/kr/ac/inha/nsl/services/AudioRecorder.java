package kr.ac.inha.nsl.services;

import java.io.IOException;

import android.media.MediaRecorder;


public class AudioRecorder {
    // region Constants
    private final int SAMPLING_RATE = 44100;
    // endregion

    // region Variables
    private MediaRecorder recorder;
    private String path;
    private boolean started;
    // endregion

    public AudioRecorder(String path) {
        this.path = path;
        this.recorder = new MediaRecorder();
        this.started = false;
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        recorder.setAudioEncodingBitRate(64000);
        recorder.setAudioSamplingRate(SAMPLING_RATE);
        recorder.setOutputFile(path);
    }

    public void start() throws IOException {
        recorder.prepare();
        recorder.start();
        started = true;
    }

    public void stop() {
        if (started) {
            recorder.stop();
            recorder.release();
            started = false;
        }
    }

    public boolean isRecording() {
        return started;
    }

    public String getPath() {
        return path;
    }
}
