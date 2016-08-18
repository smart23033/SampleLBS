package example.tacademy.com.samplelbs;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.LocationManager;
import android.os.IBinder;
import android.support.v7.app.NotificationCompat;

public class ProximityService extends Service {
    public ProximityService() {
    }

    NotificationManager mNM;

    @Override
    public void onCreate() {
        super.onCreate();
        mNM = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Address address = intent.getParcelableExtra("address");
        boolean isEnter = intent.getBooleanExtra(LocationManager.KEY_PROXIMITY_ENTERING,false);
        if(isEnter){
            sendNotification("enter",address);
        }else{
            sendNotification("exit",address);
        }
        return Service.START_NOT_STICKY;
    }

    private void sendNotification(String title, Address address){
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(android.R.drawable.ic_dialog_alert);
        builder.setTicker("proximity");
        builder.setContentTitle(title);
        builder.setContentText("lat : " + address.getLatitude() + ", lng : " + address.getLongitude());
        builder.setAutoCancel(true);
        PendingIntent pi = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pi);
        builder.setDefaults(NotificationCompat.DEFAULT_ALL);

        mNM.notify(0, builder.build());
    }
}
