package me.zhl.libkeyboard.keyboard;

import android.content.Context;
import android.os.SystemClock;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;


import java.util.Locale;

import me.zhl.libkeyboard.R;


/**
 * 输入密码的安全键盘
 * Created by zhang on 2016/4/7 0007.
 */
public class SafeKeyboard extends PopupWindow implements OnClickListener {

    public static final String TAG = SafeKeyboard.class.getSimpleName();

    private boolean mIsVisible = false;

    private int mMaxLength = 32;

    private int dp4 = 4;
    private int dp24 = 24;

    private Context mContext;

    private SafeInputView mSafeInputView;

    private enum KeyboardType {ALPHABET, ALPHABET_CAPITAL, ALPHABET_CAPITAL_ONCE, NUMBER, SYMBAL};

    private static String[] ALPHABET_TABLE = new String[]{"q", "w", "e", "r", "t", "y", "u", "i", "o", "p",
            "a", "s", "d", "f", "g", "h", "j", "k", "l", "",
            "z", "x", "c", "v", "b", "n", "m"};
    private static String[] ALPHABET_CAPITAL_TABLE =
            new String[]{"Q", "W", "E", "R", "T", "Y", "U", "I", "O", "P",
                    "A", "S", "D", "F", "G", "H", "J", "K", "L", "",
                    "Z", "X", "C", "V", "B", "N", "M"};

    private static String[] NUMBER_TABLE = new String[]{"1", "2", "3", "4", "5", "6", "7", "8", "9", "0",
            "-", "/", ":", ";", "(", ")", "$", "&", "@", "\"",
            ".", ",", "?", "!", "\'", "", ""};

    private static String[] SYMBOL_TABLE = new String[]{"[", "]", "{", "}", "#", "%", "^", "*", "+", "=",
            "_", "\\", "|", "~", "<", ">", "€", "£", "¥", "·",
            ".", ",", "?", "!", "\'", "", ""};

    private KeyboardType mType = KeyboardType.ALPHABET;

    private int[] mBtnIds = new int[]{R.id.btn_0, R.id.btn_1, R.id.btn_2, R.id.btn_3, R.id.btn_4,
            R.id.btn_5, R.id.btn_6, R.id.btn_7, R.id.btn_8, R.id.btn_9,
            R.id.btn_10, R.id.btn_11, R.id.btn_12, R.id.btn_13, R.id.btn_14,
            R.id.btn_15, R.id.btn_16, R.id.btn_17, R.id.btn_18, R.id.btn_19,
            R.id.btn_20, R.id.btn_21, R.id.btn_22, R.id.btn_23, R.id.btn_24,
            R.id.btn_25, R.id.btn_26};

    private TextView[] mTvButtons = new TextView[mBtnIds.length];
    private ImageView mIvDel;
    private TextView mTvSpace;
    private ImageView mIvShift;
    private TextView mTvNumSymbol;
    private TextView mTvChange;
    private TextView mTvFinish;

    private View mRootView;

    private View mContainerView;

    private int[] mKeyboardLocation = new int[2];

    private byte[] mEncryptedData = null;

    private int mLengthOfContent = 0;

    private long mFirstTime = 0L;

    public SafeKeyboard(Context context, SafeInputView safeInputView, View containerView) {
        mSafeInputView = safeInputView;
        mContainerView = containerView;

        if (mSafeInputView != null) {
            mSafeInputView.setKeyboard(this);
        }

        init(context);
    }

    private void init(Context context) {

        mContext = context;

        initVars();
        initView();

        setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        setAnimationStyle(R.style.keyboard_anim_style);

    }

    private void initVars() {
        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metric = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(metric);

        dp4 = (int) (4 * metric.density);
        dp24 = (int) (24 * metric.density);
    }


