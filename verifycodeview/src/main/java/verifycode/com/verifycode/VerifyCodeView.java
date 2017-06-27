package verifycode.com.verifycode;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.InputType;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by MBENBEN on 2017/6/26.
 */

public class VerifyCodeView extends View {
    private int verifyCodeMode;
    private int verifyCodeLength;//验证码的个数
    private int textSize ;//单个密码大小
    private int borderColor;//边框颜色
    private int borderWidth;//下划线粗细
    private int cursorWidth;//光标粗细
    private int cursorHeight;//光标长度
    private int cursorColor;//光标颜色
    private int textColor;
    private boolean isCursorShowing;//光标是否正在显示
    private boolean isCursorEnable;//是否开启光标
    private boolean isInputComplete;//是否输入完毕
    //private boolean isCanLetter;//是否支持字母
    private long cursorFlashTime;//光标闪动间隔时间
    private int verifyCodePadding;//每个验证码间的间隔

    //private int cipherTextSize;//密文符号大小
    //private boolean cipherEnable;//是否开启密文
    //private static String cipherText; //密文符号
    private String[] verifyCode;//验证码数组
    private InputMethodManager inputManager;

    private Paint borderPaint;
    private Paint cursorPaint;
    private Paint textPaint;
    private Timer timer;
    private TimerTask timerTask;
    private int cursorPosition;//光标位置
    private VerifyCodeListener verifyCodeListener;

    public VerifyCodeView(Context context) {
        super(context);
        init();
    }

