
package net.cattaka.android.humitemp4pd;

import net.cattaka.android.humitemp4pd.db.DbHelper;
import net.cattaka.android.humitemp4pd.util.MyPreference;
import net.cattaka.libgeppa.IPassiveGeppaService;

public interface IAppStub {
    public IPassiveGeppaService getGeppaService();

    public DbHelper getDroiballDatabase();

    public MyPreference getMyPreference();
}
