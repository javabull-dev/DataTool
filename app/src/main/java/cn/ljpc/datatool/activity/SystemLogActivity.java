package cn.ljpc.datatool.activity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import cn.ljpc.datatool.R;
import cn.ljpc.datatool.entity.SystemLog;

public class SystemLogActivity extends AppCompatActivity {

    private TextView systemLogView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_system_log);
        initView();
    }

    /**
     * 初始化view
     */
    private void initView() {
        systemLogView = findViewById(R.id.system_log);
        systemLogView.setText(SystemLog.getLog());
        //可滑动
        systemLogView.setMovementMethod(ScrollingMovementMethod.getInstance());
        //给View注册上下文菜单
        this.registerForContextMenu(systemLogView);
    }

    /**
     * 创建上下文菜单
     */
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0, 0, Menu.NONE, "复制");
    }

    /**
     * 上下文菜单某项被选中
     */
    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case 0:
                //获取剪贴板管理器：
                ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                // 创建普通字符型ClipData
                ClipData mClipData = ClipData.newPlainText("log", systemLogView.getText());
                // 将ClipData内容放到系统剪贴板里。
                cm.setPrimaryClip(mClipData);
                break;
            default:
                return super.onContextItemSelected(item);
        }
        return true;
    }
}
