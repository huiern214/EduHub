//package com.example.eduhub;
//
//import android.os.Bundle;
//
//import androidx.fragment.app.Fragment;
//
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//
///**
// * A simple {@link Fragment} subclass.
// * Use the {@link user_shortsFragment#newInstance} factory method to
// * create an instance of this fragment.
// */
//public class user_shortsFragment extends Fragment  {
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//        // Inflate the layout for this fragment
//        return inflater.inflate(R.layout.fragment_shorts, container, false);
//    }
//
//
//
//}

package com.example.eduhub;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link user_shortsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class user_shortsFragment extends Fragment {
    private List<MediaObject> mediaObjectList;
    private DemoAdapter demoAdapter;

    public user_shortsFragment() {
        // Required empty public constructor
    }

    public static user_shortsFragment newInstance() {
        return new user_shortsFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_shorts, container, false);

        RecyclerView recyclerView = rootView.findViewById(R.id.recyclerView); // Replace with your actual RecyclerView ID
        demoAdapter = new DemoAdapter(mediaObjectList, requireContext()); // Use requireContext() to get the Fragment's context
        recyclerView.setAdapter(demoAdapter);

        return rootView;
    }
}
