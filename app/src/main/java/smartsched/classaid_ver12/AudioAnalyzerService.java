package smartsched.classaid_ver12;

import android.app.Service;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.IBinder;

public class AudioAnalyzerService extends Service {
    public static final String BROADCAST_ACTION = "smartsched.classaid_ver11.AudioAnalyzerService";
    private final Handler handler = new Handler();
    Intent intent;
    int counter = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        intent = new Intent(BROADCAST_ACTION);
    }


    @Override
    public void onStart(Intent intent, int startId) {
        handler.removeCallbacks(sendUpdatesToUI);
        handler.postDelayed(sendUpdatesToUI, 1000); // 1 second
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private Runnable sendUpdatesToUI = new Runnable() {
        public void run() {
            Analyze();
            handler.postDelayed(this, 1000*60*10); // 5 minutes
        }
    };

    private void Analyze() {
        boolean x = isBlowing();
        intent.putExtra("isDe", x);
        sendBroadcast(intent);
    }

    @Override
    public void onDestroy() {
        handler.removeCallbacks(sendUpdatesToUI);
        super.onDestroy();
    }

    public boolean isBlowing(){
        boolean recorder=true;
        int counter=0;
        int minSize = AudioRecord.getMinBufferSize( // minimum buffer size for succesful creation of AudioRecord object
                8000, // Sample Rate in [Hz]
                AudioFormat.CHANNEL_IN_MONO, // Channel Configuration - set it to Mono
                AudioFormat.ENCODING_PCM_16BIT); // format to represent the audio data -- 16 bit supported in any device

        AudioRecord ar = new AudioRecord(
                MediaRecorder.AudioSource.MIC, // Audio Source -- Mic
                8000, // Sample Rate in [Hz]
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                minSize); // Size of buffer data is written to

        short[] buffer = new short[minSize];

        ar.startRecording();
        while(recorder)
        {
            ar.read(buffer, 0, minSize);
            for (short s : buffer)
            {
                counter++;
                if (Math.abs(s) > 18000)
                {
                    ar.stop();
                    recorder=false;
                    return true;
                }
            }
            if (counter>1000) recorder = false;
        }
        ar.stop();
        return false;

    }
}
