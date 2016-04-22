package com.sandroid.freetracker;


import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

//Класс фрагмента Current_way_info
public class Current_way_info extends Fragment {

    //Конструктор
    public Current_way_info() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        //привязываю xml разметку к фрагменту
        return inflater.inflate(R.layout.fragment_current_way_info, container, false);
    }
}
