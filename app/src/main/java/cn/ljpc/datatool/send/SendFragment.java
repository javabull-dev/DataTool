package cn.ljpc.datatool.send;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import cn.ljpc.datatool.ApplicationContext;
import cn.ljpc.datatool.R;
import cn.ljpc.datatool.service.GatewayInfoAsyncTask;
import cn.ljpc.datatool.service.SendDataAsyncTask;
import cn.ljpc.datatool.util.NetWorkUtil;

public class SendFragment extends Fragment implements View.OnClickListener {

    private final int SELECT_SYSTEM_FILE = 0;

    private EditText mEditTextContent;

    private EditText mEditTextIP;

    private EditText mEditTextPort;

    public SendFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_send, container, false);
        root.findViewById(R.id.btn_clear).setOnClickListener(this);
        root.findViewById(R.id.btn_send).setOnClickListener(this);
        root.findViewById(R.id.btn_select_file).setOnClickListener(this);
        root.findViewById(R.id.btn_gateway_info).setOnClickListener(this);
        mEditTextContent = root.findViewById(R.id.edit_content);
        mEditTextIP = root.findViewById(R.id.edit_ip);
        mEditTextPort = root.findViewById(R.id.edit_port);

        //设置初始参数
        mEditTextIP.setText(ApplicationContext.sData.localIP);
        mEditTextPort.setText(String.valueOf(ApplicationContext.sData.localPort));
        return root;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_send:
                //开启后台线程，发送文文本数据
                String ip = mEditTextIP.getText().toString();
                if (!NetWorkUtil.checkIP(ip)) {
                    return;
                }
                String portString = mEditTextPort.getText().toString();
                int port = NetWorkUtil.checkPort(portString);
                if (port == -1) {
                    return;
                }
                SendDataAsyncTask asyncTask = new SendDataAsyncTask(this,
                        ApplicationContext.sData.storeFilePath, ip, port, null);
                asyncTask.execute();
                break;
            case R.id.btn_clear:
                //清空文本框
                mEditTextContent.setText("");
                break;
            case R.id.btn_select_file:
                //选择要发送的文件
                //创建一个新文件 Intent.ACTION_CREATE_DOCUMENT
                //选择一个文件 Intent.ACTION_OPEN_DOCUMENT
                // Intent.ACTION_GET_CONTENT
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("*/*");
                //允许多选
                intent.putExtra(intent.EXTRA_ALLOW_MULTIPLE, true);

                startActivityForResult(intent, SELECT_SYSTEM_FILE);
                break;
            case R.id.btn_gateway_info:
                GatewayInfoAsyncTask gatewayInfoAsyncTask = new GatewayInfoAsyncTask(this);
                gatewayInfoAsyncTask.execute();
                break;
            default:
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (Activity.RESULT_OK == resultCode) {
            if (requestCode == SELECT_SYSTEM_FILE) {//选择文件
                ClipData clipData = intent.getClipData();
                Uri uri = intent.getData();
                if (uri != null && clipData == null) {
                    //使用ClipData 包裹Uri
                    clipData = new ClipData(new ClipDescription("", new String[]{ClipDescription.MIMETYPE_TEXT_URILIST}),
                            new ClipData.Item(uri));
                    uri = null;
                }
                if (clipData != null && uri == null) {
                    String ip = mEditTextIP.getText().toString();
                    if (!NetWorkUtil.checkIP(ip)) {
                        return;
                    }
                    String portString = mEditTextPort.getText().toString();
                    int port = NetWorkUtil.checkPort(portString);
                    if (port == -1) {
                        return;
                    }

                    //发送文件
                    SendDataAsyncTask asyncTask = new SendDataAsyncTask(this,
                            ApplicationContext.sData.storeFilePath, ip, port, clipData);
                    asyncTask.execute();
                }
            }
        }
    }
}
