
package net.cattaka.android.foxkehrobo.fragment;

import java.util.ArrayList;
import java.util.List;

import net.cattaka.android.foxkehrobo.R;
import net.cattaka.android.foxkehrobo.activity.SelectDeviceActivity;
import net.cattaka.android.foxkehrobo.core.ServiceWrapper;
import net.cattaka.libgeppa.data.DeviceEventCode;
import net.cattaka.libgeppa.data.DeviceInfo;
import net.cattaka.libgeppa.data.DeviceState;

import org.opencv.core.Size;
import org.opencv.highgui.VideoCapture;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

public class ConnectFragment extends BaseFragment implements OnClickListener,
        OnItemSelectedListener {

    private Spinner mPreviewSizeView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_connect, null);

        // Binds event listener
        view.findViewById(R.id.startButton).setOnClickListener(this);
        view.findViewById(R.id.goToSelectDeviceButton).setOnClickListener(this);

        mPreviewSizeView = (Spinner)view.findViewById(R.id.previewSizeSpinner);
        {
            List<String> sizeStrs = new ArrayList<String>();
            {
                VideoCapture mCapture = new VideoCapture();
                mCapture.open(0);
                List<Size> sizes = mCapture.getSupportedPreviewSizes();
                mCapture.release();
                for (Size size : sizes) {
                    sizeStrs.add((int)size.width + "x" + (int)size.height);
                }
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(),
                    android.R.layout.simple_list_item_1, sizeStrs);
            mPreviewSizeView.setAdapter(adapter);
            mPreviewSizeView.setOnItemSelectedListener(this);
            setSelection(mPreviewSizeView, getMyPreference().getPreviewSize());
        }
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        ServiceWrapper service = getServiceWrapper();
        int v = (service != null && service.getCurrentDeviceInfo() != null) ? View.VISIBLE
                : View.INVISIBLE;
        getView().findViewById(R.id.startButton).setVisibility(v);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.startButton) {
            connectToService();
        } else if (v.getId() == R.id.goToSelectDeviceButton) {
            Intent intent = new Intent(getContext(), SelectDeviceActivity.class);
            startActivity(intent);
        }
    }

    private void connectToService() {
        ActionListFragment nextFragment = new ActionListFragment();

        replacePrimaryFragment(nextFragment, false);
    }

    @Override
    public void onDeviceStateChanged(DeviceState state, DeviceEventCode code, DeviceInfo deviceInfo) {
        super.onDeviceStateChanged(state, code, deviceInfo);

        int v = (state == DeviceState.CONNECTED) ? View.VISIBLE : View.INVISIBLE;
        getView().findViewById(R.id.startButton).setVisibility(v);
    }

    public void setSelection(AdapterView<?> view, Object obj) {
        if (obj != null) {
            for (int i = 0; i < view.getCount(); i++) {
                if (obj.equals(view.getItemAtPosition(i))) {
                    view.setSelection(i);
                    break;
                }
            }
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
        if (parent.getId() == R.id.previewSizeSpinner) {
            String item = (String)parent.getItemAtPosition(position);
            getMyPreference().edit();
            getMyPreference().putPreviewSize(item);
            getMyPreference().commit();
        }

    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0) {
        // none
    }
}
