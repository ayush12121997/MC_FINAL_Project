package com.example.mcfinalproject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

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
        friends = new ArrayList<>();
        friendsList = findViewById(R.id.Friends_List);
        final ArrayAdapter<String> AdtArr = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, friends);
        friendsList.setAdapter(AdtArr);

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

        updateRecievingCall(new FirebaseCallback2()
        {
            @Override
            public void onCallback(String i)
            {
                if(!i.equals("None"))
                {
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
        friendsList.setOnItemClickListener(this);
    }

    public void onItemClick(AdapterView<?> L, View v, int position, long id)
    {
        String name = friends.get(position);
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
                    mDatabase = FirebaseDatabase.getInstance().getReference();
                    mDatabase = mDatabase.child("Users").child("User_" + num);
                    mDatabase.addListenerForSingleValueEvent(new ValueEventListener()
                    {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                        {
                            String otherName = dataSnapshot.child("Username").getValue().toString();
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
                                otherID = dataSnapshot.getKey();
                                mDatabase = mDatabase.getRoot();
                                mDatabase = mDatabase.child("Call_User").child(otherID);
                                mDatabase.addListenerForSingleValueEvent(new ValueEventListener()
                                {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                                    {
                                        String check = dataSnapshot.getValue().toString();
                                        if(check.equals("None"))
                                        {
                                            mDatabase = mDatabase.getRoot();
                                            mDatabase = mDatabase.child("Recieve_User").child(otherID);
                                            mDatabase.addListenerForSingleValueEvent(new ValueEventListener()
                                            {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                                                {
                                                    String check = dataSnapshot.getValue().toString();
                                                    if(!check.equals("None"))
                                                    {
                                                        ShowBusy();
                                                        return;
                                                    }
                                                    mDatabase.getRoot().child("Call_User").child(userID).setValue(otherID);
                                                    mDatabase.getRoot().child("Recieve_User").child(otherID).setValue(userID);
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
        Toast.makeText(this, "THE USER IS BUSY ON ANOTHER CALL", Toast.LENGTH_LONG).show();
    }

    public void MakeCall(FirebaseCallback fbcb)
    {
        mDatabase = mDatabase.getRoot().child("Users").child("Num_Projects");
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
                    mDatabase = FirebaseDatabase.getInstance().getReference();
                    mDatabase = mDatabase.child("Connections").child("Proj_" + num);
                    mDatabase.addListenerForSingleValueEvent(new ValueEventListener()
                    {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                        {
                            String check = dataSnapshot.child("User_1").getValue().toString();
                            if(check.equals("None"))
                            {
                                if(update)
                                {
                                    return;
                                }
                                update = true;
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

    public void makeCallIntent(int i)
    {
        Intent intent = new Intent();
        intent.setClass(this, MainActivity.class);
        intent.putExtra("CallFrom", userID);
        intent.putExtra("CallTo", otherID);
        intent.putExtra("Proj_ID", "Proj_" + String.valueOf(i));
        intent.putExtra("UserID", userID);
        startActivity(intent);
        finish();
    }

    public void getCallIntent(String name)
    {
        Intent intent = new Intent(getApplicationContext(), IncomingCall.class);
        intent.putExtra("CallTo", userID);
        intent.putExtra("CallerName", name);
        intent.putExtra("UserID", userID);
        startActivity(intent);
        finish();
    }

    private interface FirebaseCallback
    {
        void onCallback(int i);
    }

    public void updateRecievingCall(FirebaseCallback2 fbcb2)
    {
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Recieve_User");
        mDatabase.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                String callFrom = dataSnapshot.child(userID).getValue().toString();
                fbcb2.onCallback(callFrom);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {
            }
        });
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
                String name = dataSnapshot.child(s).child("Username").getValue().toString();
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
        Intent intent = new Intent(getApplicationContext(), SendRequestScreen.class);
        intent.putExtra("UserID", userID);
        startActivity(intent);
        finish();
    }

    public void goToAddFriends(View view)
    {
        Intent intent = new Intent(getApplicationContext(), AcceptRequestScreen.class);
        intent.putExtra("UserID", userID);
        startActivity(intent);
        finish();
    }
}