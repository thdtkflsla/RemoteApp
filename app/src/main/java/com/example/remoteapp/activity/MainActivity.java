package com.example.remoteapp.activity;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import com.example.remoteapp.helper.SQLiteHandler;
import com.example.remoteapp.helper.SessionManager;

import java.util.HashMap;


import android.content.Intent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.example.remoteapp.R;

public class MainActivity extends AppCompatActivity {

    private TextView txtName;
    private TextView txtEmail;
    //창문
    private Button btnwindow;
    private String chkwindow;
    //도어락
    private Button btndoor;
    private String chkdoor;
    //가스벨브
    private Button btngas;
    private String chkgas;
    //보일러
    private Button btnboiler;
    private String chkboiler;
    //로그아웃
    private Button btnLogout;


    private SQLiteHandler db;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtName = (TextView) findViewById(R.id.name);
        txtEmail = (TextView) findViewById(R.id.email);
        btnwindow = (Button)findViewById(R.id.btnwindow);
        btndoor = (Button)findViewById(R.id.btndoor);
        btngas = (Button)findViewById(R.id.btngas);
        btnboiler = (Button)findViewById(R.id.btnboiler);
        btnLogout = (Button) findViewById(R.id.btnLogout);
        chkwindow = "off";
        chkdoor = "off";
        chkgas = "off";
        chkboiler = "off";

        if(chkwindow == "off"){
            btnwindow.setBackgroundColor(Color.GRAY);
        }

        //SqLite 데이터베이스 핸들러
        db = new SQLiteHandler(getApplicationContext());

        //세션 관리
        session = new SessionManager(getApplicationContext());

        if (!session.isLoggedIn()) {
            logoutUser();
        }

        //sqlite에서 사용자 세부 정보 가져 오기
        HashMap<String, String> user = db.getUserDetails();

        String name = user.get("name");
        String email = user.get("email");
        //화면에 사용자 세부 정보 표시
        txtName.setText(name);
        txtEmail.setText(email);

        //창문버튼 이벤트
        btnwindow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(chkwindow == "off"){
                    btnwindow.setBackgroundColor(Color.GRAY);
                    chkwindow = "on";
                } else if(chkwindow == "on"){
                    btnwindow.setBackgroundColor(Color.GREEN);
                    chkwindow = "off";
                }
            }
        });

        //로그아웃 버튼 이벤트
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logoutUser();
            }
        });
    }
    //sqlite 사용자 테이블에서 사용자 데이터를 지움
    private void logoutUser(){
        session.setLogin(false);
        db.deleteUsers();

        //로그인 화면으로 이동
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

}
