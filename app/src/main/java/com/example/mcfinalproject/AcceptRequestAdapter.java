package com.example.mcfinalproject;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class AcceptRequestAdapter extends BaseAdapter implements ListAdapter
{
    private String userID;
    private Context CTX;
    private ArrayList<String> List;

    public AcceptRequestAdapter(ArrayList<String> List, String usr, Context context)
    {
        super();
        this.CTX = context;
        this.List = List;
        this.userID = usr;
    }

    @Override
    public int getCount()
    {
        return this.List.size();
    }

    @Override
    public Object getItem(int i)
    {
        return this.List.get(i);
    }

    @Override
    public long getItemId(int i)
    {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup)
    {
        if(view == null)
        {
            LayoutInflater Infl = (LayoutInflater) this.CTX.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = Infl.inflate(R.layout.item_friend_request_accept_or_decline, null);
        }
        int currIndx = i;
        TextView Name = (TextView) view.findViewById(R.id.sendersname1);
        Name.setText(this.List.get(i));
        Button accept = (Button) view.findViewById(R.id.accept_request);
        Button decline = (Button) view.findViewById(R.id.decline_request);
        accept.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                AcceptRequestScreen ars = new AcceptRequestScreen();
                String acceptFrom = List.get(currIndx);
                ars.acceptRequest(acceptFrom, userID);
                List.remove(currIndx);
                notifyDataSetChanged();
            }
        });
        decline.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                AcceptRequestScreen ars = new AcceptRequestScreen();
                String rejectFrom = List.get(currIndx);
                ars.rejectRequest(rejectFrom, userID);
                List.remove(currIndx);
                notifyDataSetChanged();

            }
        });
        return view;
    }
}
