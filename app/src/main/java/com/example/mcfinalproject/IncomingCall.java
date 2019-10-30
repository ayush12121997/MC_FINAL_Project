package com.example.mcfinalproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class IncomingCall extends AppCompatActivity
{
    private String userID;
    private String callerName;
    private DatabaseReference mDatabase;
    private String otherID;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incoming_call);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        Intent intent = getIntent();
        callerName = intent.getStringExtra("CallerName");
        ((TextView) findViewById(R.id.incomingCallText)).setText("Incoming call from " + callerName);
        userID = intent.getStringExtra("UserID");
        otherID = "";
        runAnim();
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
}
