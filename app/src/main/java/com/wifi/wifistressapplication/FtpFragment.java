package com.wifi.wifistressapplication;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ToggleButton;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by kirt-server on 2016/2/1.
 */
public class FtpFragment extends Fragment {
    private final String TAG = "FtpFragment";
    private final int BUFFER_SIZE = 4096;
    private final int DOWNLOAD_COMPLETE = 0;
    private final int DOWNLOAD_FAILED = 1;
    private EditText destAddress;
    private EditText loginName;
    private EditText loginPwd;
    private EditText destPath;
    private EditText testRounds;
    private ToggleButton mBtn;
    private long startTime;
    private long endTime;
    private double avgBandwidth = 0;
    private boolean testStarted = false;
    private int totalRounds = 0;
    private String ftpUrl;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.ftp_layout, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Activity activity = getActivity();
        destAddress = (EditText) activity.findViewById(R.id.dest_address);
        loginName = (EditText) activity.findViewById(R.id.user);
        loginPwd = (EditText) activity.findViewById(R.id.password);
        destPath = (EditText) activity.findViewById(R.id.file_path);
        testRounds = (EditText) activity.findViewById(R.id.rounds);
        mBtn = (ToggleButton) activity.findViewById(R.id.button);

        mBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Log.i(TAG, "Test Started");
                    totalRounds = Integer.parseInt(testRounds.getText().toString());
                    testStarted = true;
                    String dest = String.valueOf(destAddress.getText());
                    String user = String.valueOf(loginName.getText());
                    String pwd = String.valueOf(loginPwd.getText());
                    String targetPath = String.valueOf(destPath.getText());
                    ftpUrl = String.format("ftp://%s:%s@%s/%s", user, pwd, dest, targetPath);
                    Log.i(TAG, "URL: " + ftpUrl);
                    new DownloadFtpTask().execute(ftpUrl);
                } else {
                    Log.i(TAG, "Test Stopped");
                    testStarted = false;
                }
            }
        });

        /*
        mBtn = (Button) activity.findViewById(R.id.button);

        View.OnClickListener buttonClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String dest = String.valueOf(destAddress.getText());
                String user = String.valueOf(loginName.getText());
                String pwd = String.valueOf(loginPwd.getText());
                String targetPath = String.valueOf(destPath.getText());
                String ftpUrl = "ftp://%s:%s@%s/%s";
                ftpUrl = String.format(ftpUrl, user, pwd, dest, targetPath);
                Log.i(TAG, "URL: " + ftpUrl);
                String localPath = "/sdcard/Download/" + getFileName(ftpUrl);
                new DownloadFtpTask().execute(ftpUrl);
            }
        };
        mBtn.setOnClickListener(buttonClickListener);
        */
    }

    private class DownloadFtpTask extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... params) {
            int count = params.length;
            boolean res = false;
            try {
                for (int i = 0; i < count; i++) {
                    res = downloadUrl(params[i]);
                }
                return res;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            Message msg = new Message();
            if (result) {
                msg.what = DOWNLOAD_COMPLETE;
                mHandler.sendMessageDelayed(msg, 10000);
            } else {
                msg.what = DOWNLOAD_FAILED;
                mHandler.sendMessage(msg);
            }
        }
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case DOWNLOAD_COMPLETE:
                    Log.i(TAG, "DOWNLOAD_COMPLETE received!");
                    totalRounds--;
                    if (testStarted && totalRounds > 0) {
                        new DownloadFtpTask().execute(ftpUrl);
                    } else {
                        mBtn.setChecked(false);
                    }
                    break;
                case DOWNLOAD_FAILED:
                    Log.i(TAG, "DOWNLOAD_FAILED received!");
                    break;
            }
        }
    };

    private boolean downloadUrl(String myUrl) throws IOException {
        Log.i(TAG, "downloadUrl in");
        InputStream is = null;
        String localPath = "/sdcard/Download/" + getFileName(myUrl);
        File file = new File(localPath);
        String downPath = localPath;
        if (file.exists()) {
            downPath = localPath + ".test";
        }
        FileOutputStream fos = new FileOutputStream(downPath);
        byte[] buffer = new byte[BUFFER_SIZE];
        int byteRead = -1;
        URL url = new URL(myUrl);
        URLConnection conn = url.openConnection();
        is = conn.getInputStream();
        startTime = System.currentTimeMillis();
        while ((byteRead = is.read(buffer)) != -1) {
            fos.write(buffer, 0, byteRead);
        }

        is.close();
        fos.close();

        endTime = System.currentTimeMillis();
        return isDownloadSuccessful(localPath, downPath);
    }

    private boolean isDownloadSuccessful(String local, String down) {
        File localFile = new File(local);
        File downFile = new File(down);
        if (localFile.length() == downFile.length()) {
            avgBandwidth = (double) downFile.length()*1000/(double) (endTime - startTime);
            avgBandwidth = avgBandwidth/1024/1024;
            return true;
        }
        return false;
    }

    private String getFileName(String path) {
        String[] names = path.split("/");
        String res = "";
        for (String str : names) {
            res = str;
        }
        return res;
    }
}