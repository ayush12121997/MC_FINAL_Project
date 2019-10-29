package com.example.mcfinalproject;

import android.os.Bundle;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class SendRequestScreen extends AppCompatActivity {

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
        Send_Friends_Requests_List = (ListView) findViewById(R.id.holder_list2);

        ISR isr=new ISR(Send_Friends_Requests,getApplicationContext());
        Send_Friends_Requests_List.setAdapter(isr);

    }

}
