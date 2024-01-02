package com.example.eduhub;

import android.app.Activity;
import android.graphics.Color;
import android.media.Image;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {
    private List<MediaObject> mediaObjectList = new ArrayList<>();
    private DemoAdapter demoAdapter;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_shorts);

        setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,false);
        getWindow().setStatusBarColor(Color.TRANSPARENT);

        recyclerView = (RecyclerView)findViewById(R.id.recyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        ///////////////////////////////////////////////
        SnapHelper mSnapHelper = new PagerSnapHelper();
        mSnapHelper.attachToRecyclerView(recyclerView);
        ///////////////////////////////////////////////

        mediaObjectList.add(new MediaObject("","","","","","","","","",""));
        mediaObjectList.add(new MediaObject("","","","","","","","","",""));
        mediaObjectList.add(new MediaObject("","","","","","","","","",""));
        mediaObjectList.add(new MediaObject("","","","","","","","","",""));
        mediaObjectList.add(new MediaObject("","","","","","","","","",""));
        mediaObjectList.add(new MediaObject("","","","","","","","","",""));
        mediaObjectList.add(new MediaObject("","","","","","","","","",""));
        mediaObjectList.add(new MediaObject("","","","","","","","","",""));
        mediaObjectList.add(new MediaObject("","","","","","","","","",""));
        mediaObjectList.add(new MediaObject("","","","","","","","","",""));
        mediaObjectList.add(new MediaObject("","","","","","","","","",""));
        mediaObjectList.add(new MediaObject("","","","","","","","","",""));
        mediaObjectList.add(new MediaObject("","","","","","","","","",""));
        mediaObjectList.add(new MediaObject("","","","","","","","","",""));

        demoAdapter = new DemoAdapter(mediaObjectList,getApplicationContext());
        recyclerView.setAdapter(demoAdapter);
        demoAdapter.notifyDataSetChanged();
    }

    public static void setWindowFlag(Activity activity, final int bits, boolean on){

    }

}
