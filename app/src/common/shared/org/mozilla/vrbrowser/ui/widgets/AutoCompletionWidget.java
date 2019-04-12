/* -*- Mode: Java; c-basic-offset: 4; tab-width: 4; indent-tabs-mode: nil; -*-
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.vrbrowser.ui.widgets;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import org.mozilla.geckoview.GeckoSession;
import org.mozilla.vrbrowser.R;
import org.mozilla.vrbrowser.audio.AudioEngine;
import org.mozilla.vrbrowser.browser.SessionStore;
import org.mozilla.vrbrowser.ui.views.UIButton;
import org.mozilla.vrbrowser.ui.views.UITextButton;

import static android.view.Gravity.CENTER_VERTICAL;

public class AutoCompletionWidget extends UIWidget {
    private LinearLayout mFirstLine;
    private ScrollView mScrollView;
    private View mSeparator;
    private int mKeyWidth;
    private int mKeyHeight;

    public AutoCompletionWidget(Context aContext) {
        super(aContext);
        initialize(aContext);
    }

    public AutoCompletionWidget(Context aContext, AttributeSet aAttrs) {
        super(aContext, aAttrs);
        initialize(aContext);
    }

    public AutoCompletionWidget(Context aContext, AttributeSet aAttrs, int aDefStyle) {
        super(aContext, aAttrs, aDefStyle);
        initialize(aContext);
    }

    private void initialize(Context aContext) {
        inflate(aContext, R.layout.autocompletion_bar, this);
        mFirstLine = findViewById(R.id.autoCompletionFirstLine);
        mSeparator = findViewById(R.id.autoCompletionSeparator);
        mScrollView = findViewById(R.id.autoCompletionScroll);
        mKeyWidth = WidgetPlacement.pixelDimension(getContext(), R.dimen.keyboard_key_width);
        mKeyHeight = WidgetPlacement.pixelDimension(getContext(), R.dimen.keyboard_key_height);
    }

    @Override
    protected void initializeWidgetPlacement(WidgetPlacement aPlacement) {
        Context context = getContext();
        aPlacement.width = WidgetPlacement.dpDimension(context, R.dimen.keyboard_alphabetic_width);
        aPlacement.height = WidgetPlacement.dpDimension(context, R.dimen.autocompletion_widget_line_height);
        aPlacement.anchorX = 0.0f;
        aPlacement.anchorY = 1.0f;
        aPlacement.parentAnchorX = 0.0f;
        aPlacement.parentAnchorY = 1.0f;
        int margin = WidgetPlacement.dpDimension(context, R.dimen.autocompletion_widget_margin);
        aPlacement.translationY = aPlacement.height + margin;
        aPlacement.opaque = false;
    }


    public void setItems(Iterable<String> aItems) {
        mFirstLine.removeAllViews();
        for (String item: aItems) {
            UITextButton key = new UITextButton(getContext());
            key.setTintColorList(R.drawable.main_button_icon_color);
            key.setBackground(getContext().getDrawable(R.drawable.keyboard_key_background));
            key.setText(item);
            key.setBackgroundColor(Color.RED);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(mKeyWidth, mKeyHeight);
            params.gravity = CENTER_VERTICAL;
            key.setLayoutParams(params);
            mFirstLine.addView(key);
        }
    }
}
