package saberapplications.pawpads.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.EditText;

import com.quickblox.core.helper.StringUtils;

import saberapplications.pawpads.R;

/**
 * Created by Stanislav Volnjanskij on 28.09.16.
 */

public class FontEditText extends EditText {
    public FontEditText(Context context) {
        this(context, null);
    }

    public FontEditText(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FontEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        if (isInEditMode())
            return;

        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.FontText);

        if (ta != null) {
            String fontAsset = ta.getString(R.styleable.FontText_typefaceAsset);

            if (fontAsset!=null && !StringUtils.isEmpty(fontAsset)) {
                Typeface tf = FontManager.getInstance().getFont(fontAsset);
                int style = Typeface.NORMAL;
                float size = getTextSize();

                if (getTypeface() != null)
                    style = getTypeface().getStyle();

                if (tf != null)
                    setTypeface(tf, style);
                else
                    Log.d("FontText", String.format("Could not create a font from asset: %s", fontAsset));
            }
        }
    }

    @Override
    public void setError(CharSequence error) {
        String value=this.getText().toString();
        if (value==null || value.equals("")){
            this.setHintTextColor(getResources().getColor(R.color.error));
            this.setHint(error);
        }else {
            super.setError(error);
        }

    }
}
