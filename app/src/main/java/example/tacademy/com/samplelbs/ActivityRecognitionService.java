package example.tacademy.com.samplelbs;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import java.util.List;

public class ActivityRecognitionService extends Service {
    public ActivityRecognitionService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
        if (result != null) {
            List<DetectedActivity> list = result.getProbableActivities();
            for (DetectedActivity da : list) {
                switch (da.getType()) {
                    case DetectedActivity.ON_FOOT:
                    case DetectedActivity.ON_BICYCLE:
                    case DetectedActivity.IN_VEHICLE:
                }
                Log.i("Activity", "confid : " + da.getConfidence());
            }
        }
        return Service.START_NOT_STICKY;
    }
}
