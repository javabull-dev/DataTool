package cn.ljpc.datatool.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import cn.ljpc.datatool.ApplicationContext;
import cn.ljpc.datatool.R;
import cn.ljpc.datatool.activity.file.FolderPickerActivity;
import cn.ljpc.datatool.entity.Data;
import cn.ljpc.datatool.util.NetWorkUtil;

/**
 * 有关设置的 activity
 */
public class SettingActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int CHOOSE_FILE_CODE = 1;
    private static final int CHOOSE_DIR_CODE = 2;
    private static final int FOLDER_PICKER_CODE = 3;

    private static final int SDCARD_PERMISSION = 1;

    //启动服务器的按钮
    private Button mButtonStartServer;

    //服务器的状态
    private TextView mTextViewServerStatus;

    //服务器的端口号
    private EditText mEditTextServerPort;

    //接收文件的存储路径
    private EditText mEditTextFileStorePath;

    //选择文件
    private ImageButton mImageButtonSelect;

    //退出按鈕
    private Button mButtonSelectExit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        findView();
        checkComponentStatus();
        checkStoragePermission();
    }

    /**
     * 获取控件
     */
    private void findView() {
        mButtonStartServer = findViewById(R.id.btn_start_server);
        mButtonStartServer.setOnClickListener(this);

        mTextViewServerStatus = findViewById(R.id.textView_server_status);
        mEditTextServerPort = findViewById(R.id.editText_server_port);

        mEditTextFileStorePath = findViewById(R.id.editText_file_store_path);
        //不允许手动填写接收文件在Android设备上的存储路径
        mEditTextFileStorePath.setEnabled(false);
        mImageButtonSelect = findViewById(R.id.imageButton_select);
        mImageButtonSelect.setOnClickListener(this);

        mButtonSelectExit = findViewById(R.id.btn_select_exit);
        mButtonSelectExit.setOnClickListener(this);

        findViewById(R.id.btn_select_save).setOnClickListener(this);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_start_server:
                //检查端口号是否正确
                String portString = mEditTextServerPort.getText().toString();
                int port1 = NetWorkUtil.checkPort(portString);
                if (port1 != -1) {
                    //改变按钮的text
                    String s = mButtonStartServer.getText().toString();
                    if (s.equals("启动")) {
                        ApplicationContext.sData.serverPort = port1;
                        mButtonStartServer.setText("关闭");
                        //EditText禁用
                        mEditTextServerPort.setEnabled(false);
                        mTextViewServerStatus.setText("服务器已启动,正在监听:" + ApplicationContext.sData.serverPort);
                        mImageButtonSelect.setEnabled(false);
                        //  启动服务器
                        ApplicationContext.sServer.setFilepath(ApplicationContext.sData.storeFilePath);
                        ApplicationContext.sServer.setPort(ApplicationContext.sData.serverPort);
                        ApplicationContext.sServer.start();
                        ApplicationContext.serverStatus = true;
                    } else if (s.equals("关闭")) {
                        mButtonStartServer.setText("启动");
                        mEditTextServerPort.setEnabled(true);
                        mTextViewServerStatus.setText("服务器尚未启动...");
                        mImageButtonSelect.setEnabled(true);
                        // 关闭服务器
                        ApplicationContext.serverStatus = false;
                        ApplicationContext.sServer.stop();
                    }
                }
                break;
            case R.id.imageButton_select:
                Intent intent = new Intent(this, FolderPickerActivity.class);
                startActivityForResult(intent, FOLDER_PICKER_CODE);
                break;
            case R.id.btn_select_exit:
//                //直接回到主界面，修改的数据丢失
//                AlertDialog.Builder builder = new AlertDialog.Builder(this);
//                builder.setTitle("回到主界面提示")
//                        .setMessage("填写的数据将不会生效，是否要退出?")
//                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                                //do nothing
//                            }
//                        })
//                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                                finish();
//                            }
//                        });
//                AlertDialog dialog = builder.create();
//                dialog.show();
//                break;
                finish();
                break;
            case R.id.btn_select_save:
                //保存填写的数据，使数据立即生效
                updateData();
                Toast.makeText(this, "保存成功!", Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
        }
    }

    /**
     * 将数据更新ApplicationContext.sData中
     */
    private void updateData() {
        Data data = ApplicationContext.sData;
        data.storeFilePath = mEditTextFileStorePath.getText().toString();
        //serverPort在缺省的情况下为9999
        data.serverPort = 9999;
        String serverPortString = mEditTextServerPort.getText().toString();
        int port = NetWorkUtil.checkPort(serverPortString);
        if (port != -1) {
            data.serverPort = port;
        }
    }

    /**
     * 将实时的数据更新值界面控件
     */
    private void checkComponentStatus() {
        String ipAddress = NetWorkUtil.getIpAddress(this);
        ((TextView) findViewById(R.id.textView_local_ip)).setText("本机的IP地址" + (ipAddress.equals("") ? "无" : ipAddress));
        mEditTextFileStorePath.setText(ApplicationContext.sData.storeFilePath);
        mEditTextServerPort.setText(String.valueOf(ApplicationContext.sData.serverPort));
        if (ApplicationContext.serverStatus) {
            mEditTextServerPort.setEnabled(false);
            mButtonStartServer.setText("关闭");
            //EditText禁用
            mTextViewServerStatus.setText("服务器已启动,正在监听:" + ApplicationContext.sData.serverPort);
            mImageButtonSelect.setEnabled(false);
        } else {
            mButtonStartServer.setText("启动");
            mEditTextServerPort.setEnabled(true);
            mTextViewServerStatus.setText("服务器尚未启动...");
            mImageButtonSelect.setEnabled(true);
        }
    }

    /**
     * @param requestCode
     * @param resultCode
     * @param intent
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (Activity.RESULT_OK == resultCode) {
            if (requestCode == CHOOSE_FILE_CODE) {//选择文件
                final Uri uri = intent.getData();
                if (uri != null) {
                    System.out.println("uri: --------------" + uri);
                } else {
                    Toast.makeText(this, "没有文件", Toast.LENGTH_SHORT).show();
                }
            } else if (requestCode == CHOOSE_DIR_CODE) {//选择文件夹
                final Uri uri = intent.getData();
                if (uri != null) {
                    System.out.println(uri);
                }
            } else if (requestCode == FOLDER_PICKER_CODE) {
                if (intent.hasExtra("data")) {
                    String data = intent.getExtras().getString("data");
                    mEditTextFileStorePath.setText(data);
                }
            }
        }
    }

    /**
     * 检查应用是否有权限
     */
    private void checkStoragePermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    SDCARD_PERMISSION);
        }
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    SDCARD_PERMISSION);
        }
    }

    /**
     * 按下后退键
     *
     * @param keyCode
     * @param event
     * @return
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            updateData();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
