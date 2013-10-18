
package net.cattaka.android.foxkehrobo.activity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import net.cattaka.android.foxkehrobo.Constants;
import net.cattaka.android.foxkehrobo.R;
import net.cattaka.android.foxkehrobo.entity.handler.ActionModelHandler;
import android.app.Activity;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class ManageActionDbActivity extends Activity implements View.OnClickListener {
    private static final int REQUEST_CODE_IN = 1;

    private static final int REQUEST_CODE_OUT = 2;

    private TextView mFilenameOutEdit;

    private TextView mFilenameInEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_action_db);

        findViewById(R.id.saveButton).setOnClickListener(this);
        findViewById(R.id.loadButton).setOnClickListener(this);
        findViewById(R.id.selectInButton).setOnClickListener(this);
        findViewById(R.id.selectOutButton).setOnClickListener(this);

        mFilenameOutEdit = (TextView)findViewById(R.id.filenameOutEdit);
        mFilenameInEdit = (TextView)findViewById(R.id.filenameInEdit);
    }

    @Override
    protected void onResume() {
        super.onResume();
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmm", Locale.getDefault());
        String filename = Environment.getExternalStorageDirectory().getAbsolutePath()
                + File.separatorChar
                + String.format(Locale.getDefault(), Constants.DB_SAVE_NAME, df.format(new Date()));
        mFilenameOutEdit.setText(filename);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_IN) {
            String path = data.getDataString();
            path = (path.startsWith("file://")) ? path.substring("file://".length()) : path;
            mFilenameInEdit.setText(path);
        } else if (requestCode == REQUEST_CODE_OUT) {
            String path = data.getDataString();
            path = (path.startsWith("file://")) ? path.substring("file://".length()) : path;
            mFilenameOutEdit.setText(path);
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.saveButton) {
            if (doSave()) {
                Toast.makeText(this, "Save finished.", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else if (v.getId() == R.id.loadButton) {
            if (doLoad()) {
                Toast.makeText(this, "Load finished.", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else if (v.getId() == R.id.selectInButton) {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("*/*");
            Intent i = Intent.createChooser(intent, "File");
            startActivityForResult(i, REQUEST_CODE_IN);
        } else if (v.getId() == R.id.selectOutButton) {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("*/*");
            Intent i = Intent.createChooser(intent, "File");
            startActivityForResult(i, REQUEST_CODE_OUT);
        }
    }

    private boolean doLoad() {
        String filename = String.valueOf((mFilenameInEdit.getText()));
        File srcFile = new File(filename);
        { // Check whether is openable as SQLite.
            SQLiteDatabase db = null;
            try {
                db = SQLiteDatabase.openDatabase(filename, null, SQLiteDatabase.OPEN_READONLY);
                ActionModelHandler.findOrderBySortAsc(db, 1);
                db.close();
            } catch (SQLiteException e) {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                return false;
            } finally {
                if (db != null) {
                    db.close();
                }
            }
        }
        boolean result = false;
        File dstFile = getDatabasePath(Constants.DB_NAME);
        FileInputStream in = null;
        FileOutputStream out = null;
        try {
            byte[] buf = new byte[1 << 20];
            in = new FileInputStream(srcFile);
            out = new FileOutputStream(dstFile);
            int r;
            while ((r = in.read(buf)) > 0) {
                out.write(buf, 0, r);
            }
            out.flush();
            result = true;
        } catch (IOException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    Log.d(Constants.TAG, e.getMessage(), e);
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    Log.d(Constants.TAG, e.getMessage(), e);
                }
            }
        }
        return result;
    }

    private boolean doSave() {
        boolean result = false;
        String filename = String.valueOf((mFilenameOutEdit.getText()));
        File dstFile = new File(filename);
        File srcFile = getDatabasePath(Constants.DB_NAME);
        FileInputStream in = null;
        FileOutputStream out = null;
        try {
            byte[] buf = new byte[1 << 20];
            in = new FileInputStream(srcFile);
            out = new FileOutputStream(dstFile);
            int r;
            while ((r = in.read(buf)) > 0) {
                out.write(buf, 0, r);
            }
            out.flush();
            result = true;
        } catch (IOException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    Log.d(Constants.TAG, e.getMessage(), e);
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    Log.d(Constants.TAG, e.getMessage(), e);
                }
            }
        }
        return result;
    }
}
