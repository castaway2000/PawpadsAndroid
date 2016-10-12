package saberapplications.pawpads.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.support.design.widget.TabLayout;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.quickblox.core.helper.StringUtils;

import saberapplications.pawpads.R;

/**
 * Created by Stanislav Volnjanskij on 05.10.16.
 */

public class FontTabLayout extends TabLayout {

    private Typeface mTypeface;

    public FontTabLayout(Context context) {
        this(context, null);
    }

    public FontTabLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FontTabLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.FontText);

        if (ta != null) {
            String fontAsset = ta.getString(R.styleable.FontText_typefaceAsset);

            if (fontAsset != null && !StringUtils.isEmpty(fontAsset)) {
                mTypeface = FontManager.getInstance().getFont(fontAsset);
            }

        }
    }


    @Override
    public void addTab(Tab tab) {
        super.addTab(tab);

        ViewGroup mainView = (ViewGroup) getChildAt(0);
        ViewGroup tabView = (ViewGroup) mainView.getChildAt(tab.getPosition());

        int tabChildCount = tabView.getChildCount();
        for (int i = 0; i < tabChildCount; i++) {
            View tabViewChild = tabView.getChildAt(i);
            if (tabViewChild instanceof TextView) {
                if (mTypeface!=null){
                    ((TextView) tabViewChild).setTypeface(mTypeface, Typeface.NORMAL);
                }

            }
        }
    }


}
