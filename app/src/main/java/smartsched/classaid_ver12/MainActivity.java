package smartsched.classaid_ver12;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileObserver;
import android.os.Handler;
import android.os.Looper;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.philips.lighting.hue.sdk.PHHueSDK;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.ExecutionException;

import HueModule.Controller;
import fragments.CalendarFragmentCommunicator;
import fragments.MapFragmentCommunicator;
import motiondetector.SensorsActivity;
import motiondetector.data.Preferences;
import motiondetector.detection.AggregateLumaMotionDetection;
import motiondetector.detection.IMotionDetection;
import motiondetector.detection.LumaMotionDetection;
import motiondetector.detection.RgbMotionDetection;
import motiondetector.image.ImageProcessing;
import scheduleModule.*;
import classAidManager.*;


public class MainActivity extends SensorsActivity
        implements CalendarFragmentCommunicator, MapFragmentCommunicator {
    public static final String THISROOMNAME = "4D";
    GridView calendarView;
    private TextView todayDate;
    private TextView classroomName;
    private TextView freeroomName;
    Intent audioIntent;
    Intent availabilityIntent;
    Intent cardDetectingIntent;
    private Controller HueController;

    private static SurfaceView preview = null;
    private static SurfaceHolder previewHolder = null;
    private static Camera camera = null;
    private static boolean inPreview = false;
    private static long mReferenceTime = 0;
    private static IMotionDetection detector = null;
    private Room thisROOM;
    private String thisRoomName;
    private classAidManager mngr;
    private Set<CalendarEntry> scheduleList;
    private FragmentManager fragmentManager;
    private Fragment studentInfoFragment;
    private Fragment mapInfoFragment;
    //TODO - add politecnico logo at the top
    //TODO - add "pass card" hint label
    //TODO - test new schedule
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //=============
        // disable top android menu and bottom android menu
        // * to disable screen timeout - added property to RelativeLayout Element in activity xml
        //=============
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

        this.setContentView(R.layout.activity_main);
        // set up all room elements
        setUpSchedule();
        setUpManagerSystem();
        thisROOM = mngr.getRooms().get(THISROOMNAME);
        setUpCalendarView(thisROOM.getCalendar());
        setUpRoomParameters();
        cardDetectingIntent = new Intent(this, CardDetectingService.class);
        fragmentManager = getFragmentManager();

        studentInfoFragment = fragmentManager.findFragmentById(R.id.studentInfoFragment);

        mapInfoFragment = fragmentManager.findFragmentById(R.id.mapFragment);
        fragmentManager.beginTransaction()
                .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
                .hide(studentInfoFragment)
                .hide(mapInfoFragment)
                .commit();
    }
    private BroadcastReceiver cardBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                String idnumber = intent.getStringExtra("smartsched.classaid_ver12.idnumber");
                String command = intent.getStringExtra("smartsched.classaid_ver12.command");
                if (idnumber!=null && command.equals("open")) {
                    handleCardInsertion(idnumber, command);
                }
                else
                {
                    handleCardInsertion("", command);
                }
            }
            catch (Exception e){
                Toast.makeText(getApplicationContext(), "Fragment Failed", Toast.LENGTH_SHORT).show();
            }
        }
    };
    public MapFragmentCommunicator mapFragmentCommunicator;
    public CalendarFragmentCommunicator fragmentCommunicator;
    private void handleCardInsertion(String idnumber, String command){
        GregorianCalendar gc = new GregorianCalendar();
        studyDay sd = new studyDay(gc.get(GregorianCalendar.DAY_OF_WEEK));
        Student thisUser = mngr.getStudentByID(idnumber);
        String nextRoom;
        if (thisUser!=null){
            for (CalendarEntry c: thisUser.studentListCalendarEntries(gc)){
                sd.addEntry(c);
            }
            CalendarEntry e = sd.getNextCalendarEntry(new GregorianCalendar().getTime());
            boolean isExistNextRoom;
            if (e!=null){
                nextRoom = e.getRoom();
                isExistNextRoom = true;
            }
            else{
                nextRoom = thisROOM.getName();
                isExistNextRoom = false;
            }
            handleFragment(command, sd,idnumber, nextRoom, isExistNextRoom);
        }
        else{
            handleFragment(command,sd,idnumber, "nouser", true);
        }
    }

    private void handleFragment(String command, studyDay sd, String idnumber, String nextRoom, boolean isExistNextRoom) {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        if (fragmentCommunicator!=null) {
            fragmentCommunicator.passDataToFragment(sd,idnumber );
        }
        if (mapFragmentCommunicator!=null){
            mapFragmentCommunicator.passDataToMapFragment(nextRoom, isExistNextRoom);
        }
        fragmentTransaction.setCustomAnimations(android.R.animator.fade_in,
                android.R.animator.fade_out);
        if (command.equals("open")) {
            fragmentTransaction.show(studentInfoFragment);
            fragmentTransaction.show(mapInfoFragment);
        } else if (command.equals("close")){
            fragmentTransaction.hide(studentInfoFragment);
            fragmentTransaction.hide(mapInfoFragment);
        }
        fragmentTransaction.commit();
    }


