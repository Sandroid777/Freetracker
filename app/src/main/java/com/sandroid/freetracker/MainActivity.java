package com.sandroid.freetracker;


import android.app.ActivityManager;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;

import java.util.List;


public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    //Включение тестового режима программы
    protected boolean Test = true;

    //Логи приложения
    final String LOG_TAG = "myLogs";

    //UI элементы
    protected Button btnStartStopService;   //Кнопка которая стартует и останавливает сервис
    protected BroadcastReceiver br;         //BroadcastReceiver получаю через него сообщения от сервиса

    private GoogleMap googleMap;            //Карта
    private MapController mapController;    //Экземпляр контроллера управляющего картой.

    //URI для запросов в базе
    final Uri ALL_WAY_URI = Uri.parse("content://com.sandroid.providers.RunTracker/Way");

    //Константы для BroadcastReceiver
    public final static String BROADCAST_ACTION = "com.sandroid.freetracker";
    public final static String SERVICE_MESSAGE = "message";

    //Сообщения для BroadcastReceiver
    public final static int INSERT_OK = 1;
    public final static int THROW_LOCATION = 3;

    boolean bound = false;  //В этой переменной будем хранить значение конекта Подключен/Отключен
    ServiceConnection sConn;
    LocationService myService;

    //!!!ВНИМАНИЕ ИЗМЕНЯТЬ ЗНАЧЕНИЕ ВСЕГДА ПЕРЕД bindService
    public int currentTask;

    //Задачи для сервиса. Необходимы для переменной currentTask
    public final static int NULL = 0;
    public final static int GET_LAST = 1;
    public final static int GET_ALL = 2;

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ///----БЛОК ЖИЗНЕНОГО ЦИКЛА------///////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        currentTask = NULL; //Инициализирую чтоб не болталась

        //Кнопка которая будет запускать и останавливать сервис
        btnStartStopService = (Button) findViewById(R.id.btnStartStop);
        btnStartStopService.setOnClickListener(this);

        ///////////////////////////////////////////////////////////////////////////////////////////
        //Создаю BroadcastReceiver который будет ждать сообщения от сервиса
        //и запускать привязку (bindService)
        //////////////////////////////////////////////////////////////////////////////////////////
        br = new BroadcastReceiver() {
            @Override //Переопределяю метод который будет обрабатывать сообщения
            public void onReceive(Context context, Intent intent) {
                int serviceMSG = intent.getIntExtra(SERVICE_MESSAGE, 0);
                switch(serviceMSG) {
                    case INSERT_OK:
                        Log.d(LOG_TAG, "MainActivity BroadcastReceiver new message (Сохранение в базу данных прошло успешно!)");
                        break;
                    case THROW_LOCATION:
                        Log.d(LOG_TAG, "MainActivity BroadcastReceiver new message (У сервиса есть новая локация!)");
                        currentTask = GET_LAST;
                        bindService(new Intent(context, LocationService.class), sConn, 0);
                    break;
                }
            }
        };
        //Создаю интент фильтр для BroadcastReceiver и регистрирую
        IntentFilter intFilt = new IntentFilter(BROADCAST_ACTION);
        registerReceiver(br, intFilt);
        //////////////////////////////////////////////////////////////////////////////////////////
        //---BroadcastReceiver END---
        //////////////////////////////////////////////////////////////////////////////////////////

        //Создаю объект для коннекта с сервисом и переопределяю методы onServiceConnected и
        //onServiceDisconnected. Они будут запускаться автоматически при соединении с сервисом.
        sConn = new ServiceConnection() {
            public void onServiceConnected(ComponentName name, IBinder binder) {
                    Log.d(LOG_TAG, "MainActivity onServiceConnected");
                    //Вытаскиваю из IBinder ссылку на сервис.
                    myService = ((LocationService.MyBinder) binder).getService();
                    bound = true;

                    //Во время привязки к сервису должен был проставлен флаг currentTask.
                    switch (currentTask){
                        case NULL:      //0
                            Log.d(LOG_TAG, "MainActivity onServiceConnected ERROR NULL in currentTask");
                            break;
                        case GET_LAST:  //1
                            //Вытаскиваю из сервиса последнюю локацию и передаю её на карту
                            mapController.addMarker(myService.GetLastLocations(), Test);
                            //Обновляю данные на InfoPanel
                            UpdateInfoPanel(myService.GetAllWay());
                            //Отвязываемся от сервиса
                            unbindService(sConn);
                            break;
                        case GET_ALL:   //2
                            //Вытаскиваю из сервиса весь маршрут и отправляю в MapController
                            mapController.addAllWay(myService.GetAllWay(), Test);

                            //На всякий случай очищаю InfoPanel чтоб не было устаревших данных.
                            ClearInfoPanel();

                            //Обновляю данные на InfoPanel если маршрут не пустой.
                            UpdateInfoPanel(myService.GetAllWay());

                            //Отвязываюсь от сервиса
                            unbindService(sConn);
                            break;
                    }
            }

            public void onServiceDisconnected(ComponentName name) {
                    myService = null;
                    Log.d(LOG_TAG, "MainActivity onServiceDisconnected");
                    bound = false;
                    }
                };


        //Создаю карту
        createMapView();
        if(googleMap != null){
            //Если карта создана создаю контроллер
            mapController = new MapController(googleMap);
        }
        Log.d(LOG_TAG, "MainActivity onCreate_END");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(LOG_TAG, "MainActivity onStart");

        //Обновляю текст на кнопке
        ButtonStartStopSETTEXT();

        //Если сервис уже запущен необходимо надо у него забрать маршрут.
        //Для этого ставлю задачу GET_ALL и запускаю bindService
        if(CheckServiceStatus()){
            currentTask = GET_ALL;
            bindService(new Intent(this, LocationService.class), sConn, 0);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(LOG_TAG, "MainActivity onResume");
        //Обновляю текст на кнопке
        ButtonStartStopSETTEXT();
    }

    @Override
    protected void onDestroy() {
        //Выключаю BroadcastReceiver
        unregisterReceiver(br);
        Log.d(LOG_TAG, "MainActivity onDestroy");
        super.onDestroy();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ///----КОНЕЦ БЛОКА ЖИЗНЕНОГО ЦИКЛА------////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    //Обработка нажатий
    @Override
    public void onClick(View v) {
        //Сейчас обрабатывается только кнопка btnStartStop. Тестовые кнопки удалены.
        switch (v.getId()) {
            case R.id.btnStartStop:
                Log.d(LOG_TAG, "MainActivity onClick.btnStartStop");
                if(CheckServiceStatus()){
                    //Стоплю сервис если запущен
                    stopService(new Intent(this, LocationService.class));
                    ButtonStartStopSETTEXT();//Обнавляю текст на кнопке
                }
                else {
                    //Подготовка к старту сервиса
                    //Очищаю InfoPanel
                    ClearInfoPanel();
                    //id нового маршрута который передам в интенте
                    int id;
                    //Запрашиваю у базы последний ID для передачи в сервис
                    Cursor cursor = getContentResolver().query(RunerDBProvider.MAX_ID_URI, null, null, null, null);

                    //Определяю номера столбцов по именам в выборке
                    int idColIndex = cursor.getColumnIndex("maxID");
                    //Вытаскиваю ID если ничего нет то пусть id =0
                    if (cursor.moveToFirst()) {
                        id = cursor.getInt(idColIndex)+1;
                        Log.d(LOG_TAG, "MainActivity onClick.btnStartStop Новый ID маршрута = " + id);
                    }
                    else{
                        Log.d(LOG_TAG, "MainActivity onClick.btnStartStop Новый ID маршрута = 1");
                        id = 1;
                    }
                    //Стартую сервис. Передаю через интент ID для нового маршрута(Way).
                    startService(new Intent(this, LocationService.class).putExtra("ID", id));
                    //Обновляю текст на кнопке
                    ButtonStartStopSETTEXT();
                }
                break;
        }
    }

    //Меняю текст на кнопке в зависимости от состояния сервиса
    private void ButtonStartStopSETTEXT(){
        if(CheckServiceStatus()){
            btnStartStopService.setText("STOP");
        }
        else {
            btnStartStopService.setText("START");
        }
    }

    //Проверка статуса сервиса
    private boolean CheckServiceStatus(){

        boolean b = false;

        ActivityManager am = (ActivityManager) this.getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> rs = am.getRunningServices(100);

        //Ищу в списке свой сервис
        for (int i = 0; i < rs.size(); i++) {
            ActivityManager.RunningServiceInfo rsi = rs.get(i);
            if (rsi.service.getClassName().equals("com.sandroid.freetracker.LocationService")){
                b = true;
            }
        }
        //Если не нашел то оставляю без изменений false
        return b;
    }

    //Создание карты
    private void createMapView(){
        try {
            if(googleMap == null){
                //Присваиваем нашей карте соответствующий фрагмент
                googleMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.mapView)).getMap();
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

    //Информационная панель
    private void UpdateInfoPanel(Way way){
        Fragment fragment = getFragmentManager().findFragmentById(R.id.way_info);

        //Если маршрут пустой сразу выходим.
        if(way == null){return;}

        ((TextView) fragment.getView().findViewById(R.id.tv_distance)).setText(way.GetDistance().toString() + " м");
        ((TextView) fragment.getView().findViewById(R.id.tv_duration)).setText(way.GetDuration());
        ((TextView) fragment.getView().findViewById(R.id.tv_avgspeed)).setText(way.GetAverageSpeed().toString() + " км/ч");
    }

    //Сброс значений на информационной панели
    private void ClearInfoPanel(){
        Fragment fragment = getFragmentManager().findFragmentById(R.id.way_info);

        ((TextView) fragment.getView().findViewById(R.id.tv_distance)).setText("0 км");
        ((TextView) fragment.getView().findViewById(R.id.tv_duration)).setText("0 мин");
        ((TextView) fragment.getView().findViewById(R.id.tv_avgspeed)).setText("0 км/ч");
    }

    //МЕНЮ//////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Беру пункты меню из xml
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        //Определяю отображение тестовой группы в меню. Зависит от переменной Test.
        menu.setGroupVisible(R.id.test_group, Test);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //Обработка нажатий пунктов меню
        switch(item.getItemId()){
            case R.id.menu_item_history:
                //Переход на активность с историей
                Log.d(LOG_TAG, "нажато MainActivity menu_item_history");
                Intent intent = new Intent(this, History.class);
                startActivity(intent);
                break;
            case R.id.menu_item_settings:
                //Тут будет переход на активность с настройками
                break;
            case R.id.menu_item_promotion:
                //Тут будет переход на активность с рекламой
                break;
            case R.id.menu_item_delalldata:
                //Удаляем все записи
                int delete_count = getContentResolver().delete(ALL_WAY_URI, null, null);
                Log.d(LOG_TAG, "Удалены все записи из БД.");
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
