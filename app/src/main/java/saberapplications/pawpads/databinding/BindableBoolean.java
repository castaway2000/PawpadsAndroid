package saberapplications.pawpads.databinding;

import android.databinding.BaseObservable;
import android.databinding.BindingAdapter;
import android.databinding.BindingConversion;
import android.widget.CheckBox;
import android.widget.CompoundButton;

public class BindableBoolean extends BaseObservable {
    private Boolean value = new Boolean(false);

    public Boolean get() {
        return value;
    }

    public void set(Boolean value) {
        if (this.value == null || !this.value.equals(value)) {
            this.value = value;
            notifyChange();
        }
    }

    public BindableBoolean(Boolean value) {
        super();
        this.value = value;
    }

    public BindableBoolean() {
        super();
    }

    @BindingConversion
    public static boolean convertBindableBooleanToBoolean(
            BindableBoolean bindable) {
        return bindable.get();
    }

    @BindingAdapter({"binding2way"})
    public static void bindCheckBox(CheckBox view,
                                    final BindableBoolean bindableBoolean) {


        view.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                bindableBoolean.set(isChecked);
            }
        });

        boolean newValue = bindableBoolean.get();
        if (view.isChecked() != newValue) {
            view.setChecked(newValue);
        }
    }

}