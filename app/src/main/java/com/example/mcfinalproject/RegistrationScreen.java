package com.example.mcfinalproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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
        if(!(username.equals("") || pass.equals(""))) {
            mDatabase = mDatabase.child("Users");
            mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    String n = dataSnapshot.child("Num_Users").getValue().toString();
                    boolean check = false;
                    for(int i = 0; i < Integer.parseInt(n); i++)
                    {
                        String usn = dataSnapshot.child("User_"+String.valueOf(i)).child("Username").getValue().toString();
                        if(usn.equals(username))
                        {
                            check = true;
                            break;
                        }
                    }
                    if(check)
                    {
                        Toast.makeText(getApplicationContext(),"Username already taken",Toast.LENGTH_LONG).show();
                        UsernameField.setText("");
                        PasswordField.setText("");
                    }
                    else
                    {
                        update_db(username, pass, n);
                        Intent intent = new Intent(getApplicationContext(), Login.class);
                        startActivity(intent);
                        finish();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
        else{
            Toast.makeText(getApplicationContext(),"Username or password cannot be empty",Toast.LENGTH_LONG).show();
            UsernameField.setText("");
            PasswordField.setText("");
        }
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
        mDatabase.child("Friend_Lists").child("User_" + n).child("Num_Friends").setValue("0");
        mDatabase.child("Friend_Requests").child("User_" + n).child("Num_Requests").setValue("0");
    }
}