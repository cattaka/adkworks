
package net.cattaka.android.foxkehrobo.dialog;

import net.cattaka.android.foxkehrobo.R;
import net.cattaka.android.foxkehrobo.entity.MySocketAddress;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

public class EditAddrresDialog implements DialogInterface.OnClickListener,
        DialogInterface.OnCancelListener {

    public interface IEditAddrresDialogListener {
        public void onEditAddrresDialogFinished(MySocketAddress result);

        public void onEditAddrresDialogDelete(Long id);

        public void onEditAddrresDialogCanceled();
    }

    private AlertDialog mDialog;

    private IEditAddrresDialogListener mListener;

    private EditText mHostNameEdit;

    private EditText mPortEdit;

    private Long mCurrentId;

    private EditAddrresDialog() {

    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (which == DialogInterface.BUTTON_POSITIVE) {
            String hostName = mHostNameEdit.getText().toString();
            int port = net.cattaka.libgeppa.Constants.DEFAULT_SERVER_PORT;
            try {
                port = Integer.parseInt(mPortEdit.getText().toString());
            } catch (NumberFormatException e) {
                // Ignore
            }
            MySocketAddress addr = new MySocketAddress();
            addr.setId(mCurrentId);
            addr.setHostName(hostName);
            addr.setPort(port);
            mListener.onEditAddrresDialogFinished(addr);
        } else if (which == DialogInterface.BUTTON_NEGATIVE) {
            dialog.cancel();
        } else if (which == DialogInterface.BUTTON_NEUTRAL) {
            mListener.onEditAddrresDialogDelete(mCurrentId);
        }
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        mListener.onEditAddrresDialogCanceled();
    }

    public AlertDialog getDialog() {
        return mDialog;
    }

    public void show(MySocketAddress addr) {
        mDialog.show();
        if (addr != null) {
            mDialog.setTitle(R.string.title_edit_socket_address);
            mDialog.getButton(DialogInterface.BUTTON_NEUTRAL).setVisibility(View.VISIBLE);
            mCurrentId = addr.getId();
            mHostNameEdit.setText(addr.getHostName());
            if (addr.getPort() != null) {
                mPortEdit.setText(String.valueOf(addr.getPort()));
            } else {
                mPortEdit.setText(String
                        .valueOf(net.cattaka.libgeppa.Constants.DEFAULT_SERVER_PORT));
            }
        } else {
            mDialog.setTitle(R.string.title_input_socket_address);
            mDialog.getButton(DialogInterface.BUTTON_NEUTRAL).setVisibility(View.INVISIBLE);
            mHostNameEdit.setText("");
            mPortEdit.setText(String.valueOf(net.cattaka.libgeppa.Constants.DEFAULT_SERVER_PORT));
            mCurrentId = null;
        }
    }

    public static EditAddrresDialog createEditAddrresDialog(Context context,
            IEditAddrresDialogListener listener) {
        View view;
        EditAddrresDialog holder = new EditAddrresDialog();
        holder.mListener = listener;
        {
            LayoutInflater inflater = LayoutInflater.from(context);
            view = inflater.inflate(R.layout.dialog_edit_address, null);
            holder.mHostNameEdit = (EditText)view.findViewById(R.id.hostNameEdit);
            holder.mPortEdit = (EditText)view.findViewById(R.id.portEdit);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.title_input_socket_address);
        builder.setView(view);
        builder.setPositiveButton(android.R.string.ok, holder);
        builder.setNegativeButton(android.R.string.cancel, holder);
        builder.setNeutralButton(R.string.btn_delete, holder);
        builder.setOnCancelListener(holder);
        holder.mDialog = builder.create();
        return holder;
    }
}
