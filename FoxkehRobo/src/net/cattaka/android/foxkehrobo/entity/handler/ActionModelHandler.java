package net.cattaka.android.foxkehrobo.entity.handler;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import net.cattaka.android.foxkehrobo.entity.ActionModel;

public class ActionModelHandler {
    public static final String SQL_CREATE_TABLE = "CREATE TABLE actionModel(id INTEGER PRIMARY KEY AUTOINCREMENT,sort INTEGER,name TEXT,UNIQUE(name),UNIQUE(sort))";
    public static final String TABLE_NAME = "actionModel";
    public static final String COLUMNS = "id,sort,name";
    public static final String[] COLUMNS_ARRAY = new String[] {"id", "sort", "name"};
    public static final int COL_INDEX_ID = 0;
    public static final int COL_INDEX_SORT = 1;
    public static final int COL_INDEX_NAME = 2;
    public static final String COL_NAME_ID = "id";
    public static final String COL_NAME_SORT = "sort";
    public static final String COL_NAME_NAME = "name";
    public static long insert(SQLiteDatabase db, ActionModel model) {
        ContentValues values = new ContentValues();
        values.put("sort", model.getSort());
        values.put("name", model.getName());
        long key = db.insert(TABLE_NAME, null, values);
        model.setId(key);
        return key;
    }
    public static int update(SQLiteDatabase db, ActionModel model) {
        ContentValues values = new ContentValues();
        String whereClause = "id=?";
        String[] whereArgs = new String[]{String.valueOf(model.getId())};
        values.put("sort", model.getSort());
        values.put("name", model.getName());
        return db.update(TABLE_NAME, values, whereClause, whereArgs);
    }
    public static int delete(SQLiteDatabase db, Long key) {
        String whereClause = "id=?";
        String[] whereArgs = new String[]{String.valueOf(key)};
        return db.delete(TABLE_NAME, whereClause, whereArgs);
    }
    public static ActionModel findById(SQLiteDatabase db, java.lang.Long id) {
        Cursor cursor = findCursorById(db, id);
        ActionModel model = (cursor.moveToNext()) ? readCursorByIndex(cursor) : null;
        cursor.close();
        return model;
    }
    public static ActionModel findByName(SQLiteDatabase db, java.lang.String name) {
        Cursor cursor = findCursorByName(db, name);
        ActionModel model = (cursor.moveToNext()) ? readCursorByIndex(cursor) : null;
        cursor.close();
        return model;
    }
    public static java.util.List<ActionModel> findOrderBySortAsc(SQLiteDatabase db, int limit) {
        Cursor cursor = findCursorOrderBySortAsc(db, limit);
        java.util.List<ActionModel> result = new java.util.ArrayList<ActionModel>();
        while (cursor.moveToNext()) {
            result.add(readCursorByIndex(cursor));
        }
        cursor.close();
        return result;
    }
    public static java.util.List<ActionModel> findOrderBySortDesc(SQLiteDatabase db, int limit) {
        Cursor cursor = findCursorOrderBySortDesc(db, limit);
        java.util.List<ActionModel> result = new java.util.ArrayList<ActionModel>();
        while (cursor.moveToNext()) {
            result.add(readCursorByIndex(cursor));
        }
        cursor.close();
        return result;
    }
    public static Cursor findCursorById(SQLiteDatabase db, java.lang.Long id) {
        String selection = "id=?";
        String[] selectionArgs = new String[]{String.valueOf(id)};
        return db.query(TABLE_NAME, COLUMNS_ARRAY, selection, selectionArgs, null, null, null);
    }
    public static Cursor findCursorByName(SQLiteDatabase db, java.lang.String name) {
        String selection = "name=?";
        String[] selectionArgs = new String[]{String.valueOf(name)};
        return db.query(TABLE_NAME, COLUMNS_ARRAY, selection, selectionArgs, null, null, null);
    }
    public static Cursor findCursorOrderBySortAsc(SQLiteDatabase db, int limit) {
        String selection = "";
        String[] selectionArgs = new String[]{};
        String limitStr = (limit > 0) ? String.valueOf(limit) : null;
        String orderBy = "sort asc";
        return db.query(TABLE_NAME, COLUMNS_ARRAY, selection, selectionArgs, null, null, orderBy, limitStr);
    }
    public static Cursor findCursorOrderBySortDesc(SQLiteDatabase db, int limit) {
        String selection = "";
        String[] selectionArgs = new String[]{};
        String limitStr = (limit > 0) ? String.valueOf(limit) : null;
        String orderBy = "sort desc";
        return db.query(TABLE_NAME, COLUMNS_ARRAY, selection, selectionArgs, null, null, orderBy, limitStr);
    }
    public static void readCursorByIndex(Cursor cursor, ActionModel dest) {
        dest.setId(!cursor.isNull(0) ? cursor.getLong(0) : null);
        dest.setSort(!cursor.isNull(1) ? cursor.getLong(1) : null);
        dest.setName(!cursor.isNull(2) ? cursor.getString(2) : null);
    }
    public static ActionModel readCursorByIndex(Cursor cursor) {
        ActionModel result = new ActionModel();
        readCursorByIndex(cursor, result);
        return result;
    }
    public static void readCursorByName(Cursor cursor, ActionModel dest) {
        int idx;
        idx = cursor.getColumnIndex("id");
        dest.setId(idx>=0 && !cursor.isNull(idx) ? cursor.getLong(idx) : null);
        idx = cursor.getColumnIndex("sort");
        dest.setSort(idx>=0 && !cursor.isNull(idx) ? cursor.getLong(idx) : null);
        idx = cursor.getColumnIndex("name");
        dest.setName(idx>=0 && !cursor.isNull(idx) ? cursor.getString(idx) : null);
    }
    public static ActionModel readCursorByName(Cursor cursor) {
        ActionModel result = new ActionModel();
        readCursorByName(cursor, result);
        return result;
    }
    public static String toStringValue(Object arg) {
        return (arg != null) ? arg.toString() : null;
    }
}
