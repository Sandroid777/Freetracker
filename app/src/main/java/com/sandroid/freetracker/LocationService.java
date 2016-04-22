package com.sandroid.freetracker;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

public class LocationService extends Service implements LocationListener {

    final String LOG_TAG = "myLogs";

    //Идентификатор для уведомления
    private static final int NOTIFY_ID = 101;

    //URI для запросов в базе
    final Uri ALL_WAY_URI = Uri.parse("content://com.sandroid.providers.RunTracker/Way");


    private MyBinder binder = new MyBinder();

    //Менеджер системных сервисов
    private LocationManager mgr;

    //Текущий (записываемый) маршрут.
    private Way currentWay;

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //ЖИЗНЕНЫЙ ЦИКЛ СЕРВИСА//////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(LOG_TAG, "MyService onCreate");

        mgr = (LocationManager) getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        //Выбираю лучшего GPS провайдера
        String best = mgr.getBestProvider(criteria, true);

        // Запуск получения GPS локаций
        mgr.requestLocationUpdates(best, 5000, 1, this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //Создаём объект Way
        currentWay = new Way(intent.getIntExtra("ID", 0));

        //Показываю уведомление
        sendNotif();

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        //Сохраняю маршрут в базу
        SaveWayToDB();

        super.onDestroy();
        Log.d(LOG_TAG, "MyService onDestroy");
        //Останавливаем обновление GPS
        mgr.removeUpdates(this);
        //Удаляю уведомление
        dropNotif();
    }
    //ЖИЗНЕНЫЙ ЦИКЛ КОНЕЦ////////////////////////////////////////////////////////////////////////////////////////

    //Привязка к сервису Binder//////////////////////////////////////////////////////////////////////////////////
    public IBinder onBind(Intent arg0) {
        Log.d(LOG_TAG, "MyService onBind");
        return binder;
    }

    public void onRebind(Intent intent) {
        super.onRebind(intent);
        Log.d(LOG_TAG, "MyService onRebind");
    }

    public boolean onUnbind(Intent intent) {
        Log.d(LOG_TAG, "MyService onUnbind");
        return super.onUnbind(intent);
    }

    class MyBinder extends Binder {
        LocationService getService() {
            return LocationService.this;
        }
    }
    //Binder конец//////////////////////////////////////////////////////////////////////////////////////////////

    //Работа с GPS///////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void onLocationChanged(Location newloc) {
        PointLocation point = new PointLocation(newloc.getLongitude(), newloc.getLatitude(), newloc.getAccuracy(),
                newloc.getBearing(), newloc.getAltitude(), newloc.getSpeed(), System.currentTimeMillis()); //Перевожу локэйшн в свой формат PointLocation

        currentWay.AddLocationToWay(point);

        //Информирую приложение о наличии новой локации
        Intent service_intent = new Intent(MainActivity.BROADCAST_ACTION);
        service_intent.putExtra(MainActivity.SERVICE_MESSAGE, MainActivity.THROW_LOCATION);
        sendBroadcast(service_intent);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        //Пока не использую
        Log.d(LOG_TAG, "MyService onStatusChanged");
    }

    @Override
    public void onProviderEnabled(String provider) {
        //Пока не использую
        Log.d(LOG_TAG, "MyService onProviderEnabled");
    }

    @Override
    public void onProviderDisabled(String provider) {
        //Пока не использую
        Log.d(LOG_TAG, "MyService onProviderDisabled");
    }
    //GPS КОНЕЦ//////////////////////////////////////////////////////////////////////////////////////////////////


    //Сервис возвращает последнюю точку маршрута PointLocation вызываться будет из bind-сервис
    public PointLocation GetLastLocations(){
        Log.d(LOG_TAG, "MyService GetLastLocations");
        //Если маршрут пустой возвращаю null
        if(currentWay.isEmpty()) {
            return null;
        }
        else {
            return currentWay.GetLastLocations();
        }
    }

    //Возвращаю текущий маршрут вызываться будет из bind-сервис
    public Way GetAllWay(){
        Log.d(LOG_TAG, "MyService GetAllWay");
        //Если маршрут пустой возвращаю null
        if(currentWay.isEmpty()){
            return null;
        }
        else {
            return currentWay;
        }
    }

    //Сохранение в базу данных
    public boolean SaveWayToDB() {
        if (currentWay.isEmpty()) {
            Log.d(LOG_TAG, "SaveWayToDB, NO DATA TO INSERT");
            return false;
        }
        else {
            //Создаю массив данных равный по длине массиву точек маршрута
            int size = currentWay.GetAllLocations().size();

            ContentValues[] cv_array = new ContentValues[size];

            //Заполняю массив данными
            for(int i=0; i < size; i++) {
                cv_array[i] = new ContentValues();
                cv_array[i].put("way_id", currentWay.GetWayID());
                cv_array[i].put("longitude", currentWay.GetAllLocations().get(i).GetPointLongitude());
                cv_array[i].put("latitude", currentWay.GetAllLocations().get(i).GetPointLatitude());
                cv_array[i].put("accuracy", currentWay.GetAllLocations().get(i).GetPointAccuracy());
                cv_array[i].put("bearing", currentWay.GetAllLocations().get(i).GetPointBearing());
                cv_array[i].put("altitude", currentWay.GetAllLocations().get(i).GetPointAltitude());
                cv_array[i].put("speed", currentWay.GetAllLocations().get(i).GetPointSpeed());
                cv_array[i].put("time", currentWay.GetAllLocations().get(i).GetPointTime());
            }

            //Передаю в ContentProvider данные пачкой
            int inserted_count = getContentResolver().bulkInsert(ALL_WAY_URI, cv_array);

            //Информирую приложение о успешном сохранении.
            Intent service_intent = new Intent(MainActivity.BROADCAST_ACTION);
            service_intent.putExtra(MainActivity.SERVICE_MESSAGE, MainActivity.INSERT_OK);
            sendBroadcast(service_intent);

            Log.d(LOG_TAG, "SaveWayToDB, " + inserted_count + " RAW INSERTED");
            return true;
        }
    }

    //Notification//////////////////////////////////////////////////////////////////////////////////////////////
    private void sendNotif() {
        Context context = getApplicationContext();
        Resources res = context.getResources();

        Intent notificationIntent = new Intent(context, MainActivity.class);

        Notification.Builder builder = new Notification.Builder(this);

        //Настраиваю уведомлние
        builder.setSmallIcon(R.drawable.ic_stat_maps_navigation)
                .setLargeIcon(BitmapFactory.decodeResource(res, R.drawable.ic_notif_big_icon))
                .setAutoCancel(true)
                .setContentIntent(PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT))
                .setTicker("FreeTracker!!!!")
                .setContentTitle("FreeTracker")
                .setContentText("Application steel running!")
                .setDefaults(Notification.DEFAULT_ALL);

        //Инициирую чтоб не было ошибок
        Notification notification = null;
        //На разных API уведомление работает по разному позтому
        //Проверяю версию андройда если (api 16 Android 4.1 Jellybean)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            notification = builder.build();
        }
        //Если версия ниже
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {
            notification = builder.getNotification();
        }
        //Вывожу уведомление
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(NOTIFY_ID, notification);

    }

    //Удаление уведомления
    private void dropNotif() {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.cancel(NOTIFY_ID);
    }
}
