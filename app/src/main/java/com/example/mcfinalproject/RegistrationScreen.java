package com.example.mcfinalproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class RegistrationScreen extends AppCompatActivity
{
    private DatabaseReference mDatabase;
    private EditText UsernameField;
    private EditText PasswordField;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration_screen);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        UsernameField = findViewById(R.id.NEWUSERNAME);
        PasswordField = findViewById(R.id.NEWPASSWORD);
    }

    public void register_User(View view)
    {
        String username = UsernameField.getText().toString().trim();
        String pass = PasswordField.getText().toString().trim();
        mDatabase = mDatabase.child("Users");
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                String n = dataSnapshot.child("Num_Users").getValue().toString();
                update_db(username, pass, n);
                Intent intent = new Intent(getApplicationContext(), Login.class);
                startActivity(intent);
                finish();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });
    }

    public void update_db(String user, String pass, String n)
    {
        mDatabase = mDatabase.getRoot();
        String num_users = String.valueOf(Integer.parseInt(n) + 1);
        mDatabase.child("Users").child("Num_Users").setValue(num_users);
        mDatabase.child("Users").child("User_" + n).child("Username").setValue(user);
        mDatabase.child("Users").child("User_" + n).child("Password").setValue(pass);
        mDatabase.child("Call_User").child("User_" + n).setValue("None");
        mDatabase.child("Recieve_User").child("User_" + n).setValue("None");
    }
}
