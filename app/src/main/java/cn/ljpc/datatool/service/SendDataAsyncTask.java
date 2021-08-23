package cn.ljpc.datatool.service;

import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.documentfile.provider.DocumentFile;
import androidx.fragment.app.Fragment;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import cn.hutool.core.util.StrUtil;
import cn.ljpc.datatool.R;
import cn.ljpc.datatool.entity.SystemLog;

/**
 * 发送文件获取数据的后台任务
 * <p>
 * <p>
 * 1.execute(Params... params)，执行一个异步任务，需要我们在代码中调用此方法，触发异步任务的执行。
 * <p>
 * 2.onPreExecute()，在execute(Params... params)被调用后立即执行，一般用来在执行后台任务前对UI做一些标记。
 * <p>
 * 3.doInBackground(Params... params)，在onPreExecute()完成后立即执行，用于执行较为费时的操作，此方法将接收输入参数和返回计算结果。在执行过程中可以调用publishProgress(Progress... values)来更新进度信息。
 * <p>
 * 4.onProgressUpdate(Progress... values)，在调用publishProgress(Progress... values)时，此方法被执行，直接将进度信息更新到UI组件上。
 * <p>
 * 5.onPostExecute(Result result)，当后台操作结束时，此方法将会被调用，计算结果将做为参数传递到此方法中，直接将结果显示到UI组件上。
 */
public class SendDataAsyncTask extends AsyncTask<String, Object, Boolean> {

    /**
     * 类型
     */
    enum TYPE {
        UPDATE_PROGRESS_VALUE,
        RUN_ERROR,
        UPDATE_EDIT_TEXT_DATA,
        TEXT_SEND_ERROR,
        SHOW_PROGRESS_DIALOG,
        UPDATE_DIALOG_PARAM,
        FILE_TRANSPORT_LOG
    }

    /**
     * 日志记录
     */
    private static String TAG = SendDataAsyncTask.class.getCanonicalName();

    /**
     * 接收文件后所要存放文件的路径
     */
    private String mFilepath;

    /**
     * mFragment
     */
    private Fragment mFragment;

    /**
     * 需要发送的数据
     */
    private StringBuffer mStringBuffer;

    /**
     * 對方的ip地址
     */
    private String mIP;

    /**
     * 對方的端口号
     */
    private int mPort;

    /**
     * mEditText
     */
    private EditText mEditText;

    /**
     * 待传输的文件
     */
    private ClipData mClipData;

    /**
     * 进度条dialog
     */
    private AlertDialog mProgressDialog;

    /**
     * @param fragment
     * @param filepath
     * @param ip
     * @param port
     * @param clipData
     */
    public SendDataAsyncTask(Fragment fragment, String filepath,
                             String ip, int port, ClipData clipData) {
        mFilepath = filepath;
        mIP = ip;
        mPort = port;
        mClipData = clipData;
        mFragment = fragment;
    }

    private static class DialogParamGroup {
        public TextView tv_filename;
        public ProgressBar pb_fileprog;
        public TextView tv_prog;
        public EditText et_log;
    }

    /**
     * 包装处理对话框的控件
     */
    private DialogParamGroup mDialogParamGroup;

    /**
     * execute方法被调用后，执行
     */
    @Override
    protected void onPreExecute() {
        if (mClipData == null) {//传输文本数据
            View view = mFragment.getView();
            mEditText = (EditText) view.findViewById(R.id.edit_content);
            mStringBuffer = new StringBuffer();
            mStringBuffer.append(mEditText.getText().toString());
        } else {//传输文件
            mDialogParamGroup = new DialogParamGroup();
            AlertDialog.Builder builder = new AlertDialog.Builder(mFragment.getActivity());
            LayoutInflater layoutInflater = mFragment.getActivity().getLayoutInflater();
            View view = layoutInflater.inflate(R.layout.dialog_send, null);
            builder.setView(view);
            builder.setTitle("文件传输");
            builder.setPositiveButton("结束", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //终止文件的上传
                    SendDataAsyncTask.this.cancel(true);
                }
            });
            builder.setCancelable(false);
            mProgressDialog = builder.create();

