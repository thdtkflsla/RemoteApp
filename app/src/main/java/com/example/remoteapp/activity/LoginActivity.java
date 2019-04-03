package com.example.remoteapp.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import com.example.remoteapp.app.AppConfig;
import com.example.remoteapp.app.AppController;
import com.example.remoteapp.helper.SQLiteHandler;
import com.example.remoteapp.helper.SessionManager;
import com.example.remoteapp.R;
import com.example.remoteapp.activity.MainActivity;
import com.example.remoteapp.app.AppConfig;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = RegisterActivity.class.getSimpleName();
    private Button btnLogin;
    private Button btnLinkToRegister;
    private EditText inputEmail;
    private EditText inputPassword;
    private ProgressDialog pDialog;
    private SessionManager session;
    private SQLiteHandler db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        inputEmail = (EditText) findViewById(R.id.email);
        inputPassword = (EditText) findViewById(R.id.password);
        btnLogin = (Button)findViewById(R.id.btnLogin);
        btnLinkToRegister = (Button)findViewById(R.id.btnLinkToRegisterScreen);

        //진행 대화 상자
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        //SQLite 데이터베이스 핸들러
        db = new SQLiteHandler(getApplicationContext());

        //세션 관리자
        session = new SessionManager(getApplicationContext());

        //로그인 확인
        if(session.isLoggedIn()){
            //로그인 되어있는 경우 메인으로 이동
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }

        //로그인 버튼 클릭 이벤트
        btnLogin.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                String email = inputEmail.getText().toString().trim();
                String password = inputPassword.getText().toString().trim();

                // 양식확인
                if (!email.isEmpty() && !password.isEmpty()) {
                    // login user
                    checkLogin(email, password);
                } else {

                    Toast.makeText(getApplicationContext(),
                            "빈칸을 입력하세요", Toast.LENGTH_LONG)
                            .show();
                }
            }

        });

        //회원가입 링크 클릭
        btnLinkToRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), RegisterActivity.class);
                startActivity(i);
                finish();
            }
        });
    }

    //db 로그인 세부사항 확인
    private void checkLogin(final String email, final String password){
        String tag_string_req = "req_login";

        pDialog.setMessage("로그인 중...");
        showDialog();

        StringRequest strReq = new StringRequest(Method.POST, AppConfig.URL_LOGIN, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Login Response : " + response.toString());
                hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");

                    //json오류 확인
                    if (!error) {
                        //성공적으로 로그인 했을 때 로그인 세션 생성
                        session.setLogin(true);

                        //사용자를 SQLite에 저장
                        String uid = jObj.getString("uid");

                        JSONObject user = jObj.getJSONObject("user");
                        String name = user.getString("name");
                        String email = user.getString("email");
                        String created_at = user
                                .getString("created_at");

                        //사용자 테이블에 행 삽입
                        db.addUser(name, email, uid, created_at);

                        //주요 활동 시작
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        //로그인 에러, 메세지 가져오기
                        String errorMsg = jObj.getString("error_msg");
                        Toast.makeText(getApplicationContext(), errorMsg, Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    //json에러
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Login Error :" + error.getMessage());
                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                hideDialog();
            }
        }){
            @Override
            protected Map<String, String> getParams() {
                // 로그인 URL에 매개 변수 게시
                Map<String, String> params = new HashMap<String, String>();
                params.put("email", email);
                params.put("password", password);

                return params;
            }
        };
        //요청 대기열에 요청 추가 중
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    private void showDialog(){
        if(!pDialog.isShowing())
            pDialog.show();
    }
    private  void hideDialog(){
        if(pDialog.isShowing())
            pDialog.show();
    }
}
