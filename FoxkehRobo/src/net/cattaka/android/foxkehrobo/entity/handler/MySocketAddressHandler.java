package net.cattaka.android.foxkehrobo.entity.handler;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import net.cattaka.android.foxkehrobo.entity.MySocketAddress;

public class MySocketAddressHandler {
    public static final String SQL_CREATE_TABLE = "CREATE TABLE mySocketAddress(id INTEGER PRIMARY KEY AUTOINCREMENT,hostName TEXT,port INTEGER)";
    public static final String TABLE_NAME = "mySocketAddress";
    public static final String COLUMNS = "id,hostName,port";
    public static final String[] COLUMNS_ARRAY = new String[] {"id", "hostName", "port"};
    public static final int COL_INDEX_ID = 0;
    public static final int COL_INDEX_HOST_NAME = 1;
    public static final int COL_INDEX_PORT = 2;
    public static final String COL_NAME_ID = "id";
    public static final String COL_NAME_HOST_NAME = "hostName";
    public static final String COL_NAME_PORT = "port";
    public static long insert(SQLiteDatabase db, MySocketAddress model) {
        ContentValues values = new ContentValues();
        values.put("hostName", model.getHostName());
        values.put("port", model.getPort());
        long key = db.insert(TABLE_NAME, null, values);
        model.setId(key);
        return key;
    }
    public static int update(SQLiteDatabase db, MySocketAddress model) {
        ContentValues values = new ContentValues();
        String whereClause = "id=?";
        String[] whereArgs = new String[]{String.valueOf(model.getId())};
        values.put("hostName", model.getHostName());
        values.put("port", model.getPort());
        return db.update(TABLE_NAME, values, whereClause, whereArgs);
    }
    public static int delete(SQLiteDatabase db, Long key) {
        String whereClause = "id=?";
        String[] whereArgs = new String[]{String.valueOf(key)};
        return db.delete(TABLE_NAME, whereClause, whereArgs);
    }
    public static java.util.List<MySocketAddress> findOrderByIdAsc(SQLiteDatabase db, int limit) {
        Cursor cursor = findCursorOrderByIdAsc(db, limit);
        java.util.List<MySocketAddress> result = new java.util.ArrayList<MySocketAddress>();
        while (cursor.moveToNext()) {
            result.add(readCursorByIndex(cursor));
        }
        cursor.close();
        return result;
    }
    public static Cursor findCursorOrderByIdAsc(SQLiteDatabase db, int limit) {
        String selection = "";
        String[] selectionArgs = new String[]{};
        String limitStr = (limit > 0) ? String.valueOf(limit) : null;
        String orderBy = "id asc";
        return db.query(TABLE_NAME, COLUMNS_ARRAY, selection, selectionArgs, null, null, orderBy, limitStr);
    }
    public static void readCursorByIndex(Cursor cursor, MySocketAddress dest) {
        dest.setId(!cursor.isNull(0) ? cursor.getLong(0) : null);
        dest.setHostName(!cursor.isNull(1) ? cursor.getString(1) : null);
        dest.setPort(!cursor.isNull(2) ? cursor.getInt(2) : null);
    }
    public static MySocketAddress readCursorByIndex(Cursor cursor) {
        MySocketAddress result = new MySocketAddress();
        readCursorByIndex(cursor, result);
        return result;
    }
    public static void readCursorByName(Cursor cursor, MySocketAddress dest) {
        int idx;
        idx = cursor.getColumnIndex("id");
        dest.setId(idx>=0 && !cursor.isNull(idx) ? cursor.getLong(idx) : null);
        idx = cursor.getColumnIndex("hostName");
        dest.setHostName(idx>=0 && !cursor.isNull(idx) ? cursor.getString(idx) : null);
        idx = cursor.getColumnIndex("port");
        dest.setPort(idx>=0 && !cursor.isNull(idx) ? cursor.getInt(idx) : null);
    }
    public static MySocketAddress readCursorByName(Cursor cursor) {
        MySocketAddress result = new MySocketAddress();
        readCursorByName(cursor, result);
        return result;
    }
    public static String toStringValue(Object arg) {
        return (arg != null) ? arg.toString() : null;
    }
}
