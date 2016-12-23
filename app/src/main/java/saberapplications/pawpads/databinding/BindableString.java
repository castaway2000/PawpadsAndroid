package saberapplications.pawpads.databinding;

import android.databinding.BaseObservable;
import android.databinding.BindingAdapter;
import android.databinding.BindingConversion;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import saberapplications.pawpads.R;


public class BindableString extends BaseObservable {
    private String value;

    public String get() {
        return value != null ? value : "";
    }

    public void set(String value) {
        if (this.value == null || !this.value.equals(value)) {
            this.value = value;
            notifyChange();
        }
    }

    public BindableString(String value) {
        super();
        this.value = value;
    }
    public BindableString(){
        super();
    }
    public boolean isEmpty() {
        return value == null || value.isEmpty();
    }
    @BindingConversion
    public static String convertBindableToString(
            BindableString bindableString) {
        return bindableString.get();
    }
    @BindingAdapter({"binding2way"})
    public static void bindEditText(EditText view,
                                    final BindableString bindableString) {

        if (view.getTag(R.id.BIND_ID) == null) {
            view.setTag(R.id.BIND_ID, true);
            view.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start,
                                          int before, int count) {
                    bindableString.set(s.toString());
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });
        }
        String newValue = bindableString.get();
        if (!view.getText().toString().equals(newValue)) {
            view.setText(newValue);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof String){
            return value.equals((String) o);
        }
        if (o instanceof BindableString){
            BindableString b=(BindableString)o;
            return value.equals(b.get());
        }
        return false;
    }
}