            mDialogParamGroup.tv_filename = view.findViewById(R.id.tv_filename);
            mDialogParamGroup.tv_prog = view.findViewById(R.id.tv_prog);
            mDialogParamGroup.et_log = view.findViewById(R.id.et_log);
            mDialogParamGroup.pb_fileprog = view.findViewById(R.id.pb_fileprog);

            mDialogParamGroup.pb_fileprog.setMax(100);
            //设置EditText不可编辑
            mDialogParamGroup.et_log.setEnabled(false);
        }
    }

    /**
     * 处理后台任务
     *
     * @param strings
     * @return
     */
    @Override
    protected Boolean doInBackground(String... strings) {
        Boolean result = true;
        for (int i = 0; i < (mClipData == null ? 1 : mClipData.getItemCount()); i++) {
            try (Socket socket = new Socket(mIP, mPort)) {
                try (OutputStream os = new BufferedOutputStream(socket.getOutputStream());
                     InputStream is = new BufferedInputStream(socket.getInputStream())) {
                    if (mClipData != null) {// 传输文件
                        /**
                         * FileInputStream fis = new FileInputStream(mFragment.getActivity()
                         *                             .getContentResolver()
                         *                             .openFileDescriptor(mUri, "r").getFileDescriptor())
                         */
                        Uri uri = mClipData.getItemAt(i).getUri();
                        try (ParcelFileDescriptor.AutoCloseInputStream fis =
                                     (ParcelFileDescriptor.AutoCloseInputStream) mFragment.getActivity()
                                             .getContentResolver().openInputStream(uri)) {

                            long filelength = fis.available();
                            DocumentFile documentFile = DocumentFile.fromSingleUri(mFragment.getActivity(), uri);
                            publishProgress(TYPE.UPDATE_DIALOG_PARAM, documentFile.getName());
                            //写数据
                            os.write(StrUtil.format("FILE:{}:{}", documentFile.getName(), filelength)
                                    .getBytes(Charset.forName("utf8")));
                            os.flush();
                            //传输的buffer 大小 2048 byte
                            int bufSize = 2048;
                            byte[] buf = new byte[bufSize];
                            // 读数据
                            int read = is.read(buf);
                            String data = new String(buf, 0, read, Charset.forName("utf8"));
                            if (data.contains("class OK")) {
                                // 传输文件数据
                                try (BufferedInputStream inputStream = new BufferedInputStream(fis)) {
                                    int length;
                                    long progress = 0;
                                    // SHOW_PROGRESS_DIALOG
                                    publishProgress(TYPE.SHOW_PROGRESS_DIALOG);
                                    while (-1 != (length = inputStream.read(buf, 0, buf.length))) {
                                        os.write(buf, 0, length);
                                        os.flush();
                                        progress += length;
                                        // UPDATE_PROGRESS_VALUE
                                        publishProgress(TYPE.UPDATE_PROGRESS_VALUE, Long.valueOf(progress), Long.valueOf(filelength));
                                    }
                                    Thread.sleep(20);
                                    publishProgress(TYPE.UPDATE_PROGRESS_VALUE, Long.valueOf(progress), Long.valueOf(filelength));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    // RUN_ERROR
                                    publishProgress(TYPE.RUN_ERROR, new String(e.getCause() + " " + e.getMessage()));
                                    result = false;
                                }

                            } else if (data.contains("class NO")) {
                                publishProgress(TYPE.RUN_ERROR, new String("对方无法识别发送的数据类型!"));
                                result = false;
                            } else {
                                publishProgress(TYPE.RUN_ERROR, new String("对方无法识别发送的数据类型!"));
                                result = false;
                            }
                            publishProgress(TYPE.FILE_TRANSPORT_LOG, Boolean.valueOf(false),
                                    DocumentFile.fromSingleUri(mFragment.getActivity(), uri).getName());
                        } catch (Exception e) {
                            e.printStackTrace();
                            // FILE_TRANSPORT_LOG
                            publishProgress(TYPE.FILE_TRANSPORT_LOG, Boolean.valueOf(true),
                                    DocumentFile.fromSingleUri(mFragment.getActivity(), uri).getName());
                        }

                    } else if (mStringBuffer != null) {// 传输数据
                        // 发送数据
                        // 通过socket发送数据
                        // 数据在txta_send_data中的格式如下
                        //
                        // 今天天气很好！
                        // --------------------------
                        // 数据正在发送给 192.168.19.1 80
                        // 数据发送成功
                        // --------------------------
                        // \n
                        os.write(StrUtil.format("DATA:{}", "utf8").getBytes(Charset.forName("utf8")));
                        os.flush();
                        byte[] buf = new byte[80];
                        int read = is.read(buf);
                        String data = new String(buf, 0, read, Charset.forName("utf8"));
                        if (data.contains("class OK")) {
                            // 在发送数据的EditText中显示 "数据正在发送给 192.168.19.1 80"
                            publishProgress(TYPE.UPDATE_EDIT_TEXT_DATA, new String(getSeparate() + "数据正在发送给"
                                    + socket.getInetAddress().getHostAddress() + ":" + socket.getPort() + "\n"));
                            os.write(mStringBuffer.toString().getBytes(StandardCharsets.UTF_8));
                            os.flush();
                            // UPDATE_EDIT_TEXT_DATA
                            publishProgress(TYPE.UPDATE_EDIT_TEXT_DATA, new String("数据发送成功" + getSeparate()));
                        } else if (data.contains("class NO")) {
                            // TEXT_SEND_ERROR
                            publishProgress(TYPE.TEXT_SEND_ERROR, new String(getSeparate() + "对方无法识别发送的数据类型!" + getSeparate()));
                            result = false;
                        } else {
                            publishProgress(TYPE.TEXT_SEND_ERROR, new String(getSeparate() + "无法识别对方发送类型的应答!" + getSeparate()));
                            result = false;
                        }
                    }
                } catch (Exception e) {
                    // OTHER_ERROR
                    e.printStackTrace();
                    publishProgress(TYPE.RUN_ERROR, new String(getSeparate() + e.getCause() + " " + e.getMessage() + getSeparate()));
                    result = false;
                }

            } catch (Exception e) {
                e.printStackTrace();
                publishProgress(TYPE.RUN_ERROR, new String(getSeparate() + e.getCause() + " " + e.getMessage() + getSeparate()));
                result = false;
            }
        }
        return result;
    }

    /**
     * 处理中间数据
     *
     * @param values
     */
    @Override
    protected void onProgressUpdate(Object... values) {
        TYPE type = (TYPE) values[0];
        switch (type) {
            case UPDATE_EDIT_TEXT_DATA:
            case TEXT_SEND_ERROR:
                String message = (String) values[1];
                mEditText.append(message);
                SystemLog.addLog(message);
                break;
            case RUN_ERROR:
                String message1 = (String) values[1];
                SystemLog.addLog(message1);
                Toast.makeText(mFragment.getActivity(), message1, Toast.LENGTH_SHORT).show();
                break;
            case UPDATE_PROGRESS_VALUE:
                Long progress = (Long) values[1];
                Long length = (Long) values[2];
                int prog = (int) (progress * 100 / length);
                mDialogParamGroup.pb_fileprog.setProgress(prog);
                mDialogParamGroup.tv_prog.setText(prog + "%");
                break;
            case SHOW_PROGRESS_DIALOG:
                mProgressDialog.show();
                break;
            case UPDATE_DIALOG_PARAM:
                //设置进度条对话框的参数
                if (mDialogParamGroup != null) {
                    String message2 = (String) values[1];
                    mDialogParamGroup.tv_prog.setText("0%");
                    mDialogParamGroup.pb_fileprog.setProgress(0);
                    mDialogParamGroup.tv_filename.setText("文件名 " + message2);
                }
                break;
            case FILE_TRANSPORT_LOG:
                Boolean flag = (Boolean) values[1];
                String filename = (String) values[2];
                if (!flag) {
                    mDialogParamGroup.et_log.append(filename + " 传输成功" + "\n");
                } else {
                    mDialogParamGroup.et_log.append(filename + " 传输失败" + "\n");
                }
                break;
            default:
                break;
        }
    }

    /**
     * 后台任务处理完成
     *
     * @param result
     */
    @Override
    protected void onPostExecute(Boolean result) {
//        Log.i(TAG, result ? "传输成功" : "传输失败");
        //还原界面控件的初始状态
        //....
    }

    /**
     * 分隔符
     *
     * @return
     */
    private String getSeparate() {
        return "\n---------------------\n";
    }
}
