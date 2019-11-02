package com.example.mcfinalproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.DragEvent;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class IncomingCall extends AppCompatActivity implements View.OnTouchListener,View.OnDragListener,GestureDetector.OnGestureListener
{
    private String userID;
    private String callerName;
    private DatabaseReference mDatabase;
    private String otherID;
    private VideoView blackhole;
    private GestureDetector GD;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incoming_call);
        blackhole=findViewById(R.id.blackhole);
        blackhole.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                blackhole.start();
            }
        });
        blackhole.setVideoPath("android.resource://"+getPackageName()+"/"+R.raw.videox);
        blackhole.start();
        blackhole.setOnTouchListener(this);

        GD=new GestureDetector(this,this);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        Intent intent = getIntent();
        callerName = intent.getStringExtra("CallerName");
        ((TextView) findViewById(R.id.incomingCallText)).setText(callerName);
        userID = intent.getStringExtra("UserID");
        otherID = "";
        runAnim();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Recieve_User").child(userID);
        mDatabase.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                String check = dataSnapshot.getValue().toString();
                if(check.equals("None"))
                {
                    Toast.makeText(getApplicationContext(), "THE CALL WAS DISCONNECTED", Toast.LENGTH_LONG).show();
                    rejectCall();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });
    }

    public void acceptCall(View view)
    {
        mDatabase = mDatabase.getRoot();
        mDatabase = mDatabase.child("Recieve_User").child(userID);
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                String caller = dataSnapshot.getValue().toString();
                if(!caller.equals("None"))
                {
                    otherID = caller;
                }
                mDatabase = mDatabase.getRoot().child("Users").child("Num_Projects");
                mDatabase.addListenerForSingleValueEvent(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                    {
                        String number = dataSnapshot.getValue().toString();
                        mDatabase = mDatabase.getRoot().child("Connections");
                        mDatabase.addListenerForSingleValueEvent(new ValueEventListener()
                        {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                            {
                                for(int i = 1; i <= Integer.parseInt(number); i++)
                                {
                                    String check = dataSnapshot.child("Proj_" + String.valueOf(i)).child("User_1").getValue().toString();
                                    if(check.equals(caller))
                                    {
                                        mDatabase.child("Proj_" + String.valueOf(i)).child("User_2").setValue(userID);
                                        connectCall(i);
                                        break;
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError)
                            {
                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError)
                    {
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {
            }
        });
    }

    public void declineCall(View view)
    {
        mDatabase = mDatabase.getRoot().child("Recieve_User").child(userID);
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                String callFrom = dataSnapshot.getValue().toString();
                if(!callFrom.equals("None"))
                {
                    mDatabase.getRoot().child("Call_User").child(callFrom).setValue("None");
                    mDatabase = mDatabase.getRoot().child("Users").child("Num_Projects");
                    mDatabase.addListenerForSingleValueEvent(new ValueEventListener()
                    {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                        {
                            String number = dataSnapshot.getValue().toString();
                            mDatabase = mDatabase.getRoot().child("Connections");
                            mDatabase.addListenerForSingleValueEvent(new ValueEventListener()
                            {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                                {
                                    for(int i = 1; i <= Integer.parseInt(number); i++)
                                    {
                                        String check = dataSnapshot.child("Proj_" + String.valueOf(i)).child("User_1").getValue().toString();
                                        if(check.equals(callFrom))
                                        {
                                            mDatabase.child("Proj_" + String.valueOf(i)).child("User_1").setValue("None");
                                            mDatabase.child("Proj_" + String.valueOf(i)).child("User_2").setValue("None");
                                            mDatabase.getRoot().child("Recieve_User").child(userID).setValue("None");
                                            mDatabase.getRoot().child("Call_User").child(callFrom).setValue("None");
                                            rejectCall();
                                            break;
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError)
                                {
                                }
                            });
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError)
                        {
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {
            }
        });
    }

    public void runAnim()
    {
        Button Accept_Button = findViewById(R.id.button3);
        Button Decline_Button = findViewById(R.id.button4);
        AlphaAnimation AN1 = new AlphaAnimation(1f, 0.1f);
        AN1.setDuration(550);
        AN1.setStartOffset(250);
        AN1.setRepeatCount(Animation.INFINITE);
        Accept_Button.startAnimation(AN1);

        AlphaAnimation AN2 = new AlphaAnimation(0.1f, 1f);
        AN2.setDuration(550);
        AN2.setStartOffset(250);
        AN2.setRepeatCount(Animation.INFINITE);
        Decline_Button.startAnimation(AN2);
    }

    public void connectCall(int i)
    {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.putExtra("CallTo", userID);
        intent.putExtra("CallFrom", otherID);
        intent.putExtra("Proj_ID", "Proj_" + String.valueOf(i));
        intent.putExtra("UserID", userID);
        startActivity(intent);
        finish();
    }

    public void rejectCall()
    {
        Intent intent = new Intent(getApplicationContext(), HomeScreen.class);
        intent.putExtra("UserID", userID);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {

        if(view.getId()==R.id.blackhole){
            GD.onTouchEvent(motionEvent);
            return true;
        }
        return true;
    }

    @Override
    public boolean onDrag(View view, DragEvent dragEvent) {
        return true;
    }

    @Override
    public boolean onDown(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent motionEvent) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent motionEvent) {
            return false;
    }

    @Override
    public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
            return false;
    }

    @Override
    public void onLongPress(MotionEvent motionEvent) {

    }

    @Override
    public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
        if(v>30){
            Button X=findViewById(R.id.button3);
            X.performClick();
        }
        else if(v<-30){
            Button X=findViewById(R.id.button4);
            X.performClick();
        }
        return true;
    }
}