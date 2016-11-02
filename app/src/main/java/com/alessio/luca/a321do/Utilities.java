package com.alessio.luca.a321do;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** this class contains methods that don't really belong to a specific class but are useful across various situations
 * Created by Luca on 30/10/2016.
 */

public class Utilities {
    public static final String LIST_SEPARATOR = "__,__";
    public static final String EDIT_NOTE_PAYLOAD_CODE = "EditNotePayload";
    public static final String NOTIFICATION_PAYLOAD_CODE = "NotificationNotePayload";

    public static void openKeyboard(Context context) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
    }
    public static void closeKeyboard(Context context, EditText editText) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
    }
    public static String checkListToString(List<String> stringList) {
        if (stringList==null || stringList.isEmpty()) {
            return new String();
        }
        String toString = new String();
        for(int i=0; i<stringList.size(); i++)
        {
            toString = toString + stringList.get(i);
            if(i!=stringList.size()-1)
                toString = toString + LIST_SEPARATOR;
        }
        return toString;
    }
    public static List<String> stringToCheckList(String str) {
        if(str != null)
            return new ArrayList<String>(Arrays.asList(str.split(LIST_SEPARATOR))); //forse restituisce oggetto non modificabile
        else
            return new ArrayList<String>();
    }
    public static Bitmap resizeImage(Bitmap srcBitmap, float percentage) {
        int srcWidth = srcBitmap.getWidth();
        int srcHeight = srcBitmap.getHeight();
        int dstWidth = (int)(srcWidth*percentage);
        int dstHeight = (int)(srcHeight*percentage);
        return Bitmap.createScaledBitmap(srcBitmap, dstWidth, dstHeight, true);
    }
    public static byte[] toByteArray(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int read = 0;
        byte[] buffer = new byte[1024];
        while (read != -1) {
            read = in.read(buffer);
            if (read != -1)
                out.write(buffer,0,read);
        }
        out.close();
        return out.toByteArray();
    }
}
