package org.mozilla.vrbrowser.ui.widgets.keyboards;

import android.content.Context;

import org.mozilla.vrbrowser.R;
import org.mozilla.vrbrowser.input.CustomKeyboard;
import org.mozilla.vrbrowser.ui.widgets.KeyboardWidget;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class EnglishKeyboard implements KeyboardWidget.KeyboardInterface {
    private Context mContext;
    private CustomKeyboard mKeyboard;

    public EnglishKeyboard(Context aContext) {
        mContext = aContext;
    }

    @NonNull
    @Override
    public CustomKeyboard getAlphabeticKeyboard() {
        if (mKeyboard == null) {
            mKeyboard = new CustomKeyboard(mContext.getApplicationContext(), R.xml.keyboard_qwerty);
        }
        return mKeyboard;
    }

    @Nullable
    @Override
    public Collection<String> getCandidates(String aText) {
        String[] values = new String[]{"a", "b", "c", "d", "e", "f", "g"};
        ArrayList result = new ArrayList();
        Collections.addAll(result, values);
        return result;
        //return null;
    }

    @Override
    public int geName() {
        return R.string.settings_language_english;
    }
}
