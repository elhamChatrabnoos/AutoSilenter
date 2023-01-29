package com.android.silent.autosilenter.Dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import com.android.silent.autosilenter.R;
import com.android.silent.autosilenter.Services.AlwaysService;
import com.android.silent.autosilenter.databinding.NotifDialogBinding;

public class NotificationSetDialog extends DialogFragment {
    public static final String ALWAYS_NOTIFY = "alwaysNotify";
    public static final String JUST_SET_NOTIFY = "justSetNotify";
    NotifDialogBinding binding;
    public static boolean alwaysNotify = false;
    public static boolean justSetNotify = false;
    private Intent intent;
    SharedPreferences preferences;
    SharedPreferences.Editor editor;


    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        binding = NotifDialogBinding.inflate(getLayoutInflater());
        builder.setView(binding.getRoot());

        // change radio button of language depend on last selected
        preferences = getContext().getSharedPreferences("notificationSet", Context.MODE_MULTI_PROCESS);
        editor = preferences.edit();
        if (preferences.getBoolean(ALWAYS_NOTIFY, true)) {
            binding.firstRadio.setChecked(true);
        } else {
            binding.secondRadio.setChecked(true);
        }

        intent = new Intent(getContext(), AlwaysService.class);
        // when always button clicked
        binding.firstRadio.setOnClickListener(view -> {
            editor.putBoolean(ALWAYS_NOTIFY, true);
            editor.putBoolean(JUST_SET_NOTIFY, false);
            editor.commit();
            alwaysNotify = true;
            justSetNotify = false;
        });

        binding.secondRadio.setOnClickListener(view -> {
            editor.putBoolean(ALWAYS_NOTIFY, false);
            editor.putBoolean(JUST_SET_NOTIFY, true);
            editor.commit();
            justSetNotify = true;
            alwaysNotify = false;
        });
        builder.setPositiveButton(R.string.confirm_txt, (dialogInterface, i) -> {
            if (alwaysNotify) {
                ContextCompat.startForegroundService(requireContext(), intent);
            }
            if (justSetNotify) {
                getContext().stopService(intent);
            }
        });
        return builder.create();
    }
}
