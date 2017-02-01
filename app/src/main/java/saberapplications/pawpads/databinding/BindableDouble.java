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

public class BindableDouble extends BaseObservable {

    private Double value;
    private String format="%f";

    public Double get() {
        return value;
    }

    public void set(double value) {
        if (this.value == null || !this.value.equals(value)) {
            this.value = value;
            notifyChange();
        }
    }
    public void setSilent(double value) {
        if (this.value == null || !this.value.equals(value)) {
            this.value = value;
        }
    }

    public BindableDouble(double value) {
        super();
        this.value = value;
    }

    public BindableDouble() {
        super();
    }

    @BindingConversion
    public static String convertIntegerToString(BindableDouble value) {
        if (value != null && value.get()!=null)
            return String.format(value.getFormat(), value.get());
        else {
            return null;
        }
    }



    @BindingAdapter({"binding2way"})
    public static void bindEditText(EditText view,
                                    final BindableDouble bindableDouble) {

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
                        bindableDouble.setSilent(Double.parseDouble(s.toString()));
                    } catch (Exception e) {

                    }

                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });
        }
        //initial value
        if (bindableDouble == null) return;
        Double newValue = bindableDouble.get();
        if (newValue == null) return;
        String strValue= String.format(bindableDouble.getFormat(),newValue);
        if (!view.getText().toString().equals(strValue) ) {
            view.setText(strValue);
        }

    }

    /**
     * Number format to display in text field
     * @return
     */
    public String getFormat() {
        return format;
    }

    /**
     *Set  number format to display in text field
     * @param format
     */
    public void setFormat(String format) {
        this.format = format;
    }
}
