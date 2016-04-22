package com.sandroid.freetracker;

import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;

// ---Просмотр сохранённого маршрута---
// Активность создаётся при выборе одного из маршрутов на активности History
// Во входящем интенте содержится ID маршрута

public class HistoryView extends AppCompatActivity {

    //Включение тестового режима программы
    protected boolean Test = true;

    final String LOG_TAG = "myLogs";

    private GoogleMap googleMap;            //карта
    private MapController mapController;    //Экземпляр контроллера управляющего картой.
    private int CurrentWayID;               //ID текущего маршрута
    private Way CurrentWay;                 //Текущий маршрут

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_view);

        //Создаю объект для входящего интента
        Intent intent = getIntent();
        //Получаю из интента входящие параметры. если параметров нет то по умолчанию -1
        CurrentWayID = intent.getIntExtra("selectedWayID", -1);

        //Иннициирую объект маршрута(Way) перед использованием.
        // Проверка на >= 0 нужна для того если кто-то запустил активность с пустым интентом
        if(CurrentWayID >= 0){
            CurrentWay = GetWayfromDB();
        }
        else{
            CurrentWay = null;
            //Если сюда попали то значит криво был сформирован интент для старта активности
            Log.d(LOG_TAG, "HistoryView: Стартовано без ID маршута!" );
        }

        //Запускаю создание карты
        createMapView();

        //Если карта создалась, и маршрут не пустой тогда делаем магию
        if(googleMap != null && CurrentWay != null){
            //Создаю контроллер карты и передаю в него маршрут
            mapController = new MapController(googleMap);
            mapController.addAllWay(CurrentWay,Test);
        }
        else {
            Log.d(LOG_TAG, "HistoryView: ERROR Карта не создана или пустой маршрут");
        }
    }

    private void createMapView(){
        try {
            if(googleMap == null){
                //Присваиваем нашей карте соответствующий объект на активности
                googleMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.mapHistoryView)).getMap();
                //Карта будет обычной схемой
                googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                if(null == googleMap) {
                    Toast.makeText(getApplicationContext(),
                            "Error creating map", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (NullPointerException exception){
            Log.e("mapApp", exception.toString());
        }
    }

    //Вытаскиваю маршрут из базы
    protected Way GetWayfromDB() {

        Way ZZWay = new Way(CurrentWayID); //Готовый массив маршрутов

        //Запрашиваю у базы маршрут по id
        //Формирую ury из полного ury + полученный по интенту ID
        Uri uri = ContentUris.withAppendedId(RunerDBProvider.ALL_WAY_URI, CurrentWayID);
        //запускаю курсор
        Cursor cursor = getContentResolver().query(uri, null, null,null, null);

        // Определяю номера столбцов по имени в выборке
        int wayColIndex = cursor.getColumnIndex("way_id");
        int longitudeColIndex = cursor.getColumnIndex("longitude");
        int latitudeColIndex = cursor.getColumnIndex("latitude");
        int accuracyColIndex = cursor.getColumnIndex("accuracy");
        int bearingColIndex = cursor.getColumnIndex("bearing");
        int altitudeColIndex = cursor.getColumnIndex("altitude");
        int speedColIndex = cursor.getColumnIndex("speed");
        int timeColIndex = cursor.getColumnIndex("time");

        // Ставлю позицию курсора на первую строку выборки
        // Если в выборке нет строк, вернется false
        if (cursor.moveToFirst()) {
            do {
                //Если получаемый ID из базы равен нашему то заполняем маршрут.
                if(CurrentWayID == cursor.getInt(wayColIndex)) {
                    Double longitude = cursor.getDouble(longitudeColIndex);
                    Double latitude = cursor.getDouble(latitudeColIndex);
                    Float acuracy = cursor.getFloat(accuracyColIndex);
                    Float bearing = cursor.getFloat(bearingColIndex);
                    Double alt = cursor.getDouble(altitudeColIndex);
                    Float speed = cursor.getFloat(speedColIndex);
                    Long time = new Long(new String(cursor.getString(timeColIndex)));

                    ZZWay.AddLocationToWay(new PointLocation(longitude, latitude, acuracy, bearing, alt, speed, time));
                }
                //Иначе ругаемся и выходим
                else{
                    Log.d(LOG_TAG, "HistoryView: ContentProvider передаёт данные не по тому ID. ID = " + cursor.getInt(wayColIndex));
                }
            }while (cursor.moveToNext());
        }
        return ZZWay;
    }
}
