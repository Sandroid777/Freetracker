package com.sandroid.freetracker;

        import android.content.ContentProvider;
        import android.content.ContentValues;
        import android.content.Context;
        import android.content.UriMatcher;
        import android.database.Cursor;
        import android.database.SQLException;
        import android.database.sqlite.SQLiteDatabase;
        import android.database.sqlite.SQLiteOpenHelper;
        import android.net.Uri;
        import android.support.annotation.NonNull;
        import android.text.TextUtils;
        import android.util.Log;

public class RunerDBProvider extends ContentProvider {

    final String LOG_TAG = "myLogs";

    //Константы
    static final int DB_VERSION = 1;
    static final String DB_NAME = "Test01";

    //Часть Uri <authority>
    static final String AUTHORITY = "com.sandroid.providers.RunTracker";
    //Часть Uri <path>
    static final String WAY_PATH = "Way";

    //Общий Uri
    public static final Uri ALL_WAY_URI = Uri.parse("content://" + AUTHORITY + "/" + WAY_PATH);
    //Общий Uri + MAX_ID
    public static final Uri MAX_ID_URI = Uri.parse("content://" + AUTHORITY + "/" + WAY_PATH + "/MAX_ID");


    //Константы для UriMatcher
    static final int URI_ALL_WAY = 1;   // Общий Uri
    static final int URI_WAY_ID = 2;    // Uri с указанным ID
    static final int URI_MAX_ID = 3;    // Uri с max ID

    private static final UriMatcher uriMatcher;
    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(AUTHORITY, WAY_PATH, URI_ALL_WAY);            //общий Uri
        uriMatcher.addURI(AUTHORITY, WAY_PATH + "/#", URI_WAY_ID);      //Uri с ID
        uriMatcher.addURI(AUTHORITY, WAY_PATH + "/MAX_ID", URI_MAX_ID); //Uri для получения max ID
    }

    //Объявляю переменную dbHelper через которую буду подключаться к базе.
    DBHalper dbHelper;
    //Объявляю переменную базы данных.
    SQLiteDatabase db;

    @Override
    public boolean onCreate() {
        Log.d(LOG_TAG, "RunerDBProvider: onCreate");

        //Инициирую dbHelper, он создаст базу если её нет.
        dbHelper = new DBHalper(getContext(), DB_NAME, null, DB_VERSION);
        return true;
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        //запросы к базе данных
        Log.d(LOG_TAG, "query, " + uri.toString());

        db = dbHelper.getWritableDatabase();

        //Формирую запросы в зависимости от полученного URY
        switch(uriMatcher.match(uri)){
            case URI_ALL_WAY:
                //Если запрашивают все данные, тогда ничего настраивать не надо.
                break;
            case URI_WAY_ID:
                //достаю из ури id
                String id = uri.getLastPathSegment();
                // добавляем ID к условию выборки
                if (TextUtils.isEmpty(selection)) {
                    selection = "way_id" + " = " + id;
                } else {
                    selection = selection + " AND " + "way_id" + " = " + id;
                }
                break;
            //Запрашываю последний id. Нужно для формирования новых маршрутов.
            case URI_MAX_ID:
                projection = new String[]{"MAX(way_id) as maxID"};
                break;
            default:
                throw new IllegalArgumentException("Wrong URI: " + uri);
        }
        //Возвращаю выборку
        return db.query("tWay", projection, selection, selectionArgs, null, null, sortOrder);
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        //В проекте для записи используется только bulkInsert. Метод оставил, может ещё пригодится.
        Log.d(LOG_TAG, "insert, " + uri.toString());

        db = dbHelper.getWritableDatabase();
        long rowID = db.insert("tWay", null, values);

        return null;
    }

    @Override
    //Пакетная вставка данных
    public int bulkInsert(Uri uri, ContentValues[] values){
        Log.d(LOG_TAG, "bulkInsert, " + uri.toString());
        int numInserted = 0;

        db = dbHelper.getWritableDatabase();
        //Открываю транзакцию
        db.beginTransaction();

        try {
            //Бегу по массиву данных
            for (ContentValues cv : values) {
                //Вставляю строку
                long newID = db.insertOrThrow("tWay", null, cv);

                //Выдаю ошибку если вернулся 0
                if (newID <= 0) {
                    throw new SQLException("Failed to insert row into " + uri);
                }
            }
            db.setTransactionSuccessful();
            numInserted = values.length;
        } finally {
            //Если ошибка закрываю транзакцию
            db.endTransaction();
        }
        Log.d(LOG_TAG, "bulkInsert, " + numInserted + " inserted");
        //Возвращаю количество вставленных строк
        return numInserted;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        Log.d(LOG_TAG, "delete, " + uri.toString());
        db = dbHelper.getWritableDatabase();
        int cnt = db.delete("tWay", selection, selectionArgs);
        //Возвращаю количество удаленных строк
        return cnt;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        //Изменение данных пока не используется
        Log.d(LOG_TAG, "update, " + uri.toString());
        return 0;
    }

    //Переопределяю класс DBHalper. Этот класс будет использовать ContentProvider.
    public class DBHalper extends SQLiteOpenHelper {

        public DBHalper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            Log.d(LOG_TAG, "--- onCreate database ---");
            //Скрипт создания базы данных
            db.execSQL("create table tWay (id integer primary key autoincrement, way_id integer, longitude REAL, latitude REAL, accuracy REAL, bearing  REAL, altitude REAL, speed REAL, time text);");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            //Метод через который будет обновляться структура базы данных.
        }
    }

}

