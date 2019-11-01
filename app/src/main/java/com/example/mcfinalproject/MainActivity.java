package com.example.mcfinalproject;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
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
import androidx.fragment.app.Fragment;

import android.Manifest;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;
import top.defaults.colorpicker.ColorPickerPopup;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;
import top.defaults.colorpicker.ColorPickerView;

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

    public Publisher getmPublisher() {
        return mPublisher;
    }

    public Subscriber getmSubscriber() {
        return mSubscriber;
    }

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
    private mFragment fragment;

    public void setCurrentFragment(mFragment fragment)
    {
        this.fragment = fragment;
    }
//    public void makeConnection2()
//    {
//        mSession = new Session.Builder(this, "46431312", "1_MX40NjQzMTMxMn5-MTU3MjI5NzM5MjY5M35wdnV2S1NPSVkrTDBIU09Ucm5zU0Q2RTB-fg").build();
//        mSession.setSessionListener(this);
//        mSession.connect("T1==cGFydG5lcl9pZD00NjQzMTMxMiZzaWc9NTEzZjBiOTVmNWU1ZGM4MWJiZDZiOWU4Mzc5Y2E0ZTNjZDY2NzY5ZjpzZXNzaW9uX2lkPTFfTVg0ME5qUXpNVE14TW41LU1UVTNNakk1TnpNNU1qWTVNMzV3ZG5WMlMxTlBTVmtyVERCSVUwOVVjbTV6VTBRMlJUQi1mZyZjcmVhdGVfdGltZT0xNTcyMjk3NDE0Jm5vbmNlPTAuNTE4MDMwMjQ0NzU3NzQ1NiZyb2xlPXB1Ymxpc2hlciZleHBpcmVfdGltZT0xNTc0ODkzMDEyJmluaXRpYWxfbGF5b3V0X2NsYXNzX2xpc3Q9");
//    }
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Log.d("fragment","MainActivity onCreate");
        setContentView(R.layout.activity_main_fragments);
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        decorView.setSystemUiVisibility(uiOptions);

//        ColorPickerView
        fragment = new mFragment(1);
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.main_fragmentFrame,fragment)
                .commit();
        getSupportFragmentManager().executePendingTransactions();
//        afterF/ragmentLoaded();
        requestPermissions();
        Intent intent = getIntent();
        Call_From = intent.getStringExtra("CallFrom");
        Call_To = intent.getStringExtra("CallTo");
        Proj = intent.getStringExtra("Proj_ID");
        userID = intent.getStringExtra("UserID");
//        Log.i("CheckUser", userID);
//        but2 = (Button)findViewById(R.id.button);
//        canvasView = (CanvasViewClient)findViewById(R.id.canvas);
//        canvasView2 = (CanvasViewServer)findViewById(R.id.canvas2);
//
//        but = (Button)findViewById(R.id.button2);
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
//                    Button btn = (Button) findViewById(R.id.buttonDisconnect);
//                    btn.performClick();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });
//        but.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Log.i("HelloCheck", "ASASASAS");
//                Log.i("LOLOLOLOLOL", canvasView.pl);
//                mDatabase.getRoot().child("Connections").child("Proj_1").child("CanvasDraw").setValue(canvasView.pl);
//
//            }
//        });
//        but2.setOnClickListener(new View.OnCli    ckListener() {
//            @Override
//            public void onClick(View v) {
//                canvasView.clearCanvas();
//                canvasView.pl = "Noneee";
//                mDatabase.getRoot().child("Connections").child("Proj_1").child("CanvasDraw").setValue(canvasView.pl);
//
//            }
//        });
//        makeConnection2();
        checkDrawing();
    }
    public void afterFragmentLoaded()
    {
        requestPermissions();
        Log.d("fragment","Done with requestPermissions");
        Intent intent = getIntent();
        Call_From = intent.getStringExtra("CallFrom");
        Call_To = intent.getStringExtra("CallTo");
        Proj = intent.getStringExtra("Proj_ID");
        userID = intent.getStringExtra("UserID");
//        Log.i("CheckUser", userID);
//        but2 = (Button)findViewById(R.id.button);
//        canvasView = (CanvasViewClient)findViewById(R.id.canvas);
//        canvasView2 = (CanvasViewServer)findViewById(R.id.canvas2);
//
//        but = (Button)findViewById(R.id.button2);
//        connectCall();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Call_User").child(Call_From);
        mDatabase.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                String check = dataSnapshot.getValue().toString();
                if(check.equals("None"))
                {
                    disconnect(null);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });
//        but.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Log.i("HelloCheck", "ASASASAS");
//                Log.i("LOLOLOLOLOL", canvasView.pl);
//                mDatabase.getRoot().child("Connections").child("Proj_1").child("CanvasDraw").setValue(canvasView.pl);
//
//            }
//        });
//        but2.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                canvasView.clearCanvas();
//                canvasView.pl = "Noneee";
//                mDatabase.getRoot().child("Connections").child("Proj_1").child("CanvasDraw").setValue(canvasView.pl);
//
//            }
//        });
//        makeConnection2();
        checkDrawing();

    }
    @AfterPermissionGranted(RC_VIDEO_APP_PERM)
    private void requestPermissions()
    {
        Log.d("fragment","in requestPermissions");
        String[] perms = {Manifest.permission.INTERNET, Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO};
        if(EasyPermissions.hasPermissions(this, perms))
        {
//            // initialize view objects from your layout
//            mPublisherViewContainer=null;
//            mSubscriberViewContainer=null;
//            int i = 0;
////            while(((Fragment1)fragment).fragView==null)
////            {
////                Log.d("fragment","fragView null "+i);
////                ++i;
////            }
//            while(mPublisherViewContainer==null)
//            {
////                mPublisherViewContainer = ((Fragment1)fragment).fragView.findViewById(R.id.publisher_container);
////                mPublisherViewContainer = ((Fragment1) fragment).mPubContainer;
////                    mPublisherViewContainer = findViewById(R.id.publisher_container_1);
//                mPublisherViewContainer = fragment.getView().findViewById(R.id.publisher_container_1);
//                Log.d("fragment","mPublisherViewContainer==null "+i++);
//                if(i>100)
//                    break;;
//            }
//            while(mSubscriberViewContainer==null)
//            {
////                mSubscriberViewContainer = ((Fragment1)fragment).fragView.findViewById(R.id.subscriber_container);
////                mSubscriberViewContainer = ((Fragment1) fragment).mSubContainer;
//
////                mSubscriberViewContainer = findViewById(R.id.subscriber_container_1);
//                mSubscriberViewContainer = fragment.getView().findViewById(R.id.subscriber_container_1);
////                Log.d("fragment","mSubscriberViewContainer==null "+i++);
//            }
//            Log.d("fragment","requestPermission DONE");
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

    public void showPallete(View view) {
        new ColorPickerPopup.Builder(this)
                .initialColor(Color.RED) // Set initial color
                .enableBrightness(true) // Enable brightness slider or not
                .enableAlpha(true) // Enable alpha slider or not
                .okTitle("Choose")
                .cancelTitle("Cancel")
                .showIndicator(true)
                .showValue(true)
                .build()
                .show(new ColorPickerPopup.ColorPickerObserver() {
                    @Override
                    public void onColorPicked(int color) {
//                        v.setBackgroundColor(color);
                        fragment.setColor(color);
//                        ((CanvasViewClient)fragment.getCanvasViewClient()).setColor(color);
                    }

                    public void onColor(int color, boolean fromUser) {

                    }
                });
    }

    public void send(View view)
    {

    }

    public void clearCanvas(View view) {
        ((CanvasViewClient)fragment.getCanvasViewClient()).clearCanvas();
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
                Log.i("CHECK_LOL", API_KEY);
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

//        mPublisher = new Publisher.Builder(this).build();
//        mPublisher.setPublisherListener(this);
//        mPublisherViewContainer.addView(mPublisher.getView());
//        if(mPublisher.getView() instanceof GLSurfaceView)
//        {
//            ((GLSurfaceView) mPublisher.getView()).setZOrderOnTop(true);
//        }
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
        Log.d("fragment","onStreamReceived");

        if(mSubscriber == null)
        {
            mSubscriber = new Subscriber.Builder(this, stream).build();
            mSession.subscribe(mSubscriber);
            ((mFragment)fragment).addSubscriber(mSubscriber.getView());
            ((mFragment)fragment).addPublisher(mPublisher.getView());
//            mSubscriberViewContainer.addView(mSubscriber.getView());
//            mPublisherViewContainer.addView(mPublisher.getView());
        }
    }

    @Override
    public void onStreamDropped(Session session, Stream stream)
    {
        Log.i(LOG_TAG, "Stream Dropped");

        if(mSubscriber != null)
        {
            mSubscriber = null;
//            fragment.removeSubscriber();
//            fragment.removePublisher();
            disconnect(null);
//            mSubscriberViewContainer.removeAllViews();
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
        mDatabase = mDatabase.getRoot().child("Connections").child("Proj_1").child("CanvasDraw");
        mDatabase.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                String rec = dataSnapshot.getValue().toString();
                if(!rec.equals("Noneee"))
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
                if(userID.equals("1"))
                {
                    Log.i("Get drawing", rekt);
                    ((CanvasViewClient)fragment.getCanvasViewClient()).;
                }
            }
        });
    }
}