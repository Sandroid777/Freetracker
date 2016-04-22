package com.sandroid.freetracker;

import android.location.Location;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.TimeZone;


public class Way {

    //Идентификатор маршрута
    private int WayID;

    //Массив в котором будут храниться точки маршрута.
    private ArrayList <PointLocation> WayTrackLocations;

    //Конструктор.
    Way(int id){
        //Присваиваем ID маршрута
        WayID = id;
        //Создаём массив точек маршрута
        WayTrackLocations = new ArrayList <PointLocation>();
    }

    //Отдаю ID маршрута
    public int GetWayID(){
        return WayID;
    }

    //Отдаём все точки маршрута
    public ArrayList <PointLocation> GetAllLocations(){
        return WayTrackLocations;
    }

    //Проверка на то есть ли точки в маршруте
    public boolean isEmpty(){
        return WayTrackLocations.isEmpty();
    }

    //Отдаю последнюю точку маршрута
    public PointLocation GetLastLocations(){
        int index = WayTrackLocations.size()-1;
        return  WayTrackLocations.get(index);
    }

    //Добавляю новую точкув массив
    public void AddLocationToWay(PointLocation pointloc){
        WayTrackLocations.add(pointloc);
    }

    //Возвращает время начальной точки маршрута
    public Long GetStartTime(){
        //Если маршрута ещё нет то возвращаем 0
        if(WayTrackLocations.isEmpty()){
            return Long.valueOf("0");
        }
        else {
            return  WayTrackLocations.get(0).GetPointTime();
        }
    }

    //Возвращаю продолжительность маршрута (дата последней точки - дата первой точка).
    public String GetDuration(){
        Long data = Long.valueOf("0");
        SimpleDateFormat DurationFormat = new SimpleDateFormat("HH:mm:ss");
        DurationFormat.setTimeZone(TimeZone.getTimeZone("GMT"));//задаю формат времени ГМТ

        //Если маршрут пустой то верну 0
        if(WayTrackLocations.isEmpty()){
            return DurationFormat.format(data);
        }
        else {
            data = WayTrackLocations.get(WayTrackLocations.size() - 1).GetPointTime() - WayTrackLocations.get(0).GetPointTime();
            return DurationFormat.format(data);
        }
    }

    //Возвращаю среднюю скорость маршрута
    public BigDecimal GetAverageSpeed(){
        //Переменная для расчёта скорости
        Float AvgSpeed = Float.valueOf(0);

        if(WayTrackLocations.isEmpty()) {
           //Если маршрут пустой привожу к BigDecimal и возвращаю.
           return new BigDecimal(AvgSpeed);
        }
        else {
            //Перебираю массив и плюсую скорости
            for (PointLocation p : WayTrackLocations) {
                AvgSpeed += p.GetPointSpeed();
            }
            //Делю на количество точек, получаю среднюю скорость.
            AvgSpeed = AvgSpeed / WayTrackLocations.size();

            //Привожу к BigDecimal и округляю до одного знака.
            return new BigDecimal(AvgSpeed).setScale(1, BigDecimal.ROUND_HALF_UP);
        }
    }

    //Вычисляю дистанцию маршрута
    public BigDecimal GetDistance() {

        Float Distance = Float.valueOf(0);

        if(WayTrackLocations.isEmpty()) {
            //Если пустой маршрут возвращаю 0
            return new BigDecimal(Distance);
        }
        else {
            //В цикле я бегу по точкам и вычисляю расстояние между ними.
            //Точки я беру равные итератору i и i+1.
            //Поэтому в условии цикла i<size-1 чтобы не вылететь за пределы массива.
            for (int i = 0; i<WayTrackLocations.size()-1; i++) {
                //Две переменные типа Location чтобы использовать встроенный метод distanceTo.
                Location loc1 = new Location("");
                Location loc2 = new Location("");
                //Присваиваю широту и долготу.
                loc1.setLatitude(WayTrackLocations.get(i).GetPointLatitude());
                loc1.setLongitude(WayTrackLocations.get(i).GetPointLongitude());
                loc2.setLatitude(WayTrackLocations.get(i+1).GetPointLatitude());
                loc2.setLongitude(WayTrackLocations.get(i+1).GetPointLongitude());

                //Вычисляю расстояние между точками
                Distance += loc1.distanceTo(loc2);
                }
            //Округляю до целых метров
            return new BigDecimal(Distance).setScale(0, BigDecimal.ROUND_HALF_UP);
        }
    }
}


