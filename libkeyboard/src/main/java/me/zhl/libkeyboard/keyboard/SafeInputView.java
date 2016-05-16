package me.zhl.libkeyboard.keyboard;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;

import java.lang.reflect.Method;


/**
 * 安全键盘输入控件
 * Created by zhang on 2016/4/18 0018.
 */
public class SafeInputView extends EditText {
    private SafeKeyboard mKeyboard;

    public SafeInputView(Context context) {
        super(context);
        init();
    }

    public SafeInputView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SafeInputView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setSingleLine(true);
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mKeyboard != null && !mKeyboard.isShowing()) {
                    hideSoftInputMethod();
                    mKeyboard.show();
                }
            }
        });

        setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {

                if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_BACK) {
                    if (mKeyboard.isShowing()) {
                        hideKeyboard();
                        return true;
                    }
                }
                return false;
            }
        });
    }

    @Override
    protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {

        if ( mKeyboard != null) {
            if (focused) {
                hideSoftInputMethod();
                mKeyboard.show();
            } else {
                mKeyboard.dismiss();
            }
        }

        super.onFocusChanged(focused, direction, previouslyFocusedRect);
    }

    public void setKeyboard(SafeKeyboard safeKeyboard) {
        mKeyboard = safeKeyboard;
    }

    public void clear() {
        setText("");
        if (mKeyboard != null) {
            mKeyboard.clearContent();
        }
    }

    public void initKeyboard(View rootView) {
        mKeyboard = new SafeKeyboard(getContext(), this, rootView);
    }

    public void setContentVisible(boolean isVisible) {
        mKeyboard.setContentVisible(isVisible);
    }

    public String getContentWithPublicKey() {
        return mKeyboard != null ? mKeyboard.getContentWithPublicKey() : "";
    }

    public String getRawContent() {
        return mKeyboard != null ? mKeyboard.getRawContent() : "";
    }

    public String getContent() {
        return mKeyboard != null ? mKeyboard.getContent() : "";
    }


    public void hideKeyboard() {
        if (mKeyboard != null) {
            mKeyboard.dismiss();
        }
    }

    private void hideSoftInputMethod() {
        Context context = getContext();
        if (context instanceof Activity) {
            ((Activity) context).getWindow().setSoftInputMode(
                    WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        }

        int currentVersion = android.os.Build.VERSION.SDK_INT;
        String methodName = null;
        if (currentVersion >= 16) {
            // 4.2
            methodName = "setShowSoftInputOnFocus";
        } else if (currentVersion >= 14) {
            // 4.0
            methodName = "setSoftInputShownOnFocus";
        }
        if (methodName == null) {
            this.setInputType(InputType.TYPE_NULL);
        } else {
            Class<EditText> cls = EditText.class;
            Method setShowSoftInputOnFocus;
            try {
                setShowSoftInputOnFocus = cls.getMethod(methodName, boolean.class);
                setShowSoftInputOnFocus.setAccessible(true);
                setShowSoftInputOnFocus.invoke(this, false);
            } catch (NoSuchMethodException e) {
                this.setInputType(InputType.TYPE_NULL);
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
