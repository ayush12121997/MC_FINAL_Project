package com.example.mcfinalproject;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class SendRequestScreen extends AppCompatActivity
{
    private ArrayList<String> Send_Friends_Requests;
    private ListView Send_Friends_Requests_List;
    private DatabaseReference mDatabase;
    private String userID;
    private SendRequestAdapter sendRequestAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_request);
        Intent intent = getIntent();
        userID = intent.getStringExtra("UserID");
        Send_Friends_Requests = new ArrayList<String>();
        Send_Friends_Requests_List = findViewById(R.id.holder_list2);
        sendRequestAdapter = new SendRequestAdapter(Send_Friends_Requests, userID, getApplicationContext());
        Send_Friends_Requests_List.setAdapter(sendRequestAdapter);
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mDatabase.addChildEventListener(new ChildEventListener()
        {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s)
            {
                String value = dataSnapshot.getKey();
                if(!value.equals("Num_Users") && !value.equals("Num_Projects"))
                {
                    String name = dataSnapshot.child("Username").getValue(String.class);
                    String userKey = dataSnapshot.getKey();
                    if(!userKey.equals(userID))
                    {
                        mDatabase = mDatabase.getRoot().child("Friend_Lists").child(userID);
                        mDatabase.addListenerForSingleValueEvent(new ValueEventListener()
                        {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                            {
                                String numFriends = dataSnapshot.child("Num_Friends").getValue().toString();
                                boolean exists = false;
                                for(int i = 1; i <= Integer.parseInt(numFriends); i++)
                                {
                                    String currFriend = dataSnapshot.child("Friend_" + String.valueOf(i)).getValue().toString();
                                    if(currFriend.equals(userKey))
                                    {
                                        exists = true;
                                        break;
                                    }
                                }
                                if(!exists)
                                {
                                    mDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_Requests").child(userKey);
                                    mDatabase.addListenerForSingleValueEvent(new ValueEventListener()
                                    {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                                        {
                                            String num = dataSnapshot.child("Num_Requests").getValue().toString();
                                            boolean exists2 = false;
                                            for(int i = 1; i <= Integer.parseInt(num); i++)
                                            {
                                                String requestFrom = dataSnapshot.child("Request_" + String.valueOf(i)).getValue().toString();
                                                if(requestFrom.equals(userID))
                                                {
                                                    exists2 = true;
                                                    break;
                                                }
                                            }
                                            if(!exists2)
                                            {
                                                Send_Friends_Requests.add(name);
                                                sendRequestAdapter.notifyDataSetChanged();
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
    }

    public void sendRequest(String sendTo, String sendFrom)
    {
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                String num = dataSnapshot.child("Num_Users").getValue().toString();
                for(int i = 0; i < Integer.parseInt(num); i++)
                {
                    String name = dataSnapshot.child("User_" + String.valueOf(i)).child("Username").getValue().toString();
                    String key = "User_" + String.valueOf(i);
                    if(name.equals(sendTo))
                    {
                        mDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_Requests").child(key);
                        mDatabase.addListenerForSingleValueEvent(new ValueEventListener()
                        {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                            {
                                String currFriends = dataSnapshot.child("Num_Requests").getValue().toString();
                                String newNum = String.valueOf(Integer.parseInt(currFriends) + 1);
                                mDatabase.getRoot().child("Friend_Requests").child(key).child("Num_Requests").setValue(newNum);
                                mDatabase.getRoot().child("Friend_Requests").child(key).child("Request_" + newNum).setValue(sendFrom);
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

    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
        Intent intent = new Intent(this, HomeScreen.class);
        intent.putExtra("UserID", userID);
        startActivity(intent);
        finish();
    }
}
