package me.zhl.libkeyboard.keyboard;

import android.content.Context;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Locale;

import me.zhl.libkeyboard.R;


/**
 * 输入密码的安全键盘
 * Created by zhang on 2016/4/7 0007.
 */
public class KeyboardView extends View {

    public static final String TAG = KeyboardView.class.getSimpleName();

    private int dp4 = 4;
    private int dp24 = 24;

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

    private KeyboardStatus mKeyboardStatus = KeyboardStatus.ALPHABET;

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

    private long mFirstTime = 0L;


    public KeyboardView(Context context) {
        super(context);
        this.init();
    }

    public KeyboardView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.init();
    }

    private void init() {
        initVars();
        initView();
    }

    private void initVars() {
        dp4 = getContext().getResources().getDimensionPixelOffset(R.dimen.dp4);
        dp24 = getContext().getResources().getDimensionPixelOffset(R.dimen.dp24);
    }


    private void initView() {
        mRootView = View.inflate(getContext(), R.layout.view_keyboard, null);
        initButtons();
        initButtonsListener();
        mKeyboardStatus = KeyboardStatus.ALPHABET;
        changeToAlphabet();
    }

    private void initButtons() {
        for (int i = 0; i < mBtnIds.length; i++) {
            mTvButtons[i] = mRootView.findViewById(mBtnIds[i]);
            mTvButtons[i].setText(String.format(Locale.CHINA, "%d", i));
        }

        mIvShift = mRootView.findViewById(R.id.iv_shift);
        mTvNumSymbol = mRootView.findViewById(R.id.tv_num_symbol);
        mIvDel = mRootView.findViewById(R.id.btn_del);
        mTvChange = mRootView.findViewById(R.id.btn_change);
        mTvSpace = mRootView.findViewById(R.id.btn_space);
        mTvFinish = mRootView.findViewById(R.id.btn_finish);
    }

    private void initButtonsListener() {
        for (TextView btn : mTvButtons) {
            btn.setOnClickListener(this::onKey);
        }

        mIvShift.setOnClickListener(view -> onShift());
        mTvNumSymbol.setOnClickListener(view -> onShift());
        mIvDel.setOnClickListener(view -> onDel());
        mTvChange.setOnClickListener(view -> onChange());
        mTvSpace.setOnClickListener(view -> onSpace());
        mTvFinish.setOnClickListener(view -> onFinish());
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
            switch (mKeyboardStatus) {
                case ALPHABET:
                    return ALPHABET_TABLE[index];
                case ALPHABET_CAPITAL:
                case ALPHABET_CAPITAL_ONCE:
                    return ALPHABET_CAPITAL_TABLE[index];
                case NUMBER:
                    return NUMBER_TABLE[index];
                case SYMBOL:
                    return SYMBOL_TABLE[index];
                default:
                    throw new RuntimeException("Error Character");
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

    }

    /**
     * 改变键盘布局
     */
    private void onChange() {
        switch (mKeyboardStatus) {
            case ALPHABET:
            case ALPHABET_CAPITAL:
            case ALPHABET_CAPITAL_ONCE:
                mKeyboardStatus = KeyboardStatus.NUMBER;
                break;
            case NUMBER:
            case SYMBOL:
                mKeyboardStatus = KeyboardStatus.ALPHABET;
                break;
            default:
                break;
        }

        changeKeyboardLayout();
    }

    /**
     * 切换大小写、数字、符号等
     */
    private void onShift() {
        if (mKeyboardStatus == KeyboardStatus.ALPHABET) {
            mKeyboardStatus = KeyboardStatus.ALPHABET_CAPITAL_ONCE;
            mFirstTime = SystemClock.uptimeMillis();
        } else if (mKeyboardStatus == KeyboardStatus.ALPHABET_CAPITAL_ONCE) {
            long currentTime = SystemClock.uptimeMillis();
            if (currentTime - mFirstTime < 500) {
                mKeyboardStatus = KeyboardStatus.ALPHABET_CAPITAL;
            } else {
                mKeyboardStatus = KeyboardStatus.ALPHABET;
            }
        } else if (mKeyboardStatus == KeyboardStatus.ALPHABET_CAPITAL) {
            mKeyboardStatus = KeyboardStatus.ALPHABET;
        } else if (mKeyboardStatus == KeyboardStatus.NUMBER) {
            mKeyboardStatus = KeyboardStatus.SYMBOL;
        } else if (mKeyboardStatus == KeyboardStatus.SYMBOL) {
            mKeyboardStatus = KeyboardStatus.NUMBER;
        }
        changeKeys();
    }

    /**
     * 根据键盘类型更改键盘上键
     */
    private void changeKeys() {

        String[] table = null;
        switch (mKeyboardStatus) {
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
            case SYMBOL:
                table = SYMBOL_TABLE;
                mTvChange.setText(R.string.keyboard_alphabet);
                mTvNumSymbol.setText(R.string.keyboard_number);
                mTvNumSymbol.setVisibility(View.VISIBLE);
                mIvShift.setVisibility(View.GONE);
                break;
            default:
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
        switch (mKeyboardStatus) {
            case ALPHABET:
            case ALPHABET_CAPITAL:
                changeToAlphabet();
                break;
            case NUMBER:
            case SYMBOL:
                changeToNumberOrSymbol();
                break;
            default:
                break;
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

    /**
     * 按键时间
     * @param v 键
     */
    private void onKey(View v) {
        int index = indexButton(v.getId());
        String str = getCharacter(index);

        add(str);

        if (mKeyboardStatus == KeyboardStatus.ALPHABET_CAPITAL_ONCE) {
            mKeyboardStatus = KeyboardStatus.ALPHABET;
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

    }

    /**
     * 添加内容
     * @param character 键盘对应的字符
     */
    private void add(String character) {

    }
}
