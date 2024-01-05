package com.example.eduhub;

import android.annotation.SuppressLint;
import android.widget.Filter;

import com.example.eduhub.adapter.user_AdapterNote;
import com.example.eduhub.model.Notes;

import java.util.ArrayList;

public class user_filterNote extends Filter {
    //ArrayList in which we want to search
    ArrayList<Notes> filterList;
    //Adapter in which filter needs to be implemented
    user_AdapterNote adapterNote;

    //Constructor
    public user_filterNote(ArrayList<Notes> filterList, user_AdapterNote adapterNote){
        this.filterList = filterList;
        this.adapterNote = adapterNote;
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        FilterResults results = new FilterResults();

        //Value should not be null
        if (constraint != null && constraint.length()>0){
            //Change to upper case, or lower case to avoid case sensitivity
            constraint = constraint.toString().toUpperCase();
            ArrayList<Notes> filteredModels = new ArrayList<>();

            for (int i=0; i<filterList.size(); i++){
                //Validate
                if (filterList.get(i).getResource_name().toUpperCase().contains(constraint)){
                    //Add to filtered list
                    filteredModels.add(filterList.get(i));
                }
            }
            results.count = filteredModels.size();
            results.values = filteredModels;
        } else{
            results.count = filterList.size();
            results.values = filterList;
        }
        return results;
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {
        //Clear the existing items in the adapter's noteArrayList
        //adapterNote.noteArrayList.clear()
        //Add filtered items to be noteArrayList
        adapterNote.noteList = (ArrayList<Notes>) results.values;
        //Notify the adapter that the data has changed
        adapterNote.notifyDataSetChanged();
    }
}
