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

public class ISR extends BaseAdapter implements ListAdapter{
    private Context CTX;
    private ArrayList<String> List;
    private int dex;

    public ISR(ArrayList<String> List,Context context) {
        super();
        this.CTX=context;
        this.List=List;
    }

    @Override
    public int getCount() {
        return this.List.size();
    }

    @Override
    public Object getItem(int i) {
        return this.List.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        boolean flag=false;
        LayoutInflater Infl=(LayoutInflater) this.CTX.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view=Infl.inflate(R.layout.item_send_friend_request,null);
        TextView Name=(TextView) view.findViewById(R.id.sendersname2);
        Name.setText(this.List.get(i));
        dex=i;
        int dex2=i;
        Button send_request=(Button) view.findViewById(R.id.send_request);

        send_request.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ////// SEND FRIEND REQUEST
                List.remove(dex2);
                notifyDataSetChanged();

            }
        });
        return view;
    }
}
