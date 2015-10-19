package com.raggiadolf.connectfour;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

/**
 * Created by ragnaradolf on 18/10/15.
 */
public class ConnectFourState {
    public static final String TAG = "C4State";
    private String turnState;
    private Integer lastRow;
    private Integer lastCol;
    private String lastPlayer;

    public ConnectFourState() {
    }

    public String getTurnState() {
        return turnState;
    }

    public void setTurnState(String turnState) {
        this.turnState = turnState;
    }

    public Integer getLastRow() {
        return lastRow;
    }

    public void setLastRow(Integer lastRow) {
        this.lastRow = lastRow;
    }

    public Integer getLastCol() {
        return lastCol;
    }

    public void setLastCol(Integer lastCol) {
        this.lastCol = lastCol;
    }

    public String getLastPlayer() {
        return lastPlayer;
    }

    public void setLastPlayer(String lastPlayer) {
        this.lastPlayer = lastPlayer;
    }

    public byte[] persist() {
        JSONObject retVal = new JSONObject();

        try {
            retVal.put("turnState", turnState);
            retVal.put("lastRow", lastRow);
            retVal.put("lastCol", lastCol);
            retVal.put("lastPlayer", lastPlayer);
        } catch (JSONException e) {
            // TODO: Log error
            e.printStackTrace();
        }

        String st = retVal.toString();
        Log.d(TAG, "===== PERSISTING\n" + st);

        return st.getBytes(Charset.forName("UTF-8"));
    }

    static public ConnectFourState unpersist(byte[] byteArray) {
        if (byteArray == null) {
            Log.d(TAG, "Empty array---possible bug.");
            return new ConnectFourState();
        }

        String st = null;
        try {
            st = new String(byteArray, "UTF-8");
        } catch (UnsupportedEncodingException e1) {
            // TODO: Log error
            e1.printStackTrace();
            return null;
        }

        Log.d(TAG, "==== UNPERSISTING\n" + st);

        ConnectFourState retVal = new ConnectFourState();

        try {
            JSONObject obj = new JSONObject(st);

            if (obj.has("turnState")) {
                retVal.setTurnState(obj.getString("turnState"));
            }
            if (obj.has("lastRow")) {
                retVal.setLastRow(obj.getInt("lastRow"));
            }
            if (obj.has("lastCol")) {
                retVal.setLastCol(obj.getInt("lastCol"));
            }
            if (obj.has("lastPlayer")) {
                retVal.setLastPlayer(obj.getString("lastPlayer"));
            }
        } catch (JSONException e) {
            // TODO: Log error
            e.printStackTrace();
        }

        return retVal;
    }
}
