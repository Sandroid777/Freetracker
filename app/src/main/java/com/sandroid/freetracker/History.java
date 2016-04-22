package com.sandroid.freetracker;

import android.content.ContentUris;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;


import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;

import java.util.ArrayList;

public class History extends AppCompatActivity {

    //Включение тестового режима программы
    protected boolean Test = true;

    final String LOG_TAG = "myLogs";//ЛОГИ ПРИЛОЖЕНИЯ

    private ListView listView;
    private HistoryViewAdapter historyViewAdapter;
    private ArrayList<Way> WayArray;
    private MapController mapController;
    private GoogleMap googleMap;
    private Way CurrentWay;
    private int CurrentWayID;

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ///----БЛОК ЖИЗНЕНОГО ЦИКЛА------///////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        //Заполняю массив Way для адаптера
        WayArray = GetWayData();

        //Нахожу список ListView
        listView = (ListView) findViewById(R.id.history_listView);

        //Создаю адаптер списка
        historyViewAdapter = new HistoryViewAdapter(this, WayArray);

        //присваиваю адаптер ListView
        listView.setAdapter(historyViewAdapter);

        //Вешаю обработчик событий на ListView
        listView.setOnItemClickListener(new OnItemClickListener() {
            //Переопределяю метод который будет вызываться когда пользователь нажимает на элемент
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(LOG_TAG, "itemClick: position = " + position + ", id = " + id);
                //Передаю в метод номер выбранного элемента из списка
                goToHistoryView(position);
            }
        });

        //Если экран большой и горизонтальный создаю карту
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE && isLarge()){
            createMapView();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
}
    ////////////////////////////////////////////////////////////////////////////////////////////////
    ///----КОНЕЦ БЛОКА ЖИЗНЕНОГО ЦИКЛА------////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////


    //Отрисовка маршрута на карте или переход на активность HistoryView
    private void goToHistoryView(int indexWay) {

        //На всякий случай очищаю переменные
        ClearWays();

        //Опредиляю размер экрана
        //Если большой и горизонтальный то выводить карту буду в боковой фрагмент
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE && isLarge()){

            //Задаю идентификатор маршрута
            CurrentWayID = WayArray.get(indexWay).GetWayID();
            //Заполняю маршрут CurrentWay
            CurrentWay = GetWayfromDB();


            //Если карта проиннициализирована и маршрут не пустой тогда делаем магию
            if(googleMap != null && CurrentWay != null){
                //Если карта иннициализировалась тогда создаю контроллер
                mapController = new MapController(googleMap);
                mapController.addAllWay(CurrentWay, Test);
            }
            else {
                Log.d(LOG_TAG, "HistoryView: ERROR Карта не создана или пустой маршрут");
            }
        }
        //Сюда попадаем в случае если экран маленький или вертикальный.
        //Для отображения карты будем запускать свою активность и передавать в неё id маршрута
        else {
            //Опредиляю ID маршрута для выбранного элемента.
            int selectedWayID = WayArray.get(indexWay).GetWayID();

            Intent intent = new Intent(this, HistoryView.class);
            intent.putExtra("selectedWayID", selectedWayID);
            startActivity(intent);
        }
    }

    ////Очитска переменных
    private void ClearWays() {
        CurrentWayID = -1;
        CurrentWay = null;
    }

    //Подготовка данных для адаптера
    protected ArrayList<Way> GetWayData() {

        ArrayList<Way>array = new ArrayList<>(); //Готовый массив маршрутов

        //Запрашиваю у базы все данные по всем маршрутам
        Cursor cursor = getContentResolver().query(RunerDBProvider.ALL_WAY_URI, null, null,null, null);

        // Определяем номера столбцов по имени в выборке
        int wayColIndex = cursor.getColumnIndex("way_id");
        int longitudeColIndex = cursor.getColumnIndex("longitude");
        int latitudeColIndex = cursor.getColumnIndex("latitude");
        int accuracyColIndex = cursor.getColumnIndex("accuracy");
        int bearingColIndex = cursor.getColumnIndex("bearing");
        int altitudeColIndex = cursor.getColumnIndex("altitude");
        int speedColIndex = cursor.getColumnIndex("speed");
        int timeColIndex = cursor.getColumnIndex("time");

        int tmpID = 0;                  //Переменная для разделения ID в потоке
        Way tmpWay = new Way(tmpID);    //Временный маршрут для цикла

        //Ставим позицию курсора на первую строку выборки
        //Если в выборке нет строк, вернется false
        if (cursor.moveToFirst()) {
            do {
                //Если id маршрута изменился, то создаю новый маршрут
                if(tmpID != cursor.getInt(wayColIndex)){
                    tmpWay = new Way(cursor.getInt(wayColIndex));
                    tmpID = cursor.getInt(wayColIndex);
                    array.add(tmpWay);
                }
                //Вытаскиваю поля для точки маршрута
                Double longitude = cursor.getDouble(longitudeColIndex);
                Double latitude = cursor.getDouble(latitudeColIndex);
                Float acuracy = cursor.getFloat(accuracyColIndex);
                Float bearing = cursor.getFloat(bearingColIndex);
                Double alt = cursor.getDouble(altitudeColIndex);
                Float speed = cursor.getFloat(speedColIndex);
                Long time = new Long(new String(cursor.getString(timeColIndex)));

                //Полученную точку добавляю к маршруту
                tmpWay.AddLocationToWay(new PointLocation(longitude, latitude, acuracy, bearing, alt, speed,time));
             //Крутимся пока не закончатся данные
            }while (cursor.moveToNext());
        }
        //Возвращаю получившийся массив маршрутов
        return array;
    }

    //Проверка на размер устройства
    boolean isLarge() {
        return (getResources().getConfiguration().screenLayout &
                Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    //Создаю карту
    private void createMapView(){
        try {
            if(googleMap == null){
                //Присваивем нашей карте соотвествующий обьект на активити
                googleMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.mapView)).getMap();
                //карта будет обычной схемой
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

        //Готовый маршрут
        Way ZZWay = new Way(CurrentWayID);

        //Запрашиваю у базы маршрут по id
        //Формирую ury из полного ury + полученный по интенту ID
        Uri uri = ContentUris.withAppendedId(RunerDBProvider.ALL_WAY_URI, CurrentWayID);
        //Запускаю запрос к базе
        Cursor cursor = getContentResolver().query(uri, null, null,null, null);

        //Определяю номера столбцов по имени в выборке
        int wayColIndex = cursor.getColumnIndex("way_id");
        int longitudeColIndex = cursor.getColumnIndex("longitude");
        int latitudeColIndex = cursor.getColumnIndex("latitude");
        int accuracyColIndex = cursor.getColumnIndex("accuracy");
        int bearingColIndex = cursor.getColumnIndex("bearing");
        int altitudeColIndex = cursor.getColumnIndex("altitude");
        int speedColIndex = cursor.getColumnIndex("speed");
        int timeColIndex = cursor.getColumnIndex("time");

        // Ставим позицию курсора на первую строку выборки
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
