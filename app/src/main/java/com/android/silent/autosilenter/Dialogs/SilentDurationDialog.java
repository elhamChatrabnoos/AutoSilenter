package com.android.silent.autosilenter.Dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.silent.autosilenter.R;
import com.android.silent.autosilenter.databinding.QuickSilentDuLayoutBinding;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.Arrays;


public class SilentDurationDialog extends BottomSheetDialogFragment {

    private QuickSilentDuLayoutBinding binding;
    InputListener inputListener;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        BottomSheetDialog durationPicker = new BottomSheetDialog(getContext());
        binding  = QuickSilentDuLayoutBinding.inflate(LayoutInflater.from(getContext()));
        inputListener = (InputListener) getContext();
        initializeWheelView();

        // when click out side don t close dialog
        durationPicker.setCanceledOnTouchOutside(false);
        durationPicker.setCancelable(false);
        BottomSheetBehavior behavior = durationPicker.getBehavior();
        behavior.setDraggable(false);
        durationPicker.onAttachedToWindow();
        durationPicker.setContentView(binding.getRoot());
        return durationPicker;
    }

    private void initializeWheelView() {
        String[] times = {getString(R.string.first_item_txt),
                getString(R.string.second_item_txt),
                getString(R.string.third_item_txt),
                getString(R.string.fourth_item_txt)};
        binding.wheelView.setTitles(Arrays.asList(times));
        // when confirm selected item index send
        binding.confirmTxt.setOnClickListener(view -> {
            int focusedIndex = binding.wheelView.getFocusedIndex();
            inputListener.getInput(focusedIndex);
            dismiss();
        });
    }

    public interface InputListener{
        void getInput(int durationItem);
    }
}
