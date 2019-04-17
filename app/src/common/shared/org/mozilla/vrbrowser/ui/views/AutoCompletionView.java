/* -*- Mode: Java; c-basic-offset: 4; tab-width: 4; indent-tabs-mode: nil; -*-
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.vrbrowser.ui.views;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SearchView;

import org.mozilla.geckoview.GeckoSession;
import org.mozilla.vrbrowser.R;
import org.mozilla.vrbrowser.audio.AudioEngine;
import org.mozilla.vrbrowser.browser.SessionStore;
import org.mozilla.vrbrowser.ui.views.CustomUIButton;
import org.mozilla.vrbrowser.ui.views.UIButton;
import org.mozilla.vrbrowser.ui.views.UITextButton;
import org.mozilla.vrbrowser.ui.widgets.UIWidget;
import org.mozilla.vrbrowser.ui.widgets.WidgetPlacement;

import java.util.ArrayList;

import static android.view.Gravity.CENTER;
import static android.view.Gravity.CENTER_VERTICAL;

public class AutoCompletionView extends FrameLayout {
    private LinearLayout mFirstLine;
    private LinearLayout mExtendContent;
    private ScrollView mScrollView;
    private View mSeparator;
    private int mKeyWidth;
    private int mKeyHeight;
    private int mLineHeight;
    private UIButton mExtendButton;
    private int mExtendedHeight;
    private final int kMaxItemsPerLine = 11;
    private ArrayList<Words> mExtraItems = new ArrayList<>();
    private boolean mIsExtended;
    private Delegate mDelegate;

    public interface Delegate {
        void onAutoCompletionItemClick(String aItemName);
    }

    public static class Words {
        public int syllable;
        public String code;
        public String value;

        public Words(int aSyllable, String aCode, String aValue) {
            syllable = aSyllable;
            code = aCode;
            value = aValue;
        }
    }

    public AutoCompletionView(Context aContext) {
        super(aContext);
        initialize(aContext);
    }

    public AutoCompletionView(Context aContext, AttributeSet aAttrs) {
        super(aContext, aAttrs);
        initialize(aContext);
    }

    public AutoCompletionView(Context aContext, AttributeSet aAttrs, int aDefStyle) {
        super(aContext, aAttrs, aDefStyle);
        initialize(aContext);
    }

    private void initialize(Context aContext) {
        inflate(aContext, R.layout.autocompletion_bar, this);
        mFirstLine = findViewById(R.id.autoCompletionFirstLine);
        mSeparator = findViewById(R.id.autoCompletionSeparator);
        mScrollView = findViewById(R.id.autoCompletionScroll);
        mExtendButton = findViewById(R.id.extendButton);
        mExtendButton.setTintColorList(R.drawable.main_button_icon_color);
        mExtendContent = findViewById(R.id.extendContent);
        mExtendButton.setOnClickListener(v -> {
            if (mIsExtended) {
                exitExtend();
            } else {
                enterExtend();
            }
        });
        mFirstLine.removeAllViews();
        mKeyWidth = WidgetPlacement.pixelDimension(getContext(), R.dimen.keyboard_key_width);
        mKeyHeight = WidgetPlacement.pixelDimension(getContext(), R.dimen.keyboard_key_height);
        mLineHeight = WidgetPlacement.pixelDimension(getContext(), R.dimen.autocompletion_widget_line_height);
        mExtendedHeight = mLineHeight * 6;
    }

    public void setExtendedHeight(int aHeight) {
        mExtendedHeight = aHeight;
    }

    public void setDelegate(AutoCompletionView.Delegate aDelegate) {
        mDelegate = aDelegate;
    }

    private UITextButton createButton(Words aWords, OnClickListener aHandler) {
        UITextButton key = new UITextButton(getContext());
        key.setTintColorList(R.drawable.main_button_icon_color);
        key.setBackground(getContext().getDrawable(R.drawable.keyboard_key_background));
        //key.setBackgroundColor(Color.RED);
        if (aHandler != null) {
            key.setOnClickListener(aHandler);
        }
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(mKeyWidth, mKeyHeight);
        params.gravity = CENTER_VERTICAL;
        key.setLayoutParams(params);
        key.setPadding(0, 20, 0, 0);
        key.setIncludeFontPadding(false);
        key.setText(aWords.value);
        key.setTextAlignment(TEXT_ALIGNMENT_CENTER);

        return key;
    }

    private LinearLayout createRow() {
        LinearLayout row = new LinearLayout(getContext());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, mLineHeight);
        row.setLayoutParams(params);
        return row;
    }

    public void setItems(Iterable<Words> aItems) {
        mFirstLine.removeAllViews();
        mExtraItems.clear();
        mExtendContent.removeAllViews();
        if (aItems == null) {
            return;
        }

        int n = 0;
        for (Words item : aItems) {
            if (n <= kMaxItemsPerLine) {
                mFirstLine.addView(createButton(item, clickHandler));
            } else {
                if (n == kMaxItemsPerLine + 1) {
                    mFirstLine.addView(mExtendButton);
                }
                mExtraItems.add(item);
            }
            n++;
        }
    }

    private OnClickListener clickHandler = v -> {
        UITextButton button = (UITextButton) v;
        String key = button.getText().toString();
        if (mIsExtended) {
            exitExtend();
        }
        if (mDelegate != null) {
            mDelegate.onAutoCompletionItemClick(key);
        }
    };

    private void layoutExtendedItems() {
        int index = 0;
        LinearLayout current = createRow();
        for (Words item: mExtraItems) {
            current.addView(createButton(item, clickHandler));
            index++;
            if (index >= kMaxItemsPerLine) {
                mExtendContent.addView(current);
                current = createRow();
                index = 0;
            }
        }
        if (index > 0) {
            mExtendContent.addView(current);
        }
    }

    private void enterExtend() {
        if (mIsExtended) {
            return;
        }
        mIsExtended = true;
        mScrollView.setVisibility(View.VISIBLE);
        mSeparator.setVisibility(View.VISIBLE);
        if (mExtendContent.getChildCount() == 0) {
            layoutExtendedItems();
        }

        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) getLayoutParams();
        params.height = mExtendedHeight;
        setLayoutParams(params);
    }

    private void exitExtend() {
        if (!mIsExtended) {
            return;
        }
        mIsExtended = false;
        mScrollView.setVisibility(View.GONE);
        mSeparator.setVisibility(View.GONE);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) getLayoutParams();
        params.height = WidgetPlacement.pixelDimension(getContext(), R.dimen.autocompletion_widget_line_height);
        setLayoutParams(params);
    }
}
