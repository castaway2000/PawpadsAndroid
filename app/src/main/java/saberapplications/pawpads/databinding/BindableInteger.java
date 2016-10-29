package saberapplications.pawpads.databinding;

import android.databinding.BaseObservable;
import android.databinding.BindingAdapter;
import android.databinding.BindingConversion;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import saberapplications.pawpads.R;


/**
 * Created by Stanislav Volnjanskij on 25.08.16.
 */

public class BindableInteger extends BaseObservable {

    private Integer value;

    public Integer get() {
        return value;
    }

    public void set(int value) {
        if (this.value == null || !this.value.equals(value)) {
            this.value = value;
            notifyChange();
        }
    }

    public BindableInteger(int value) {
        super();
        this.value = value;
    }

    public BindableInteger() {
        super();
    }

    @BindingConversion
    public static String convertIntegerToString(BindableInteger value) {
        if (value != null && value.get()!=null)
            return String.format("%d", value.get());
        else {
            return null;
        }
    }
    @BindingConversion
    public static int convertIntegerToInt(BindableInteger value) {
        if (value != null && value.get()!=null)
            return value.get().intValue();
        else {
            return 0;
        }
    }

    @BindingAdapter({"binding2way"})
    public static void bindEditText(EditText view,
                                    final BindableInteger bindableInteger) {

        if (view.getTag(R.id.BIND_ID) == null) {
            view.setTag(R.id.BIND_ID, true);
            view.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start,
                                          int before, int count) {
                    try {
                        bindableInteger.set(Integer.parseInt(s.toString()));
                    } catch (Exception e) {

                    }

                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });
        }
        //initial value
        if (bindableInteger == null) return;
        Integer newValue = bindableInteger.get();
        if (newValue == null) return;
        if (!view.getText().toString().equals(newValue.toString())) {
            view.setText(newValue.toString());
        }


    }
}
