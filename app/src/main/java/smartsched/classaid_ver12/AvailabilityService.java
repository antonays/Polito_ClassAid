package smartsched.classaid_ver12;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.widget.Toast;

import classAidManager.Room;

public class AvailabilityService extends Service {
    public static final String BROADCAST_ACTION = "smartsched.classaid_ver11.availabilityService";
    private final Handler handler = new Handler();
    Intent intent;
    private Room thisRoom;

    @Override
    public void onCreate() {
        super.onCreate();

        intent = new Intent(BROADCAST_ACTION);
    }

    @Override
    public void onStart(Intent intent, int startId) {
        handler.removeCallbacks(sendUpdatesToUI);
        try {
            thisRoom = (Room) intent.getSerializableExtra("room");
        }
        catch (Exception e){
            Toast.makeText(getApplicationContext(), "Probem in Availability Service onStart() "+e.getMessage(), Toast.LENGTH_LONG).show();
        }
        handler.postDelayed(sendUpdatesToUI, 1000); // 1 second
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private Runnable sendUpdatesToUI = new Runnable() {
        public void run() {
            if (thisRoom!=null) {
                Analyze();
            }
            handler.postDelayed(this, 1000*60*15);
        }
    };

    private void Analyze() {
        try {
            boolean b = thisRoom.getAvailability();
            intent.putExtra("isDe", b);
            sendBroadcast(intent);
        }
        catch (Exception e){
            Toast.makeText(getApplicationContext(), "Probem in Availability Service Analyze(): "+ e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        handler.removeCallbacks(sendUpdatesToUI);
        super.onDestroy();
    }
}