
package net.cattaka.positiondetector;

import net.cattaka.positiondetector.v4.PositionDetectorFragment;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.widget.TextView;

public class TestActivity extends FragmentActivity implements OnPositionDetectorListener {
    private TextView[] mRelativePosViews;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test);

        PositionDetectorFragment fragment = (PositionDetectorFragment)getSupportFragmentManager()
                .findFragmentByTag("QCAR");
        fragment.setOnPositionDetectorListener(this);

        mRelativePosViews = new TextView[4];
        mRelativePosViews[0] = (TextView)findViewById(R.id.textView1);
        mRelativePosViews[1] = (TextView)findViewById(R.id.textView2);
        mRelativePosViews[2] = (TextView)findViewById(R.id.textView3);
        mRelativePosViews[3] = (TextView)findViewById(R.id.textView4);
    }

    @Override
    public void onPositionDetectEvent(TagState tagState) {
        int index = tagState.getIndex();
        TagEvent tagEvent = tagState.getTagEvent();
        float[] poseMat = tagState.getPoseMats();
        index = index % mRelativePosViews.length;
        if (0 <= index && index < mRelativePosViews.length) {
            if (tagEvent != TagEvent.DISAPPEAR) {
                String pos = String.format("(% .2f, % .2f, % .2f", poseMat[12], poseMat[13],
                        poseMat[14]);
                mRelativePosViews[index].setText(pos);
            } else {
                mRelativePosViews[index].setText("");
            }
        }
    }
}
