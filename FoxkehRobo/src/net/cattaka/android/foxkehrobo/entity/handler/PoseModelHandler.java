package net.cattaka.android.foxkehrobo.entity.handler;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import net.cattaka.android.foxkehrobo.entity.PoseModel;

public class PoseModelHandler {
    public static final String SQL_CREATE_TABLE = "CREATE TABLE poseModel(id INTEGER PRIMARY KEY AUTOINCREMENT,actionId INTEGER,sort INTEGER,headYaw INTEGER,headPitch INTEGER,armLeft INTEGER,armRight INTEGER,footLeft INTEGER,footRight INTEGER,earLeft INTEGER,earRight INTEGER,tailYaw INTEGER,tailPitch INTEGER,time INTEGER,UNIQUE(actionId,sort))";
    public static final String TABLE_NAME = "poseModel";
    public static final String COLUMNS = "id,actionId,sort,headYaw,headPitch,armLeft,armRight,footLeft,footRight,earLeft,earRight,tailYaw,tailPitch,time";
    public static final String[] COLUMNS_ARRAY = new String[] {"id", "actionId", "sort", "headYaw", "headPitch", "armLeft", "armRight", "footLeft", "footRight", "earLeft", "earRight", "tailYaw", "tailPitch", "time"};
    public static final int COL_INDEX_ID = 0;
    public static final int COL_INDEX_ACTION_ID = 1;
    public static final int COL_INDEX_SORT = 2;
    public static final int COL_INDEX_HEAD_YAW = 3;
    public static final int COL_INDEX_HEAD_PITCH = 4;
    public static final int COL_INDEX_ARM_LEFT = 5;
    public static final int COL_INDEX_ARM_RIGHT = 6;
    public static final int COL_INDEX_FOOT_LEFT = 7;
    public static final int COL_INDEX_FOOT_RIGHT = 8;
    public static final int COL_INDEX_EAR_LEFT = 9;
    public static final int COL_INDEX_EAR_RIGHT = 10;
    public static final int COL_INDEX_TAIL_YAW = 11;
    public static final int COL_INDEX_TAIL_PITCH = 12;
    public static final int COL_INDEX_TIME = 13;
    public static final String COL_NAME_ID = "id";
    public static final String COL_NAME_ACTION_ID = "actionId";
    public static final String COL_NAME_SORT = "sort";
    public static final String COL_NAME_HEAD_YAW = "headYaw";
    public static final String COL_NAME_HEAD_PITCH = "headPitch";
    public static final String COL_NAME_ARM_LEFT = "armLeft";
    public static final String COL_NAME_ARM_RIGHT = "armRight";
    public static final String COL_NAME_FOOT_LEFT = "footLeft";
    public static final String COL_NAME_FOOT_RIGHT = "footRight";
    public static final String COL_NAME_EAR_LEFT = "earLeft";
    public static final String COL_NAME_EAR_RIGHT = "earRight";
    public static final String COL_NAME_TAIL_YAW = "tailYaw";
    public static final String COL_NAME_TAIL_PITCH = "tailPitch";
    public static final String COL_NAME_TIME = "time";
    public static long insert(SQLiteDatabase db, PoseModel model) {
        ContentValues values = new ContentValues();
        values.put("actionId", model.getActionId());
        values.put("sort", model.getSort());
        values.put("headYaw", ((model.getHeadYaw() != null) ? net.cattaka.android.foxkehrobo.entity.PoseModel.ShortCorder.encode(model.getHeadYaw()) : null));
        values.put("headPitch", ((model.getHeadPitch() != null) ? net.cattaka.android.foxkehrobo.entity.PoseModel.ShortCorder.encode(model.getHeadPitch()) : null));
        values.put("armLeft", ((model.getArmLeft() != null) ? net.cattaka.android.foxkehrobo.entity.PoseModel.ShortCorder.encode(model.getArmLeft()) : null));
        values.put("armRight", ((model.getArmRight() != null) ? net.cattaka.android.foxkehrobo.entity.PoseModel.ShortCorder.encode(model.getArmRight()) : null));
        values.put("footLeft", ((model.getFootLeft() != null) ? net.cattaka.android.foxkehrobo.entity.PoseModel.ShortCorder.encode(model.getFootLeft()) : null));
        values.put("footRight", ((model.getFootRight() != null) ? net.cattaka.android.foxkehrobo.entity.PoseModel.ShortCorder.encode(model.getFootRight()) : null));
        values.put("earLeft", ((model.getEarLeft() != null) ? net.cattaka.android.foxkehrobo.entity.PoseModel.ShortCorder.encode(model.getEarLeft()) : null));
        values.put("earRight", ((model.getEarRight() != null) ? net.cattaka.android.foxkehrobo.entity.PoseModel.ShortCorder.encode(model.getEarRight()) : null));
        values.put("tailYaw", ((model.getTailYaw() != null) ? net.cattaka.android.foxkehrobo.entity.PoseModel.ShortCorder.encode(model.getTailYaw()) : null));
        values.put("tailPitch", ((model.getTailPitch() != null) ? net.cattaka.android.foxkehrobo.entity.PoseModel.ShortCorder.encode(model.getTailPitch()) : null));
        values.put("time", model.getTime());
        long key = db.insert(TABLE_NAME, null, values);
        model.setId(key);
        return key;
    }
    public static int update(SQLiteDatabase db, PoseModel model) {
        ContentValues values = new ContentValues();
        String whereClause = "id=?";
        String[] whereArgs = new String[]{String.valueOf(model.getId())};
        values.put("actionId", model.getActionId());
        values.put("sort", model.getSort());
        values.put("headYaw", ((model.getHeadYaw() != null) ? net.cattaka.android.foxkehrobo.entity.PoseModel.ShortCorder.encode(model.getHeadYaw()) : null));
        values.put("headPitch", ((model.getHeadPitch() != null) ? net.cattaka.android.foxkehrobo.entity.PoseModel.ShortCorder.encode(model.getHeadPitch()) : null));
        values.put("armLeft", ((model.getArmLeft() != null) ? net.cattaka.android.foxkehrobo.entity.PoseModel.ShortCorder.encode(model.getArmLeft()) : null));
        values.put("armRight", ((model.getArmRight() != null) ? net.cattaka.android.foxkehrobo.entity.PoseModel.ShortCorder.encode(model.getArmRight()) : null));
        values.put("footLeft", ((model.getFootLeft() != null) ? net.cattaka.android.foxkehrobo.entity.PoseModel.ShortCorder.encode(model.getFootLeft()) : null));
        values.put("footRight", ((model.getFootRight() != null) ? net.cattaka.android.foxkehrobo.entity.PoseModel.ShortCorder.encode(model.getFootRight()) : null));
        values.put("earLeft", ((model.getEarLeft() != null) ? net.cattaka.android.foxkehrobo.entity.PoseModel.ShortCorder.encode(model.getEarLeft()) : null));
        values.put("earRight", ((model.getEarRight() != null) ? net.cattaka.android.foxkehrobo.entity.PoseModel.ShortCorder.encode(model.getEarRight()) : null));
        values.put("tailYaw", ((model.getTailYaw() != null) ? net.cattaka.android.foxkehrobo.entity.PoseModel.ShortCorder.encode(model.getTailYaw()) : null));
        values.put("tailPitch", ((model.getTailPitch() != null) ? net.cattaka.android.foxkehrobo.entity.PoseModel.ShortCorder.encode(model.getTailPitch()) : null));
        values.put("time", model.getTime());
        return db.update(TABLE_NAME, values, whereClause, whereArgs);
    }
    public static int delete(SQLiteDatabase db, Long key) {
        String whereClause = "id=?";
        String[] whereArgs = new String[]{String.valueOf(key)};
        return db.delete(TABLE_NAME, whereClause, whereArgs);
    }
    public static PoseModel findById(SQLiteDatabase db, java.lang.Long id) {
        Cursor cursor = findCursorById(db, id);
        PoseModel model = (cursor.moveToNext()) ? readCursorByIndex(cursor) : null;
        cursor.close();
        return model;
    }
    public static java.util.List<PoseModel> findByActionIdOrderBySortAsc(SQLiteDatabase db, int limit, java.lang.Long actionId) {
        Cursor cursor = findCursorByActionIdOrderBySortAsc(db, limit, actionId);
        java.util.List<PoseModel> result = new java.util.ArrayList<PoseModel>();
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
    public static Cursor findCursorByActionIdOrderBySortAsc(SQLiteDatabase db, int limit, java.lang.Long actionId) {
        String selection = "actionId=?";
        String[] selectionArgs = new String[]{String.valueOf(actionId)};
        String limitStr = (limit > 0) ? String.valueOf(limit) : null;
        String orderBy = "sort asc";
        return db.query(TABLE_NAME, COLUMNS_ARRAY, selection, selectionArgs, null, null, orderBy, limitStr);
    }
    public static void readCursorByIndex(Cursor cursor, PoseModel dest) {
        dest.setId(!cursor.isNull(0) ? cursor.getLong(0) : null);
        dest.setActionId(!cursor.isNull(1) ? cursor.getLong(1) : null);
        dest.setSort(!cursor.isNull(2) ? cursor.getLong(2) : null);
        dest.setHeadYaw(!cursor.isNull(3) ? net.cattaka.android.foxkehrobo.entity.PoseModel.ShortCorder.decode(cursor.getShort(3)) : null);
        dest.setHeadPitch(!cursor.isNull(4) ? net.cattaka.android.foxkehrobo.entity.PoseModel.ShortCorder.decode(cursor.getShort(4)) : null);
        dest.setArmLeft(!cursor.isNull(5) ? net.cattaka.android.foxkehrobo.entity.PoseModel.ShortCorder.decode(cursor.getShort(5)) : null);
        dest.setArmRight(!cursor.isNull(6) ? net.cattaka.android.foxkehrobo.entity.PoseModel.ShortCorder.decode(cursor.getShort(6)) : null);
        dest.setFootLeft(!cursor.isNull(7) ? net.cattaka.android.foxkehrobo.entity.PoseModel.ShortCorder.decode(cursor.getShort(7)) : null);
        dest.setFootRight(!cursor.isNull(8) ? net.cattaka.android.foxkehrobo.entity.PoseModel.ShortCorder.decode(cursor.getShort(8)) : null);
        dest.setEarLeft(!cursor.isNull(9) ? net.cattaka.android.foxkehrobo.entity.PoseModel.ShortCorder.decode(cursor.getShort(9)) : null);
        dest.setEarRight(!cursor.isNull(10) ? net.cattaka.android.foxkehrobo.entity.PoseModel.ShortCorder.decode(cursor.getShort(10)) : null);
        dest.setTailYaw(!cursor.isNull(11) ? net.cattaka.android.foxkehrobo.entity.PoseModel.ShortCorder.decode(cursor.getShort(11)) : null);
        dest.setTailPitch(!cursor.isNull(12) ? net.cattaka.android.foxkehrobo.entity.PoseModel.ShortCorder.decode(cursor.getShort(12)) : null);
        dest.setTime(!cursor.isNull(13) ? cursor.getInt(13) : null);
    }
    public static PoseModel readCursorByIndex(Cursor cursor) {
        PoseModel result = new PoseModel();
        readCursorByIndex(cursor, result);
        return result;
    }
    public static void readCursorByName(Cursor cursor, PoseModel dest) {
        int idx;
        idx = cursor.getColumnIndex("id");
        dest.setId(idx>=0 && !cursor.isNull(idx) ? cursor.getLong(idx) : null);
        idx = cursor.getColumnIndex("actionId");
        dest.setActionId(idx>=0 && !cursor.isNull(idx) ? cursor.getLong(idx) : null);
        idx = cursor.getColumnIndex("sort");
        dest.setSort(idx>=0 && !cursor.isNull(idx) ? cursor.getLong(idx) : null);
        idx = cursor.getColumnIndex("headYaw");
        dest.setHeadYaw(idx>=0 && !cursor.isNull(idx) ? net.cattaka.android.foxkehrobo.entity.PoseModel.ShortCorder.decode(cursor.getShort(idx)) : null);
        idx = cursor.getColumnIndex("headPitch");
        dest.setHeadPitch(idx>=0 && !cursor.isNull(idx) ? net.cattaka.android.foxkehrobo.entity.PoseModel.ShortCorder.decode(cursor.getShort(idx)) : null);
        idx = cursor.getColumnIndex("armLeft");
        dest.setArmLeft(idx>=0 && !cursor.isNull(idx) ? net.cattaka.android.foxkehrobo.entity.PoseModel.ShortCorder.decode(cursor.getShort(idx)) : null);
        idx = cursor.getColumnIndex("armRight");
        dest.setArmRight(idx>=0 && !cursor.isNull(idx) ? net.cattaka.android.foxkehrobo.entity.PoseModel.ShortCorder.decode(cursor.getShort(idx)) : null);
        idx = cursor.getColumnIndex("footLeft");
        dest.setFootLeft(idx>=0 && !cursor.isNull(idx) ? net.cattaka.android.foxkehrobo.entity.PoseModel.ShortCorder.decode(cursor.getShort(idx)) : null);
        idx = cursor.getColumnIndex("footRight");
        dest.setFootRight(idx>=0 && !cursor.isNull(idx) ? net.cattaka.android.foxkehrobo.entity.PoseModel.ShortCorder.decode(cursor.getShort(idx)) : null);
        idx = cursor.getColumnIndex("earLeft");
        dest.setEarLeft(idx>=0 && !cursor.isNull(idx) ? net.cattaka.android.foxkehrobo.entity.PoseModel.ShortCorder.decode(cursor.getShort(idx)) : null);
        idx = cursor.getColumnIndex("earRight");
        dest.setEarRight(idx>=0 && !cursor.isNull(idx) ? net.cattaka.android.foxkehrobo.entity.PoseModel.ShortCorder.decode(cursor.getShort(idx)) : null);
        idx = cursor.getColumnIndex("tailYaw");
        dest.setTailYaw(idx>=0 && !cursor.isNull(idx) ? net.cattaka.android.foxkehrobo.entity.PoseModel.ShortCorder.decode(cursor.getShort(idx)) : null);
        idx = cursor.getColumnIndex("tailPitch");
        dest.setTailPitch(idx>=0 && !cursor.isNull(idx) ? net.cattaka.android.foxkehrobo.entity.PoseModel.ShortCorder.decode(cursor.getShort(idx)) : null);
        idx = cursor.getColumnIndex("time");
        dest.setTime(idx>=0 && !cursor.isNull(idx) ? cursor.getInt(idx) : null);
    }
    public static PoseModel readCursorByName(Cursor cursor) {
        PoseModel result = new PoseModel();
        readCursorByName(cursor, result);
        return result;
    }
    public static String toStringValue(Object arg) {
        return (arg != null) ? arg.toString() : null;
    }
}
