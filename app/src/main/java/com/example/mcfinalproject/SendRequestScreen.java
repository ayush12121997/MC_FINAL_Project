package com.example.mcfinalproject;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class SendRequestScreen extends AppCompatActivity implements AdapterView.OnItemClickListener{

    ArrayList<String> Send_Friends_Requests;
    ListView Send_Friends_Requests_List;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_request);
        //FILL FRIENDS_REQUESTS_ FILL IT WITH FRIENDS REQUESTS!
        Send_Friends_Requests = new ArrayList<String>();
        Send_Friends_Requests.add("CobberMyRegister");
        Send_Friends_Requests.add("CobberYourRegister");
        Send_Friends_Requests.add("XKCDXKCDXKCDXKCDXKCDXKCDXKCDXKCDXKCD");
        Send_Friends_Requests_List = (ListView) findViewById(R.id.holder_list2);

        ISR isr=new ISR(Send_Friends_Requests,getApplicationContext());
        Send_Friends_Requests_List.setAdapter(isr);


    }

    @Override
    public void onItemClick(AdapterView<?> L, View v, int position, long id) {
        Log.i("11x",Send_Friends_Requests.get(position)+" This is bs!");
        Log.i("x11","I mesg!");
        Send_Friends_Requests.remove(position);
        ISR isr=new ISR(Send_Friends_Requests,getApplicationContext());
        Send_Friends_Requests_List.setAdapter(isr);
    }
}
