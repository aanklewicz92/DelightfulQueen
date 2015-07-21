package com.droidsonroids.delightfulqueen;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;

import com.droidsonroids.awesomeprogressbar.AwesomeProgressBar;
import com.droidsonroids.awesomeprogressbar.IAnimationStateListener;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends Activity implements IAnimationStateListener {

    @Bind(R.id.super_awesome_progress_bar)
    AwesomeProgressBar mSuperAwesomeProgressBar;

    @Bind(R.id.button_success)
    Button mButtonSuccess;
    @Bind(R.id.button_failure)
    Button mButtonFailure;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_custom_view);
        ButterKnife.bind(this);

        mSuperAwesomeProgressBar.setIAnimationStateListener(this);
    }

    @OnClick(R.id.button_success)
    public void onClickSuccess() {
        mSuperAwesomeProgressBar.play(true);
        setButtonsState(false);
    }

    @OnClick(R.id.button_failure)
    public void onClickFailure() {
        mSuperAwesomeProgressBar.play(false);
        setButtonsState(false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.unbind(this);
    }

    @Override
    public void finished() {
        setButtonsState(true);
    }

    private void setButtonsState(boolean state) {
        mButtonSuccess.setEnabled(state);
        mButtonFailure.setEnabled(state);
    }
}
