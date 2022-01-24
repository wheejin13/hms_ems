package com.hms.pms;

import android.telephony.TelephonyManager;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import java.io.IOException;

/**
 * Created by hgsky on 2018-07-01.
 */

public class EmsFirebaseInstanceIdService extends FirebaseInstanceIdService{

    private static final String TAG = "MainIns";
    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     *
     * 이 클래스는 어플리케이션이 처음 설치되거나 가끔 토큰이 갱신될 때 onTokenRefresh 메서드를 통해 FCM Token을 하나 생성이 되어집니다.
     * 그리고 이 토큰이 생성되거나 갱신된 경우 서버에 보내주는 메서드인 sendRegistrationToServer를 호출하게 됩니다.
     * 이 FCM 토큰을 통해 해당 기기로 서버에서 푸시 메세지를 보내게 해주는 고유 값입니다.
     * 이 부분은 향후에 서버 측 구현 시 FCM 토큰이 갱신되는 부분을 개발해야 합니다.
     *
     */
    // [START refresh_token]
    @Override
    public void onTokenRefresh() {
//        super.onTokenRefresh();
        Log.v(TAG, "onTokenRefresh() 호출됨.");
        /**
         * 어플리케이션을 처음 설치할 때나 FCM 토큰을 갱신할 경우엔 Sharedpreference에 토큰 값을 저장해두는 게 좋습니다.
         * 1. 갱신된 토큰이 정상인지 체크
         * 2. Sharedpreference에 저장된 FCM 토큰 값이 없는 경우에만 저장.
         * 3. 만약 로그인 상태일 경우 Sharedpreference에 갱신된 토큰을 저장하고 서버로 갱신된 토큰을 전송하는 메서드를 호출.
         *
         * 현재 토큰을 검색하려면 FirebaseInstanceId.getInstance().getToken()을 호출합니다. 토큰이 아직 생성되지 않은 경우 null이 반환됩니다.
         */
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();

        Log.v(TAG, "Refreshed Token1 : " + refreshedToken);
//        if (isValidString(refreshedToken)) { //토큰이 널이거나 빈 문자열이 아닌 경우
//            if (!isValidString(getSharedPreferencesStringData(getApplicationContext(), AD_FCM_TOKEN))) { //토큰에 데이터가 없는 경우에만 저장
//                setSharedPreferencesStringData(getApplicationContext(), AD_FCM_TOKEN, refreshedToken);
//            }
//
//            if (isValidString(getSharedPreferencesStringData(getApplicationContext(), AD_LOGIN_ID))) { //로그인 상태일 경우에는 서버로 보낸다.
//                if (!refreshedToken.equals(getSharedPreferencesStringData(getApplicationContext(), AD_FCM_TOKEN))) { //기존에 저장된 토큰과 비교하여 다를 경우에만 서버 업데이트
//                    setSharedPreferencesStringData(getApplicationContext(), AD_FCM_TOKEN, refreshedToken);
//                    sendRegistrationToServer(refreshedToken);
//                }
//            }
//        }


        sendRegistrationToServer(refreshedToken);
    }
    // [END refresh_token]

    /**
     * Persist token to third-party servers.
     *
     * Modify this method to associate the user's FCM InstanceID token with any server-side account
     * maintained by your application.
     *
     * @param token The new token.
     */
    private void sendRegistrationToServer(String token) {
        //FCM 토큰 갱신
//        OkHttpClient tokenClient = new OkHttpClient();
//        RequestBody tokenBody = new FormBody.Builder().add("COM_CD",BuildConfig.COM_CD.toString()).add("Token", token).build();
//
//        Request request = new Request.Builder().url("").post(tokenBody).build();
//
//        try{
//            tokenClient.newCall(request).execute();
//        }
//        catch (IOException e){
//            e.printStackTrace();
//        }
    }
}
