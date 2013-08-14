package net.cattaka.android.humitemp.entity.handler;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import net.cattaka.android.humitemp.entity.HumiTempModel;

public class HumiTempModelHandler {
    public static final String SQL_CREATE_TABLE = "CREATE TABLE humiTempModel(id INTEGER PRIMARY KEY AUTOINCREMENT,date LONG,humidity REAL,temperature REAL)";
    public static final String TABLE_NAME = "humiTempModel";
    public static final String COLUMNS = "id,date,humidity,temperature";
    public static final String[] COLUMNS_ARRAY = new String[] {"id", "date", "humidity", "temperature"};
    public static final int COL_INDEX_ID = 0;
    public static final int COL_INDEX_DATE = 1;
    public static final int COL_INDEX_HUMIDITY = 2;
    public static final int COL_INDEX_TEMPERATURE = 3;
    public static final String COL_NAME_ID = "id";
    public static final String COL_NAME_DATE = "date";
    public static final String COL_NAME_HUMIDITY = "humidity";
    public static final String COL_NAME_TEMPERATURE = "temperature";
    public static long insert(SQLiteDatabase db, HumiTempModel model) {
        ContentValues values = new ContentValues();
        values.put("date", ((model.getDate() != null) ? model.getDate().getTime() : null));
        values.put("humidity", model.getHumidity());
        values.put("temperature", model.getTemperature());
        long key = db.insert(TABLE_NAME, null, values);
        model.setId(key);
        return key;
    }
    public static int update(SQLiteDatabase db, HumiTempModel model) {
        ContentValues values = new ContentValues();
        String whereClause = "id=?";
        String[] whereArgs = new String[]{String.valueOf(model.getId())};
        values.put("date", ((model.getDate() != null) ? model.getDate().getTime() : null));
        values.put("humidity", model.getHumidity());
        values.put("temperature", model.getTemperature());
        return db.update(TABLE_NAME, values, whereClause, whereArgs);
    }
    public static int delete(SQLiteDatabase db, Long key) {
        String whereClause = "id=?";
        String[] whereArgs = new String[]{String.valueOf(key)};
        return db.delete(TABLE_NAME, whereClause, whereArgs);
    }
    public static void readCursorByIndex(Cursor cursor, HumiTempModel dest) {
        dest.setId(!cursor.isNull(0) ? cursor.getLong(0) : null);
        dest.setDate(!cursor.isNull(1) ? new java.util.Date(cursor.getLong(1)) : null);
        dest.setHumidity(!cursor.isNull(2) ? cursor.getFloat(2) : null);
        dest.setTemperature(!cursor.isNull(3) ? cursor.getFloat(3) : null);
    }
    public static HumiTempModel readCursorByIndex(Cursor cursor) {
        HumiTempModel result = new HumiTempModel();
        readCursorByIndex(cursor, result);
        return result;
    }
    public static void readCursorByName(Cursor cursor, HumiTempModel dest) {
        int idx;
        idx = cursor.getColumnIndex("id");
        dest.setId(idx>=0 && !cursor.isNull(idx) ? cursor.getLong(idx) : null);
        idx = cursor.getColumnIndex("date");
        dest.setDate(idx>=0 && !cursor.isNull(idx) ? new java.util.Date(cursor.getLong(idx)) : null);
        idx = cursor.getColumnIndex("humidity");
        dest.setHumidity(idx>=0 && !cursor.isNull(idx) ? cursor.getFloat(idx) : null);
        idx = cursor.getColumnIndex("temperature");
        dest.setTemperature(idx>=0 && !cursor.isNull(idx) ? cursor.getFloat(idx) : null);
    }
    public static HumiTempModel readCursorByName(Cursor cursor) {
        HumiTempModel result = new HumiTempModel();
        readCursorByName(cursor, result);
        return result;
    }
    public static String toStringValue(Object arg) {
        return (arg != null) ? arg.toString() : null;
    }
}
