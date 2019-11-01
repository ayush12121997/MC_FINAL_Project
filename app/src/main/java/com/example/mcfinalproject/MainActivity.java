package com.example.mcfinalproject;

import android.Manifest;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.renderscript.Sampler;
import android.telecom.Call;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.opentok.android.OpentokError;
import com.opentok.android.Publisher;
import com.opentok.android.PublisherKit;
import com.opentok.android.Session;
import com.opentok.android.Stream;
import com.opentok.android.Subscriber;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;
import top.defaults.colorpicker.ColorPickerPopup;

public class MainActivity extends AppCompatActivity implements Session.SessionListener, PublisherKit.PublisherListener
{
    private static String API_KEY;
    private static String SESSION_ID;
    private static String TOKEN;
    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private static final int RC_SETTINGS_SCREEN_PERM = 123;
    private static final int RC_VIDEO_APP_PERM = 124;
    private Session mSession;
    private Publisher mPublisher;
    private Subscriber mSubscriber;
    private DatabaseReference mDatabase;
    private String Call_From = "";
    private String Call_To = "";
    private String Proj = "";
    private String userID;
    private mFragment fragment;
    private boolean subBig = true;

    public void invertSubBig()
    {
        subBig = !subBig;
    }

    public void setCurrentFragment(mFragment fragment)
    {
        this.fragment = fragment;
    }

    public Publisher getmPublisher()
    {
        return mPublisher;
    }

    public Subscriber getmSubscriber()
    {
        return mSubscriber;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Log.d("fragment", "MainActivity onCreate");
        setContentView(R.layout.activity_main_fragments);
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        decorView.setSystemUiVisibility(uiOptions);
        fragment = new mFragment(1);
        getSupportFragmentManager().beginTransaction().add(R.id.main_fragmentFrame, fragment).commit();
        getSupportFragmentManager().executePendingTransactions();
        requestPermissions();
        Intent intent = getIntent();
        Call_From = intent.getStringExtra("CallFrom");
        Call_To = intent.getStringExtra("CallTo");
        Proj = intent.getStringExtra("Proj_ID");
        userID = intent.getStringExtra("UserID");
        connectCall();
//        checkDrawing();
    }

    @AfterPermissionGranted(RC_VIDEO_APP_PERM)
    private void requestPermissions()
    {
        Log.d("fragment", "in requestPermissions");
        String[] perms = {Manifest.permission.INTERNET, Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO};
        if(!EasyPermissions.hasPermissions(this, perms))
        {
            EasyPermissions.requestPermissions(this, "This app needs access to your camera and mic to make video calls", RC_VIDEO_APP_PERM, perms);
        }
    }

