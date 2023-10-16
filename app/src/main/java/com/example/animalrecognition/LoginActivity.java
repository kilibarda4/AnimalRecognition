package com.example.animalrecognition;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import android.os.Bundle;
import android.widget.Toast;

public class LoginActivity extends AppCompatActivity {

    EditText username, password;
    Button login;
    DBHelper DB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        username = findViewById(R.id.username1);
        password = findViewById(R.id.password1);
        login    = findViewById(R.id.login1);
        DB       = new DBHelper(this);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String user = username.getText().toString();
                String pass = password.getText().toString();

                if(TextUtils.isEmpty(user) || TextUtils.isEmpty(pass)) {
                    Toast.makeText(LoginActivity.this, "All fields Required", Toast.LENGTH_SHORT).show();
                }
                else{
                    Boolean checkUserPass = DB.checkUsernamePassword(user, pass);
                    if(checkUserPass) {
                        Toast.makeText(LoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                        startActivity(intent);
                    }else{
                        Toast.makeText(LoginActivity.this, "Invalid Credentials", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

    }
}