//=================================
// Availability Service COmponents (e.g: frame around view, hue leds, closest available room)
//=================================
    private void initiateAvailabilityService() {
        // to make this work properly, need to add some delay to allow bridge search and connection
        PHHueSDK phHueSDK = PHHueSDK.create();
        phHueSDK.setAppName("ClassAid");
        phHueSDK.setDeviceName(android.os.Build.MODEL);
        HueController = new Controller();
        phHueSDK.getNotificationManager().registerSDKListener(HueController.getListener());
        try {
            HueController.findBridges();
            HueController.connectToLastKnownAccessPoint();
        }
        catch (Exception e){
            Toast.makeText(getApplicationContext(), "Failed to start HUE", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }

    }

    //is what handles any update recieved from the service
    private BroadcastReceiver availabilityBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

             updateAvailabilityIndicators(intent.getBooleanExtra("isDe", false));
        }
    };

    private void updateAvailabilityIndicators(Boolean b){

        View v = findViewById(R.id.layout);
        LayerDrawable bgDrawable = (LayerDrawable)v.getBackground();
        final GradientDrawable shape = (GradientDrawable)   bgDrawable.findDrawableByLayerId(R.id.colorFrame);
        try {
            if (b) {
                shape.setColor(Color.argb(255, 0, 255, 0)); //green
                HueController.setToGreen();
            } else {
                shape.setColor(Color.argb(255, 255, 0, 0)); // red
                HueController.setToRed();
            }

            mngr.setNearFreeRooms();
            try {
                //String nearFreeRoom = mngr.getRooms().get(thisROOM.getName()).getNearestFreeRoom().getName();
                //freeroomName.setText("Closest Free Room: "+ nearFreeRoom);
                String r = mngr.getNearFreeRoomName(thisROOM);
                freeroomName.setText("Closest Free Room: "+ r);
            }
            catch (NullPointerException e){
                Toast.makeText(getApplicationContext(), "Nearest free Room not updated", Toast.LENGTH_LONG).show();
                freeroomName.setText("Nearest Free Room Not Available");
            }

        }
        catch (NullPointerException ne){
            Toast.makeText(getApplicationContext(), "Hue was not updated", Toast.LENGTH_LONG).show();
        }
    }