    public void updateInitials(FirebaseCallback fbcb)
    {
        mDatabase = FirebaseDatabase.getInstance().getReference().child(Proj);
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                String api = dataSnapshot.child("API_KEY").getValue().toString();
                String token = dataSnapshot.child("TOKEN").getValue().toString();
                String session = dataSnapshot.child("SESSION_ID").getValue().toString();
                fbcb.onCallback(api, token, session);
                return;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {
            }
        });
    }

    public void showPallete(View view)
    {
        new ColorPickerPopup.Builder(this).initialColor(Color.RED) // Set initial color
                .enableBrightness(true) // Enable brightness slider or not
                .enableAlpha(true) // Enable alpha slider or not
                .okTitle("Choose").cancelTitle("Cancel").showIndicator(true).showValue(true).build().show(new ColorPickerPopup.ColorPickerObserver()
        {
            @Override
            public void onColorPicked(int color)
            {
                fragment.setColor(color);
            }
        });
    }

    private interface FirebaseCallback
    {
        void onCallback(String API, String TOKEN, String SESSION);
    }

    public void connectCall()
    {
        updateInitials(new FirebaseCallback()
        {
            @Override
            public void onCallback(String API, String token, String SESSION)
            {
                API_KEY = API;
                SESSION_ID = SESSION;
                TOKEN = token;
                makeConnection();
            }
        });
    }

    public void makeConnection()
    {
        mSession = new Session.Builder(this, API_KEY, SESSION_ID).build();
        mSession.setSessionListener(this);
        mSession.connect(TOKEN);
    }

    public void disconnect(View view)
    {
        mSession.disconnect();
        mDatabase.getRoot().child("Connections").child(Proj).child("User_1").setValue("None");
        mDatabase.getRoot().child("Connections").child(Proj).child("User_2").setValue("None");
        mDatabase.getRoot().child("Call_User").child(Call_From).setValue("None");
        mDatabase.getRoot().child("Recieve_User").child(Call_To).setValue("None");
        Toast.makeText(getApplicationContext(), "THE SESSION WAS DISCONNECTED", Toast.LENGTH_LONG).show();
        Intent intent = new Intent(getApplicationContext(), HomeScreen.class);
        intent.putExtra("UserID", userID);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
        mSession.disconnect();
        mDatabase.getRoot().child("Connections").child(Proj).child("User_1").setValue("None");
        mDatabase.getRoot().child("Connections").child(Proj).child("User_2").setValue("None");
        mDatabase.getRoot().child("Call_User").child(Call_From).setValue("None");
        mDatabase.getRoot().child("Recieve_User").child(Call_To).setValue("None");
        Toast.makeText(getApplicationContext(), "THE SESSION WAS DISCONNECTED", Toast.LENGTH_LONG).show();
        Intent intent = new Intent(getApplicationContext(), HomeScreen.class);
        intent.putExtra("UserID", userID);
        startActivity(intent);
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onConnected(Session session)
    {
        Log.i(LOG_TAG, "Session Connected");
        mPublisher = new Publisher.Builder(this).build();
        mPublisher.setPublisherListener(this);
        mSession.publish(mPublisher);
    }

    @Override
    public void onDisconnected(Session session)
    {
        Log.i(LOG_TAG, "Session Disconnected");
    }

    @Override
    public void onStreamReceived(Session session, Stream stream)
    {
        Log.i(LOG_TAG, "Stream Received");
        Log.d("fragment", "onStreamReceived");

        if(mSubscriber == null)
        {
            mSubscriber = new Subscriber.Builder(this, stream).build();
            mSession.subscribe(mSubscriber);
            ((mFragment) fragment).addSubscriber(mSubscriber.getView());
            ((mFragment) fragment).addPublisher(mPublisher.getView());
        }
    }

    @Override
    public void onStreamDropped(Session session, Stream stream)
    {
        Log.i(LOG_TAG, "Stream Dropped");

        if(mSubscriber != null)
        {
            mSubscriber = null;
            disconnect(null);
        }
    }

    @Override
    public void onError(Session session, OpentokError opentokError)
    {
        Log.e(LOG_TAG, "Session error: " + opentokError.getMessage());
    }

    @Override
    public void onStreamCreated(PublisherKit publisherKit, Stream stream)
    {

    }

    @Override
    public void onStreamDestroyed(PublisherKit publisherKit, Stream stream)
    {

    }

    @Override
    public void onError(PublisherKit publisherKit, OpentokError opentokError)
    {

    }

    public void send(View view)
    {
        if(Call_From.equals(userID) && subBig)
        {
            mDatabase = FirebaseDatabase.getInstance().getReference().child("Connections").child(Proj).child("Draw_Sub1");
            mDatabase.addListenerForSingleValueEvent(new ValueEventListener()
            {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                {
                    String curr = dataSnapshot.getValue().toString();
                    mDatabase.getRoot().child("Connections").child(Proj).child("Draw_Sub1").setValue(((CanvasViewClient) fragment.getCanvasSubClient()).pl + curr);
                    ((CanvasViewClient) fragment.getCanvasSubClient()).clearCanvas();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError)
                {

                }
            });
        }
        else if(Call_From.equals(userID) && !subBig)
        {
            mDatabase = FirebaseDatabase.getInstance().getReference().child("Connections").child(Proj).child("Draw_Pub1");
            mDatabase.addListenerForSingleValueEvent(new ValueEventListener()
            {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                {
                    String curr = dataSnapshot.getValue().toString();
                    mDatabase.getRoot().child("Connections").child(Proj).child("Draw_Pub1").setValue(((CanvasViewClient) fragment.getCanvasPubClient()).pl + curr);
                    ((CanvasViewClient) fragment.getCanvasPubClient()).clearCanvas();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError)
                {

                }
            });
        }
        else if(!Call_From.equals(userID) && !subBig)
        {
            mDatabase = FirebaseDatabase.getInstance().getReference().child("Connections").child(Proj).child("Draw_Pub2");
            mDatabase.addListenerForSingleValueEvent(new ValueEventListener()
            {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                {
                    String curr = dataSnapshot.getValue().toString();
                    mDatabase.getRoot().child("Connections").child(Proj).child("Draw_Pub2").setValue(((CanvasViewClient) fragment.getCanvasPubClient()).pl + curr);
                    ((CanvasViewClient) fragment.getCanvasPubClient()).clearCanvas();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError)
                {

                }
            });
        }
        else
        {
            mDatabase = FirebaseDatabase.getInstance().getReference().child("Connections").child(Proj).child("Draw_Sub2");
            mDatabase.addListenerForSingleValueEvent(new ValueEventListener()
            {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                {
                    String curr = dataSnapshot.getValue().toString();
                    mDatabase.getRoot().child("Connections").child(Proj).child("Draw_Sub2").setValue(((CanvasViewClient) fragment.getCanvasSubClient()).pl + curr);
                    ((CanvasViewClient) fragment.getCanvasSubClient()).clearCanvas();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError)
                {

                }
            });
        }
    }

    public void clearCanvas(View view)
    {
        if(Call_From.equals(userID) && subBig)
        {
            ((CanvasViewClient) fragment.getCanvasSubClient()).clearCanvas();
            mDatabase.getRoot().child("Connections").child(Proj).child("Draw_Sub1").setValue("");
        }
        else if(Call_From.equals(userID) && !subBig)
        {
            ((CanvasViewClient) fragment.getCanvasPubClient()).clearCanvas();
            mDatabase.getRoot().child("Connections").child(Proj).child("Draw_Pub1").setValue("");
        }
        else if(!Call_From.equals(userID) && subBig)
        {
            ((CanvasViewClient) fragment.getCanvasSubClient()).clearCanvas();
            mDatabase.getRoot().child("Connections").child(Proj).child("Draw_Sub2").setValue("");
        }
        else
        {
            ((CanvasViewClient) fragment.getCanvasPubClient()).clearCanvas();
            mDatabase.getRoot().child("Connections").child(Proj).child("Draw_Pub2").setValue("");
        }
    }

    public void getDrawingSub(FirebaseCallback2 fbcb2)
    {
        if(!Call_From.equals(userID))
        {
            mDatabase = FirebaseDatabase.getInstance().getReference();
            mDatabase = mDatabase.getRoot().child("Connections").child(Proj).child("Draw_Sub2");
            mDatabase.addValueEventListener(new ValueEventListener()
            {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                {
                    String sub2 = dataSnapshot.getValue().toString();
                    mDatabase = mDatabase.getRoot().child("Connections").child(Proj).child("Draw_Pub1");
                    mDatabase.addListenerForSingleValueEvent(new ValueEventListener()
                    {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                        {
                            String pub1 = dataSnapshot.getValue().toString();
                            fbcb2.onCallback2(sub2, pub1);
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
            mDatabase = FirebaseDatabase.getInstance().getReference();
            mDatabase = mDatabase.getRoot().child("Connections").child(Proj).child("Draw_Pub1");
            mDatabase.addValueEventListener(new ValueEventListener()
            {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                {
                    String pub1 = dataSnapshot.getValue().toString();
                    mDatabase = mDatabase.getRoot().child("Connections").child(Proj).child("Draw_Sub2");
                    mDatabase.addListenerForSingleValueEvent(new ValueEventListener()
                    {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                        {
                            String sub2 = dataSnapshot.getValue().toString();
                            fbcb2.onCallback2(pub1, sub2);
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
        else
        {
            mDatabase = FirebaseDatabase.getInstance().getReference();
            mDatabase = mDatabase.getRoot().child("Connections").child(Proj).child("Draw_Sub1");
            mDatabase.addValueEventListener(new ValueEventListener()
            {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                {
                    String sub1 = dataSnapshot.getValue().toString();
                    mDatabase = mDatabase.getRoot().child("Connections").child(Proj).child("Draw_Pub2");
                    mDatabase.addListenerForSingleValueEvent(new ValueEventListener()
                    {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                        {
                            String pub2 = dataSnapshot.getValue().toString();
                            fbcb2.onCallback2(sub1, pub2);
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
            mDatabase = FirebaseDatabase.getInstance().getReference();
            mDatabase = mDatabase.getRoot().child("Connections").child(Proj).child("Draw_Pub2");
            mDatabase.addValueEventListener(new ValueEventListener()
            {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                {
                    String pub2 = dataSnapshot.getValue().toString();
                    mDatabase = mDatabase.getRoot().child("Connections").child(Proj).child("Draw_Sub1");
                    mDatabase.addListenerForSingleValueEvent(new ValueEventListener()
                    {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                        {
                            String sub1 = dataSnapshot.getValue().toString();
                            fbcb2.onCallback2(pub2, sub1);
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

    public void getDrawingPub(FirebaseCallback2 fbcb2)
    {
        if(Call_From.equals(userID))
        {
            mDatabase = FirebaseDatabase.getInstance().getReference();
            mDatabase = mDatabase.getRoot().child("Connections").child(Proj).child("Draw_Sub2");
            mDatabase.addValueEventListener(new ValueEventListener()
            {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                {
                    String sub2 = dataSnapshot.getValue().toString();
                    mDatabase = mDatabase.getRoot().child("Connections").child(Proj).child("Draw_Pub1");
                    mDatabase.addListenerForSingleValueEvent(new ValueEventListener()
                    {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                        {
                            String pub1 = dataSnapshot.getValue().toString();
                            fbcb2.onCallback2(sub2, pub1);
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
            mDatabase = FirebaseDatabase.getInstance().getReference();
            mDatabase = mDatabase.getRoot().child("Connections").child(Proj).child("Draw_Pub1");
            mDatabase.addValueEventListener(new ValueEventListener()
            {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                {
                    String pub1 = dataSnapshot.getValue().toString();
                    mDatabase = mDatabase.getRoot().child("Connections").child(Proj).child("Draw_Sub2");
                    mDatabase.addListenerForSingleValueEvent(new ValueEventListener()
                    {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                        {
                            String sub2 = dataSnapshot.getValue().toString();
                            fbcb2.onCallback2(pub1, sub2);
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
        else
        {
            mDatabase = FirebaseDatabase.getInstance().getReference();
            mDatabase = mDatabase.getRoot().child("Connections").child(Proj).child("Draw_Sub1");
            mDatabase.addValueEventListener(new ValueEventListener()
            {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                {
                    String sub1 = dataSnapshot.getValue().toString();
                    mDatabase = mDatabase.getRoot().child("Connections").child(Proj).child("Draw_Pub2");
                    mDatabase.addListenerForSingleValueEvent(new ValueEventListener()
                    {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                        {
                            String pub2 = dataSnapshot.getValue().toString();
                            fbcb2.onCallback2(sub1, pub2);
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
            mDatabase = FirebaseDatabase.getInstance().getReference();
            mDatabase = mDatabase.getRoot().child("Connections").child(Proj).child("Draw_Pub2");
            mDatabase.addValueEventListener(new ValueEventListener()
            {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                {
                    String pub2 = dataSnapshot.getValue().toString();
                    mDatabase = mDatabase.getRoot().child("Connections").child(Proj).child("Draw_Sub1");
                    mDatabase.addListenerForSingleValueEvent(new ValueEventListener()
                    {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                        {
                            String sub1 = dataSnapshot.getValue().toString();
                            fbcb2.onCallback2(pub2, sub1);
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

    private interface FirebaseCallback2
    {
        void onCallback2(String d1, String d2);
    }

    public void checkDrawing()
    {
        getDrawingSub(new FirebaseCallback2()
        {
            @Override
            public void onCallback2(String d1, String d2)
            {
                if(subBig)
                {
                    ((CanvasViewServer) fragment.getCanvasSubServer()).updateCanvas(d1, d2);
                }
            }
        });
        getDrawingPub(new FirebaseCallback2()
        {
            @Override
            public void onCallback2(String d1, String d2)
            {
                if(!subBig)
                {
                    ((CanvasViewServer) fragment.getCanvasPubServer()).updateCanvas(d1, d2);
                }
            }
        });
    }
}