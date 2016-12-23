package saberapplications.pawpads.databinding;

import android.databinding.BaseObservable;
import android.databinding.BindingAdapter;
import android.databinding.BindingConversion;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;

public class BindableBoolean extends BaseObservable {
    private boolean isNot;
    private Boolean value = new Boolean(false);

    // oposite value
    // user var.not instead of !var
    public  BindableBoolean not;


    public Boolean get() {
        return value;
    }

    public boolean getValue() { return value.booleanValue();}

    public void set(Boolean value) {
        if (this.value == null || !this.value.equals(value)) {
            this.value = value;
            if (not!=null) this.not.set(!value.booleanValue());
            notifyChange();
        }
    }

    public BindableBoolean(Boolean value) {
        this();
        set(value);

    }
    private BindableBoolean(Boolean value,Boolean isNot){
        super();
        this.isNot=isNot;
        set(value);
        not=null;
    }


    public BindableBoolean() {
        super();
        not=new BindableBoolean(true,true);
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
    @BindingAdapter({"android:visibility"})
    public static  void bindVisibility(View view,BindableBoolean bindableBoolean){
        if (bindableBoolean.get()) {
            view.setVisibility(View.VISIBLE);
        }else {
            view.setVisibility(View.GONE);
        }
    }

    @BindingAdapter({"android:visibility"})
    public static  void bindVisibilityToBooolean(View view,boolean b){
        if (b) {
            view.setVisibility(View.VISIBLE);
        }else {
            view.setVisibility(View.GONE);
        }
    }


}