
package net.cattaka.android.humitemp4pd.db;

import java.util.List;

import net.cattaka.android.humitemp4pd.entity.MySocketAddress;
import net.cattaka.android.humitemp4pd.entity.handler.MySocketAddressHandler;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class BtDbHelper extends SQLiteOpenHelper {
    public static final String DB_NAME = "BlackTortoise.db";

    public BtDbHelper(Context context) {
        super(context, DB_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(MySocketAddressHandler.SQL_CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // none
    }

    public List<MySocketAddress> findMySocketAddresses() {
        SQLiteDatabase db = getReadableDatabase();
        try {
            return MySocketAddressHandler.findOrderByIdAsc(db, 0);
        } finally {
            db.close();
        }
    }

    public boolean registerMySocketAddress(MySocketAddress item) {
        SQLiteDatabase db = getWritableDatabase();
        long result = 0;
        try {
            db.beginTransaction();
            if (item.getId() == null) {
                result = MySocketAddressHandler.insert(db, item);
            } else {
                result = MySocketAddressHandler.update(db, item);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            db.close();
        }
        return result != 0;
    }

    public boolean deleteMySocketAddress(Long id) {
        SQLiteDatabase db = getWritableDatabase();
        long result = 0;
        try {
            db.beginTransaction();
            result = MySocketAddressHandler.delete(db, id);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            db.close();
        }
        return result != 0;
    }
}