//==========================
// Setup Activity Elements
//  -Rooms Parameters
//  -CalendarView
//  -Manager System
//  -Schedule
//  -Backgroiund Services: Motion Detection and Audio Analyzer
//==========================
    private void setUpRoomParameters(){
        DateFormat dateFormat = new SimpleDateFormat("EEEE - dd/MM/yyyy");// add HH:mm:ss to display time too
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+3"));
        Date today = Calendar.getInstance().getTime();

        todayDate = (TextView) findViewById(R.id.todayDateId);
        todayDate.setText(dateFormat.format(today));
        classroomName = (TextView) findViewById(R.id.ClassViewId);
        classroomName.setText(THISROOMNAME);
        freeroomName = (TextView) findViewById(R.id.FreeRoomId);
        freeroomName.setText("Closest Free Room: "+ mngr.getNearFreeRoomName(thisROOM));
        //freeroomName.setText("Closest Free Room: "+ thisROOM.getNearestFreeRoom().getName());
    }


    private void setUpCalendarView(Set<CalendarEntry> schedule){
        Map<Integer,studyDay> perDays = new HashMap<>(); // map indexed by integer which is the day of the week 1 = sunday
        // created studyDay Object which represents a day with CalendarEntry attributes for every time frame [ default: null ]
        // created a map for every week in the day with corresponding studyDay object
        for (CalendarEntry ce : schedule){
            if (!perDays.containsKey(ce.getWeekDay())){
                studyDay sd = new studyDay(ce.getWeekDay());
                sd.addEntry(ce);
                perDays.put(ce.getWeekDay(),sd);
            }
            else{
                perDays.get(ce.getWeekDay()).addEntry(ce);
            }
        }
        // created empty studyDays( all timeframes are null ) for days with no classes
        for (int i=2;i<=6;i++){
            if (!perDays.containsKey(i)) {
                studyDay sd = new studyDay(i);
                perDays.put(i,sd);
            }
        }

        //====================
        //Initiate the adapter View
        //====================
        CustomGrid adapter = new CustomGrid(MainActivity.this, perDays);
        calendarView=(GridView)findViewById(R.id.grid);
        try {
            calendarView.setAdapter(adapter);
        }
        catch(Exception e){ e.printStackTrace();}

    }

    private void setUpManagerSystem() {
        // classAidManager is an object that defines the 'map' of classRooms with relative distances
        // classAidManager is an object that recieves the scheduleList and organizes it by parameters: // by rooms // by course // by student Personal schedule
        try
         {
            mngr = new classAidManager(Environment.getExternalStorageDirectory(), scheduleList);
         }
        catch (IOException e){
            Toast.makeText(getApplicationContext(), "Room FILE Loading Problem", Toast.LENGTH_LONG).show();
        }
        catch (Exception e){
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void setUpSchedule() {
        // schedule is an object that is used to initiate the collection of all Calendar entries of classes,
        // filter identical entries and save & retrieve to\from file.
        // file is "schedule.txt" in /sdcard0/classAidRaw
        getScheduleWithProgress getSchedTask = new getScheduleWithProgress();
        try {
            Object res = getSchedTask.execute().get();
            if (res!=null){
                scheduleList = ((Schedule)res).getList();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    private void setUpVidDetection(){
        // Motion Detector is based on analyzing images detected in a hidden camera preview panel and comparing them to find
        // differences. Motion Detector is based on callbacks that initiate a thread to compare camera data
        //preview = (SurfaceView) findViewById(R.id.preview);
        previewHolder = preview.getHolder();
        preview.setVisibility(View.INVISIBLE);
        previewHolder.addCallback(surfaceCallback);
        previewHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        if (Preferences.USE_RGB) {
            detector = new RgbMotionDetection();
        } else if (Preferences.USE_LUMA) {
            detector = new LumaMotionDetection();
        } else {
            // Using State based (aggregate map)
            detector = new AggregateLumaMotionDetection();
        }
        if (preview != null)
            preview.setVisibility(View.VISIBLE);
    }

    @Override
    public void passDataToFragment(studyDay someValue, String idnumber) {

    }

    @Override
    public void passDataToMapFragment(String nextRoom, boolean isExistNextRoom) {

    }

    //==========================
// Schedule Fetching Thread
//==========================
    // ** system will wait for this thread to finish because can't continue without the schedule
    private class getScheduleWithProgress extends AsyncTask<Void, Integer, Schedule>{
        Schedule sched;
        @Override
        protected Schedule doInBackground(Void... params) {
            sched = null;
            try{
               sched = new Schedule("01/06/2015",Schedule.selection.FILE, Environment.getExternalStorageDirectory());
            }
            catch (IOException e) {
                Toast.makeText(getApplicationContext(), "Schedule FILE Loading Problem", Toast.LENGTH_LONG).show();
                return null;
            }
            catch (Exception e){
                e.printStackTrace();
            }
            return sched;
        }
    }

//======================================
// Audio Service Listener and UI Updater
//======================================
    // waits for events to arrive from audio analyzing service, then calls for a method that update UI
    private BroadcastReceiver audioBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateUI(intent);
        }
    };

    private void updateUI(Intent i){
        boolean b = i.getBooleanExtra("isDe", false);
        thisROOM.setOccupy(b);
    }

//==================
// Activity Life
//==================
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    private FileObserver stayingAlive;
    public void setObserver(FileObserver fo){
        stayingAlive = fo;
    }

    @Override
    public void onResume(){
        super.onResume();
        //camera = openFrontFacingCamera();
        //setUpVidDetection();
        //cardDetectingIntent = new Intent(this, CardDetectingService.class);
        //startService(cardDetectingIntent);
        startService(cardDetectingIntent);
        availabilityIntent = new Intent(this, AvailabilityService.class);
        availabilityIntent.putExtra("room", thisROOM);
        initiateAvailabilityService();
        startService(availabilityIntent);
        audioIntent = new Intent(this, AudioAnalyzerService.class);
        startService(audioIntent);
        //try {
        //    if (preview != null)
        //        preview.setVisibility(View.VISIBLE);
        //}
    //
        //catch (Exception e){
        //    Toast.makeText(getApplicationContext(), "Failed to start motion detection", Toast.LENGTH_SHORT).show();
        //}
    }

    @Override
    public void onPause() {
        super.onPause();

        //camera.setPreviewCallback(null);
        //if (inPreview) camera.stopPreview();
        //inPreview = false;
        //camera.release();
        //camera = null;
    }

    @Override
    public void onStart(){
        super.onStart();
        try {
            registerReceiver(audioBroadcastReceiver, new IntentFilter(AudioAnalyzerService.BROADCAST_ACTION));
        }
        catch (Exception e){
            Toast.makeText(getApplicationContext(), "Failed to register audio service", Toast.LENGTH_SHORT).show();
        }
        try {
            registerReceiver(availabilityBroadcastReceiver, new IntentFilter(AvailabilityService.BROADCAST_ACTION));
        }
        catch (Exception e){
            Toast.makeText(getApplicationContext(), "Failed to register availability service", Toast.LENGTH_SHORT).show();
        }
        try {
            registerReceiver(cardBroadcastReceiver, new IntentFilter(CardDetectingService.BROADCAST_ACTION));
        }
        catch (Exception e){
            Toast.makeText(getApplicationContext(), "Failed to register card Detecting service", Toast.LENGTH_SHORT).show();
        }
    }

    public void onDestroy(){
        super.onDestroy();
        unregisterReceiver(availabilityBroadcastReceiver);
        unregisterReceiver(audioBroadcastReceiver);
        unregisterReceiver(cardBroadcastReceiver);
        stopService(cardDetectingIntent);
        stopService(audioIntent);
        stopService(availabilityIntent);

    }

//==================
// Motion Detector
//==================
    private Camera.PreviewCallback previewCallback = new Camera.PreviewCallback() {

        @Override
        public void onPreviewFrame(byte[] data, Camera cam) {
            Handler handler = new Handler();
            if (data == null) return;
            Camera.Size size = cam.getParameters().getPreviewSize();
            if (size == null) return;

                final ArrayList<Object> ls = new ArrayList<>();
                ls.add(data);
                ls.add(size.width);
                ls.add(size.height);
                final DetectionThread dt = new DetectionThread(data,size.width,size.height);
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        dt.execute();
                    }
                },1000);
            }
    };

    private SurfaceHolder.Callback surfaceCallback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            try {
                camera.setPreviewDisplay(previewHolder);
                camera.setPreviewCallback(previewCallback);
            } catch (Throwable t) {
                Toast.makeText(getApplicationContext(), "Surface Callback failed", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            Camera.Parameters parameters = camera.getParameters();
            Camera.Size size = getBestPreviewSize(width, height, parameters);
            if (size != null) {
                parameters.setPreviewSize(size.width, size.height);
            }
            camera.setParameters(parameters);
            camera.startPreview();
            inPreview = true;
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {

        }
    };

    private static Camera.Size getBestPreviewSize(int width, int height, Camera.Parameters parameters) {
        Camera.Size result = null;
        for (Camera.Size size : parameters.getSupportedPreviewSizes()) {
            if (size.width <= width && size.height <= height) {
                if (result == null) {
                    result = size;
                } else {
                    int resultArea = result.width * result.height;
                    int newArea = size.width * size.height;

                    if (newArea > resultArea) result = size;
                }
            }
        }
        return result;
    }
    //                                      -- Type 1 --------Type 2 --Type 3 --
    class DetectionThread extends AsyncTask<Void, Void, Boolean> {
        //Type1 - params of doInBackground, Type2 - params of onProgressUpdate, Type3 - return type of doInBackground, params of onPostExecute

        private byte[] data;
        private int width;
        private int height;

        public DetectionThread(byte[] d, int w, int h) {
            data = d;
            height = h;
            width = w;
        }

        @Override
        protected void onPreExecute(){
            preview.setVisibility(View.INVISIBLE);
        }
        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                // Previous frame
                int[] pre = null;
                if (Preferences.SAVE_PREVIOUS) pre = detector.getPrevious();

                // Current frame (with changes)
                int[] img = null;
                if (Preferences.USE_RGB) {
                    img = ImageProcessing.decodeYUV420SPtoRGB(data, width, height);
                } else {
                    img = ImageProcessing.decodeYUV420SPtoLuma(data, width, height);
                }

                // Current frame (without changes)
                int[] org = null;
                if (Preferences.SAVE_ORIGINAL && img != null) org = img.clone();

                if (img != null && detector.detect(img, width, height)) {
                    long now = System.currentTimeMillis();
                    if (now > (mReferenceTime + Preferences.PICTURE_DELAY)) {
                        //Thread.sleep(3000);
                        Looper.prepare();
                    }
                    return true;
                }
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), "Detection Service stopped", Toast.LENGTH_SHORT).show();}
            return false;
        }

        protected void onPostExecute(Boolean isDe) {
            WindowManager.LayoutParams lp = getWindow().getAttributes();
            if (isDe) {
                lp.screenBrightness = 0.8f;
                getWindow().setAttributes(lp);
            }
            else{
                lp.screenBrightness = 0.001f;
                getWindow().setAttributes(lp);
            }
        }
    };

    // setup and open front camera
    private Camera openFrontFacingCamera() {
        int cameraCount = 0;
        Camera cam = null;
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        cameraCount = Camera.getNumberOfCameras();
        for (int camIdx = 0; camIdx<cameraCount; camIdx++) {
            Camera.getCameraInfo(camIdx, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                try {
                    cam = Camera.open(camIdx);
                } catch (RuntimeException e) {
                    Toast.makeText(getApplicationContext(), "Failed to open Camera", Toast.LENGTH_SHORT).show();
                }
            }
        }
        return cam;
    }
}

