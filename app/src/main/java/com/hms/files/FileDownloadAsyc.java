package com.hms.files;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.support.v4.BuildConfig;
import android.support.v4.content.FileProvider;
import android.widget.Toast;

import com.hms.global.PmsGlobal;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.List;

/**
 * Created by hgsky on 2017-11-23.
 */

public class FileDownloadAsyc extends AsyncTask<String, Integer, String>
{
    private ProgressDialog mDlg;
    private Context mContext;

    public FileDownloadAsyc(Context context) {
        mContext = context;
    }

    /**
     * doInBackground() 가 실행되기 이전에 호출되는 함수로 여기에서는
     * ProgressDialog 객체를 생성한다. 그리고 show 함수를 통해 다이얼로그를
     * 화면에 띄운다.
     */
    @Override
    protected void onPreExecute() {
        mDlg = new ProgressDialog(mContext);
        mDlg.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mDlg.setMessage("Downloading file, Please wait...");
        mDlg.setIndeterminate(false);
        mDlg.setMax(100);
//            mDlg.setCancelable(true);
        mDlg.show();

        super.onPreExecute();
    }

    @Override
    protected String doInBackground(String... paramVarArgs) {

        URL myFileUrl = null;
        FileOutputStream fos = null;
        InputStream is = null;
        URLConnection conn = null;

        BufferedInputStream bufIns = null;
        String dirPath = "";
        try
        {
            String urlStr = paramVarArgs[0];

            myFileUrl = new URL(urlStr);
            conn = (URLConnection) myFileUrl.openConnection();
            conn.setDoInput(true);
            conn.connect();


//                int totCnt = is.available();
            int totCnt = conn.getContentLength();

            is = new URL(urlStr).openStream();
            dirPath = PmsGlobal.SAVE_DIR;               //다운로드 받을 경로

            File tempFile = new File(dirPath + paramVarArgs[1]);

            fos = new FileOutputStream(tempFile);
            byte[] buf = new byte[1024];

            int read = 0;
            long total = 0;                 //다운받은 파일 량
            while((read = is.read()) != -1){
                fos.write(read);
                total += read;

                int processCnt = (int)(total  * 100 / totCnt);          //다운로드 진행율 계산

                //작업이 진행되면서 호출하며 화면의 업그레이드를 담당하게 된다.
                publishProgress(processCnt);
            }

            fos.flush();;
            fos.close();
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
        finally {
            if(fos != null){
                try {
                    fos.close();
                }
                catch(Exception foEx)
                {
                    foEx.printStackTrace();
                }
            }

            if(bufIns != null){
                try {
                    bufIns.close();
                }
                catch(Exception isEx)
                {
                    isEx.printStackTrace();
                }
            }
        }

        return dirPath + paramVarArgs[1];
    }

    @Override
    protected void onProgressUpdate(Integer... param) {
        super.onProgressUpdate(param);
        mDlg.setProgress(param[0]);
    }

    @Override
    protected void onPostExecute(String paramVarArgs) {

        super.onPostExecute(paramVarArgs);

        mDlg.dismiss();
//        Toast.makeText(mContext, "1 : " + paramVarArgs, Toast.LENGTH_LONG).show();
        //다운로드 완료후 인텐트
        Intent fileLinkIntent = new Intent(Intent.ACTION_VIEW);
        try
        {
            fileLinkIntent.addCategory(Intent.CATEGORY_DEFAULT);

            Uri fileUri = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {

//                fileUri = FileProvider.getUriForFile(mContext, "com.hms.pms.provider" , new File(paramVarArgs));

//                final PackageManager pm = mContext.getPackageManager();     //패키지 매니저에서 설치된 앱 리스트 가져오기
//                List<ApplicationInfo> list = pm.getInstalledApplications(0);

//                String appName = "";    //앱 이름
//                String packageName = "";    //패키지이름
//                Drawable iconDrawable = null;       // 앱아이콘
//                for(ApplicationInfo applicationInfo : list){
//                    appName = String.valueOf(applicationInfo.loadLabel(pm));
//                    packageName = applicationInfo.packageName;
//                    Toast.makeText(mContext.getApplicationContext(), "packageName : " + packageName, Toast.LENGTH_LONG).show();
//                    iconDrawable = applicationInfo.loadIcon(pm);
//                }
//                Toast.makeText(mContext.getApplicationContext(), "packageName : " +  mContext.getPackageName(), Toast.LENGTH_LONG).show();
//                fileUri = FileProvider.getUriForFile(mContext, mContext.getPackageName(), new File(paramVarArgs));

                final String authFile = com.hms.pms.BuildConfig.APPLICATION_ID + ".provider";
           //     final String authFile = mContext.getApplicationContext().getPackageName() + ".provider";
                fileUri = FileProvider.getUriForFile(mContext, authFile, new File(paramVarArgs));

                fileLinkIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                fileLinkIntent.setDataAndType(fileUri, "application/vnd.ms-excel");
//                fileLinkIntent.setDataAndType(fileUri, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
//                fileLinkIntent.setDataAndType(fileUri, "application/excel");
                mContext.startActivity(fileLinkIntent);
            } else {
//                intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(mPath)));
                File file = new File(paramVarArgs);
                fileUri = Uri.fromFile(file);
                fileLinkIntent.setDataAndType(fileUri, "application/vnd.ms-excel");
//                fileLinkIntent.setDataAndType(fileUri, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
//                fileLinkIntent.setDataAndType(fileUri, "application/excel");
                mContext.startActivity(fileLinkIntent);
            }

//            File file = new File(paramVarArgs);
//            Uri fileUri = Uri.fromFile(file);
//            fileLinkIntent.setDataAndType(fileUri, "application/vnd.ms-powerpoint");
////            fileLinkIntent.setDataAndType(fileUri, URLConnection.guessContentTypeFromName(paramVarArgs));
//            PackageManager pm = mContext.getPackageManager();
//            List<ResolveInfo> list = pm.queryIntentActivities(fileLinkIntent, PackageManager.GET_META_DATA);
//            if(list.size() == 0){
//                Toast.makeText(mContext, "확인할 수 있는 앱이 설치되지 않았습니다.", Toast.LENGTH_LONG).show();
//            }
//            else{
//                mContext.startActivity(fileLinkIntent);
//            }
        }
        catch(Exception ex){
            ex.printStackTrace();
            Toast.makeText(mContext, "확인할 수 있는 앱이 설치되지 않았습니다.", Toast.LENGTH_LONG).show();
        }

    }
}

