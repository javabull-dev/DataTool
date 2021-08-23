package cn.ljpc.datatool.service;

import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cn.ljpc.datatool.R;

public class TcpServer {

    /**
     * 日志
     */
    private static String TAG = TcpServer.class.getCanonicalName();

    /**
     * 监听端口号
     */
    private int port;

    /**
     * 线程池内管理的最大线程数量
     */
    private int THREAD = 80;

    /**
     * 保存文件的位置
     */
    private String filepath;

    /**
     * socket
     */
    private ServerSocket serverSocket;

    /**
     * 线程池
     */
    ExecutorService pool;

    /**
     *
     */
    Thread thread;

    /**
     *
     */
    Runnable runnable;

    /**
     *
     */
    AppCompatActivity mAppCompatActivity;

    /**
     * 启动一个AsyncWorker前，必须先设置port，filepath，appCompatActivity
     *
     * @param port
     */
    public void setPort(int port) {
        this.port = port;
    }

    public void setFilepath(String filepath) {
        this.filepath = filepath;
    }

    public void setAppCompatActivity(AppCompatActivity appCompatActivity) {
        mAppCompatActivity = appCompatActivity;
    }

    public TcpServer() {
        this.runnable = new TaskRunnable();
        this.pool = Executors.newFixedThreadPool(10);
    }

    /**
     * 关闭服务器
     */
    public void stop() {
        try {
            if (this.serverSocket != null && !this.serverSocket.isClosed()) {
                this.serverSocket.close();
            }
            if (pool != null && pool.isShutdown()) {
                pool.shutdownNow();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 启动服务器
     */
    public void start() {
        thread = new Thread(runnable);
        thread.start();
    }

    class TaskRunnable implements Runnable {
        @Override
        public void run() {
            try {
                serverSocket = new ServerSocket();
                serverSocket.bind(new InetSocketAddress(port));

                while (true) {
                    Socket client = serverSocket.accept();
                    //通过MainActivity获取Fragment
                    FragmentManager supportFragmentManager = mAppCompatActivity.getSupportFragmentManager();
                    Fragment fragment = supportFragmentManager.findFragmentById(R.id.receive);

                    final ReceiveDataAsyncTask task = new ReceiveDataAsyncTask(fragment, client, filepath);
                    Looper mainLooper = mAppCompatActivity.getMainLooper();
                    Handler handler = new Handler(mainLooper);
                    handler.post(() -> task.executeOnExecutor(pool));
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (serverSocket != null) {
                    try {
                        serverSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
