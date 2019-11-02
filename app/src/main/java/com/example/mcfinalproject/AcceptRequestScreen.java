package com.example.mcfinalproject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class AcceptRequestScreen extends AppCompatActivity
{
    ArrayList<String> Friends_Requests;
    ListView Friends_Requests_List;
    private DatabaseReference mDatabase;
    private String userID;
    private AcceptRequestAdapter acceptRequestAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accept_request);
        Intent intent = getIntent();
        userID = intent.getStringExtra("UserID");
        Friends_Requests = new ArrayList<String>();
        Friends_Requests_List = findViewById(R.id.holder_list1);
        acceptRequestAdapter = new AcceptRequestAdapter(Friends_Requests, userID, getApplicationContext());
        Friends_Requests_List.setAdapter(acceptRequestAdapter);
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_Requests").child(userID);
        mDatabase.addChildEventListener(new ChildEventListener()
        {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s)
            {
                String value = dataSnapshot.getKey();
                if(!value.equals("Num_Requests"))
                {
                    String user = dataSnapshot.getValue().toString();
                    mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(user);
                    mDatabase.addListenerForSingleValueEvent(new ValueEventListener()
                    {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                        {
                            String name = dataSnapshot.child("Username").getValue().toString();
                            Friends_Requests.add(name);
                            acceptRequestAdapter.notifyDataSetChanged();
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
    }

    public void acceptRequest(String acceptFrom, String user)
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
                    if(name.equals(acceptFrom))
                    {
                        mDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_Requests").child(user);
                        mDatabase.addListenerForSingleValueEvent(new ValueEventListener()
                        {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                            {
                                String currFriends = dataSnapshot.child("Num_Requests").getValue().toString();
                                String newNum = String.valueOf(Integer.parseInt(currFriends) - 1);
                                String currReq;
                                String nextReq;
                                for(int j = 1; j <= Integer.parseInt(currFriends); j++)
                                {
                                    currReq = dataSnapshot.child("Request_" + String.valueOf(j)).getValue().toString();
                                    if((j != Integer.parseInt(currFriends)))
                                    {
                                        if(currReq.equals(key))
                                        {
                                            for(int j2 = j; j2 < Integer.parseInt(currFriends); j2++)
                                            {
                                                nextReq = dataSnapshot.child("Request_" + String.valueOf(j2+1)).getValue().toString();
                                                mDatabase.getRoot().child("Friend_Requests").child(user).child("Request_" + String.valueOf(j2)).setValue(nextReq);
                                            }
                                            mDatabase.getRoot().child("Friend_Requests").child(user).child("Request_" + currFriends).removeValue();
                                            break;
                                        }
                                    }
                                    else
                                    {
                                        if(currReq.equals(key))
                                        {
                                            mDatabase.getRoot().child("Friend_Requests").child(user).child("Request_" + currFriends).removeValue();
                                        }
                                    }
                                }
                                mDatabase.getRoot().child("Friend_Requests").child(user).child("Num_Requests").setValue(newNum);
                                mDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_Lists").child(user);
                                mDatabase.addListenerForSingleValueEvent(new ValueEventListener()
                                {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                                    {
                                        String numFriends = dataSnapshot.child("Num_Friends").getValue().toString();
                                        String newFriends = String.valueOf(Integer.parseInt(numFriends) + 1);
                                        mDatabase.getRoot().child("Friend_Lists").child(user).child("Num_Friends").setValue(newFriends);
                                        mDatabase.getRoot().child("Friend_Lists").child(user).child("Friend_" + newFriends).setValue(key);
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError)
                                    {

                                    }
                                });
                                mDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_Lists").child(key);
                                mDatabase.addListenerForSingleValueEvent(new ValueEventListener()
                                {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                                    {
                                        String numFriends = dataSnapshot.child("Num_Friends").getValue().toString();
                                        String newFriends = String.valueOf(Integer.parseInt(numFriends) + 1);
                                        mDatabase.getRoot().child("Friend_Lists").child(key).child("Num_Friends").setValue(newFriends);
                                        mDatabase.getRoot().child("Friend_Lists").child(key).child("Friend_" + newFriends).setValue(user);
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError)
                                    {

                                    }
                                });
                                mDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_Requests").child(key);
                                mDatabase.addListenerForSingleValueEvent(new ValueEventListener()
                                {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                                    {
                                        String currFriends = dataSnapshot.child("Num_Requests").getValue().toString();
                                        String newNum = String.valueOf(Integer.parseInt(currFriends) - 1);
                                        String currReq;
                                        String nextReq;
                                        for(int j = 1; j <= Integer.parseInt(currFriends); j++)
                                        {
                                            currReq = dataSnapshot.child("Request_" + String.valueOf(j)).getValue().toString();
                                            if((j != Integer.parseInt(currFriends)))
                                            {
                                                if(currReq.equals(user))
                                                {
                                                    for(int j2 = j; j2 < Integer.parseInt(currFriends); j2++)
                                                    {
                                                        nextReq = dataSnapshot.child("Request_" + String.valueOf(j2 + 1)).getValue().toString();
                                                        mDatabase.getRoot().child("Friend_Requests").child(key).child("Request_" + String.valueOf(j2)).setValue(nextReq);
                                                    }
                                                    mDatabase.getRoot().child("Friend_Requests").child(key).child("Request_" + currFriends).removeValue();
                                                    mDatabase.getRoot().child("Friend_Requests").child(key).child("Num_Requests").setValue(newNum);
                                                    break;
                                                }
                                            }
                                            else
                                            {
                                                if(currReq.equals(user))
                                                {
                                                    mDatabase.getRoot().child("Friend_Requests").child(key).child("Request_" + currFriends).removeValue();
                                                    mDatabase.getRoot().child("Friend_Requests").child(key).child("Num_Requests").setValue(newNum);
                                                }
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
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });
    }

    public void rejectRequest(String rejectFrom, String user)
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
                    if(name.equals(rejectFrom))
                    {
                        mDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_Requests").child(user);
                        mDatabase.addListenerForSingleValueEvent(new ValueEventListener()
                        {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                            {
                                String currFriends = dataSnapshot.child("Num_Requests").getValue().toString();
                                String newNum = String.valueOf(Integer.parseInt(currFriends) - 1);
                                String currReq;
                                String nextReq;
                                for(int j = 1; j <= Integer.parseInt(currFriends); j++)
                                {
                                    currReq = dataSnapshot.child("Request_" + String.valueOf(j)).getValue().toString();
                                    if((j != Integer.parseInt(currFriends)))
                                    {
                                        if(currReq.equals(key))
                                        {
                                            for(int j2 = j; j2 < Integer.parseInt(currFriends); j2++)
                                            {
                                                nextReq = dataSnapshot.child("Request_" + String.valueOf(j2+1)).getValue().toString();
                                                mDatabase.getRoot().child("Friend_Requests").child(user).child("Request_" + String.valueOf(j2)).setValue(nextReq);
                                            }
                                            mDatabase.getRoot().child("Friend_Requests").child(user).child("Request_" + currFriends).removeValue();
                                            break;
                                        }
                                    }
                                    else
                                    {
                                        if(currReq.equals(key))
                                        {
                                            mDatabase.getRoot().child("Friend_Requests").child(user).child("Request_" + currFriends).removeValue();
                                        }
                                    }
                                }
                                mDatabase.getRoot().child("Friend_Requests").child(user).child("Num_Requests").setValue(newNum);
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
