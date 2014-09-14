
package net.cattaka.android.humitemp.db;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.cattaka.android.humitemp.Constants;
import net.cattaka.android.humitemp.entity.HumiTempModel;
import net.cattaka.android.humitemp.entity.HumiTempModelCatHands;
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
        db.execSQL(HumiTempModelCatHands.SQL_CREATE_TABLE);
        db.execSQL("CREATE INDEX IDX_humiTempModel ON humiTempModel(date)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion == 1) {
            db.execSQL("CREATE INDEX IDX_humiTempModel ON humiTempModel(date)");
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
                selection = "?<=" + HumiTempModelCatHands.COL_NAME_DATE + " AND "
                        + HumiTempModelCatHands.COL_NAME_DATE + "<=?";
                selectionArgs = new String[] {
                        String.valueOf(from.getTime()), String.valueOf(to.getTime())
                };
            } else if (from != null) {
                selection = "?<=" + HumiTempModelCatHands.COL_NAME_DATE;
                selectionArgs = new String[] {
                    String.valueOf(from.getTime())
                };
            } else if (to != null) {
                selection = HumiTempModelCatHands.COL_NAME_DATE + "<=?";
                selectionArgs = new String[] {
                    String.valueOf(String.valueOf(to.getTime()))
                };
            } else {
                selection = null;
                selectionArgs = null;
            }
            cursor = db.query( //
                    HumiTempModelCatHands.TABLE_NAME, // table,
                    HumiTempModelCatHands.COLUMNS_ARRAY, // columns,
                    selection,// selection,
                    selectionArgs,// selectionArgs,
                    null, // groupBy,
                    null, // having,
                    HumiTempModelCatHands.COL_NAME_DATE // orderBy)
                    );

            List<HumiTempModel> models = new ArrayList<HumiTempModel>();
            while (cursor.moveToNext()) {
                models.add(HumiTempModelCatHands.readCursorByIndex(cursor));
            }
            return models;
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

    public boolean registerHumiTempModel(HumiTempModel model) {
        boolean result = false;
        SQLiteDatabase db = null;
        try {
            db = getWritableDatabase();
            db.beginTransaction();
            if (model.getId() == null) {
                result = (HumiTempModelCatHands.insert(db, model) > 0);
            } else {
                result = (HumiTempModelCatHands.update(db, model) > 0);
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
