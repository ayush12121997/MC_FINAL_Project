package com.example.mcfinalproject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.widget.ArrayAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
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
    private ListView Users_List;
    private DatabaseReference mDatabase;
    private ArrayList<String> Friends;
    private String user;
    private String userID;
    private String OtherID = "";
    private boolean update = false;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);

        Intent intent = getIntent();
        user = intent.getStringExtra("Username");
        userID = intent.getStringExtra("UserID");

        Friends = new ArrayList<>();
        Users_List = findViewById(R.id.Other_Users_List);
        final ArrayAdapter<String> AdtArr = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, Friends);
        Users_List.setAdapter(AdtArr);

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
                    if(!name.equals(user))
                    {
                        Friends.add(name);
                        AdtArr.notifyDataSetChanged();
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

        updateRecievingCall(new FirebaseCallback2()
        {
            @Override
            public void onCallback(String i)
            {
                if(i.equals("None"))
                {
                    updateText(i);
                }
                else
                {
                    getCallerName(new FirebaseCallback3()
                    {
                        @Override
                        public void onCallback(String i)
                        {
                            updateText(i);
                            ConnectCallIntent(1);
                        }
                    }, i.substring(5));
                }
            }
        });

        Users_List.setOnItemClickListener(this);
    }

    public void onItemClick(AdapterView<?> L, View v, int position, long id)
    {
        if(position < Integer.parseInt(userID))
        {
            mDatabase = mDatabase.getRoot();
            mDatabase = mDatabase.child("Call_User").child("User_" + String.valueOf(position));
            OtherID = String.valueOf(position);
            mDatabase.addListenerForSingleValueEvent(new ValueEventListener()
            {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                {
                    String check = dataSnapshot.getValue().toString();
                    if(check.equals("None"))
                    {
                        mDatabase = mDatabase.getRoot();
                        mDatabase = mDatabase.child("Recieve_User").child("User_" + String.valueOf(position));
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
                                mDatabase.getRoot().child("Call_User").child("User_" + userID).setValue("User_" + String.valueOf(position));
                                mDatabase.getRoot().child("Recieve_User").child("User_" + String.valueOf(position)).setValue("User_" + userID);
                                MakeCall(new FirebaseCallback()
                                {
                                    @Override
                                    public void onCallback(int i)
                                    {
                                        CreateCallIntent(i);
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
        else
        {
            mDatabase = mDatabase.getRoot();
            mDatabase = mDatabase.child("Call_User").child("User_" + String.valueOf(position + 1));
            OtherID = String.valueOf(position + 1);
            mDatabase.addListenerForSingleValueEvent(new ValueEventListener()
            {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                {
                    String check = dataSnapshot.getValue().toString();
                    if(check.equals("None"))
                    {
                        mDatabase = mDatabase.getRoot();
                        mDatabase = mDatabase.child("Recieve_User").child("User_" + String.valueOf(position + 1));
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
                                mDatabase.getRoot().child("Call_User").child("User_" + userID).setValue("User_" + String.valueOf(position + 1));
                                mDatabase.getRoot().child("Recieve_User").child("User_" + String.valueOf(position + 1)).setValue("User_" + userID);
                                MakeCall(new FirebaseCallback()
                                {
                                    @Override
                                    public void onCallback(int i)
                                    {
                                        CreateCallIntent(i);
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
                                mDatabase.getRoot().child("Connections").child("Proj_" + num).child("User_1").setValue("User_" + userID);
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

    public void CreateCallIntent(int i)
    {
        Intent intent = new Intent();
        intent.setClass(this, MainActivity.class);
        intent.putExtra("CallFrom", "User_" + userID);
        intent.putExtra("CallTo", "User_" + OtherID);
        intent.putExtra("Proj_ID", "Proj_" + String.valueOf(i));
        intent.putExtra("User", user);
        intent.putExtra("UserID", userID);
        startActivity(intent);
        finish();
    }

    public void ConnectCallIntent(int i)
    {
        Intent intent = new Intent(getApplicationContext(),IncomingCall.class);
        intent.setClass(this, MainActivity.class);
        intent.putExtra("CallTo", "User_" + userID);
        intent.putExtra("CallFrom", "User_" + OtherID);
        intent.putExtra("Proj_ID", "Proj_" + String.valueOf(i));
        intent.putExtra("User", user);
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
                String callFrom = dataSnapshot.child("User_" + userID).getValue().toString();
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
                String name = dataSnapshot.child("User_" + s).child("Username").getValue().toString();
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

    public void updateText(String name)
    {
        TextView incoming_Call = (TextView) findViewById(R.id.Incoming_Call);
        if(name.equals("None"))
        {
            incoming_Call.setText("No Incoming Call");
        }
        else
        {
            incoming_Call.setText("Incoming call from " + name);
        }
        Button Connect_Button = (Button) findViewById(R.id.Connect_Button);
        Button Decline_Button = (Button) findViewById(R.id.Decline_Button);
        if(incoming_Call.getText().toString().equals("No Incoming Call"))
        {
            Connect_Button.setEnabled(false);
            Decline_Button.setEnabled(false);
        }
        else
        {
            Connect_Button.setEnabled(true);
            Decline_Button.setEnabled(true);
        }
    }

    public void Start_Video_Call(View view)
    {
        mDatabase = mDatabase.getRoot();
        mDatabase = mDatabase.child("Recieve_User").child("User_" + userID);
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                String callFrom = dataSnapshot.getValue().toString();
                if(!callFrom.equals("None"))
                {
                    OtherID = callFrom.substring(5);
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
                                    if(check.equals(callFrom))
                                    {
                                        mDatabase.child("Proj_" + String.valueOf(i)).child("User_2").setValue("User_" + userID);
                                        ConnectCallIntent(i);
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

    public void Decline_Video_Call(View view)
    {
        mDatabase = mDatabase.getRoot().child("Recieve_User").child("User_" + userID);
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
                                        mDatabase.getRoot().child("Recieve_User").child("User_" + userID).setValue("None");
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
}