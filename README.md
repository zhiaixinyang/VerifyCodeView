# VerifyCodeView  
###自定义的验证码View，支持如下俩种显示模式

![](https://github.com/zhiaixinyang/VerifyCodeView/blob/master/show/line.png)

![](https://github.com/zhiaixinyang/VerifyCodeView/blob/master/show/circle.png)

###使用方式Gradle依赖：
```xml
compile 'verifycode.com.verifycode:verifycodeview:1.0'
```

###XML的使用：

**width使用match_parent**
```xml

    <verifycode.com.verifycode.VerifyCodeView
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        app:verifyCodeMode="1"
        app:verifyCodeBorderColor="@color/black"
        app:verifyCodeCursorColor="@color/red"
        app:verifyCodeTextColor="@color/green"
        app:verifyCodeTextSize="16sp"
        app:verifyCodeLength="4" />

```

###自定义的全属性：
```xml
<attr name="verifyCodeLength" format="integer" />
<attr name="verifyCodeMode" format="integer" />
<attr name="verifyCodePadding" format="dimension" />
<attr name="verifyCodeBorderColor" format="color" />
<attr name="verifyCodeBorderWidth" format="dimension" />
<attr name="verifyCodeTextSize" format="dimension" />
<attr name="verifyCodeTextColor" format="color" />
<attr name="verifyCodeCursorFlashTime" format="integer" />
<attr name="isVerifyCodeCursorEnable" format="boolean" />
<attr name="verifyCodeCursorColor" format="color" />
```
- 上诉的自定义属性的作用依次是：
- 验证码的个数
- 验证码的显示模式：0为横线，1为圆形
- 每个验证码的间距
- 验证码线段的颜色
- 验证码线段的宽度
- 验证码数字的大小
- 验证码数字的颜色
- 验证码光标闪烁间隔
- 是否开启光标
- 验证码光标的颜色
