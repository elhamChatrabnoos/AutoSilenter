package com.android.silent.autosilenter.Dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.android.silent.autosilenter.MainActivity;
import com.android.silent.autosilenter.R;
import com.android.silent.autosilenter.databinding.LanguageDialogBinding;

import java.util.Locale;

public class LanguageSetDialog extends DialogFragment {
    public static final String LANGUAGE_PREF = "languagePref";
    public static final String ENGLISH_LAN = "english_lan";
    public static final String PERSIAN_LAN = "persian_lan";
    public static final String PERSIAN_LAN1 = "persian_lan";
    private LanguageDialogBinding binding;
    public static boolean persian = false;
    public static boolean english = false;
    public static SharedPreferences sharedPreferences;
    public static SharedPreferences.Editor editor;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        binding = LanguageDialogBinding.inflate(getLayoutInflater());
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(binding.getRoot());

        sharedPreferences = getActivity().getSharedPreferences(LANGUAGE_PREF, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        // select the value of radio button when dialog open
        if (sharedPreferences.getBoolean(ENGLISH_LAN, true)){
            binding.secondRadio.setChecked(true);
        }
        else if (sharedPreferences.getBoolean(PERSIAN_LAN, true)){
            binding.firstRadio.setChecked(true);
        }

        builder.setPositiveButton(R.string.confirm_txt, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (english){
                    changeLanguage("en", getContext());
                    editor.putBoolean(ENGLISH_LAN, true);
                    editor.putBoolean(PERSIAN_LAN1, false);
                    editor.commit();
                }
                if (persian){
                    changeLanguage("fa", getContext());
                    editor.putBoolean(PERSIAN_LAN, true);
                    editor.putBoolean(ENGLISH_LAN, false);
                    editor.commit();
                }
                restartTheApp(getContext());
            }
        });
        binding.firstRadio.setOnClickListener(view -> {
            persian = true;
            english = false;
        });
        binding.secondRadio.setOnClickListener(view -> {
            english = true;
            persian = false;
        });
        return builder.create();
    }

    private void restartTheApp(Context context) {
        Activity activity = (Activity) context;
        activity.finish();
        Intent intent = new Intent(context, MainActivity.class);
        activity.startActivity(intent);
    }

    public static void changeLanguage(String language, Context context) {
        Locale locale = new Locale(language);
        Locale.setDefault(locale);
        Resources resources = context.getResources();
        Configuration configuration = resources.getConfiguration();
        configuration.setLocale(locale);
        configuration.setLayoutDirection(locale);
        context.getResources().updateConfiguration(configuration, resources.getDisplayMetrics());
    }
}
