package org.ezsplace.horsefallalert;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

public class AlertLogManager {

    private static final String DB_NAME = "events";
    private static SQLiteDatabase db;

    public static synchronized void register(Context context, String msg) {
        try {
            init(context);
            SQLiteStatement st = db.compileStatement("insert into alert_events (event_text) values (?)");
            st.bindString(1, msg);
            st.executeInsert();
        } catch (IOException e) {
            Log.e("Zedan", "db init", e);
            e.printStackTrace();
        }
    }

    private static void init(Context context) throws IOException {
        if (db == null || !db.isOpen()) {
            final File dbFile = context.getDatabasePath(DB_NAME);
            final boolean wasExisting = dbFile.exists();
            db = context.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);
            if (wasExisting) {
                db.execSQL(getSql(context, R.raw.create_schema));
            }
        }
    }
    private static String getSql(Context context, int resId) throws IOException {
        StringBuffer bf = new StringBuffer();
        Resources res = context.getResources();
        final Reader reader = new InputStreamReader(res.openRawResource(resId));
        BufferedReader br = new BufferedReader(reader);

        while (true) {
            String line = br.readLine();
            if (line == null) {
                break;
            }
            bf.append(line);
            bf.append("\n");
        }
        return bf.toString();
    }

    public static List<AlertLogItem> getAllRows(Context context) {
        List<AlertLogItem> logs = new ArrayList<AlertLogItem>();
        try {
            Log.d("Zedan", "init db");
            init(context);
            String[] cols;
            cols = new String[] {"_id", "strftime('%d/%m %H:%M:%S', entry_datetime, 'localtime')", "event_text"};
            Cursor st = db.query("alert_events", cols, null, null, null, null, null);
            while (st.moveToNext()) {
                logs.add(new AlertLogItem(st.getInt(0), st.getString(1), st.getString(2)));
            }
        } catch (IOException e) {
            Log.e("Zedan", "db init", e);
            e.printStackTrace();
        }
        return logs;
    }

    public static void clearAll(Context context) {
        try {
            Log.d("Zedan", "init db");
            init(context);
            db.execSQL("delete from alert_events;");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
