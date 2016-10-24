package smartsched.classaid_ver12;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileObserver;
import android.os.IBinder;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import scheduleModule.CalendarEntry;

public class CardDetectingService extends Service {

    public static final String BROADCAST_ACTION = "smartsched.classaid_ver12.cardDetectingService";
    FileObserver observer;
    String dirPath;
    @Override
    public void onCreate() {
        super.onCreate();
        dirPath = Environment.getExternalStorageDirectory() + "/classAidRaw/";
    }

    @Override
    public int onStartCommand(Intent intent, int flags,  int startId) {
        super.onStartCommand(intent,flags,startId);
        observer = new FileObserver(dirPath) {
            @Override
            public void onEvent(int event, String path) {
                String idnum=null;
                if(path!=null) {
                    if (path.equals("inserted.txt")) {
                        Intent thisintent = new Intent(BROADCAST_ACTION);
                        //TODO - make this shit work
                        String filePath = dirPath + path;
                        switch (event) {
                            case (FileObserver.CREATE):
                                thisintent.putExtra("smartsched.classaid_ver12.command", "open");
                                try {
                                    Thread.sleep(500);
                                    idnum = getStudentId(filePath);
                                    Thread.sleep(500);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }

                                thisintent.putExtra("smartsched.classaid_ver12.idnumber", idnum);
                                sendBroadcast(thisintent);
                                break;
                            case (FileObserver.DELETE):
                                thisintent.putExtra("smartsched.classaid_ver12.command", "close");
                                sendBroadcast(thisintent);
                                break;

                            default:
                                break;
                        }
                    }
                }

            }
        };
        observer.startWatching();
        return Service.START_STICKY;
    }
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy(){
        observer.stopWatching();
    }

    private String getStudentId(String path) throws IOException {
        BufferedReader br;
        String line;
        br = new BufferedReader(new FileReader(path));
        line = br.readLine();
        br.close();
        return line;
    }
}
