package org.mozilla.vrbrowser.ui.widgets.keyboards;

import android.content.Context;

import org.mozilla.vrbrowser.R;
import org.mozilla.vrbrowser.input.CustomKeyboard;
import org.mozilla.vrbrowser.ui.views.AutoCompletionView;
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
    public Collection<AutoCompletionView.Words> getCandidates(String aText) {
        return null;
    }

    @Override
    public int geName() {
        return R.string.settings_language_english;
    }
}
