package com.example.mcfinalproject;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ListView;

import java.util.ArrayList;

public class AcceptRequestScreen extends AppCompatActivity {

    ArrayList<String> Friends_Requests;
    ListView Friends_Requests_List;
    ArrayList<String> Send_Friends_Requests;
    ListView Send_Friends_Requests_List;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accept_request);
        //FILL FRIENDS_REQUESTS_ FILL IT WITH FRIENDS REQUESTS!
        Friends_Requests = new ArrayList<String>();
        Friends_Requests.add("CobberMyRegister");
        Friends_Requests.add("CobberYourRegister");
        Friends_Requests_List = (ListView) findViewById(R.id.holder_list1);

        IFR ifr=new IFR(Friends_Requests,getApplicationContext());
        Friends_Requests_List.setAdapter(ifr);

    }

}
