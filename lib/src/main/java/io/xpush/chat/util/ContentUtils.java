package io.xpush.chat.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Looper;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.TypedValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

public class ContentUtils {

    private static final String[] sArrays = {"png","jpg","jpeg","bmp","gif"};
    private static final ArrayList<String> imageExtList = new ArrayList<String>(Arrays.asList(sArrays));

    @SuppressLint("NewApi")
    public static String getRealPathFromURI_API19(Context context, Uri uri){
        String filePath = "";
        String wholeID = DocumentsContract.getDocumentId(uri);

        // Split at colon, use second item in the array
        String id = wholeID.split(":")[1];

        String[] column = { MediaStore.Images.Media.DATA };

        // where id is equal to
        String sel = MediaStore.Images.Media._ID + "=?";

        Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                column, sel, new String[]{ id }, null);

        int columnIndex = cursor.getColumnIndex(column[0]);

        if (cursor.moveToFirst()) {
            filePath = cursor.getString(columnIndex);
        }
        cursor.close();
        return filePath;
    }


    @SuppressLint("NewApi")
    public static String getRealPathFromURI_API11to18(Context context, Uri contentUri) {
        String[] proj = { MediaStore.Images.Media.DATA };
        String result = null;

        Looper.prepare();

        CursorLoader cursorLoader = new CursorLoader(
                context,
                contentUri, proj, null, null, null);
        Cursor cursor = cursorLoader.loadInBackground();

        if(cursor != null){
            int column_index =
                    cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            result = cursor.getString(column_index);
        }
        return result;
    }

    public static String getRealPathFromURI_BelowAPI11(Context context, Uri contentUri){

        Looper.prepare();

        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
        int column_index
                = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    public static String getRealPath(Context context, Uri uri){
        String realPath = null;

        if( uri.toString().startsWith("file:")){
            realPath = uri.toString().replace("file:", "");
        } else {

            // SDK < API11
            if (Build.VERSION.SDK_INT < 11) {
                realPath = ContentUtils.getRealPathFromURI_BelowAPI11(context, uri);

                // SDK >= 11 && SDK < 19
            } else if (Build.VERSION.SDK_INT < 19) {
                realPath = ContentUtils.getRealPathFromURI_API11to18(context, uri);

                // SDK > 19 (Android 4.4)
            } else {
                if (String.valueOf(uri).contains("documents")) {
                    realPath = ContentUtils.getRealPathFromURI_API19(context, uri);
                } else {
                    realPath = ContentUtils.getRealPathFromURI_API11to18(context, uri);
                }
            }
        }

        return realPath;
    }


    public static boolean isImagePath(String path){

        boolean result = false;
        if( path.indexOf(".") > -1 ) {
            String ext = path.substring(path.lastIndexOf(".") + 1);
            if (imageExtList.indexOf(ext) > -1) {
                result = true;
            }
        }
        return result;
    }

    public static int[] getActualImageSize(int originalWidth , int originalHeight, Context context ){
        int[] results = new int[2];

        double ratio = (double) originalWidth / (double) originalHeight;

        double w = 0;
        double h = 0;

        boolean isMaxWidth = false;
        int maxWidth = 240;
        if( maxWidth > originalWidth ) {
            w =  originalWidth;
        } else {
            isMaxWidth = true;
            w = maxWidth;
        }

        boolean isMaxHeight = false;
        int maxHeight = 240;
        if( maxHeight  > originalHeight ){
            h = originalHeight;
        } else {
            isMaxHeight = true;
            h = maxHeight;
        }

        if( isMaxWidth && isMaxHeight ){
            if( originalWidth > originalHeight ){
                h = w / ratio;
            } else {
                w = h * ratio;
            }
        } else if( isMaxWidth ){
            h = w / ratio;
        } else if ( isMaxHeight ){
            w = h * ratio;
        }

        int width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (int) w, context.getResources().getDisplayMetrics());
        int height = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (int)h, context.getResources().getDisplayMetrics());

        results[0] = width;
        results[1] = height;

        return results;
    }

    public static String getInputStringLength(String paramString, int paramInt){
        if (paramString == null) {
            return null;
        }
        return String.format(Locale.US, "%1$d/%2$d", new Object[] { Integer.valueOf(paramString.length()), Integer.valueOf(paramInt) });
    }
}