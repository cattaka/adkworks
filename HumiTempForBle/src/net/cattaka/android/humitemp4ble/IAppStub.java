
package net.cattaka.android.humitemp4ble;

import net.cattaka.android.humitemp4ble.core.MyPreference;
import net.cattaka.android.humitemp4ble.db.DbHelper;
import net.cattaka.libgeppa.IPassiveGeppaService;

public interface IAppStub {
    public IPassiveGeppaService getGeppaService();

    public DbHelper getDroiballDatabase();

    public MyPreference getMyPreference();
}
