package cn.ljpc.datatool.activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import cn.hutool.core.util.StrUtil;
import cn.ljpc.datatool.ApplicationContext;
import cn.ljpc.datatool.R;
import cn.ljpc.datatool.entity.Data;
import cn.ljpc.datatool.receive.ReceiveFragment;
import cn.ljpc.datatool.entity.Item;
import cn.ljpc.datatool.util.ConfigFileUtil;
import cn.ljpc.datatool.util.NetWorkUtil;

public class MainActivity extends AppCompatActivity implements ReceiveFragment.OnListFragmentInteractionListener {

    private static final int SDCARD_PERMISSION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ApplicationContext.sServer.setAppCompatActivity(this);
        checkStoragePermission();
    }

    /**
     * 点击创建菜单
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    //点击菜单项
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.server_setting:
                Intent intent = new Intent(this, SettingActivity.class);
                startActivity(intent);
                return true;
            case R.id.system_log:
                startActivity(new Intent(this, SystemLogActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * recycleview的列表项的view点击事件
     */
    @Override
    public void onListFragmentInteraction(Item item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setPositiveButton(R.string.dialog_ok, (dialog, which) -> dialog.dismiss())
                .setTitle("详细信息");
        LayoutInflater layoutInflater = getLayoutInflater();
        View view = layoutInflater.inflate(R.layout.dialog_receive, null);
        builder.setView(view);
        TextView textView = view.findViewById(R.id.dialog_address);
        //设置地址
        textView.setText(StrUtil.format("地址：{}", item.address));
        EditText editTextContent = (EditText) view.findViewById(R.id.dialog_content);
        if (item.type == 0) { //文件
            editTextContent.setEnabled(false);
            editTextContent.setText(StrUtil.format("文件名：{}\n文件存储路径{}", item.filename, item.filepath));
        } else {//文本
            editTextContent.setText(item.message);
        }
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * 长按删除item
     */
    @Override
    public void OnLongClickListener(Item item) {
        //1.提示用户是否删除
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setPositiveButton(R.string.dialog_ok, (dialog, which) -> {
                    //2.执行删除逻辑
                    Item.remove(item);
                    final FragmentManager supportFragmentManager = MainActivity.this.getSupportFragmentManager();
                    final Fragment receiveFragment = supportFragmentManager.findFragmentById(R.id.receive);
                    RecyclerView view = (RecyclerView) receiveFragment.getView();
                    //通知数据变动
                    view.getAdapter().notifyDataSetChanged();
                })
                .setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setMessage("是否要删除?")
                .setTitle("提示");

        //创建对话框
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * 按键
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setPositiveButton(R.string.dialog_ok, (dialog, id) -> {
                Data data = ApplicationContext.sData;
                FragmentManager fm = getSupportFragmentManager();
//                    List<Fragment> fragments = fm.getFragments();
//                    for (Fragment fragment:fragments){
//                        if (fragment instanceof SendFragment){
//                            SendFragment sendFragment = (SendFragment)fragment;
//                        }
//                    }
                //在avtivity中操纵fragment
                Fragment fragment = fm.findFragmentById(R.id.send);
                View view = fragment.getView();
                String ipString = ((EditText) view.findViewById(R.id.edit_ip)).getText().toString();
                String portString = ((EditText) view.findViewById(R.id.edit_port)).getText().toString();
                int port = NetWorkUtil.checkPort(portString);
                if (port != -1) {
                    data.localPort = port;
                }
                if (NetWorkUtil.checkIP(ipString)) {
                    data.localIP = ipString;
                }
                //保存配置
                ConfigFileUtil.saveConfigFile(MainActivity.this, ApplicationContext.sData);
                MainActivity.this.finish();
            });
            builder.setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // do nothing
                }
            });
            builder.setMessage(R.string.dialog_message)
                    .setTitle(R.string.dialog_title);
            //点击屏幕其他的部分，是否可以使对话框消失
            builder.setCancelable(false);
            AlertDialog dialog = builder.create();
            dialog.show();
            return true;
        }
        return super.onKeyDown(keyCode, event);
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
}
