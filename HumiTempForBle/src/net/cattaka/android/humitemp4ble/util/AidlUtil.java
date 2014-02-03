
package net.cattaka.android.humitemp4ble.util;

import android.os.RemoteException;
import android.util.SparseArray;
import android.util.SparseIntArray;

public class AidlUtil {
    public interface CallFunction<T> {
        public boolean run(T item) throws RemoteException;
    }

    public static <T> void callMethods(SparseArray<T> items, CallFunction<T> func) {
        SparseIntArray errorItems = null;
        ;
        for (int i = 0; i < items.size(); i++) {
            T item = items.valueAt(i);
            boolean result;
            try {
                result = func.run(item);
            } catch (RemoteException e) {
                result = false;
            }

            if (!result) {
                if (errorItems == null) {
                    errorItems = new SparseIntArray();
                }
                errorItems.put(i, i);
            }
        }
        if (errorItems != null) {
            for (int i = 0; i < errorItems.size(); i++) {
                items.removeAt(errorItems.valueAt(i));
            }
        }
    }
}
