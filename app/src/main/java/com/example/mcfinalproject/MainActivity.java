package com.example.mcfinalproject;

import android.Manifest;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.TextureView;
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
    private boolean connected;
    private boolean already_disconnected;
    private String userID;
    private mFragment fragment;
    private boolean subBig;

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

    public TextureView getPublisherView()
    {
        TextureView view = (TextureView) getmPublisher().getView();
        view.setScaleX(-1);
        return view;
    }

    public TextureView getSubscriberView()
    {
        return (TextureView) getmSubscriber().getView();
    }

    public Subscriber getmSubscriber()
    {
        return mSubscriber;
    }

    public boolean isCaller()
    {
        if(Call_From.equals(userID))
        {
            return true;
        }
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Log.d("fragment", "MainActivity onCreate");
        setContentView(R.layout.activity_main_fragments);
        fragment = new mFragment(1);
        getSupportFragmentManager().beginTransaction().add(R.id.main_fragmentFrame, fragment).commit();
        getSupportFragmentManager().executePendingTransactions();
        requestPermissions();
        Intent intent = getIntent();
        subBig = true;
        Call_From = intent.getStringExtra("CallFrom");
        Call_To = intent.getStringExtra("CallTo");
        Proj = intent.getStringExtra("Proj_ID");
        userID = intent.getStringExtra("UserID");
        Log.i("LastCheck - Call from", Call_From);
        Log.i("LastCheck - Call to", Call_To);
        Log.i("LastCheck - Proj ID", Proj);
        Log.i("LastCheck - UserID", userID);
        connected = true;
        already_disconnected = false;
        connectCall();

        final Handler handler = new Handler();
        Runnable runnable = new Runnable()
        {
            @Override
            public void run()
            {
                Log.i("LastCheck - isConnected - Runnable", String.valueOf(connected));
                if(connected)
                {
                    Log.i("LastCheck - Call to - Runnable", Call_To);
                    mDatabase = FirebaseDatabase.getInstance().getReference().child("Recieve_User").child(Call_To);
                    mDatabase.addListenerForSingleValueEvent(new ValueEventListener()
                    {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                        {
                            String check = dataSnapshot.getValue().toString();
                            Log.i("LastCheck - Call from in reciever table - Runnable", check);
                            if(check.equals("None"))
                            {
                                connected = false;
                                disconnect(null);
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
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        disconnect(null);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
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
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        makeConnection();
                    }
                }, 500);
            }
        });
    }

    public void updateInitials(FirebaseCallback fbcb)
    {
        Log.i("LastCheck - updateInitials - Proj", Proj);
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

    public void makeConnection()
    {
        Log.i("LastCheck - makeConnection - APIKEY", API_KEY);
        Log.i("LastCheck - makeConnection - SESSION ID", SESSION_ID);
        Log.i("LastCheck - makeConnection - TOKEN", TOKEN);
        mSession = new Session.Builder(this, API_KEY, SESSION_ID).sessionOptions(new Session.SessionOptions() {
            @Override
            public boolean useTextureViews() {
                return true;
            }
        }).build();

        mSession.setSessionListener(this);
        mSession.connect(TOKEN);
    }

    public void showPallete(View view)
    {
        new ColorPickerPopup.Builder(this).initialColor(Color.RED) // Set initial color
                .enableBrightness(true) // Enable brightness slider or not
                .enableAlpha(true) // Enable alpha slider or not
                .okTitle("CHOOSE").cancelTitle("CANCEL").showIndicator(true).showValue(true).build().show(new ColorPickerPopup.ColorPickerObserver()
        {
            @Override
            public void onColorPicked(int color)
            {
                fragment.setColor(color);
            }
        });
    }

    public void disconnect(View view)
    {
        if(!already_disconnected)
        {
            connected = false;
            already_disconnected = true;
            mSession.disconnect();
            Log.i("LastCheck - Disconnecting - Proj1", Proj);
            mDatabase.getRoot().child("Connections").child(Proj).child("User_1").setValue("None");
            Log.i("LastCheck - Disconnecting - Proj2", Proj);
            mDatabase.getRoot().child("Connections").child(Proj).child("User_2").setValue("None");
            Log.i("LastCheck - Disconnecting - Proj3", Proj);
            mDatabase.getRoot().child("Connections").child(Proj).child("Draw_Sub1").setValue("");
            Log.i("LastCheck - Disconnecting - Proj4", Proj);
            mDatabase.getRoot().child("Connections").child(Proj).child("Draw_Sub2").setValue("");
            Log.i("LastCheck - Disconnecting - Proj5", Proj);
            mDatabase.getRoot().child("Connections").child(Proj).child("Draw_Pub1").setValue("");
            Log.i("LastCheck - Disconnecting - Proj6", Proj);
            mDatabase.getRoot().child("Connections").child(Proj).child("Draw_Pub2").setValue("");
            Log.i("LastCheck - Disconnecting - Call From", Call_From);
            mDatabase.getRoot().child("Call_User").child(Call_From).setValue("None");
            Log.i("LastCheck - Disconnecting - Call To", Call_To);
            mDatabase.getRoot().child("Recieve_User").child(Call_To).setValue("None");
            Toast.makeText(getApplicationContext(), "THE CALL WAS DISCONNECTED", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
        disconnect(null);
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
            ((mFragment) fragment).addSubscriber(getSubscriberView());
            ((mFragment) fragment).addPublisher(getPublisherView());
        }
    }

    @Override
    public void onStreamDropped(Session session, Stream stream)
    {
        Log.i(LOG_TAG, "Stream Dropped");
        if(mSubscriber != null)
        {
            mSubscriber = null;
        }
        disconnect(null);
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
        Log.i("LastCheck - SendDrawing - Call from", Call_From);
        Log.i("LastCheck - SendDrawing - User ID", userID);
        Log.i("LastCheck - SendDrawing - Subscriber Big", String.valueOf(subBig));
        Log.i("LastCheck - SendDrawing - Proj", Proj);
        if(Call_From.equals(userID) && subBig)
        {
            mDatabase = FirebaseDatabase.getInstance().getReference().child("Connections").child(Proj).child("Draw_Sub1");
            mDatabase.addListenerForSingleValueEvent(new ValueEventListener()
            {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                {
                    String curr = dataSnapshot.getValue().toString();
                    mDatabase.getRoot().child("Connections").child(Proj).child("Draw_Sub1").setValue(curr + ((CanvasViewClient) fragment.getCanvasSubClient()).pl);
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
                    mDatabase.getRoot().child("Connections").child(Proj).child("Draw_Pub1").setValue(curr + ((CanvasViewClient) fragment.getCanvasPubClient()).pl );
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
                    mDatabase.getRoot().child("Connections").child(Proj).child("Draw_Pub2").setValue(curr + ((CanvasViewClient) fragment.getCanvasPubClient()).pl );
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
                    mDatabase.getRoot().child("Connections").child(Proj).child("Draw_Sub2").setValue(curr + ((CanvasViewClient) fragment.getCanvasSubClient()).pl);
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
        Log.i("LastCheck - ClearCanvas - Call from", Call_From);
        Log.i("LastCheck - ClearCanvas - User ID", userID);
        Log.i("LastCheck - ClearCanvas - Subscriber Big", String.valueOf(subBig));
        Log.i("LastCheck - ClearCanvas - Proj", Proj);
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
        final Handler handler = new Handler();
        Runnable runnable = new Runnable()
        {
            @Override
            public void run()
            {
                Log.i("LastCheck - getDrawingSub - connected", String.valueOf(connected));
                if(connected)
                {
                    Log.i("LastCheck - getDrawingSub - Call from", Call_From);
                    Log.i("LastCheck - getDrawingSub - User ID", userID);
                    Log.i("LastCheck - getDrawingSub - Proj", Proj);
                    if(!Call_From.equals(userID))
                    {
                        mDatabase = FirebaseDatabase.getInstance().getReference().child("Connections").child(Proj).child("Draw_Sub2");
                        mDatabase.addListenerForSingleValueEvent(new ValueEventListener()
                        {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                            {
                                String sub2 = dataSnapshot.getValue().toString();
                                mDatabase = FirebaseDatabase.getInstance().getReference().child("Connections").child(Proj).child("Draw_Pub1");
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
                    }
                    else
                    {
                        mDatabase = FirebaseDatabase.getInstance().getReference().child("Connections").child(Proj).child("Draw_Sub1");
                        mDatabase.addListenerForSingleValueEvent(new ValueEventListener()
                        {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                            {
                                String sub1 = dataSnapshot.getValue().toString();
                                mDatabase = FirebaseDatabase.getInstance().getReference().child("Connections").child(Proj).child("Draw_Pub2");
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
                    }
                    handler.postDelayed(this, 1000);
                }
                else
                {
                    handler.removeCallbacks(this);
                }
            }
        };
        handler.postDelayed(runnable, 1000);

//        if(!Call_From.equals(userID))
//        {
//            mDatabase = FirebaseDatabase.getInstance().getReference().child("Connections").child(Proj).child("Draw_Sub2");
//            mDatabase.addValueEventListener(new ValueEventListener()
//            {
//                @Override
//                public void onDataChange(@NonNull DataSnapshot dataSnapshot)
//                {
//                    String sub2 = dataSnapshot.getValue().toString();
//                    mDatabase = mDatabase.getRoot().child("Connections").child(Proj).child("Draw_Pub1");
//                    mDatabase.addListenerForSingleValueEvent(new ValueEventListener()
//                    {
//                        @Override
//                        public void onDataChange(@NonNull DataSnapshot dataSnapshot)
//                        {
//                            String pub1 = dataSnapshot.getValue().toString();
//                            fbcb2.onCallback2(sub2, pub1);
//                        }
//
//                        @Override
//                        public void onCancelled(@NonNull DatabaseError databaseError)
//                        {
//
//                        }
//                    });
//                }
//
//                @Override
//                public void onCancelled(@NonNull DatabaseError databaseError)
//                {
//
//                }
//            });
//            mDatabase = FirebaseDatabase.getInstance().getReference();
//            mDatabase = mDatabase.getRoot().child("Connections").child(Proj).child("Draw_Pub1");
//            mDatabase.addValueEventListener(new ValueEventListener()
//            {
//                @Override
//                public void onDataChange(@NonNull DataSnapshot dataSnapshot)
//                {
//                    String pub1 = dataSnapshot.getValue().toString();
//                    mDatabase = mDatabase.getRoot().child("Connections").child(Proj).child("Draw_Sub2");
//                    mDatabase.addListenerForSingleValueEvent(new ValueEventListener()
//                    {
//                        @Override
//                        public void onDataChange(@NonNull DataSnapshot dataSnapshot)
//                        {
//                            String sub2 = dataSnapshot.getValue().toString();
//                            fbcb2.onCallback2(pub1, sub2);
//                        }
//
//                        @Override
//                        public void onCancelled(@NonNull DatabaseError databaseError)
//                        {
//
//                        }
//                    });
//                }
//
//                @Override
//                public void onCancelled(@NonNull DatabaseError databaseError)
//                {
//
//                }
//            });
//        }
//        else
//        {
//            mDatabase = FirebaseDatabase.getInstance().getReference();
//            mDatabase = mDatabase.getRoot().child("Connections").child(Proj).child("Draw_Sub1");
//            mDatabase.addValueEventListener(new ValueEventListener()
//            {
//                @Override
//                public void onDataChange(@NonNull DataSnapshot dataSnapshot)
//                {
//                    String sub1 = dataSnapshot.getValue().toString();
//                    mDatabase = mDatabase.getRoot().child("Connections").child(Proj).child("Draw_Pub2");
//                    mDatabase.addListenerForSingleValueEvent(new ValueEventListener()
//                    {
//                        @Override
//                        public void onDataChange(@NonNull DataSnapshot dataSnapshot)
//                        {
//                            String pub2 = dataSnapshot.getValue().toString();
//                            fbcb2.onCallback2(sub1, pub2);
//                        }
//
//                        @Override
//                        public void onCancelled(@NonNull DatabaseError databaseError)
//                        {
//
//                        }
//                    });
//                }
//
//                @Override
//                public void onCancelled(@NonNull DatabaseError databaseError)
//                {
//
//                }
//            });
//            mDatabase = FirebaseDatabase.getInstance().getReference();
//            mDatabase = mDatabase.getRoot().child("Connections").child(Proj).child("Draw_Pub2");
//            mDatabase.addValueEventListener(new ValueEventListener()
//            {
//                @Override
//                public void onDataChange(@NonNull DataSnapshot dataSnapshot)
//                {
//                    String pub2 = dataSnapshot.getValue().toString();
//                    mDatabase = mDatabase.getRoot().child("Connections").child(Proj).child("Draw_Sub1");
//                    mDatabase.addListenerForSingleValueEvent(new ValueEventListener()
//                    {
//                        @Override
//                        public void onDataChange(@NonNull DataSnapshot dataSnapshot)
//                        {
//                            String sub1 = dataSnapshot.getValue().toString();
//                            fbcb2.onCallback2(pub2, sub1);
//                        }
//
//                        @Override
//                        public void onCancelled(@NonNull DatabaseError databaseError)
//                        {
//
//                        }
//                    });
//                }
//
//                @Override
//                public void onCancelled(@NonNull DatabaseError databaseError)
//                {
//
//                }
//            });
//        }
    }

    public void getDrawingPub(FirebaseCallback2 fbcb2)
    {
        final Handler handler = new Handler();
        Runnable runnable = new Runnable()
        {
            @Override
            public void run()
            {
                Log.i("LastCheck - getDrawingPub - connected", String.valueOf(connected));
                if(connected)
                {
                    Log.i("LastCheck - getDrawingPub - Call from", Call_From);
                    Log.i("LastCheck - getDrawingPub - User ID", userID);
                    Log.i("LastCheck - getDrawingPub - Proj", Proj);
                    if(Call_From.equals(userID))
                    {
                        mDatabase = FirebaseDatabase.getInstance().getReference().child("Connections").child(Proj).child("Draw_Sub2");
                        mDatabase.addListenerForSingleValueEvent(new ValueEventListener()
                        {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                            {
                                String sub2 = dataSnapshot.getValue().toString();
                                mDatabase = FirebaseDatabase.getInstance().getReference().child("Connections").child(Proj).child("Draw_Pub1");
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
                    }
                    else
                    {
                        mDatabase = FirebaseDatabase.getInstance().getReference().child("Connections").child(Proj).child("Draw_Sub1");
                        mDatabase.addListenerForSingleValueEvent(new ValueEventListener()
                        {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                            {
                                String sub1 = dataSnapshot.getValue().toString();
                                mDatabase = FirebaseDatabase.getInstance().getReference().child("Connections").child(Proj).child("Draw_Pub2");
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
                    }
                    handler.postDelayed(this, 1000);
                }
                else
                {
                    handler.removeCallbacks(this);
                }
            }
        };
        handler.postDelayed(runnable, 1000);

//        if(Call_From.equals(userID))
//        {
//            mDatabase = FirebaseDatabase.getInstance().getReference();
//            mDatabase = mDatabase.getRoot().child("Connections").child(Proj).child("Draw_Sub2");
//            mDatabase.addValueEventListener(new ValueEventListener()
//            {
//                @Override
//                public void onDataChange(@NonNull DataSnapshot dataSnapshot)
//                {
//                    String sub2 = dataSnapshot.getValue().toString();
//                    mDatabase = mDatabase.getRoot().child("Connections").child(Proj).child("Draw_Pub1");
//                    mDatabase.addListenerForSingleValueEvent(new ValueEventListener()
//                    {
//                        @Override
//                        public void onDataChange(@NonNull DataSnapshot dataSnapshot)
//                        {
//                            String pub1 = dataSnapshot.getValue().toString();
//                            fbcb2.onCallback2(sub2, pub1);
//                        }
//
//                        @Override
//                        public void onCancelled(@NonNull DatabaseError databaseError)
//                        {
//
//                        }
//                    });
//                }
//
//                @Override
//                public void onCancelled(@NonNull DatabaseError databaseError)
//                {
//
//                }
//            });
//            mDatabase = FirebaseDatabase.getInstance().getReference();
//            mDatabase = mDatabase.getRoot().child("Connections").child(Proj).child("Draw_Pub1");
//            mDatabase.addValueEventListener(new ValueEventListener()
//            {
//                @Override
//                public void onDataChange(@NonNull DataSnapshot dataSnapshot)
//                {
//                    String pub1 = dataSnapshot.getValue().toString();
//                    mDatabase = mDatabase.getRoot().child("Connections").child(Proj).child("Draw_Sub2");
//                    mDatabase.addListenerForSingleValueEvent(new ValueEventListener()
//                    {
//                        @Override
//                        public void onDataChange(@NonNull DataSnapshot dataSnapshot)
//                        {
//                            String sub2 = dataSnapshot.getValue().toString();
//                            fbcb2.onCallback2(pub1, sub2);
//                        }
//
//                        @Override
//                        public void onCancelled(@NonNull DatabaseError databaseError)
//                        {
//
//                        }
//                    });
//                }
//
//                @Override
//                public void onCancelled(@NonNull DatabaseError databaseError)
//                {
//
//                }
//            });
//        }
//        else
//        {
//            mDatabase = FirebaseDatabase.getInstance().getReference();
//            mDatabase = mDatabase.getRoot().child("Connections").child(Proj).child("Draw_Sub1");
//            mDatabase.addValueEventListener(new ValueEventListener()
//            {
//                @Override
//                public void onDataChange(@NonNull DataSnapshot dataSnapshot)
//                {
//                    String sub1 = dataSnapshot.getValue().toString();
//                    mDatabase = mDatabase.getRoot().child("Connections").child(Proj).child("Draw_Pub2");
//                    mDatabase.addListenerForSingleValueEvent(new ValueEventListener()
//                    {
//                        @Override
//                        public void onDataChange(@NonNull DataSnapshot dataSnapshot)
//                        {
//                            String pub2 = dataSnapshot.getValue().toString();
//                            fbcb2.onCallback2(sub1, pub2);
//                        }
//
//                        @Override
//                        public void onCancelled(@NonNull DatabaseError databaseError)
//                        {
//
//                        }
//                    });
//                }
//
//                @Override
//                public void onCancelled(@NonNull DatabaseError databaseError)
//                {
//
//                }
//            });
//            mDatabase = FirebaseDatabase.getInstance().getReference();
//            mDatabase = mDatabase.getRoot().child("Connections").child(Proj).child("Draw_Pub2");
//            mDatabase.addValueEventListener(new ValueEventListener()
//            {
//                @Override
//                public void onDataChange(@NonNull DataSnapshot dataSnapshot)
//                {
//                    String pub2 = dataSnapshot.getValue().toString();
//                    mDatabase = mDatabase.getRoot().child("Connections").child(Proj).child("Draw_Sub1");
//                    mDatabase.addListenerForSingleValueEvent(new ValueEventListener()
//                    {
//                        @Override
//                        public void onDataChange(@NonNull DataSnapshot dataSnapshot)
//                        {
//                            String sub1 = dataSnapshot.getValue().toString();
//                            fbcb2.onCallback2(pub2, sub1);
//                        }
//
//                        @Override
//                        public void onCancelled(@NonNull DatabaseError databaseError)
//                        {
//
//                        }
//                    });
//                }
//
//                @Override
//                public void onCancelled(@NonNull DatabaseError databaseError)
//                {
//
//                }
//            });
//        }
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