    public VerifyCodeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initAttribute(attrs);
        init();
    }

    public VerifyCodeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttribute(attrs);
        init();
    }

    private void initAttribute(AttributeSet attrs) {
        if (attrs != null) {
            TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.VerifyCodeView);
            verifyCodeMode =typedArray.getInteger(R.styleable.VerifyCodeView_verifyCodeMode,0);
            verifyCodeLength = typedArray.getInteger(R.styleable.VerifyCodeView_verifyCodeLength, 4);
            isCursorEnable = typedArray.getBoolean(R.styleable.VerifyCodeView_isVerifyCodeCursorEnable, true);
            cursorFlashTime = typedArray.getInteger(R.styleable.VerifyCodeView_verifyCodeCursorFlashTime, 500);
            borderWidth = typedArray.getDimensionPixelSize(R.styleable.VerifyCodeView_verifyCodeBorderWidth, dp2px(3));
            //默认的所有颜色都是黑色的
            borderColor = typedArray.getColor(R.styleable.VerifyCodeView_verifyCodeBorderColor, Color.BLACK);
            cursorColor = typedArray.getColor(R.styleable.VerifyCodeView_verifyCodeCursorColor, Color.BLACK);
            textColor = typedArray.getColor(R.styleable.VerifyCodeView_verifyCodeTextColor,Color.BLACK);
            textSize=typedArray.getDimensionPixelSize(R.styleable.VerifyCodeView_verifyCodeTextSize,sp2px(16));
            verifyCodePadding = typedArray.getDimensionPixelSize(R.styleable.VerifyCodeView_verifyCodePadding, dp2px(10));
            typedArray.recycle();
        }
        verifyCode = new String[verifyCodeLength];
    }

    private void init() {
        setFocusableInTouchMode(true);
        //设置键盘监听
        VerifyCodeKeyListener VerifyCodeKeyListener = new VerifyCodeKeyListener();
        setOnKeyListener(VerifyCodeKeyListener);
        inputManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        //游标画笔初始化
        cursorPaint = new Paint();
        cursorPaint.setAntiAlias(true);
        cursorPaint.setColor(cursorColor);
        cursorPaint.setStrokeWidth(cursorWidth);
        cursorPaint.setStyle(Paint.Style.FILL);

        //边框画笔初始化
        borderPaint=new Paint();
        borderPaint.setAntiAlias(true);
        borderPaint.setColor(borderColor);
        borderPaint.setStrokeWidth(borderWidth);
        borderPaint.setStyle(Paint.Style.FILL);

        //验证码画笔初始化，默认为黑色
        textPaint=new Paint();
        textPaint.setAntiAlias(true);
        textPaint.setColor(textColor);
        textPaint.setTextSize(textSize);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setStyle(Paint.Style.FILL);

        timerTask = new TimerTask() {
            @Override
            public void run() {
                isCursorShowing = !isCursorShowing;
                postInvalidate();
            }
        };
        timer = new Timer();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int width = 0;
        switch (widthMode) {
            case MeasureSpec.UNSPECIFIED:
            case MeasureSpec.AT_MOST:
                //没有指定大小，宽度 = 单个密码框大小 * 密码位数 + 密码框间距 *（密码位数 - 1）
                width = textSize * verifyCodeLength + verifyCodePadding * (verifyCodeLength - 1);
                break;
            case MeasureSpec.EXACTLY:
                //指定大小，宽度 = 指定的大小
                width = MeasureSpec.getSize(widthMeasureSpec);
                //密码框大小等于 (宽度 - 密码框间距 *(密码位数 - 1)) / 密码位数
                textSize = (width - (verifyCodePadding * (verifyCodeLength - 1))) / verifyCodeLength;
                break;
        }
        setMeasuredDimension(width, textSize);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        //光标宽度
        cursorWidth = dp2px(2);
        //光标长度
        cursorHeight = textSize / 2;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //绘制下划线
        if (verifyCodeMode ==0) {
            drawUnderLine(canvas, borderPaint);
        }else {
            drawCircle(canvas, borderPaint);
        }
        //绘制光标
        drawCursor(canvas , cursorPaint);
        drawText(canvas , textPaint);
    }

    /**
     * 绘制数字
     *
     * @param canvas
     * @param paint
     */
    private void drawText(Canvas canvas, Paint paint) {
        //文字居中的处理
        Rect r = new Rect();
        //此时的Rect已经包含了View的坐标信息
        canvas.getClipBounds(r);
        int cHeight = r.height();

        float y = cHeight / 2f;


        //根据输入的验证码位数，进行for循环绘制
        for (int i = 0; i < verifyCodeLength; i++) {
            if (!TextUtils.isEmpty(verifyCode[i])) {
                textPaint.getTextBounds(verifyCode[i],0,verifyCode[i].length(),r);
                // x = paddingLeft + 单个密码框大小/2 + ( 密码框大小 + 密码框间距 ) * i
                // y = paddingTop + 文字居中所需偏移量
                float textY=y+r.height()/2;
                canvas.drawText(verifyCode[i],
                        (getPaddingLeft() + textSize / 2) + (textSize + verifyCodePadding) * i,
                        getPaddingTop() + textY, paint);
            }
        }
    }

    class VerifyCodeKeyListener implements OnKeyListener {
        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            int action = event.getAction();
            if (action == KeyEvent.ACTION_DOWN) {
                //删除操作
                if (keyCode == KeyEvent.KEYCODE_DEL) {
                    if (TextUtils.isEmpty(verifyCode[0])) {
                        return true;
                    }
                    String deleteText = delete();
                    if (verifyCodeListener != null && !TextUtils.isEmpty(deleteText)) {
                        verifyCodeListener.verifyCodeChange(deleteText);
                    }
                    postInvalidate();
                    return true;
                }

                if (keyCode >= KeyEvent.KEYCODE_0 && keyCode <= KeyEvent.KEYCODE_9) {
                    /**
                     * 只支持数字
                     */
                    if (isInputComplete) {
                        return true;
                    }
                    String addText = add((keyCode - 7) + "");
                    if (verifyCodeListener != null && !TextUtils.isEmpty(addText)) {
                        verifyCodeListener.verifyCodeChange(addText);
                    }
                    postInvalidate();
                    return true;
                }

                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    /**
                     * 确认键
                     */
                    if (verifyCodeListener != null) {
                        verifyCodeListener.keyEnterPress(getVerifyCode(), isInputComplete);
                    }
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * 删除
     */
    private String delete() {
        String deleteText = null;
        if (cursorPosition > 0) {
            deleteText = verifyCode[cursorPosition - 1];
            verifyCode[cursorPosition - 1] = null;
            cursorPosition--;
        } else if (cursorPosition == 0) {
            deleteText = verifyCode[cursorPosition];
            verifyCode[cursorPosition] = null;
        }
        isInputComplete = false;
        return deleteText;
    }

    /**
     * 增加
     */
    private String add(String c) {
        String addText = null;
        if (cursorPosition < verifyCodeLength) {
            addText = c;
            verifyCode[cursorPosition] = c;
            cursorPosition++;
            if (cursorPosition == verifyCodeLength) {
                isInputComplete = true;
                if (verifyCodeListener != null) {
                    verifyCodeListener.verifyCodeComplete();
                }
            }
        }
        return addText;
    }

    /**
     * 获取密码
     */
    private String getVerifyCode() {
        StringBuffer stringBuffer = new StringBuffer();
        for (String c : verifyCode) {
            if (TextUtils.isEmpty(c)) {
                continue;
            }
            stringBuffer.append(c);
        }
        return stringBuffer.toString();
    }


    /**
     * 绘制光标
     *
     * @param canvas
     * @param paint
     */
    private void drawCursor(Canvas canvas, Paint paint) {

        //光标未显示 && 开启光标 && 输入位数未满 && 获得焦点
        if (!isCursorShowing && isCursorEnable && !isInputComplete && hasFocus()) {
            // 起始点x = paddingLeft + 单个密码框大小 / 2 + (单个密码框大小 + 密码框间距) * 光标下标
            // 起始点y = paddingTop + (单个密码框大小 - 光标大小) / 2
            // 终止点x = 起始点x
            // 终止点y = 起始点y + 光标高度
            canvas.drawLine((getPaddingLeft() + textSize / 2) + (textSize + verifyCodePadding) * cursorPosition,
                    getPaddingTop() + (textSize - cursorHeight) / 2,
                    (getPaddingLeft() + textSize / 2) + (textSize + verifyCodePadding) * cursorPosition,
                    getPaddingTop() + (textSize + cursorHeight) / 2,
                    paint);
        }
    }

    /**
     * 绘制密码框下划线
     *
     * @param canvas
     * @param paint
     */
    private void drawUnderLine(Canvas canvas, Paint paint) {

        for (int i = 0; i < verifyCodeLength; i++) {
            //根据密码位数for循环绘制直线
            // 起始点x为paddingLeft + (单个密码框大小 + 密码框边距) * i , 起始点y为paddingTop + 单个密码框大小
            // 终止点x为 起始点x + 单个密码框大小 , 终止点y与起始点一样不变
            canvas.drawLine(getPaddingLeft() + (textSize + verifyCodePadding) * i, getPaddingTop() + textSize,
                    getPaddingLeft() + (textSize + verifyCodePadding) * i + textSize, getPaddingTop() + textSize,
                    paint);
        }
    }

    private void drawCircle(Canvas canvas, Paint paint) {
        paint.setColor(borderColor);
        paint.setStrokeWidth(dp2px(2));
        paint.setStyle(Paint.Style.STROKE);
        for (int i = 0; i < verifyCodeLength; i++) {
            float startX = getPaddingLeft() + (textSize + verifyCodePadding) * i;
            float startY = getPaddingTop();
            float stopX = getPaddingLeft() + (textSize + verifyCodePadding) * i + textSize;
            float stopY = getPaddingTop() + textSize;
            canvas.drawCircle((startX+stopX)/2,(startY+stopY)/2,dp2px(20), paint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            /**
             * 弹出软键盘
             */
            requestFocus();
            inputManager.showSoftInput(this, InputMethodManager.SHOW_FORCED);
            return true;
        }
        return super.onTouchEvent(event);
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        if (!hasWindowFocus) {
            inputManager.hideSoftInputFromWindow(this.getWindowToken(), 0);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        //cursorFlashTime为光标闪动的间隔时间
        timer.scheduleAtFixedRate(timerTask, 0, cursorFlashTime);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        timer.cancel();
    }


    private int dp2px(float dp) {
        float scale = getContext().getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    private int sp2px(float spValue) {
        float fontScale = getContext().getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }
    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        outAttrs.inputType = InputType.TYPE_CLASS_NUMBER; //输入类型为数字
        return super.onCreateInputConnection(outAttrs);
    }

    public void setVerifyCodeListener(verifycode.com.verifycode.VerifyCodeView.VerifyCodeListener verifyCodeListener) {
        this.verifyCodeListener = verifyCodeListener;
    }

    public void setTextSize(int textSize) {
        this.textSize = textSize;
        postInvalidate();
    }

    public void setVerifyCodeLength(int verifyCodeLength) {
        this.verifyCodeLength = verifyCodeLength;
        postInvalidate();
    }

    public void setCursorColor(int cursorColor) {
        this.cursorColor = cursorColor;
        postInvalidate();
    }

    /**
     * 验证码监听者
     */
    public interface VerifyCodeListener {
        /**
         * 输入/删除监听
         *
         * @param changeText  输入/删除的字符
         */
        void verifyCodeChange(String changeText);

        /**
         * 输入完成
         */
        void verifyCodeComplete();

        /**
         * 确认键后的回调
         *
         * @param verifyCode   验证码
         * @param isComplete 是否达到要求位数
         */
        void keyEnterPress(String verifyCode, boolean isComplete);

    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable("superState", super.onSaveInstanceState());
        bundle.putStringArray("verifyCode", verifyCode);
        bundle.putInt("cursorPosition", cursorPosition);
        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;
            verifyCode = bundle.getStringArray("verifyCode");
            cursorPosition = bundle.getInt("cursorPosition");
            state = bundle.getParcelable("superState");
        }
        super.onRestoreInstanceState(state);
    }
}
