package com.hms.pms;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.hms.files.FileDownloadAsyc;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private WebView mWebview;

    private String mUrlStr = BuildConfig.URL_ADDR;
    private String downUrl = BuildConfig.DOWN_URL;

    private final long FINISH_INTERAL_TIME  = 2000;             //2초
    private long backPressTime = 0;

    private Handler mHandler = new Handler();

    public static MainActivity instance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        grantExternalStoragePermission();

        //웹뷰 세팅
        mWebview = (WebView) findViewById(R.id.webPMS);
        WebSettings webSetting = mWebview.getSettings();        //세부 세팅등록
//        webSetting.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);       //캐시사용
        webSetting.setJavaScriptEnabled(true);                  //자바스크립트 사용 허용

        //시스템 설정의 글자크기에 상관없이 WebView 글자크기 유지
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH){
            webSetting.setTextZoom(100);
        }
        mWebview.addJavascriptInterface(new PMSJavascript(), "android");        //자바스크립트 연결

        mWebview.setWebViewClient(new WebViewClient());     //클릭시 새창안뜨게

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        String pushOpen = "";
        if( bundle != null){
            if(bundle.getString("pushOpen") != null) {
                pushOpen = bundle.getString("pushOpen");
            }
        }

        if(pushOpen == null || pushOpen.equals("")){
            mWebview.loadUrl(mUrlStr);
        }
        else  if(pushOpen != null && pushOpen.equals("Y")){
            mWebview.loadUrl(mUrlStr + "?pushOpen=Y");
        }

//        mWebview.loadUrl("javascript:recToken('" + BuildConfig.COM_CD + "', '" + FirebaseInstanceId.getInstance().getToken() + "')");

        Log.v("TT", "Refreshed Token2 : " + FirebaseInstanceId.getInstance().getToken());

        //이렇게 ALL 추가 하면 이 디바이스는 ALL을 구독한다는 얘기가 된다.
