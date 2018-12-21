package org.mukdongjeil.mjchurch.ui;

import androidx.fragment.app.Fragment;

public class BaseFragment extends Fragment {

    protected void setBarTitle(String text) {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).setTitleText(text);
        }
    }

    protected void showLoadingDialog() {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).showLoadingDialog();
        }
    }

    protected void closeLoadingDialog() {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).hideLoadingDialog();
        }
    }

    protected boolean isLoadingDialogShowing() {
        if (getActivity() instanceof MainActivity) {
            return ((MainActivity) getActivity()).isLoadingDialogShowing();
        }

        return false;
    }

}
