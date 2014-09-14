
package net.cattaka.android.humitemp4pd.db;

import java.util.List;

import net.cattaka.android.humitemp4pd.entity.MySocketAddress;
import net.cattaka.android.humitemp4pd.entity.MySocketAddressCatHands;
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
        db.execSQL(MySocketAddressCatHands.SQL_CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // none
    }

    public List<MySocketAddress> findMySocketAddresses() {
        SQLiteDatabase db = getReadableDatabase();
        try {
            return MySocketAddressCatHands.findOrderByIdAsc(db, 0);
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
                result = MySocketAddressCatHands.insert(db, item);
            } else {
                result = MySocketAddressCatHands.update(db, item);
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
            result = MySocketAddressCatHands.delete(db, id);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            db.close();
        }
        return result != 0;
    }
}
