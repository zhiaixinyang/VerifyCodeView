# VerifyCodeView  自定义的验证码View，支持如下俩种显示模式

![](img_url)
使用方式Gradle依赖：
compile 'verifycode.com.verifycode:verifycodeview:1.0'

XML的使用：
width使用match_parent
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

自定义的全属性：
<attr name="verifyCodeLength" format="integer" />
<attr name="verifyCodeMode" format="integer" />
<attr name="verifyCodePadding" format="dimension" />
<attr name="verifyCodeBorderColor" format="color" />
<attr name="verifyCodeBorderWidth" format="dimension" />
<attr name="verifyCodeTextSize" format="dimension" />
<attr name="verifyCodeCursorFlashTime" format="integer" />
<attr name="isVerifyCodeCursorEnable" format="boolean" />
<attr name="verifyCodeCursorColor" format="color" />
<attr name="verifyCodeTextColor" format="color" />
