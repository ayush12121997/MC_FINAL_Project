package com.example.mcfinalproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Login extends AppCompatActivity
{
    private EditText Username;
    private EditText Password;
    private DatabaseReference mDatabase;
    private boolean check = true;
    private boolean checkExit = false;
    private static final int RC_VIDEO_APP_PERM = 124;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Username = (EditText) findViewById(R.id.EDT1);
        Password = (EditText) findViewById(R.id.EDT2);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        requestPermissions();
    }

    @AfterPermissionGranted(RC_VIDEO_APP_PERM)
    private void requestPermissions()
    {
        String[] perms = {Manifest.permission.INTERNET, Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO};
        if(EasyPermissions.hasPermissions(this, perms))
        {
            //Nothing here
        }
        else
        {
            EasyPermissions.requestPermissions(this, "This app needs access to your camera and mic to make video calls", RC_VIDEO_APP_PERM, perms);
        }
    }

    public void Autheticate_Login(View view)
    {
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Num_Users");
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                String num = dataSnapshot.getValue().toString();
                if(Integer.parseInt(num) > 0)
                {
                    Authentication_loop(num, Username.getText().toString(), Password.getText().toString());
                }
                else
                {
                    Toast.makeText(getApplicationContext(), "NO USER EXISTS. PLEASE REGISTER", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        String username = Username.getText().toString();
        outState.putString("usernme", username);
        String password = Password.getText().toString();
        outState.putString("password", username);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState)
    {
        super.onRestoreInstanceState(savedInstanceState);
        String username = savedInstanceState.getString("username");
        String password = savedInstanceState.getString("username");
        Username.setText(username);
        Password.setText(password);
    }

    public void Authentication_loop(String num, String user, String pass)
    {
        for(int i = 0; i < Integer.parseInt(num); i++)
        {
            String n = String.valueOf(i);
            mDatabase = FirebaseDatabase.getInstance().getReference().getRoot().child("Users").child("User_" + n);
            mDatabase.addListenerForSingleValueEvent(new ValueEventListener()
            {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                {
                    String USN = dataSnapshot.child("Username").getValue().toString();
                    String PSW = dataSnapshot.child("Password").getValue().toString();
                    if(USN.equals(user) && PSW.equals(pass))
                    {
                        check = false;
                        Intent Intent2 = new Intent(getApplicationContext(), HomeScreen.class);
                        Intent2.putExtra("UserID", "User_" + n);
                        startActivity(Intent2);
                        finish();
                    }
                    if(n.equals(String.valueOf(Integer.parseInt(num) - 1)) && check)
                    {
                        Username.setText("");
                        Password.setText("");
                        Toast.makeText(getApplicationContext(), "PLEASE CHECK YOUR CREDENTIALS", Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError)
                {

                }
            });
        }
    }

    public void Register_User(View view)
    {
        Intent Intent3 = new Intent(getApplicationContext(), RegistrationScreen.class);
        startActivity(Intent3);
        finish();
    }

    @Override
    public void onBackPressed()
    {
        if(checkExit)
        {
            super.onBackPressed();
            finish();
            return;
        }

        this.checkExit = true;
        Toast.makeText(this, "Click again to exit", Toast.LENGTH_SHORT).show();

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