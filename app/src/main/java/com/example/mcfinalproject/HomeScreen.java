package com.example.mcfinalproject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Handler;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class HomeScreen extends AppCompatActivity implements AdapterView.OnItemClickListener
{
    private ListView friendsList;
    private DatabaseReference mDatabase;
    private ArrayList<String> friends;
    private String userID;
    private String otherID;
    private boolean update;
    private boolean update2;
    private boolean checkExit = false;
    private boolean notReceiving = true;
    private boolean alreadyReceived;
    private ArrayAdapter<String> AdtArr;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);
        Intent intent = getIntent();
        userID = intent.getStringExtra("UserID");
        otherID = "";
        update = false;
        update2 = false;
        notReceiving = true;
        alreadyReceived = false;
        friends = new ArrayList<>();
        friendsList = findViewById(R.id.Friends_List);
        AdtArr = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, friends);
        friendsList.setAdapter(AdtArr);
        Log.i("LastCheck - HS - Initialised User ID", userID);
        Log.i("LastCheck - HS - Initialised User ID", otherID);
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_Lists").child(userID);
        mDatabase.addChildEventListener(new ChildEventListener()
        {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s)
            {
                String key = dataSnapshot.getKey();
                if(!key.equals("Num_Friends"))
                {
                    String friendKey = dataSnapshot.getValue().toString();
                    mDatabase.getRoot().child("Users").child(friendKey).child("Username").addListenerForSingleValueEvent(new ValueEventListener()
                    {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                        {
                            String name = dataSnapshot.getValue(String.class);
                            friends.add(name);
                            AdtArr.notifyDataSetChanged();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError)
                        {

                        }
                    });
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s)
            {
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot)
            {
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s)
            {
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {
            }
        });
        friendsList.setOnItemClickListener(this);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        notReceiving = true;
        alreadyReceived = false;
        updateRecievingCall(new FirebaseCallback2()
        {
            @Override
            public void onCallback(String i)
            {
                Log.i("LastCheck - HS - Not Receiving before getCallerName", String.valueOf(notReceiving));
                if(!i.equals("None"))
                {
                    notReceiving = false;
                    getCallerName(new FirebaseCallback3()
                    {
                        @Override
                        public void onCallback(String name)
                        {
                            getCallIntent(name);
                        }
                    }, i);
                }
            }
        });
    }

    public void onItemClick(AdapterView<?> L, View v, int position, long id)
    {
        String name = friends.get(position);
        Log.i("LastCheck - HS - name searching for in onClick", name);
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Num_Users");
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                String number = dataSnapshot.getValue().toString();
                update2 = false;
                for(int i = 0; i < Integer.parseInt(number); i++)
                {
                    String num = String.valueOf(i);
                    mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("User_" + num);
                    mDatabase.addListenerForSingleValueEvent(new ValueEventListener()
                    {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot3)
                        {
                            Log.i("LastCheck - HS - Checking for user in onClick", "User_" + num);
                            String otherName = dataSnapshot3.child("Username").getValue().toString();
                            Log.i("LastCheck - HS - Name found in onClick", otherName);
                            if(name.equals(otherName))
                            {
                                if(update2)
                                {
                                    return;
                                }
                                else
                                {
                                    update2 = true;
                                }
                                otherID = dataSnapshot3.getKey();
                                Log.i("LastCheck - HS - otherID of name found in onClick", otherID);
                                if(!otherID.contains("P"))
                                {
                                    mDatabase = FirebaseDatabase.getInstance().getReference().child("Call_User").child(otherID);
                                    mDatabase.addListenerForSingleValueEvent(new ValueEventListener()
                                    {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot4)
                                        {
                                            String check = dataSnapshot4.getValue().toString();
                                            Log.i("LastCheck - HS - Check business 1 in onClick", check);
                                            if(check.equals("None"))
                                            {
                                                mDatabase = FirebaseDatabase.getInstance().getReference().child("Recieve_User").child(otherID);
                                                mDatabase.addListenerForSingleValueEvent(new ValueEventListener()
                                                {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot2)
                                                    {
                                                        String check2 = dataSnapshot2.getValue().toString();
                                                        Log.i("LastCheck - HS - Check business 2 in onClick", check2);
                                                        if(!check2.equals("None"))
                                                        {
                                                            ShowBusy();
                                                            return;
                                                        }
                                                        Log.i("LastCheck - HS - userID before setting in onClick", userID);
                                                        Log.i("LastCheck - HS - otherID before setting in onClick", otherID);
                                                        mDatabase.getRoot().child("Call_User").child(userID).setValue(otherID);
                                                        mDatabase.getRoot().child("Recieve_User").child(otherID).setValue(userID);
                                                        notReceiving = false;
                                                        MakeCall(new FirebaseCallback()
                                                        {
                                                            @Override
                                                            public void onCallback(int i)
                                                            {
                                                                makeCallIntent(i);
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
                                                ShowBusy();
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError)
                                        {
                                        }
                                    });
                                }
                            }
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

    public void ShowBusy()
    {
        Toast.makeText(this, "THE USER IS BUSY ON ANOTHER CALL", Toast.LENGTH_SHORT).show();
    }

    public void MakeCall(FirebaseCallback fbcb)
    {
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Num_Projects");
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                String number = dataSnapshot.getValue().toString();
                update = false;
                for(int i = 1; i <= Integer.parseInt(number); i++)
                {
                    String num = String.valueOf(i);
                    Log.i("LastCheck - HS - Checking ProjID before calling", "Proj_" + num);
                    mDatabase = FirebaseDatabase.getInstance().getReference().child("Connections").child("Proj_" + num).child("User_1");
                    mDatabase.addListenerForSingleValueEvent(new ValueEventListener()
                    {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot2)
                        {
                            String check = dataSnapshot2.getValue().toString();
                            Log.i("LastCheck - HS - User 1 in proj before calling", check);
                            if(check.equals("None"))
                            {
                                if(update)
                                {
                                    return;
                                }
                                update = true;
                                Log.i("LastCheck - HS - userID before calling and setting in Proj", userID);
                                mDatabase.getRoot().child("Connections").child("Proj_" + num).child("User_1").setValue(userID);
                                fbcb.onCallback(Integer.parseInt(num));
                            }
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

    private interface FirebaseCallback
    {
        void onCallback(int i);
    }

    public void updateRecievingCall(FirebaseCallback2 fbcb2)
    {
        final Handler handler = new Handler();
        Runnable runnable = new Runnable()
        {
            @Override
            public void run()
            {
                Log.i("LastCheck - HS - Not Receiving in URC", String.valueOf(notReceiving));
                if(notReceiving)
                {
                    Log.i("LastCheck - HS - User ID in URC", userID);
                    mDatabase = FirebaseDatabase.getInstance().getReference().child("Recieve_User").child(userID);
                    mDatabase.addListenerForSingleValueEvent(new ValueEventListener()
                    {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                        {
                            String callFrom = dataSnapshot.getValue().toString();
                            Log.i("LastCheck - HS - Call from in URC", callFrom);
                            if(!callFrom.equals("None"))
                            {
                                notReceiving = false;
                                fbcb2.onCallback(callFrom);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError)
                        {
                        }
                    });
                    handler.postDelayed(this, 750);
                }
                else
                {
                    handler.removeCallbacks(this);
                }
            }
        };
        handler.postDelayed(runnable, 750);
    }

    private interface FirebaseCallback2
    {
        void onCallback(String i);
    }

    public void getCallerName(FirebaseCallback3 fbcb3, String s)
    {
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                Log.i("LastCheck - HS - Caller ID in getCallerName", s);
                String name = dataSnapshot.child(s).child("Username").getValue().toString();
                Log.i("LastCheck - HS - Caller name in getCallerName", name);
                fbcb3.onCallback(name);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });
    }

    private interface FirebaseCallback3
    {
        void onCallback(String i);
    }

    public void goToDiscoverFriends(View view)
    {
        if(!alreadyReceived)
        {
            Log.i("LastCheck - HS - userID before goToDiscoverFriends", userID);
            Log.i("LastCheck - HS - Not receiving before goToDiscoverFriends", String.valueOf(notReceiving));
            notReceiving = false;
            alreadyReceived = true;
            Intent intent = new Intent(getApplicationContext(), SendRequestScreen.class);
            intent.putExtra("UserID", userID);
            startActivity(intent);
            finish();
        }
    }

    public void goToAddFriends(View view)
    {
        if(!alreadyReceived)
        {
            Log.i("LastCheck - HS - userID before goToAddFriends", userID);
            Log.i("LastCheck - HS - Not receiving before goToAddFriends", String.valueOf(notReceiving));
            notReceiving = false;
            alreadyReceived = true;
            Intent intent = new Intent(getApplicationContext(), AcceptRequestScreen.class);
            intent.putExtra("UserID", userID);
            startActivity(intent);
            finish();
        }
    }

    public void makeCallIntent(int i)
    {
        if(!alreadyReceived)
        {
            Log.i("LastCheck - HS - userID before making call", userID);
            Log.i("LastCheck - HS - otherID before making call", otherID);
            Log.i("LastCheck - HS - Call from before making call", userID);
            Log.i("LastCheck - HS - Call to before making call", otherID);
            Log.i("LastCheck - HS - Proj ID before making call", "Proj_" + String.valueOf(i));
            Log.i("LastCheck - HS - Not receiving before making Call", String.valueOf(notReceiving));
            notReceiving = false;
            alreadyReceived = true;
            Intent intent = new Intent();
            intent.setClass(this, MainActivity.class);
            intent.putExtra("CallFrom", userID);
            intent.putExtra("CallTo", otherID);
            intent.putExtra("Proj_ID", "Proj_" + String.valueOf(i));
            intent.putExtra("UserID", userID);
            startActivity(intent);
        }
    }

    public void getCallIntent(String name)
    {
        if(!alreadyReceived)
        {
            Log.i("LastCheck - HS - Not receiving in getCallIntent", String.valueOf(notReceiving));
            Log.i("LastCheck - HS - Call to in getCallIntent", userID);
            Log.i("LastCheck - HS - User ID in getCallIntent", userID);
            Log.i("LastCheck - HS - Caller name in getCallIntent", name);
            alreadyReceived = true;
            notReceiving = false;
            Intent intent = new Intent(getApplicationContext(), IncomingCall.class);
            intent.putExtra("CallTo", userID);
            intent.putExtra("CallerName", name);
            intent.putExtra("UserID", userID);
            startActivity(intent);
        }
    }

    @Override
    public void onBackPressed()
    {
        if(checkExit)
        {
            super.onBackPressed();
            Intent intent = new Intent(getApplicationContext(), Login.class);
            startActivity(intent);
            finish();
            return;
        }

        this.checkExit = true;
        Toast.makeText(this, "Click again to logout", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                checkExit = false;
            }
        }, 2000);
    }
}