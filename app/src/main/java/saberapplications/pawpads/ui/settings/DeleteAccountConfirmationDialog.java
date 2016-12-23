package saberapplications.pawpads.ui.settings;

import android.app.Dialog;
import android.databinding.DataBindingUtil;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import saberapplications.pawpads.R;
import saberapplications.pawpads.databinding.DialogDeleteAccountBinding;

/**
 * Created by Stanislav Volnjanskij on 11/11/16.
 */

public class DeleteAccountConfirmationDialog extends DialogFragment{

    DialogDeleteAccountBinding binding;
    public interface Callback{
        void onDelete();
    }

    Callback callback;

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout to use as dialog or embedded fragment
        View view= inflater.inflate(R.layout.dialog_delete_account, container, false);
        binding=DataBindingUtil.bind(view);
        binding.setDialog(this);
        return view;
    }

    /** The system calls this only when creating the layout in a dialog. */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // The only reason you might override this method when using onCreateView() is
        // to modify any dialog characteristics. For example, the dialog includes a
        // title by default, but your custom layout might not need it. So here you can
        // remove the dialog title, but you must call the superclass to get the Dialog.
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        return dialog;
    }
    public void delete(){
        dismiss();
        if (callback!=null){
            callback.onDelete();
        }

    }
    public void cancel(){
        dismiss();
    }
}
