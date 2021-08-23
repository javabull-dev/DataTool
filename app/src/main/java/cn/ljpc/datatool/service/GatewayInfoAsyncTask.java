package cn.ljpc.datatool.service;

import android.os.AsyncTask;
import android.widget.EditText;

import androidx.fragment.app.Fragment;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import cn.ljpc.datatool.R;

/**
 * 获取路由信息
 */
public class GatewayInfoAsyncTask extends AsyncTask<Object, Object, Boolean> {

    private Fragment mFragment;

    private EditText mEditText;

    enum TYPE {
        CLEAR, APPEND
    }

    /**
     * @param fragment
     */
    public GatewayInfoAsyncTask(Fragment fragment) {
        mFragment = fragment;
        mEditText = mFragment.getView().findViewById(R.id.edit_content);
    }

    /**
     * @param objects
     * @return
     */
    @Override
    protected Boolean doInBackground(Object... objects) {
        try {
            Process process = Runtime.getRuntime().exec("ip route list table 0");
            //BufferedReader ie = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
            publishProgress(TYPE.CLEAR);

            StringBuffer stringBuffer = new StringBuffer();
            for (int i = 0; i < 10; i++) {
                String s = in.readLine();
                if (s == null) break;
                stringBuffer.append(s);
                stringBuffer.append("\n");
            }
            publishProgress(TYPE.APPEND, stringBuffer.toString());
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * @param values
     */
    @Override
    protected void onProgressUpdate(Object... values) {
        switch ((TYPE) values[0]) {
            case CLEAR:
                mEditText.setText("");
                break;
            case APPEND:
                mEditText.append((String) values[1]);
                break;
            default:
                break;
        }
    }

    /**
     * @param aBoolean
     */
    @Override
    protected void onPostExecute(Boolean aBoolean) {
    }
}
