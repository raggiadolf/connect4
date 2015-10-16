package com.raggiadolf.connectfour;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

/**
 * Created by ragnaradolf on 10/10/15.
 */
public class ConnectTurn {
    public static final String TAG = "C4Turn";
    private String action = "";

    public ConnectTurn() {
    }

    public ConnectTurn(String action ) {
        this.action = action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public byte[] persist() {
        JSONObject retVal = new JSONObject();

        try {
            retVal.put("action", action);
        } catch (JSONException e) {
            // TODO: Log error
            e.printStackTrace();
        }

        String st = retVal.toString();

        Log.d(TAG, "===== PERSISTING\n" + st);

        return st.getBytes(Charset.forName("UTF-8"));
    }

    static public ConnectTurn unpersist(byte[] byteArray) {
        if (byteArray == null) {
            Log.d(TAG, "Empty array---possible bug.");
            return new ConnectTurn();
        }

        String st = null;
        try {
            st = new String(byteArray, "UTF-8");
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
            return null;
        }

        Log.d(TAG, "====UNPERSIST\n" + st);

        ConnectTurn retVal = new ConnectTurn();

        try {
            JSONObject obj = new JSONObject(st);

            if (obj.has("action")) {
                retVal.action = obj.getString("action");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return retVal;
    }
}
