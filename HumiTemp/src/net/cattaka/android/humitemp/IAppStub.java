
package net.cattaka.android.humitemp;

import net.cattaka.android.humitemp.db.DbHelper;
import net.cattaka.android.humitemp.util.MyPreference;
import net.cattaka.libgeppa.IGeppaService;

public interface IAppStub {
    public IGeppaService getGeppaService();

    public DbHelper getDroiballDatabase();

    public MyPreference getMyPreference();
}
