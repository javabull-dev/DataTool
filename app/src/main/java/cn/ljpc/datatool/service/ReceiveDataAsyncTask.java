package cn.ljpc.datatool.service;

import android.os.AsyncTask;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import cn.hutool.core.util.StrUtil;
import cn.ljpc.datatool.entity.Item;
import cn.ljpc.datatool.entity.SystemLog;
import cn.ljpc.datatool.util.TimeUtil;

/**
 * 接收数据或者文件
 */
public class ReceiveDataAsyncTask extends AsyncTask<Object, Object, Boolean> {

    enum TYPE {
        UPDATE_TEXT_DATA, RUN_ERROR, SHOW_FILE_DATA
    }

    /**
     * 日志记录
     */
    private static String TAG = ReceiveDataAsyncTask.class.getCanonicalName();

    /**
     * client socket
     */
    private Socket client;

    /**
     * 文件名
     */
    private String filename;

    /**
     * 文件路径
     */
    private String filepath;


    private Fragment mFragment;

    /**
     * @param client
     */
    public ReceiveDataAsyncTask(Fragment fragment, Socket client, String filepath) {
        mFragment = fragment;
        this.client = client;
        this.filepath = filepath;
    }

    public void setFragment(Fragment fragment) {
        mFragment = fragment;
    }

    /**
     * 后台任务
     *
     * @param objects
     * @return
     */
    @Override
    protected Boolean doInBackground(Object[] objects) {
        if (client.isConnected()) {
            try (BufferedInputStream fis = new BufferedInputStream(client.getInputStream());
                 BufferedOutputStream bos = new BufferedOutputStream(client.getOutputStream())) {
                // 1.获取传输数据的类型 file（文件名，编码格式），数据文本（编码格式）
                // 1.1 "FILE:hello.jar:12380123" --- "DATA:UTF8"
                // 2.返回收到信息的响应，比如 class OK, class NO
                // 2.1 成功，"class OK\t"，失败，"class NO\tmessage:charset unsupport"
                // 2.2 当失败时，传输停止，跳到第五步
                // 3.得到数据
                // 3.1 将得到的数据进行保存
                // 4.返回收到信息的响应，比如 data OK, data NO
                // 5.关闭连接
                byte[] buf = new byte[1024];
                int len;
                String encoding = "";
                Long filesize = 0L;

                if (-1 != (len = fis.read(buf, 0, buf.length))) {
                    // 对buffer中的数据处理
                    StringBuffer stringBuffer = new StringBuffer(4);
                    for (int i = 0; i < 4; i++) {
                        char ch = (char) buf[i];
                        stringBuffer.append(ch);
                    }
                    String content = stringBuffer.toString();
                    if (content.equals("DATA")) {
                        // 字符串文本数据
                        filename = "";

                    } else if (content.equals("FILE")) {
                        // 文件
                    } else {
                        // 未知的数据类型
                        throw new Exception("未知数据类型");
                    }
                }
                String c = new String(buf, 0, len, StandardCharsets.UTF_8);
                String substring = c.substring(c.indexOf(":") + 1);
                String[] split = substring.split(":");
                if (split.length == 1) {// 接送文本数据
                    encoding = substring;
                    bos.write("class OK".getBytes(StandardCharsets.UTF_8));
                    bos.flush();
                } else if (split.length == 2) {// 接收文件数据
                    this.filename = split[0];
                    filesize = Long.parseLong(split[1]);
                    bos.write("class OK".getBytes(StandardCharsets.UTF_8));
                    bos.flush();
                } else {// 其他数据
                    bos.write("class NO".getBytes(StandardCharsets.UTF_8));
                    bos.flush();
                    // 关闭socket连接
                    return false;
                }

                if (this.filename.length() != 0) {
                    // 保存文件
                    try (BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(
                            new FileOutputStream(this.filepath + File.separator + this.filename))) {
                        int length;
                        long summary = 0L;
                        while (-1 != (length = fis.read(buf, 0, buf.length))) {
                            bufferedOutputStream.write(buf, 0, length);
                            bufferedOutputStream.flush();
                            // 显示文件传输速率
                            // @Param filesize 文件的总大小
                            // @Param summary 文件的已保存的大小
                            summary += length;
                            //不使用进度条，不显示
                            //publishProgress(filesize, summary);
                        }
                        //publishProgress(filesize, summary);
                        // SHOW_FILE_DATA
                        Item item = new Item();
                        InetAddress inetAddress = client.getInetAddress();
                        item.address = StrUtil.format("{}:{}", inetAddress.getHostAddress(), client.getPort());
                        item.filename = this.filename;
                        item.filepath = this.filepath;
                        item.message = null;
                        item.type = 0;// 文件数据
                        item.time = TimeUtil.getNowDateFormat();
                        publishProgress(TYPE.SHOW_FILE_DATA, item);
                    } catch (Exception e) {
                        e.printStackTrace();
                        publishProgress(TYPE.RUN_ERROR, e);
                        return false;
                    }
                } else {
                    InetAddress inetAddress = client.getInetAddress();
                    String readData = readData(fis);
                    Item item = new Item();
                    item.address = StrUtil.format("{}:{}", inetAddress.getHostAddress(), client.getPort());
                    item.filename = this.filename;
                    item.filepath = this.filepath;
                    item.message = readData;
                    item.type = 1;// text数据
                    item.time = TimeUtil.getNowDateFormat();
                    // UPDATE_TEXT_DATA
                    publishProgress(TYPE.UPDATE_TEXT_DATA, item);
                }
            } catch (Exception e) {
                e.printStackTrace();
                //RUN_ERROR
                publishProgress(TYPE.RUN_ERROR, e);
                return false;
            }
        }
        return true;
    }

    private String readData(BufferedInputStream fis) throws IOException {
        byte[] buffer = new byte[1024];
        int len;
        StringBuffer stringBuffer = new StringBuffer();
        while (-1 != (len = fis.read(buffer, 0, buffer.length))) {
            stringBuffer.append(new String(buffer, 0, len, StandardCharsets.UTF_8));
        }
        return stringBuffer.toString();
    }

    @Override
    protected void onProgressUpdate(Object... values) {
        TYPE type = (TYPE) values[0];
        switch (type) {
            case RUN_ERROR:
                Exception exception = (Exception) values[1];
                String msg = exception.toString();
                SystemLog.addLog(msg);
                Toast.makeText(mFragment.getActivity(), msg, Toast.LENGTH_SHORT).show();
                break;
            case SHOW_FILE_DATA:
            case UPDATE_TEXT_DATA:
                Item item = (Item) values[1];
                Item.add(item);
                //获取 recyclerView
                RecyclerView recyclerView = (RecyclerView) mFragment.getView();
                RecyclerView.Adapter adapter = recyclerView.getAdapter();
                //通知界面，数据发生改变
                adapter.notifyDataSetChanged();
                break;
            default:
                break;
        }
    }
}
