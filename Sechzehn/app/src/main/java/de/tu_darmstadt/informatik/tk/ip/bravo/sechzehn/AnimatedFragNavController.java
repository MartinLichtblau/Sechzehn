package de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn;

import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;

import com.ncapdevi.fragnav.FragNavController;
import com.ncapdevi.fragnav.FragNavTransactionOptions;

import java.util.Stack;

/**
 * Created by Alexander Geiß on 20.07.2017.
 */

public class AnimatedFragNavController {
    private FragNavController fragNavController;

    public static final int NO_TAB = -1;
    public static final int TAB1 = 0;
    public static final int TAB2 = 1;
    public static final int TAB3 = 2;
    public static final int TAB4 = 3;
    public static final int TAB5 = 4;

    public AnimatedFragNavController(FragNavController fragNavController) {
        this.fragNavController = fragNavController;
    }
    private static final FragNavTransactionOptions DEFAULT_SWITCH_TRANSITION =FragNavTransactionOptions.newBuilder().transition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).build();
    public void switchTab(int index, @Nullable FragNavTransactionOptions transactionOptions) throws IndexOutOfBoundsException {
        fragNavController.switchTab(index, transactionOptions);
    }

    public void switchTab(int index) throws IndexOutOfBoundsException {
        fragNavController.switchTab(index, DEFAULT_SWITCH_TRANSITION);
    }

    public void pushFragment(@Nullable Fragment fragment, @Nullable FragNavTransactionOptions transactionOptions) {
        fragNavController.pushFragment(fragment, transactionOptions);

    }
 private static final FragNavTransactionOptions DEFAULT_PUSH_TRANSITION =FragNavTransactionOptions.newBuilder().transition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).build();
    public void pushFragment(@Nullable Fragment fragment) {
        fragNavController.pushFragment(fragment, DEFAULT_PUSH_TRANSITION);
    }

    public void popFragment(@Nullable FragNavTransactionOptions transactionOptions) throws UnsupportedOperationException {
        fragNavController.popFragments(1, transactionOptions);
    }
    private static final FragNavTransactionOptions DEFAULT_POP_TRANSITION =FragNavTransactionOptions.newBuilder().transition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE).build();
    public void popFragment() throws UnsupportedOperationException {
        fragNavController.popFragment(DEFAULT_POP_TRANSITION);
    }

    public void popFragments(int popDepth, @Nullable FragNavTransactionOptions transactionOptions) throws UnsupportedOperationException {
        fragNavController.popFragments(popDepth, transactionOptions);
    }

    public void popFragments(int popDepth) throws UnsupportedOperationException {
        fragNavController.popFragments(popDepth, DEFAULT_POP_TRANSITION);
    }

    public void clearStack(@Nullable FragNavTransactionOptions transactionOptions) {
        fragNavController.clearStack(transactionOptions);
    }

    public void clearStack() {
        fragNavController.clearStack(DEFAULT_POP_TRANSITION);
    }

    public void replaceFragment(@NonNull Fragment fragment, @Nullable FragNavTransactionOptions transactionOptions) {
        fragNavController.replaceFragment(fragment, transactionOptions);
    }

    public void replaceFragment(@NonNull Fragment fragment) {
        fragNavController.replaceFragment(fragment, DEFAULT_SWITCH_TRANSITION);
    }

    @Nullable
    @CheckResult
    public DialogFragment getCurrentDialogFrag() {
        return fragNavController.getCurrentDialogFrag();
    }

    public void clearDialogFragment() {
        fragNavController.clearDialogFragment();

    }

    public void showDialogFragment(@Nullable DialogFragment dialogFragment) {
        fragNavController.showDialogFragment(dialogFragment);
    }


    @Nullable
    @CheckResult
    public Fragment getCurrentFrag() {
        return fragNavController.getCurrentFrag();
    }

    @CheckResult
    public int getSize() {
        return fragNavController.getSize();
    }

    @CheckResult
    @Nullable
    public Stack<Fragment> getStack(int index) {
        return fragNavController.getStack(index);
    }

    @CheckResult
    @Nullable
    public Stack<Fragment> getCurrentStack() {
        return fragNavController.getCurrentStack();
    }

    @CheckResult
    public int getCurrentStackIndex() {
        return fragNavController.getCurrentStackIndex();
    }

    @CheckResult
    public boolean isRootFragment() {
        return fragNavController.isRootFragment();
    }

    public void onSaveInstanceState(@NonNull Bundle outState) {
        fragNavController.onSaveInstanceState(outState);
    }
}
