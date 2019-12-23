package kr.ac.inha.nsl.services;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.SilenceDetector;
import be.tarsos.dsp.io.android.AudioDispatcherFactory;
import be.tarsos.dsp.mfcc.MFCC;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import be.tarsos.dsp.pitch.PitchProcessor;
import kr.ac.inha.nsl.DatabaseHelper;
import kr.ac.inha.nsl.FileHelper;

import static kr.ac.inha.nsl.services.CustomSensorsService.DATA_SRC_AUDIO_LOUDNESS;

public class AudioFeatureRecorder {
    // region Constants
    public static final String TAG = "AudioFeatureRecorder";
    private final int SAMPLING_RATE = 22050;
    private final int AUDIO_BUFFER_SIZE = 1024;
    private static final double SILENCE_THRESHOLD = -65.0D;
    // endregion

    // region Variables
    private boolean started;
    private ExecutorService executor = Executors.newCachedThreadPool();
    private final List<float[]> mfccList = new ArrayList<>(200);
    private AudioDispatcher dispatcher;
    private Context context;
    private DatabaseHelper db;
    // endregion

    AudioFeatureRecorder(Context con) {
        context = con;
        db = new DatabaseHelper(con);
        dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(SAMPLING_RATE, AUDIO_BUFFER_SIZE, 512);
        //final MFCC mfcc = new MFCC(512, 16000, 10, 50, 300, 3000);
        final SilenceDetector silenceDetector = new SilenceDetector(SILENCE_THRESHOLD, false);
        AudioProcessor pitchProcessor = new PitchProcessor(PitchProcessor.PitchEstimationAlgorithm.FFT_YIN,
                SAMPLING_RATE,
                AUDIO_BUFFER_SIZE,
                new PitchDetectionHandler() {
                    @Override
                    public void handlePitch(PitchDetectionResult res, AudioEvent e) {
                        final float pitchInHz = res.getPitch();
                        //if (pitchInHz > 0)
                        //Log.e(TAG, "" + pitchInHz);
                    }
                });

        AudioProcessor mainAudioProcessor = new AudioProcessor() {

            @Override
            public void processingFinished() {
            }

            @Override
            public boolean process(AudioEvent audioEvent) {
                if (silenceDetector.currentSPL() >= -110.0D) {
                    //Log.e(TAG, "Audio loudness in Db: " + silenceDetector.currentSPL());
                    String value = System.currentTimeMillis() + " " + silenceDetector.currentSPL();
                    db.insertSensorData(DATA_SRC_AUDIO_LOUDNESS, value);
                }
                //mfccList.add(mfcc.getMFCC());
                return true;
            }
        };

        if (dispatcher == null)
            Log.e(TAG, "Dispatcher is NULL: ");
        //dispatcher.addAudioProcessor(mfcc);
        dispatcher.addAudioProcessor(silenceDetector);
        dispatcher.addAudioProcessor(pitchProcessor);
        dispatcher.addAudioProcessor(mainAudioProcessor);

        //new Thread(dispatcher, "Audio Dispatcher").start();
    }

    void start() {
        Log.d(TAG, "Started: AudioRecorder");
        executor.execute(dispatcher);
        started = true;
    }

    void stop() {
        Log.d(TAG, "Stopped: AudioRecorder");
        if (started) {
            dispatcher.stop();
            started = false;
        }
    }
}
