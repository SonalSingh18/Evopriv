/*
 * Copyright (C) 2019-2021 The Evolution X Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.evolution.settings.fragments;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.Settings;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceGroup;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreference;

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.SearchIndexable;

import com.evolution.settings.preference.CustomSeekBarPreference;
import net.margaritov.preference.colorpicker.ColorPickerPreference;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@SearchIndexable(forTarget = SearchIndexable.ALL & ~SearchIndexable.ARC)
public class StatusbarClockSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    private static final String STATUSBAR_CLOCK = "statusbar_clock";
    private static final String STATUSBAR_CLOCK_SECONDS = "statusbar_clock_seconds";
    private static final String STATUSBAR_CLOCK_STYLE = "statusbar_clock_style";
    private static final String STATUSBAR_AM_PM = "statusbar_am_pm";
    private static final String STATUSBAR_CLOCK_DATE_DISPLAY = "clock_date_display";
    private static final String STATUSBAR_CLOCK_DATE_STYLE = "clock_date_style";
    private static final String STATUSBAR_CLOCK_DATE_FORMAT = "clock_date_format";
    private static final String STATUSBAR_CLOCK_DATE_POSITION = "statusbar_clock_date_position";
    private static final String STATUS_BAR_CLOCK_COLOR = "status_bar_clock_color";
    private static final String STATUS_BAR_CLOCK_SIZE  = "status_bar_clock_size";
    private static final String STATUS_BAR_CLOCK_FONT_STYLE  = "status_bar_clock_font_style";
    private static final String QS_HEADER_CLOCK_SIZE = "qs_header_clock_size";
    private static final String QS_HEADER_CLOCK_FONT_STYLE = "qs_header_clock_font_style";
    public static final int CLOCK_DATE_STYLE_LOWERCASE = 1;
    public static final int CLOCK_DATE_STYLE_UPPERCASE = 2;
    private static final int CUSTOM_CLOCK_DATE_FORMAT_INDEX = 18;

    static final int DEFAULT_STATUS_CLOCK_COLOR = 0xffffffff;

    private ListPreference mStatusBarClock;
    private ListPreference mStatusBarAmPm;
    private ListPreference mClockDateDisplay;
    private ListPreference mClockDateStyle;
    private ListPreference mClockDateFormat;
    private ListPreference mClockDatePosition;
    private ColorPickerPreference mClockColor;
    private CustomSeekBarPreference mClockSize;
    private ListPreference mClockFontStyle;
    private CustomSeekBarPreference mQsClockSize;
    private ListPreference mQsClockFontStyle;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.evolution_settings_statusbar_clock);
        final ContentResolver resolver = getActivity().getContentResolver();

        int intColor;
        String hexColor;

        // clock settings
        mStatusBarClock = (ListPreference) findPreference(STATUSBAR_CLOCK_STYLE);
        mStatusBarAmPm = (ListPreference) findPreference(STATUSBAR_AM_PM);
        mClockDateDisplay = (ListPreference) findPreference(STATUSBAR_CLOCK_DATE_DISPLAY);
        mClockDateStyle = (ListPreference) findPreference(STATUSBAR_CLOCK_DATE_STYLE);

        int clockStyle = Settings.System.getInt(resolver,
                Settings.System.STATUSBAR_CLOCK_STYLE, 0);
        mStatusBarClock.setValue(String.valueOf(clockStyle));
        mStatusBarClock.setSummary(mStatusBarClock.getEntry());
        mStatusBarClock.setOnPreferenceChangeListener(this);

        mClockColor = (ColorPickerPreference) findPreference(STATUS_BAR_CLOCK_COLOR);
            mClockColor.setOnPreferenceChangeListener(this);
            intColor = Settings.System.getInt(resolver,
                    Settings.System.STATUS_BAR_CLOCK_COLOR, DEFAULT_STATUS_CLOCK_COLOR);
            hexColor = String.format("#%08x", (0xffffffff & intColor));
            mClockColor.setSummary(hexColor);
            mClockColor.setNewPreviewColor(intColor);

        mClockSize = (CustomSeekBarPreference) findPreference(STATUS_BAR_CLOCK_SIZE);
        int clockSize = Settings.System.getInt(resolver,
                Settings.System.STATUS_BAR_CLOCK_SIZE, 14);
        mClockSize.setValue(clockSize / 1);
        mClockSize.setOnPreferenceChangeListener(this);

        mClockFontStyle = (ListPreference) findPreference(STATUS_BAR_CLOCK_FONT_STYLE);
        int showClockFont = Settings.System.getInt(resolver,
                Settings.System.STATUS_BAR_CLOCK_FONT_STYLE, 0);
        mClockFontStyle.setValue(String.valueOf(showClockFont));
        mClockFontStyle.setOnPreferenceChangeListener(this);

        mQsClockSize = (CustomSeekBarPreference) findPreference(QS_HEADER_CLOCK_SIZE);
        int qsClockSize = Settings.System.getInt(resolver,
                Settings.System.QS_HEADER_CLOCK_SIZE, 14);
        mQsClockSize.setValue(qsClockSize / 1);
        mQsClockSize.setOnPreferenceChangeListener(this);

        mQsClockFontStyle = (ListPreference) findPreference(QS_HEADER_CLOCK_FONT_STYLE);
        int qsClockFont = Settings.System.getInt(resolver,
                Settings.System.QS_HEADER_CLOCK_FONT_STYLE, 0);
        mQsClockFontStyle.setValue(String.valueOf(qsClockFont));
        mQsClockFontStyle.setOnPreferenceChangeListener(this);

        if (DateFormat.is24HourFormat(getActivity())) {
            mStatusBarAmPm.setEnabled(false);
            mStatusBarAmPm.setSummary(R.string.status_bar_am_pm_info);
        } else {
            int statusBarAmPm = Settings.System.getInt(resolver,
                    Settings.System.STATUSBAR_CLOCK_AM_PM_STYLE, 2);
            mStatusBarAmPm.setValue(String.valueOf(statusBarAmPm));
            mStatusBarAmPm.setSummary(mStatusBarAmPm.getEntry());
            mStatusBarAmPm.setOnPreferenceChangeListener(this);
        }

        // date settings
        int clockDateDisplay = Settings.System.getInt(resolver,
                Settings.System.STATUSBAR_CLOCK_DATE_DISPLAY, 0);
        mClockDateDisplay.setValue(String.valueOf(clockDateDisplay));
        mClockDateDisplay.setSummary(mClockDateDisplay.getEntry());
        mClockDateDisplay.setOnPreferenceChangeListener(this);

        int clockDateStyle = Settings.System.getInt(resolver,
                Settings.System.STATUSBAR_CLOCK_DATE_STYLE, 0);
        mClockDateStyle.setValue(String.valueOf(clockDateStyle));
        mClockDateStyle.setSummary(mClockDateStyle.getEntry());
        mClockDateStyle.setOnPreferenceChangeListener(this);

        mClockDateFormat = (ListPreference) findPreference(STATUSBAR_CLOCK_DATE_FORMAT);
        mClockDateFormat.setOnPreferenceChangeListener(this);
        String clkvalue = Settings.System.getString(getActivity().getContentResolver(),
                Settings.System.STATUSBAR_CLOCK_DATE_FORMAT);

        if (clkvalue == null || clkvalue.isEmpty()) {
            clkvalue = "EEE";
        }

        int index = mClockDateFormat.findIndexOfValue((String) clkvalue);
        if (index == -1) {
            mClockDateFormat.setValueIndex(CUSTOM_CLOCK_DATE_FORMAT_INDEX);
        } else {
            mClockDateFormat.setValue(clkvalue);
        }
        parseClockDateFormats();

        mClockDatePosition = (ListPreference) findPreference(STATUSBAR_CLOCK_DATE_POSITION);
        mClockDatePosition.setValue(Integer.toString(Settings.System.getInt(getActivity()
                .getContentResolver(), Settings.System.STATUSBAR_CLOCK_DATE_POSITION, 0)));
        mClockDatePosition.setSummary(mClockDatePosition.getEntry());
        mClockDatePosition.setOnPreferenceChangeListener(this);

        int clockDatePosition = Settings.System.getInt(resolver,
                Settings.System.STATUSBAR_CLOCK_DATE_POSITION, 0);
        mClockDatePosition.setValue(String.valueOf(clockDatePosition));
        mClockDatePosition.setSummary(mClockDatePosition.getEntry());
        mClockDatePosition.setOnPreferenceChangeListener(this);

        setDateOptions();
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        AlertDialog dialog;
	ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mStatusBarClock) {
            int clockStyle = Integer.parseInt((String) newValue);
            int index = mStatusBarClock.findIndexOfValue((String) newValue);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.STATUSBAR_CLOCK_STYLE, clockStyle);
            mStatusBarClock.setSummary(mStatusBarClock.getEntries()[index]);
            return true;
        } else if (preference == mStatusBarAmPm) {
            int statusBarAmPm = Integer.valueOf((String) newValue);
            int index = mStatusBarAmPm.findIndexOfValue((String) newValue);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.STATUSBAR_CLOCK_AM_PM_STYLE, statusBarAmPm);
            mStatusBarAmPm.setSummary(mStatusBarAmPm.getEntries()[index]);
            return true;
        } else if (preference == mClockColor) {
                String hex = ColorPickerPreference.convertToARGB(
                        Integer.valueOf(String.valueOf(newValue)));
                preference.setSummary(hex);
                int intHex = ColorPickerPreference.convertToColorInt(hex);
                Settings.System.putInt(resolver,
                        Settings.System.STATUS_BAR_CLOCK_COLOR, intHex);
                return true;
        }  else if (preference == mClockSize) {
            int width = ((Integer)newValue).intValue();
            Settings.System.putInt(resolver,
                    Settings.System.STATUS_BAR_CLOCK_SIZE, width);
            return true;
        }  else if (preference == mClockFontStyle) {
            int showClockFont = Integer.valueOf((String) newValue);
            int index = mClockFontStyle.findIndexOfValue((String) newValue);
            Settings.System.putInt(resolver, Settings.System.
                STATUS_BAR_CLOCK_FONT_STYLE, showClockFont);
            mClockFontStyle.setSummary(mClockFontStyle.getEntries()[index]);
            return true;
        }  else if (preference == mQsClockSize) {
            int width = ((Integer)newValue).intValue();
            Settings.System.putInt(resolver,
                    Settings.System.QS_HEADER_CLOCK_SIZE, width);
            return true;
        }  else if (preference == mQsClockFontStyle) {
            int qsClockFont = Integer.valueOf((String) newValue);
            int index = mQsClockFontStyle.findIndexOfValue((String) newValue);
            Settings.System.putInt(resolver, Settings.System.
                QS_HEADER_CLOCK_FONT_STYLE, qsClockFont);
            mQsClockFontStyle.setSummary(mQsClockFontStyle.getEntries()[index]);
            return true;
        } else if (preference == mClockDateDisplay) {
            int clockDateDisplay = Integer.valueOf((String) newValue);
            int index = mClockDateDisplay.findIndexOfValue((String) newValue);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.STATUSBAR_CLOCK_DATE_DISPLAY, clockDateDisplay);
            mClockDateDisplay.setSummary(mClockDateDisplay.getEntries()[index]);
            setDateOptions();
            return true;
        } else if (preference == mClockDateStyle) {
            int clockDateStyle = Integer.valueOf((String) newValue);
            int index = mClockDateStyle.findIndexOfValue((String) newValue);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.STATUSBAR_CLOCK_DATE_STYLE, clockDateStyle);
            mClockDateStyle.setSummary(mClockDateStyle.getEntries()[index]);
            parseClockDateFormats();
            return true;
        } else if (preference == mClockDateFormat) {
            int index = mClockDateFormat.findIndexOfValue((String) newValue);

            if (index == CUSTOM_CLOCK_DATE_FORMAT_INDEX) {
                AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
                alert.setTitle(R.string.clock_date_string_edittext_title);
                alert.setMessage(R.string.clock_date_string_edittext_summary);
                 final EditText input = new EditText(getActivity());
                String oldText = Settings.System.getString(
                    getActivity().getContentResolver(),
                    Settings.System.STATUSBAR_CLOCK_DATE_FORMAT);
                if (oldText != null) {
                    input.setText(oldText);
                }
                alert.setView(input);
                 alert.setPositiveButton(R.string.menu_save, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int whichButton) {
                        String value = input.getText().toString();
                        if (value.equals("")) {
                            return;
                        }
                        Settings.System.putString(getActivity().getContentResolver(),
                            Settings.System.STATUSBAR_CLOCK_DATE_FORMAT, value);
                         return;
                    }
                });

                alert.setNegativeButton(R.string.menu_cancel,
                    new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int which) {
                        return;
                    }
                });
                dialog = alert.create();
                dialog.show();
            } else {
                if ((String) newValue != null) {
                    Settings.System.putString(getActivity().getContentResolver(),
                        Settings.System.STATUSBAR_CLOCK_DATE_FORMAT, (String) newValue);
                }
            }
            return true;
        } else if (preference == mClockDatePosition) {
            int val = Integer.parseInt((String) newValue);
            int index = mClockDatePosition.findIndexOfValue((String) newValue);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.STATUSBAR_CLOCK_DATE_POSITION, val);
            mClockDatePosition.setSummary(mClockDatePosition.getEntries()[index]);
            parseClockDateFormats();
            return true;
        }
        return false;
    }

    private void parseClockDateFormats() {
        String[] dateEntries = getResources().getStringArray(R.array.clock_date_format_entries_values);
        CharSequence parsedDateEntries[];
        parsedDateEntries = new String[dateEntries.length];
        Date now = new Date();

        int lastEntry = dateEntries.length - 1;
        int dateFormat = Settings.System.getInt(getActivity()
                .getContentResolver(), Settings.System.STATUSBAR_CLOCK_DATE_STYLE, 0);
        for (int i = 0; i < dateEntries.length; i++) {
            if (i == lastEntry) {
                parsedDateEntries[i] = dateEntries[i];
            } else {
                String newDate;
                CharSequence dateString = DateFormat.format(dateEntries[i], now);
                if (dateFormat == CLOCK_DATE_STYLE_LOWERCASE) {
                    newDate = dateString.toString().toLowerCase();
                } else if (dateFormat == CLOCK_DATE_STYLE_UPPERCASE) {
                    newDate = dateString.toString().toUpperCase();
                } else {
                    newDate = dateString.toString();
                }
                parsedDateEntries[i] = newDate;
            }
        }
        mClockDateFormat.setEntries(parsedDateEntries);
    }

    private void setDateOptions() {
        int enableDateOptions = Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.STATUSBAR_CLOCK_DATE_DISPLAY, 0);
        if (enableDateOptions == 0) {
            mClockDateStyle.setEnabled(false);
            mClockDateFormat.setEnabled(false);
            mClockDatePosition.setEnabled(false);
        } else {
            mClockDateStyle.setEnabled(true);
            mClockDateFormat.setEnabled(true);
            mClockDatePosition.setEnabled(true);
        }
    }

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.EVOLVER;
    }

    /**
     * For Search.
     */

    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider(R.xml.evolution_settings_statusbar_clock);
}
