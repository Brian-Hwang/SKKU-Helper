package edu.skku.skkuhelper;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {
    public static final String EXT_Token = "token";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button btnLogin = findViewById(R.id.buttonLogin);
        EditText editTextToken = findViewById(R.id.editTextPassword);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //로그인 성공하여 메인페이지로 이동
                String token = editTextToken.getText().toString();
                Intent intent = new Intent(MainActivity.this, Home_page.class);
                startActivity(intent);
            }

        });
    }
}