    private void initView() {
        mRootView = View.inflate(mContext, R.layout.view_keyboard, null);
        mRootView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                mRootView.getLocationOnScreen(mKeyboardLocation);

                scrollContainer();
                return true;
            }
        });
        setContentView(mRootView);
        setWidth(RelativeLayout.LayoutParams.MATCH_PARENT);
        setHeight(RelativeLayout.LayoutParams.WRAP_CONTENT);
        initButtons();
        initButtonsListener();
        mType = KeyboardType.ALPHABET;
        changeToAlphabet();
    }

    /**
     * 设置内容的最大长度
     * @param maxLength
     */
    public void setMaxLength(int maxLength) {
        mMaxLength = maxLength;
    }

    /**
     * 滚动界面，防止键盘挡住输入框
     */
    private void scrollContainer() {
        if (mKeyboardLocation[1] == 0) {
            return;
        }

        int bottom = mSafeInputView.getMeasuredHeight();
        int[] location = new int[2];
        mSafeInputView.getLocationOnScreen(location);

        int delta = location[1] + bottom - mKeyboardLocation[1];
        if (delta > 0) {
            mContainerView.setTop(-delta);
        }
    }

    private void initButtons() {
        for (int i = 0; i < mBtnIds.length; i++) {
            mTvButtons[i] = (TextView) mRootView.findViewById(mBtnIds[i]);
            mTvButtons[i].setText(String.format(Locale.CHINA, "%d", i));
        }

        mIvShift = (ImageView) mRootView.findViewById(R.id.iv_shift);
        mTvNumSymbol = (TextView) mRootView.findViewById(R.id.tv_num_symbol);
        mIvDel = (ImageView) mRootView.findViewById(R.id.btn_del);
        mTvChange = (TextView) mRootView.findViewById(R.id.btn_change);
        mTvSpace = (TextView) mRootView.findViewById(R.id.btn_space);
        mTvFinish = (TextView) mRootView.findViewById(R.id.btn_finish);
    }

    private void initButtonsListener() {
        for (TextView btn : mTvButtons) {
            btn.setOnClickListener(this);
        }

        mIvShift.setOnClickListener(this);
        mTvNumSymbol.setOnClickListener(this);
        mIvDel.setOnClickListener(this);
        mTvChange.setOnClickListener(this);
        mTvSpace.setOnClickListener(this);
        mTvFinish.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.iv_shift || i == R.id.tv_num_symbol) {
            onShift();
        } else if (i == R.id.btn_del) {
            onDel();
        } else if (i == R.id.btn_change) {
            onChange();
        } else if (i == R.id.btn_space) {
            onSpace();
        } else if (i == R.id.btn_finish) {
            onFinish();
        } else {
            onKey(v);
        }
    }

    private int indexButton(int viewId) {
        for (int i = 0; i < mBtnIds.length; i++) {
            if (viewId == mBtnIds[i]) {
                return i;
            }
        }
        return -1;
    }

    /**
     * 获取键盘对应的字符
     * @param index 键的编号
     * @return 对应的字符
     */
    private String getCharacter(int index) {
        try {
            switch (mType) {
                case ALPHABET:
                    return ALPHABET_TABLE[index];
                case ALPHABET_CAPITAL:
                case ALPHABET_CAPITAL_ONCE:
                    return ALPHABET_CAPITAL_TABLE[index];
                case NUMBER:
                    return NUMBER_TABLE[index];
                case SYMBAL:
                    return SYMBOL_TABLE[index];
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return "";
    }

    /**
     * 完成按钮
     */
    private void onFinish() {
        dismiss();
    }

    /**
     * 改变键盘布局
     */
    private void onChange() {
        switch (mType) {
            case ALPHABET:
            case ALPHABET_CAPITAL:
            case ALPHABET_CAPITAL_ONCE:
                mType = KeyboardType.NUMBER;
                break;
            case NUMBER:
            case SYMBAL:
                mType = KeyboardType.ALPHABET;
        }

        changeKeyboardLayout();
    }

    /**
     * 切换大小写、数字、符号等
     */
    private void onShift() {
        if (mType == KeyboardType.ALPHABET) {
            mType = KeyboardType.ALPHABET_CAPITAL_ONCE;
            mFirstTime = SystemClock.uptimeMillis();
        } else if (mType == KeyboardType.ALPHABET_CAPITAL_ONCE) {
            long currentTime = SystemClock.uptimeMillis();
            if (currentTime - mFirstTime < 500) {
                mType = KeyboardType.ALPHABET_CAPITAL;
            } else {
                mType = KeyboardType.ALPHABET;
            }
        } else if (mType == KeyboardType.ALPHABET_CAPITAL) {
            mType = KeyboardType.ALPHABET;
        } else if (mType == KeyboardType.NUMBER) {
            mType = KeyboardType.SYMBAL;
        } else if (mType == KeyboardType.SYMBAL) {
            mType = KeyboardType.NUMBER;
        }
        changeKeys();
    }

    /**
     * 根据键盘类型更改键盘上键
     */
    private void changeKeys() {

        String[] table = null;
        switch (mType) {
            case ALPHABET:
                table = ALPHABET_TABLE;
                mTvChange.setText(R.string.keyboard_number_symbol);
                mTvNumSymbol.setVisibility(View.GONE);
                mIvShift.setVisibility(View.VISIBLE);
                mIvShift.setImageResource(R.drawable.shift_off);
                break;
            case ALPHABET_CAPITAL:
                table = ALPHABET_CAPITAL_TABLE;
                mTvChange.setText(R.string.keyboard_number_symbol);
                mTvNumSymbol.setVisibility(View.GONE);
                mIvShift.setVisibility(View.VISIBLE);
                mIvShift.setImageResource(R.drawable.shift_on);
                break;
            case ALPHABET_CAPITAL_ONCE:
                table = ALPHABET_CAPITAL_TABLE;
                mTvChange.setText(R.string.keyboard_number_symbol);
                mTvNumSymbol.setVisibility(View.GONE);
                mIvShift.setVisibility(View.VISIBLE);
                mIvShift.setImageResource(R.drawable.shift_on);
                break;
            case NUMBER:
                table = NUMBER_TABLE;
                mTvChange.setText(R.string.keyboard_alphabet);
                mTvNumSymbol.setText(R.string.keyboard_symbol);
                mTvNumSymbol.setVisibility(View.VISIBLE);
                mIvShift.setVisibility(View.GONE);
                break;
            case SYMBAL:
                table = SYMBOL_TABLE;
                mTvChange.setText(R.string.keyboard_alphabet);
                mTvNumSymbol.setText(R.string.keyboard_number);
                mTvNumSymbol.setVisibility(View.VISIBLE);
                mIvShift.setVisibility(View.GONE);
                break;
        }

        if (null == table) {
            return;
        }

        for (int i = 0; i < mTvButtons.length; ++i) {
            mTvButtons[i].setText(table[i]);
        }
    }

    /**
     * 更改键盘布局
     */
    private void changeKeyboardLayout() {
        switch (mType) {
            case ALPHABET:
            case ALPHABET_CAPITAL:
                changeToAlphabet();
                break;
            case NUMBER:
            case SYMBAL:
                changeToNumberOrSymbol();
        }

    }

    /**
     * 从字母键盘切换到数字符号键盘
     */
    private void changeToNumberOrSymbol() {

        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) (mTvButtons[10].getLayoutParams());
        lp.setMargins(0, 0, 0, 0);
        lp = (LinearLayout.LayoutParams) (mTvButtons[18].getLayoutParams());
        lp.setMargins(dp4, 0, 0, 0);

        mTvButtons[19].setVisibility(View.VISIBLE);
        mTvButtons[25].setVisibility(View.GONE);
        mTvButtons[26].setVisibility(View.GONE);

        changeKeys();
    }

    /**
     * 从数字符号键盘切换到字母键盘
     */
    private void changeToAlphabet() {

        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) (mTvButtons[10].getLayoutParams());
        lp.setMargins(dp24, 0, 0, 0);
        lp = (LinearLayout.LayoutParams) (mTvButtons[18].getLayoutParams());
        lp.setMargins(dp4, 0, dp24, 0);

        mTvButtons[19].setVisibility(View.GONE);
        mTvButtons[25].setVisibility(View.VISIBLE);
        mTvButtons[26].setVisibility(View.VISIBLE);

        changeKeys();
    }

    @Override
    public void dismiss() {
        mContainerView.setTop(0);
        super.dismiss();
    }

    /**
     * 显示键盘到屏幕底部
     */
    public void show() {
        if (isShowing()) {
            return;
        }
        // 关闭系统键盘
        InputMethodManager imm = (InputMethodManager) mContext
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mSafeInputView.getWindowToken(), 0);

        scrollContainer();
        showAtLocation(mSafeInputView, Gravity.BOTTOM, 0, 0);
    }

    /**
     * 按键时间
     * @param v 键
     */
    private void onKey(View v) {
        int index = indexButton(v.getId());
        String str = getCharacter(index);

        add(str);

        if (mType == KeyboardType.ALPHABET_CAPITAL_ONCE) {
            mType = KeyboardType.ALPHABET;
            changeKeys();
        }
    }

    private void onSpace() {
        add(" ");
    }

    /**
     * 删除事件
     */
    private void onDel() {
        if (mLengthOfContent > 0) {
            StringBuffer sb = new StringBuffer();
            if (mEncryptedData != null) {
                byte[] data = KeyboardEncryptTools.decrypt(mEncryptedData);
                sb.append(new String(data));
            }
            sb.delete(sb.length() - 1, sb.length());
            mEncryptedData = KeyboardEncryptTools.encrypt(sb);

            --mLengthOfContent;

            if (mSafeInputView != null) {
                if (mIsVisible) {
                    mSafeInputView.setText(sb.toString());
                } else {
                    mSafeInputView.setText(getSizeSpanUseDip());
                }
                mSafeInputView.setSelection(mSafeInputView.length());
            }

            sb.delete(0, sb.length());
        }
    }

    /**
     * 添加内容
     * @param character 键盘对应的字符
     */
    private void add(String character) {

        if(mLengthOfContent == mMaxLength) {
            return;
        }

        ++mLengthOfContent;


        StringBuffer stringBuffer = new StringBuffer();
        if (mEncryptedData != null) {
            byte[] data = KeyboardEncryptTools.decrypt(mEncryptedData);
            stringBuffer.append(new String(data));
        }
        stringBuffer.append(character);
        mEncryptedData = KeyboardEncryptTools.encrypt(stringBuffer);

        if (mSafeInputView != null) {
            if (mIsVisible) {
                mSafeInputView.setText(stringBuffer.toString());
            } else {
                mSafeInputView.setText(getSizeSpanUseDip());
            }
            mSafeInputView.setSelection(mSafeInputView.length());
        }

        stringBuffer.delete(0, stringBuffer.length());
    }

    /**
     * 根据内容长度获取用于显示的密文内容
     * @return 用于显示的内容，例如：‘····’
     */
    public SpannableString getSizeSpanUseDip() {
        int dipSize = 24;
        if (mLengthOfContent <= 0) {
            return new SpannableString("");
        }
        StringBuilder sb = new StringBuilder(mLengthOfContent);
        for (int i = 0; i < mLengthOfContent; i++) {
            sb.append('\u2022');
        }
        SpannableString ss = new SpannableString(sb.toString());
        ss.setSpan(new AbsoluteSizeSpan(dipSize, true), 0, mLengthOfContent, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return ss;
    }

    /**
     * 设置是否显示密码
     * @param isVisible 是否显示密码
     */
    public void setContentVisible(boolean isVisible) {
        mIsVisible = isVisible;
        if (mIsVisible) {
            if (mEncryptedData != null) {
                byte[] data = KeyboardEncryptTools.decrypt(mEncryptedData);
                mSafeInputView.setText(new String(data));
                mSafeInputView.setSelection(mSafeInputView.length());
            }
        } else {
            mSafeInputView.setText(getSizeSpanUseDip());
            mSafeInputView.setSelection(mSafeInputView.length());
        }
    }

    /**
     * 返回经过输入内容
     * @return 密文
     */
    public String getContent() {
        byte[] data = KeyboardEncryptTools.decrypt(mEncryptedData); //加密
        String ret = new String(data);
        if (null != mSafeInputView) {
            mSafeInputView.clear();
        }
        clearContent();
        return ret;
    }

    /**
     * 返回未加密的内容
     * @return 明文
     */
    public String getRawContent() {
        byte[] data = KeyboardEncryptTools.decrypt(mEncryptedData); //加密
        String ret = new String(data);
        if (null != mSafeInputView) {
            mSafeInputView.clear();
        }
        clearContent();
        return ret;
    }

    /**
     * 清空键盘内容
     */
    public void clearContent() {
        mEncryptedData = null;
        mLengthOfContent = 0;
    }
}
