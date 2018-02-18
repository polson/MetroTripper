package com.philsoft.metrotripper.database;

import android.app.Activity;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import com.philsoft.metrotripper.database.contracts.StopContract;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import timber.log.Timber;

/**
 * Created by polson on 1/20/15.
 */
public class DatabasePopulator {

    private final String STOPS_FILE = "stops.txt";
    private Activity activity;
    private DatabaseHelper dbHelper;
    private SQLiteDatabase db;

    public DatabasePopulator(Activity activity) {
        this.activity = activity;
        dbHelper = DatabaseHelper.getInstance(activity);
        db = dbHelper.getWritableDatabase();
    }

    public void populateStopsFast() throws IOException {
        AssetManager assetManager = activity.getAssets();
        InputStream is = assetManager.open(STOPS_FILE);

        final CSVParser parser = new CSVParser(new InputStreamReader(is), CSVFormat.RFC4180.withHeader());
        List<CSVRecord> records = parser.getRecords();

        long startTime = System.currentTimeMillis();

        insertStopsFast(records);

        long elapsed = System.currentTimeMillis() - startTime;
        Timber.d(">>ELAPSED TIME: " + elapsed);
    }

    public void insertStopsFast(List<CSVRecord> records) {
        try {
            Timber.w(">> Insert Start");
            db.beginTransaction();
            StringBuilder builder = new StringBuilder()
                    .append("Insert or Replace into ")
                    .append(StopContract.TABLE_NAME)
                    .append("(")
                    .append(StopContract.STOP_ID).append(",")
                    .append(StopContract.STOP_NAME).append(",")
                    .append(StopContract.STOP_DESC).append(",")
                    .append(StopContract.STOP_LAT).append(",")
                    .append(StopContract.STOP_LON).append(",")
                    .append(StopContract.STOP_STREET).append(",")
                    .append(StopContract.STOP_CITY).append(",")
                    .append(StopContract.STOP_REGION).append(",")
                    .append(StopContract.STOP_POSTCODE).append(",")
                    .append(StopContract.STOP_COUNTRY).append(",")
                    .append(StopContract.ZONE_ID).append(",")
                    .append(StopContract.WHEELCHAIR_BOARDING).append(",")
                    .append(StopContract.STOP_URL)
                    .append(")").append("values").append("(?,?,?,?,?,?,?,?,?,?,?,?,?)");

            String sql = builder.toString();
            SQLiteStatement insert = db.compileStatement(sql);
            for (int i = 0; i < records.size(); i++) {
                CSVRecord record = records.get(i);
                insert.bindLong(1, Long.valueOf(record.get("stop_id")));
                insert.bindString(2, record.get("stop_name"));
                insert.bindString(3, record.get("stop_desc"));
                insert.bindDouble(4, Double.valueOf(record.get("stop_lat")));
                insert.bindDouble(5, Double.valueOf(record.get("stop_lon")));
                insert.bindString(6, record.get("stop_street"));
                insert.bindString(7, record.get("stop_city"));
                insert.bindString(8, record.get("stop_region"));
                insert.bindString(9, record.get("stop_postcode"));
                insert.bindString(10, record.get("stop_country"));
                insert.bindString(11, record.get("zone_id"));
                insert.bindLong(12, Long.valueOf(record.get("wheelchair_boarding")));
                insert.bindString(13, record.get("stop_url"));
                insert.execute();
            }
            db.setTransactionSuccessful();
            Timber.d(">> Insert Done");
        } catch (Exception e) {
            Timber.d(">> Insert failed: " + e.getMessage());
        } finally {
            db.endTransaction();
        }
    }

    public boolean isTableEmpty(String table) {
        Cursor mCursor = db.rawQuery("SELECT * FROM " + table, null);
        return !mCursor.moveToFirst();
    }
}
