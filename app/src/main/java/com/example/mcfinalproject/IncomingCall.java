package com.example.mcfinalproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
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

public class IncomingCall extends AppCompatActivity implements View.OnTouchListener, View.OnDragListener, GestureDetector.OnGestureListener
{
    private String userID;
    private String callerName;
    private DatabaseReference mDatabase;
    private String otherID;
    private VideoView blackhole;
    private GestureDetector GD;
    private Vibrator vibrator;
    private boolean vibrate;
    private boolean Receiving;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incoming_call);
        Intent intent = getIntent();
        callerName = intent.getStringExtra("CallerName");
        userID = intent.getStringExtra("UserID");
        ((TextView) findViewById(R.id.incomingCallText)).setText(callerName);
        otherID = "";
        Receiving = true;
        vibrate = true;
        blackhole = findViewById(R.id.blackhole);
        GD = new GestureDetector(this, this);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        blackhole.setVideoPath("android.resource://" + getPackageName() + "/" + R.raw.videox);
        blackhole.setOnCompletionListener(new MediaPlayer.OnCompletionListener()
        {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer)
            {
                blackhole.start();
            }
        });
        blackhole.start();
        blackhole.setOnTouchListener(this);
        Log.i("LastCheck - IC - Receiving initially", String.valueOf(Receiving));
        Log.i("LastCheck - IC - User ID initially", userID);
        Log.i("LastCheck - IC - Caller Name initially", callerName);
        runAnim();

        final Handler handler = new Handler();
        Runnable runnable = new Runnable()
        {
            @Override
            public void run()
            {
                Log.i("LastCheck - IC - Receiving in runnable", String.valueOf(Receiving));
                Log.i("LastCheck - IC - User ID in runnable", userID);
                if(Receiving)
                {
                    mDatabase = FirebaseDatabase.getInstance().getReference().child("Recieve_User").child(userID);
                    mDatabase.addListenerForSingleValueEvent(new ValueEventListener()
                    {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                        {
                            String check = dataSnapshot.getValue().toString();
                            Log.i("LastCheck - IC - Receiving call from in runnable", check);
                            if(check.equals("None"))
                            {
                                Receiving = false;
                                vibrate = false;
                                Toast.makeText(getApplicationContext(), "THE CALL WAS DISCONNECTED", Toast.LENGTH_SHORT).show();
                                rejectCall();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError)
                        {

                        }
                    });
                    handler.postDelayed(this, 1500);
                }
                else
                {
                    handler.removeCallbacks(this);
                }
            }
        };
        handler.postDelayed(runnable, 1500);

//        mDatabase = FirebaseDatabase.getInstance().getReference().child("Recieve_User").child(userID);
//        mDatabase.addValueEventListener(new ValueEventListener()
//        {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
//            {
//                String check = dataSnapshot.getValue().toString();
//                if(check.equals("None"))
//                {
//                    Toast.makeText(getApplicationContext(), "THE CALL WAS DISCONNECTED", Toast.LENGTH_LONG).show();
//                    rejectCall();
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError)
//            {
//
//            }
//        });

        if(Build.VERSION.SDK_INT >= 26)
        {
            final Handler handler2 = new Handler();
            Runnable runnable2 = new Runnable()
            {
                @Override
                public void run()
                {
                    if(vibrate)
                    {
                        vibrator.vibrate(VibrationEffect.createOneShot(750, VibrationEffect.DEFAULT_AMPLITUDE));
                        handler2.postDelayed(this, 1250);
                    }
                    else
                    {
                        handler2.removeCallbacks(this);
                    }
                }
            };
            handler2.postDelayed(runnable2, 1250);
        }
    }

    @Override
    protected void onResume()
    {
        super.onResume();
    }

    public void acceptCall(View view)
    {
        Log.i("LastCheck - IC - User ID in acceptCall", userID);
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Recieve_User").child(userID);
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                String caller = dataSnapshot.getValue().toString();
                Log.i("LastCheck - IC - Caller ID in acceptCall", caller);
                if(!caller.equals("None"))
                {
                    otherID = caller;
                }
                Log.i("LastCheck - IC - Other ID in acceptCall", otherID);
                mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Num_Projects");
                mDatabase.addListenerForSingleValueEvent(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                    {
                        String number = dataSnapshot.getValue().toString();
                        mDatabase = FirebaseDatabase.getInstance().getReference().child("Connections");
                        mDatabase.addListenerForSingleValueEvent(new ValueEventListener()
                        {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                            {
                                for(int i = 1; i <= Integer.parseInt(number); i++)
                                {
                                    Log.i("LastCheck - IC - Checking for caller in Proj in acceptCall", "Proj_" + String.valueOf(i));
                                    String check = dataSnapshot.child("Proj_" + String.valueOf(i)).child("User_1").getValue().toString();
                                    Log.i("LastCheck - IC - Caller ID in Proj in acceptCall", check);
                                    if(check.equals(caller))
                                    {
                                        mDatabase.child("Proj_" + String.valueOf(i)).child("User_2").setValue(userID);
                                        Receiving = false;
                                        vibrate = false;
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
        Log.i("LastCheck - IC - Receiving in declineCall", String.valueOf(Receiving));
        Log.i("LastCheck - IC - User ID in declineCall", userID);
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Recieve_User").child(userID);
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                String callFrom = dataSnapshot.getValue().toString();
                Log.i("LastCheck - IC - Call From in declineCall", callFrom);
                if(!callFrom.equals("None"))
                {
                    mDatabase.getRoot().child("Call_User").child(callFrom).setValue("None");
                    Receiving = false;
                    vibrate = false;
                    mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Num_Projects");
                    mDatabase.addListenerForSingleValueEvent(new ValueEventListener()
                    {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                        {
                            String number = dataSnapshot.getValue().toString();
                            mDatabase = FirebaseDatabase.getInstance().getReference().child("Connections");
                            mDatabase.addListenerForSingleValueEvent(new ValueEventListener()
                            {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                                {
                                    for(int i = 1; i <= Integer.parseInt(number); i++)
                                    {
                                        Log.i("LastCheck - IC - Checking for caller in Proj in declineCall", "Proj_" + String.valueOf(i));
                                        String check = dataSnapshot.child("Proj_" + String.valueOf(i)).child("User_1").getValue().toString();
                                        Log.i("LastCheck - IC - Caller ID in Proj in declineCall", check);
                                        if(check.equals(callFrom))
                                        {
                                            Receiving = false;
                                            vibrate = false;
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
        Button Accept_Button = findViewById(R.id.pickCallButton);
        Button Decline_Button = findViewById(R.id.declineCallButton);
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
        Receiving = false;
        vibrate = false;
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
        Receiving = false;
        vibrate = false;
        finish();
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent)
    {
        if(view.getId() == R.id.blackhole)
        {
            GD.onTouchEvent(motionEvent);
            return true;
        }
        return true;
    }

    @Override
    public boolean onDrag(View view, DragEvent dragEvent)
    {
        return true;
    }

    @Override
    public boolean onDown(MotionEvent motionEvent)
    {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent motionEvent)
    {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent motionEvent)
    {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1)
    {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent motionEvent)
    {

    }

    @Override
    public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1)
    {
        if(v > 30)
        {
            Button X = findViewById(R.id.pickCallButton);
            X.performClick();
        }
        else if(v < -30)
        {
            Button X = findViewById(R.id.declineCallButton);
            X.performClick();
        }
        return true;
    }
}