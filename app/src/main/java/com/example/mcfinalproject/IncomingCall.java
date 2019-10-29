package com.example.mcfinalproject;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;

public class IncomingCall extends AppCompatActivity {

    String user;
    String userID;
    String Call_To;
    String Call_From;
    String Proj_ID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incoming_call);

        Intent intent = getIntent();
        Call_From = intent.getStringExtra("CallFrom");
        Call_To = intent.getStringExtra("CallTo");
        Proj_ID = intent.getStringExtra("Proj_ID");
        user = intent.getStringExtra("User");
        userID = intent.getStringExtra("UserID");



        Button Accept_Button=findViewById(R.id.button3);
        Button Decline_Button=findViewById(R.id.button4);
        AlphaAnimation AN1 = new AlphaAnimation(1f, 0.1f);
        AN1.setDuration(550);
        AN1.setStartOffset(250);
        AN1.setRepeatCount(Animation.INFINITE);
        Accept_Button.startAnimation(AN1);

//        AlphaAnimation AN3 = new AlphaAnimation(0.1f, 1f);
//        AN3.setDuration(550);
//        AN3.setRepeatCount(Animation.INFINITE);
//        Accept_Button.startAnimation(AN3);

        AlphaAnimation AN2=new AlphaAnimation(0.1f,1f);
        AN2.setDuration(550);
        AN2.setStartOffset(250);
        AN2.setRepeatCount(Animation.INFINITE);
        Decline_Button.startAnimation(AN2);


        //animation1.setFillAfter(true);
        //view.setVisibility(View.VISIBLE);
        //view.startAnimation(animation1);
    }


    public void ACCEPT_KALL(View view){
        Intent AcceptCallIntent=new Intent(getApplicationContext(),MainActivity.class);
        AcceptCallIntent.setClass(this, MainActivity.class);
        AcceptCallIntent.putExtra("CallTo",  Call_To);
        AcceptCallIntent.putExtra("CallFrom",  Call_From);
        AcceptCallIntent.putExtra("Proj_ID",Proj_ID);
        AcceptCallIntent.putExtra("User", user);
        AcceptCallIntent.putExtra("UserID", userID);
        startActivity(AcceptCallIntent);
    }

    public void DECLINE_KALL(View view){
        Intent DeclineCallIntent=new Intent(getApplicationContext(),HomeScreen.class);
        startActivity(DeclineCallIntent);
    }


}