//        FirebaseMessaging.getInstance().subscribeToTopic("ALL");
    }

    //Back 버튼 클릭시
    @Override
    public void onBackPressed() {

        if(backPressTime == 0){     //Back 버튼 1번 클릭시 뒤로가기
            backPressTime = System.currentTimeMillis();
            mWebview.goBack();
        }
        else{           //Back 버튼 2번 클릭시 종료
            int diffSec = (int)(System.currentTimeMillis() - backPressTime);
            if(diffSec > FINISH_INTERAL_TIME){
                backPressTime = 0;
            }
            else{
                moveTaskToBack(true);
                finish();
                android.os.Process.killProcess(android.os.Process.myPid());
            }
        }

    }

    //권한 체크(마시멜로 이상 버전에서는 필요)
    private boolean grantExternalStoragePermission(){
        if(Build.VERSION.SDK_INT >= 23){
            if(checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                    && checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                    && checkSelfPermission(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED){

                return true;
            }
            else{
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_PHONE_STATE}, 1);
                return false;
            }
        }
        else{

            return true;
        }

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(Build.VERSION.SDK_INT >= 23){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Log.v("MAINACT", "permissions : " + permissions[0] + " was " + grantResults[0]);
            }
        }
    }

    public static MainActivity getInstance()
    {
        return instance;
    }

    public class PMSJavascript {

        public PMSJavascript() {

        }

        @android.webkit.JavascriptInterface     //최근에는 이 어노테이션을 붙여줘야 동작한다.
        public void closeApp() {            //종료 버튼 클릭시
            // 네트워크를 통한 작업임으로 백그라운드 스레드를 써서 작업해야한다.
            // 또한, 백그라운드 스레드는 직접 메인 뷰에 접근해 제어할 수 없음으로
            // 핸들러를 통해서 작업해야하는데
            mHandler.post(new Runnable() {

                @Override
                public void run() {
//                    ActivityCompat.finishAffinity(this);
                    moveTaskToBack(true);
                    finish();
                    android.os.Process.killProcess(android.os.Process.myPid());
                }
            });

        }

        @android.webkit.JavascriptInterface
        public void downloadFile(final String urlStr, final String fileName, final String paramString3) {       //PCS 엑셀파일 다운로드

            if(urlStr != null && !urlStr.equals("")) {
                mHandler.postDelayed(new Runnable() {

                    @Override
                    public void run() {

//                        Toast.makeText(getApplicationContext(), urlStr, Toast.LENGTH_LONG).show();
                        FileDownloadAsyc downAsync = new FileDownloadAsyc(MainActivity.this);
                        downAsync.execute(urlStr, fileName);
                    }
                }, 300L);
            }
        }

        @android.webkit.JavascriptInterface
        public void downloadExcel(final String urlStr, final String fileName) {       //ESS 엑셀파일 다운로드

            if(urlStr != null && !urlStr.equals("")) {
                mHandler.postDelayed(new Runnable() {

                    @Override
                    public void run() {

//                        Toast.makeText(getApplicationContext(), urlStr, Toast.LENGTH_LONG).show();
                        FileDownloadAsyc downAsync = new FileDownloadAsyc(MainActivity.this);
                        downAsync.execute(downUrl + urlStr, fileName);
                    }
                }, 300L);
            }
        } //downloadExcel

        /**
         * 웹에 회사코드, PUSH TOKEN 정보를 보내준다.
         */
        @android.webkit.JavascriptInterface
        public void callAndroidInfo(){
            mHandler.post(new Runnable() {

                @Override
                public void run() {
                    String phoneNum = "";
                    String deviceId = "";

                    if(Build.VERSION.SDK_INT >= 23){
                        if(checkSelfPermission(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED){
                            TelephonyManager tm = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
                            phoneNum = tm.getLine1Number();
                            deviceId = tm.getDeviceId();
                            if (deviceId == null)
                                deviceId = android.provider.Settings.Secure.getString(
                                        getApplicationContext().getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
                        }
                    }
                    else {
                        TelephonyManager tm = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
                        phoneNum = tm.getLine1Number();
                        deviceId = tm.getDeviceId();
                    }

                    if(phoneNum == null){
                        phoneNum = "";
                    }
                    else{
                        phoneNum = phoneNum.replace("+82", "0");

                        int numLn = phoneNum.length();
                        if(numLn == 10){
                            String num1 = phoneNum.substring(0, 3);
                            String num2 = phoneNum.substring(3, 6);
                            String num3 = phoneNum.substring(6, 10);

                            phoneNum = num1 + "-" + num2 + "-" + num3;
                        }
                        else if(numLn == 11){
                            String num1 = phoneNum.substring(0, 3);
                            String num2 = phoneNum.substring(3, 7);
                            String num3 = phoneNum.substring(7, 11);

                            phoneNum = num1 + "-" + num2 + "-" + num3;
                        }
                    }

                    if(deviceId == null){
                        deviceId = "";
                    }

                    //저장
                    SharedPreferences pref = getSharedPreferences("ESS" + BuildConfig.COM_CD, Activity.MODE_PRIVATE);
                    SharedPreferences.Editor editor = pref.edit();
                    editor.putString("pnum", phoneNum);
                    editor.putString("deviceId", deviceId);
                    editor.commit();

                    mWebview.loadUrl("javascript:getAndroidInfo('" + BuildConfig.COM_CD
                            + "', '" + FirebaseInstanceId.getInstance().getToken()
                            + "', '" + phoneNum
                            + "', '" + deviceId + "')");
                }
            });
        }

        /**
         * 정보 요청을 받아서 자바스크립트 호출
         * stype
         * 1)token : push 토큰값 요청
         * 2)pnum : 핸드폰 번호 요청
         * 3)deviceId : 디바이스 정보
         * callFuc : 자바스크립트 호출할 함수
         */
        @android.webkit.JavascriptInterface
        public void callAndroidInfoReturn(final String stype, final String callFuc){

            mHandler.post(new Runnable() {

                @Override
                public void run() {

                    //토큰정보 넘기기
                    if(stype != null && stype.equals("token")){
                        mWebview.loadUrl("javascript:" + callFuc + "('" + FirebaseInstanceId.getInstance().getToken() + "')");
                    }
                    else if(stype != null && !stype.equals("token") && !stype.equals("")){
                        SharedPreferences pref = getSharedPreferences("ESS" + BuildConfig.COM_CD, Activity.MODE_PRIVATE);
                        String valueStr = pref.getString(stype, "");
                        mWebview.loadUrl("javascript:" + callFuc + "('" + valueStr + "')");
                    }
                }
            });
        }
    }
}


