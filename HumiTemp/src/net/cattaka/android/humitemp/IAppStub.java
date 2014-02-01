
package net.cattaka.android.humitemp;

import net.cattaka.android.humitemp.db.DbHelper;
import net.cattaka.android.humitemp.util.MyPreference;
import net.cattaka.libgeppa.IPassiveGeppaService;

public interface IAppStub {
    public IPassiveGeppaService getGeppaService();

    public DbHelper getDroiballDatabase();

    public MyPreference getMyPreference();
}
