package org.mozilla.vrbrowser.ui.widgets.keyboards;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

import org.mozilla.vrbrowser.R;
import org.mozilla.vrbrowser.input.CustomKeyboard;
import org.mozilla.vrbrowser.ui.views.AutoCompletionView;
import org.mozilla.vrbrowser.ui.widgets.KeyboardWidget;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ChineseKeyboard implements KeyboardWidget.KeyboardInterface {
    private static final String LOGTAG = "VRB";
    private Context mContext;
    private CustomKeyboard mKeyboard;
    private DBHelper mDB;
    private HashMap<String, KeyMap> mKeymaps = new HashMap<>();

    public ChineseKeyboard(Context aContext) {
        mContext = aContext;
    }

    @NonNull
    @Override
    public CustomKeyboard getAlphabeticKeyboard() {
        if (mKeyboard == null) {
            mKeyboard = new CustomKeyboard(mContext.getApplicationContext(), R.xml.keyboard_qwerty_pinyin);
            loadDatabase();
        }
        return mKeyboard;
    }

    @Nullable
    @Override
    public Collection<AutoCompletionView.Words> getCandidates(String aKey) {
        if (aKey == null) {
            return null;
        }

        aKey = aKey.trim();
        if (aKey.isEmpty()) {
            return null;
        }

        ArrayList<String> displayList = getDisplayCode(aKey);
        int syllables = 0;

        StringBuilder code = new StringBuilder();
        if (displayList != null) {
            syllables = displayList.size();
            for (String display: displayList) {
                if (code.length() != 0) {
                    code.append(' ');
                }
                code.append(display);
            }
        }

        ArrayList<AutoCompletionView.Words> result = new ArrayList<>();
        String candidate = "";
        String tempKey = aKey;
        String remainKey = "";

        // First candidate
        while (tempKey.length() > 0) {
            if (this.hasDisplays(tempKey)){
                candidate += mKeymaps.get(tempKey).displays.get(0).value;
                tempKey = remainKey;
                remainKey = "";
            } else {
                remainKey = tempKey.charAt(tempKey.length() - 1) + remainKey;
                tempKey = tempKey.substring(0, tempKey.length() - 1);
            }
        }
        result.add(new AutoCompletionView.Words(syllables, code.toString(), candidate));

        // Extra candidates
        tempKey = aKey;
        while (tempKey.length() > 0) {
            if (hasDisplays(tempKey)) {
                result.addAll(mKeymaps.get(tempKey).displays);
            }
            KeyMap map = mKeymaps.get(tempKey);
            if (map != null && map.candidates.size() > 0) {
                result.addAll(map.candidates);
            }
            tempKey = tempKey.substring(0, tempKey.length() - 1);
        }

        if (result.size() > 1 && result.get(0).value.equals((result.get(1).value))) {
            result.remove(0);
        }

        return result;
    }

    private ArrayList<String> getDisplayCode(String aKey) {
        if (!aKey.matches("^[a-z]+$")) {
            return null;
        }

        ArrayList<String> result = new ArrayList<>();
        String remain = "";
        while (aKey.length() > 0) {
            if (hasDisplays(aKey)) {
                result.add(mKeymaps.get(aKey).displays.get(0).code);
                aKey = remain;
                remain = "";
            } else {
                remain = aKey.charAt(aKey.length() - 1) + remain;
                aKey = aKey.substring(0, aKey.length() - 1);
            }
        }
        return result.size() > 0 ? result : null;
    }

    @Override
    public boolean supportsAutoCompletion() {
        return true;
    }

    @Override
    public int geName() {
        return R.string.settings_language_simplified_chinese;
    }


    private boolean hasDisplays(String aKey) {
        loadKeymapIfNotLoaded(aKey);
        KeyMap keyMap = mKeymaps.get(aKey);
        return keyMap != null && keyMap.displays.size() > 0;
    }


    private void loadDatabase() {
        try {
            mDB = new DBHelper(mContext);
        }
        catch (Exception ex) {
            Log.e(LOGTAG, "Error reading pinyin database: " + ex.getMessage());
        }
    }

    private void loadKeymapIfNotLoaded(String aKey) {
        if (mKeymaps.containsKey(aKey)) {
            return;
        }
        loadKeymapTable(aKey);
        loadAutoCorrectTable(aKey);
    }

    private final String[] sqliteArgs = new String[1];

    private void loadKeymapTable(String aKey) {
        SQLiteDatabase reader = mDB.getReadableDatabase();
        sqliteArgs[0] = aKey;
        Cursor cursor = reader.rawQuery("SELECT keymap, display, candidates FROM keymaps where keymap = ? ORDER BY _id ASC", sqliteArgs);
        if (!cursor.moveToFirst()) {
            return;
        }
        do {
            String key = getString(cursor, 0);
            String displays = getString(cursor, 1);
            String candidates = getString(cursor, 2);
            addToKeyMap(key, key, displays, candidates);
        } while (cursor.moveToNext());
    }

    private void loadAutoCorrectTable(String aKey) {
        SQLiteDatabase reader = mDB.getReadableDatabase();
        sqliteArgs[0] = aKey;
        Cursor cursor = reader.rawQuery("SELECT inputcode, displaycode, display FROM autocorrect where inputcode = ? ORDER BY _id ASC", sqliteArgs);
        if (!cursor.moveToFirst()) {
            return;
        }
        do {
            String key = getString(cursor, 1);
            String code = getString(cursor, 2);
            String displays = getString(cursor, 3);
            addToKeyMap(key, code, displays);
        } while (cursor.moveToNext());
    }


    private void addToKeyMap(String aKey, String aCode, String aDisplays) {
        addToKeyMap(aKey, aCode, aDisplays, null);
    }

    private void addToKeyMap(String aKey, String aCode, String aDisplays, String aCandidates) {
        if (aKey == null || aKey.isEmpty()) {
            Log.e(LOGTAG, "Pinyin key is null");
            return;
        }
        if (aCode == null || aCode.isEmpty()) {
            Log.e(LOGTAG, "Pinyin code is null");
            return;
        }
        KeyMap keyMap = mKeymaps.get(aKey);
        if (keyMap == null) {
            keyMap = new KeyMap();
            mKeymaps.put(aKey, keyMap);
        }

        if (aDisplays != null && !aDisplays.isEmpty()) {
            String[] displayList = aDisplays.split("\\|");
            for (String display: displayList) {
                keyMap.displays.add(new AutoCompletionView.Words(syllableCount(aCode), aCode, display));
            }
        }


        if (aCandidates != null && !aCandidates.isEmpty()) {
            String[] candidateList = aCandidates.split("\\|");
            for (String candidate: candidateList) {
                keyMap.candidates.add(new AutoCompletionView.Words(syllableCount(aCode), aCode, candidate));
            }
        }

    }

    private int syllableCount(String aCode) {
        if (aCode == null) {
            return 0;
        }
        aCode = aCode.trim();
        if (aCode.isEmpty()) {
            return 0;
        }

        return (int)aCode.chars().filter(ch -> ch == ' ').count() + 1;
    }

    private String getString(Cursor aCursor, int aIndex) {
        if (aCursor.isNull(aIndex)) {
            return null;
        }
        return aCursor.getString(aIndex);
    }


    class KeyMap {
        ArrayList<AutoCompletionView.Words> displays = new ArrayList<>();
        ArrayList<AutoCompletionView.Words> candidates = new ArrayList<>();
    }


    class DBHelper extends SQLiteAssetHelper {
        private static final String DATABASE_NAME = "google_pinyin.db";
        private static final int DATABASE_VERSION = 1;

        public DBHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }
    }
}
