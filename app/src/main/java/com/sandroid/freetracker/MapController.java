package com.sandroid.freetracker;


import android.graphics.Color;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;

public class MapController {

    private LatLng curentPointLatLng;   // Текущая точка координат
    private LatLng previousPointLatLng; // Предыдущая точка координат

    private GoogleMap googleMap;        // Карта

    //КОНСТРУКТОР
    public MapController(GoogleMap Map){
        googleMap = Map;
        curentPointLatLng = null;
        previousPointLatLng = null;
    }

    //Отображение новой точки маршрута
    public void addMarker(PointLocation p, boolean test){

        //Ставлю текущую точку равной полученной
        curentPointLatLng = new LatLng(p.GetPointLatitude(), p.GetPointLongitude());

        //title(описание) на маркере
        String titleString = null;

        //Если тестовый режим то формирую расширеное описание на маркере
        if(test) {
            titleString = "A=" + p.GetPointAccuracy() + " B=" + p.GetPointBearing() + " S=" + p.GetPointSpeed();
        }

        //Если получена первая точка то previousPointLatLng будет нулевой
        if(previousPointLatLng == null){
            //Инициируем стартовую точку
            previousPointLatLng = new LatLng(p.GetPointLatitude(), p.GetPointLongitude());
        }
        //Настраиваем маркер
        MarkerOptions marker  = new MarkerOptions()
                .anchor(0.5f, 0.5f)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.g_square_marker))
                .position(curentPointLatLng)
                .title(titleString)
                .draggable(true);
        //Если карта существует тогда перемещаем камеру и ставим точку
        if(null != googleMap){
            //Перемещаю камеру
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(curentPointLatLng, 13));
            //Ставлю маркер
            googleMap.addMarker(marker);
            }

            //Рисую линию
            googleMap.addPolyline((new PolylineOptions())
                    .add(previousPointLatLng, curentPointLatLng).width(6).color(Color.BLACK)
                    .visible(true));
            //Текущая точка становится предыдущей
            previousPointLatLng = curentPointLatLng;
    }

    //Отображение полного маршрута
    public void addAllWay(Way way, boolean test) {
        //Проверяем не пустой ли маршрут пришел
        if (way != null) {
            //Обнуляю previousPointLatLng вдруг остался мусор.
            previousPointLatLng = null;

            //Беру из Way массив из точек(PointLocation)
            ArrayList<PointLocation> arraypoint = way.GetAllLocations();
            //Проверяю существует ли карта
            if (null != googleMap) {
                //Удаляю с карты все старые данные
                googleMap.clear();

                //Добавляю в цикле точки маршрута
                for (PointLocation p : arraypoint) {
                    //Если пердыдущая точка нулевая значит это первая
                    if(previousPointLatLng == null) {
                        previousPointLatLng = new LatLng(p.GetPointLatitude(), p.GetPointLongitude());
                        //Перемещаю на неё камеру
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(previousPointLatLng, 13));
                    }
                    //Беру из текущей точки координаты и присваиваю к curentPointLatLng
                    curentPointLatLng= new LatLng(p.GetPointLatitude(), p.GetPointLongitude());

                    //Добавляю маркер
                    googleMap.addMarker(new MarkerOptions()
                                    .anchor(0.5f, 0.5f)
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.g_square_marker))
                                    .position(curentPointLatLng)
                                    .title("A=" + p.GetPointAccuracy() + " B=" + p.GetPointBearing() + " S=" + p.GetPointSpeed())
                                    .draggable(true)
                    );
                    //Рисую линию
                    googleMap.addPolyline((new PolylineOptions())
                            .add(previousPointLatLng, curentPointLatLng).width(6).color(Color.BLACK)
                            .visible(true));
                    //Текущая точка становится предыдущей
                    previousPointLatLng = curentPointLatLng;
                }
                //END Цикл
            }
        }
    }
}
