package saberapplications.pawpads.databinding;

import android.databinding.BindingConversion;

import java.util.Date;

/**
 * Created by Stanislav Volnjanskij on 25.08.16.
 */

public class BindingConverters {
    @BindingConversion
    public static String convertDateToString(Date date) {
        if (date!=null){
            return String.format("%tF",date);
        }else {
            return "-";
        }

    }

}
