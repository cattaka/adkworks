
package net.cattaka.android.humitemp4pd.fragment;

import net.cattaka.android.humitemp4pd.IAppListener;
import net.cattaka.android.humitemp4pd.IAppStub;
import net.cattaka.android.humitemp4pd.db.DbHelper;
import net.cattaka.android.humitemp4pd.util.MyPreference;
import net.cattaka.libgeppa.IGeppaService;
import android.support.v4.app.Fragment;

public abstract class BaseFragment extends Fragment implements IAppListener {
    protected IGeppaService getGeppaService() {
        return ((IAppStub)getActivity()).getGeppaService();
    }

    protected DbHelper getDroiballDatabase() {
        return ((IAppStub)getActivity()).getDroiballDatabase();
    }

    protected IAppStub getAppStub() {
        return (IAppStub)getActivity();
    }

    public MyPreference getMyPreference() {
        return getAppStub().getMyPreference();
    }
}
