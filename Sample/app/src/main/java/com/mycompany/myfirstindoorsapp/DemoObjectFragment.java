package com.mycompany.myfirstindoorsapp;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.customlbs.shared.Coordinate;
import com.customlbs.surface.library.IndoorsSurfaceFragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DemoObjectFragment extends Fragment {
    private IndoorsSurfaceFragment indoorsSurfaceFragment = null;
    PagedActivity pagedActivity=null;
    private static Map<String, Coordinate> zoneCoordMap=new HashMap<>();;
    ListView logList;
    ArrayAdapter<String> adapter;
    List<String> listItems;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // The last two arguments ensure LayoutParams are inflated
        // properly.
        View rootView = inflater.inflate(
                R.layout.fragment_collection_object, container, false);
        logList = (ListView) rootView.findViewById(R.id.log_list);

        listItems = new ArrayList<>();

        adapter = new ArrayAdapter<>(getContext(),
                R.layout.entry_list, R.id.entry,
                listItems);
        logList.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        logList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {
                String selectedSweet = logList.getItemAtPosition(position).toString();


                TextView textView = (TextView) view.findViewById(R.id.entry);
                String text = textView.getText().toString();

                if (pagedActivity!=null && indoorsSurfaceFragment!=null) {
                    indoorsSurfaceFragment.routeTo(zoneCoordMap.get(text), true);
                    pagedActivity.NonSwipeableViewPager.setCurrentItem(0);
                    Log.d("viewview", text);
                }
            }
        });



        //touchListener(rootView);

        return rootView;
    }


    public void addItem(PagedActivity pa, IndoorsSurfaceFragment i, String zoneName, Coordinate c) {
        pagedActivity=pa;
        indoorsSurfaceFragment=i;
        zoneCoordMap.put(zoneName, c);
        listItems.add(zoneName);
        adapter.notifyDataSetChanged();
    }

    private void touchListener(View view) {
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getActionMasked() == MotionEvent.ACTION_DOWN){
                    Toast.makeText(getActivity(), "you just touch the screen :-)", Toast.LENGTH_SHORT).show();
                }
                return true;
            }
        });
    }

}