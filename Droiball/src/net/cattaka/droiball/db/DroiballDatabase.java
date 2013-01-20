
package net.cattaka.droiball.db;

import java.util.List;

import net.cattaka.droiball.Constants;
import net.cattaka.droiball.entity.ActionModel;
import net.cattaka.droiball.entity.PoseModel;
import net.cattaka.droiball.entity.handler.ActionModelHandler;
import net.cattaka.droiball.entity.handler.PoseModelHandler;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DroiballDatabase extends SQLiteOpenHelper {

    public DroiballDatabase(Context context) {
        super(context, Constants.DB_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(ActionModelHandler.SQL_CREATE_TABLE);
        db.execSQL(PoseModelHandler.SQL_CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // none
    }

    public List<ActionModel> findActions(boolean withChild) {
        SQLiteDatabase db = null;
        try {
            db = getReadableDatabase();
            List<ActionModel> models = ActionModelHandler.findOrderBySortAsc(db, 0);
            if (withChild) {
                for (ActionModel model : models) {
                    model.setPoseModels(PoseModelHandler.findByActionIdOrderBySortAsc(db, 0,
                            model.getId()));
                }
            }
            return models;
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

    public ActionModel findActionModel(String name, boolean withChild) {
        SQLiteDatabase db = null;
        try {
            db = getReadableDatabase();
            ActionModel model = ActionModelHandler.findByName(db, name);
            if (model != null && withChild) {
                model.setPoseModels(PoseModelHandler.findByActionIdOrderBySortAsc(db, 0,
                        model.getId()));
            }
            return model;
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

    public Long findMaxActionSort() {
        SQLiteDatabase db = null;
        try {
            db = getReadableDatabase();
            List<ActionModel> models = ActionModelHandler.findOrderBySortDesc(db, 1);
            ActionModel model = (models.size() > 0) ? models.get(0) : null;
            return (model != null) ? model.getSort() : null;
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

    public boolean registerActionModel(ActionModel model, boolean withChild) {
        boolean result = false;
        SQLiteDatabase db = null;
        try {
            db = getWritableDatabase();
            db.beginTransaction();
            if (model.getId() == null) {
                result = (ActionModelHandler.insert(db, model) > 0);
            } else {
                result = (ActionModelHandler.update(db, model) > 0);
            }
            if (withChild) {
                db.delete(PoseModelHandler.TABLE_NAME, PoseModelHandler.COL_NAME_ACTION_ID + "=?",
                        new String[] {
                            model.getId().toString()
                        });
                for (PoseModel child : model.getPoseModels()) {
                    child.setId(null);
                    child.setActionId(model.getId());
                    PoseModelHandler.insert(db, child);
                }
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

    public boolean deleteActionModel(long id) {
        boolean result = false;
        SQLiteDatabase db = null;
        try {
            db = getWritableDatabase();
            db.beginTransaction();
            result = (ActionModelHandler.delete(db, id) > 0);
            PoseModelHandler.delete(db, id);
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
