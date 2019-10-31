package com.example.mcfinalproject;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.opentok.android.Session;
import com.opentok.android.Stream;
import com.opentok.android.Publisher;
import com.opentok.android.PublisherKit;
import com.opentok.android.Subscriber;

import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import com.opentok.android.OpentokError;

import androidx.annotation.NonNull;

import android.Manifest;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements Session.SessionListener, PublisherKit.PublisherListener
{
    private static String API_KEY;
    private static String SESSION_ID;
    private static String TOKEN;
    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private static final int RC_SETTINGS_SCREEN_PERM = 123;
    private static final int RC_VIDEO_APP_PERM = 124;
    private Session mSession;
    private FrameLayout mPublisherViewContainer;
    private FrameLayout mSubscriberViewContainer;
    private Publisher mPublisher;
    private Subscriber mSubscriber;
    private DatabaseReference mDatabase;
    private String Call_From = "";
    private String Call_To = "";
    private String Proj = "";
    private String userID;
    private CanvasViewClient canvasView;
    private CanvasViewServer canvasView2;
    private Button but;
    private Button but2;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestPermissions();
        Intent intent = getIntent();
        Call_From = intent.getStringExtra("CallFrom");
        Call_To = intent.getStringExtra("CallTo");
        Proj = intent.getStringExtra("Proj_ID");
        userID = intent.getStringExtra("UserID");
        Log.i("CheckUser", userID);
        Log.i("CheckCallFrom", Call_From);
        Log.i("CheckCallTo", Call_To);
        Log.i("CheckProj", Proj);
        but2 = (Button)findViewById(R.id.button);
        canvasView = (CanvasViewClient)findViewById(R.id.canvas);
        canvasView2 = (CanvasViewServer)findViewById(R.id.canvas2);
        but = (Button)findViewById(R.id.button2);
        connectCall();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Call_User").child(Call_From);
        mDatabase.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                String check = dataSnapshot.getValue().toString();
                if(check.equals("None"))
                {
                    Button btn = (Button) findViewById(R.id.buttonDisconnect);
                    btn.performClick();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });
        but.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDatabase.getRoot().child("Connections").child(Proj).child("CanvasDraw").setValue(canvasView.pl);
            }
        });
        but2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                canvasView.clearCanvas();
                canvasView.pl = "None";
                mDatabase.getRoot().child("Connections").child(Proj).child("CanvasDraw").setValue(canvasView.pl);
            }
        });
        checkDrawing();
    }

    @AfterPermissionGranted(RC_VIDEO_APP_PERM)
    private void requestPermissions()
    {
        String[] perms = {Manifest.permission.INTERNET, Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO};
        if(EasyPermissions.hasPermissions(this, perms))
        {
            // initialize view objects from your layout
            mPublisherViewContainer = (FrameLayout) findViewById(R.id.publisher_container);
            mSubscriberViewContainer = (FrameLayout) findViewById(R.id.subscriber_container);
        }
        else
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
        mDatabase.getRoot().child("Connections").child(Proj).child("CanvasDraw").setValue("None");
        mDatabase.getRoot().child("Call_User").child(Call_From).setValue("None");
        mDatabase.getRoot().child("Recieve_User").child(Call_To).setValue("None");
        Toast.makeText(getApplicationContext(),"THE SESSION WAS DISCONNECTED", Toast.LENGTH_LONG).show();
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
        Toast.makeText(getApplicationContext(),"THE SESSION WAS DISCONNECTED", Toast.LENGTH_LONG).show();
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

    // SessionListener methods
    @Override
    public void onConnected(Session session)
    {
        Log.i(LOG_TAG, "Session Connected");

        mPublisher = new Publisher.Builder(this).build();
        mPublisher.setPublisherListener(this);
        mPublisherViewContainer.addView(mPublisher.getView());

        if(mPublisher.getView() instanceof GLSurfaceView)
        {
//            ((GLSurfaceView) mPublisher.getView()).setZOrderOnTop(false);
        }

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

        if(mSubscriber == null)
        {
            mSubscriber = new Subscriber.Builder(this, stream).build();
            mSession.subscribe(mSubscriber);
            mSubscriberViewContainer.addView(mSubscriber.getView());
        }
    }

    @Override
    public void onStreamDropped(Session session, Stream stream)
    {
        Log.i(LOG_TAG, "Stream Dropped");

        if(mSubscriber != null)
        {
            mSubscriber = null;
            mSubscriberViewContainer.removeAllViews();
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

    public void getDrawing(FirebaseCallback2 fbcb2)
    {
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase = mDatabase.getRoot().child("Connections").child(Proj).child("CanvasDraw");
        mDatabase.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                String rec = dataSnapshot.getValue().toString();
                if(!rec.equals("None"))
                {
                    fbcb2.onCallback2(rec);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });
    }

    private interface FirebaseCallback2
    {
        void onCallback2(String rekt);
    }

    public void checkDrawing()
    {
        getDrawing(new FirebaseCallback2()
        {
            @Override
            public void onCallback2(String rekt)
            {
                if(userID.equals("User_1"))
                {
                    Log.i("Get drawing", rekt);
                    canvasView2.updateCanvas(rekt);
                }
            }
        });
    }
}