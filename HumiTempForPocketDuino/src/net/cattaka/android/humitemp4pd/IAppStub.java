
package net.cattaka.android.humitemp4pd;

import net.cattaka.android.humitemp4pd.db.DbHelper;
import net.cattaka.android.humitemp4pd.util.MyPreference;
import net.cattaka.libgeppa.IGeppaService;

public interface IAppStub {
    public IGeppaService getGeppaService();

    public DbHelper getDroiballDatabase();

    public MyPreference getMyPreference();
}
