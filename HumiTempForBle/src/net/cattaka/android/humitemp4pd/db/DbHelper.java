
package net.cattaka.android.humitemp4pd.db;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.cattaka.android.humitemp4pd.Constants;
import net.cattaka.android.humitemp4pd.entity.DeviceModel;
import net.cattaka.android.humitemp4pd.entity.HumiTempModel;
import net.cattaka.android.humitemp4pd.entity.handler.DeviceModelHandler;
import net.cattaka.android.humitemp4pd.entity.handler.HumiTempModelHandler;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DbHelper extends SQLiteOpenHelper {

    public DbHelper(Context context) {
        super(context, Constants.DB_NAME, null, 2);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DeviceModelHandler.SQL_CREATE_TABLE);
        db.execSQL(HumiTempModelHandler.SQL_CREATE_TABLE);
        db.execSQL("CREATE INDEX IDX_humiTempModel_date ON humiTempModel(date)");
        db.execSQL("CREATE INDEX IDX_humiTempModel_sendFlag ON humiTempModel(sendFlag)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion == 1) {
            db.execSQL("CREATE INDEX IDX_humiTempModel ON humiTempModel(date)");
        }
    }

    public DeviceModel findDeviceModel(String address) {
        SQLiteDatabase db = null;
        try {
            db = getReadableDatabase();
            return DeviceModelHandler.findByAddress(db, address);
        } finally {
            db.close();
        }
    }

    public List<DeviceModel> findDeviceModels() {
        SQLiteDatabase db = getReadableDatabase();
        try {

            return DeviceModelHandler.findOrderByAddressAsc(db, -1);
        } finally {
            db.close();
        }
    }

    public List<HumiTempModel> findHumiTempModel(Date from, Date to) {
        SQLiteDatabase db = null;
        try {
            db = getReadableDatabase();
            Cursor cursor;

            String selection;
            String[] selectionArgs;
            if (from != null && to != null) {
                selection = "?<=" + HumiTempModelHandler.COL_NAME_DATE + " AND "
                        + HumiTempModelHandler.COL_NAME_DATE + "<=?";
                selectionArgs = new String[] {
                        String.valueOf(from.getTime()), String.valueOf(to.getTime())
                };
            } else if (from != null) {
                selection = "?<=" + HumiTempModelHandler.COL_NAME_DATE;
                selectionArgs = new String[] {
                    String.valueOf(from.getTime())
                };
            } else if (to != null) {
                selection = HumiTempModelHandler.COL_NAME_DATE + "<=?";
                selectionArgs = new String[] {
                    String.valueOf(String.valueOf(to.getTime()))
                };
            } else {
                selection = null;
                selectionArgs = null;
            }
            cursor = db.query( //
                    HumiTempModelHandler.TABLE_NAME, // table,
                    HumiTempModelHandler.COLUMNS_ARRAY, // columns,
                    selection,// selection,
                    selectionArgs,// selectionArgs,
                    null, // groupBy,
                    null, // having,
                    HumiTempModelHandler.COL_NAME_DATE // orderBy)
                    );

            List<HumiTempModel> models = new ArrayList<HumiTempModel>();
            while (cursor.moveToNext()) {
                models.add(HumiTempModelHandler.readCursorByIndex(cursor));
            }
            return models;
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

    public boolean registerDeviceModel(DeviceModel model) {
        boolean result = false;
        SQLiteDatabase db = null;
        try {
            db = getWritableDatabase();
            db.beginTransaction();
            result = (DeviceModelHandler.update(db, model) > 0);
            if (!result) {
                result = (DeviceModelHandler.insert(db, model) > 0);
            }
            db.setTransactionSuccessful();
        } finally {
            if (db != null) {
                db.endTransaction();
                db.close();
            }
        }
        return result;
    }

    public boolean deleteDeviceModel(String address) {
        boolean result = false;
        SQLiteDatabase db = getWritableDatabase();
        try {
            db.beginTransaction();
            DeviceModel model = DeviceModelHandler.findByAddress(db, address);
            if (model != null) {
                result = (DeviceModelHandler.delete(db, model.getId()) > 0);
            }
            db.setTransactionSuccessful();
        } finally {
            if (db != null) {
                db.endTransaction();
                db.close();
            }
        }
        return result;
    }

    public boolean registerHumiTempModel(HumiTempModel model) {
        boolean result = false;
        SQLiteDatabase db = null;
        try {
            db = getWritableDatabase();
            db.beginTransaction();
            if (model.getId() == null) {
                result = (HumiTempModelHandler.insert(db, model) > 0);
            } else {
                result = (HumiTempModelHandler.update(db, model) > 0);
            }
            db.setTransactionSuccessful();
        } finally {
            if (db != null) {
                db.endTransaction();
                db.close();
            }
        }
        return result;
    }
}
