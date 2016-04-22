package com.sandroid.freetracker;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


public class HistoryViewAdapter extends ArrayAdapter<Way> {

    public HistoryViewAdapter(Context context, ArrayList<Way> ways) {
        super(context, R.layout.history_item, ways);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        //Вытаскиваю маршрут из списка
        Way way = getItem(position);

        //Беру начальную дату из базы данных
        Date date = new Date(way.GetStartTime());
        //Задаю формат отображения
        SimpleDateFormat format1 = new SimpleDateFormat("dd.MM.yyyy hh:mm");

        //Инициирую View
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.history_item, null);
        }

        //Заполняю элементы history_item
        //Время старта
        ((TextView) convertView.findViewById(R.id.date)).setText(format1.format(date));

        //Расстояние
        ((TextView) convertView.findViewById(R.id.tv_distance)).setText(way.GetDistance().toString() + " м");

        //Длительность
        ((TextView) convertView.findViewById(R.id.tv_duration)).setText(way.GetDuration());

        //Скорость
        ((TextView) convertView.findViewById(R.id.tv_avgspeed)).setText(way.GetAverageSpeed().toString() + " км/ч");

        //Возвращаю готовую View
        return convertView;
    }
}
