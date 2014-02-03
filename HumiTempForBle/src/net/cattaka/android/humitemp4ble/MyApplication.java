
package net.cattaka.android.humitemp4ble;

import android.app.Application;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // File dbFile = getDatabasePath(Constants.DB_NAME);
        // if (!dbFile.exists()) {
        // AssetManager am = getAssets();
        // try {
        // {
        // if (!dbFile.getParentFile().exists()) {
        // dbFile.getParentFile().mkdirs();
        // }
        // dbFile.createNewFile();
        // }
        // InputStream in = am.open("orig_" + Constants.DB_NAME);
        // OutputStream out = new BufferedOutputStream(new
        // FileOutputStream(dbFile));
        // byte[] buf = new byte[1 << 14];
        // int r;
        // while ((r = in.read(buf)) > 0) {
        // out.write(buf, 0, r);
        // }
        // out.flush();
        // out.close();
        // } catch (IOException e) {
        // throw new RuntimeException(e);
        // }
        // }
    }
}
