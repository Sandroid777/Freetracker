package com.sandroid.freetracker;

public class PointLocation{

    private Double longitude;   // Долгота
    private Double latitude;    // Широта
    private float accuracy;     // Точность
    private float bearing;      // Азимут
    private Double altitude;    // Высота
    private float speed;        // Скорость
    private Long time;          // Временная метка

    //Конструктор
    PointLocation(Double lon, Double lat,  float acc, float bea, Double alt, float sp, Long t){
        longitude = lon;
        latitude = lat;
        accuracy = acc;
        bearing = bea;
        altitude = alt;
        speed = sp;
        time = t;
    }

    //ГЕТТЕРЫ
    public Double GetPointLongitude(){
        return longitude;
    }
    public Double GetPointLatitude(){
        return latitude;
    }
    public float GetPointAccuracy(){
        return accuracy;
    }
    public float GetPointBearing(){
        return bearing;
    }
    public Double GetPointAltitude(){
        return altitude;
    }
    public float GetPointSpeed(){
        return speed;
    }
    public Long GetPointTime(){
        return time;
    }
}
