package com.sharedroute.app;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.*;
import android.graphics.Path.Direction;
import android.support.annotation.*;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

public class TooltipView extends TextView {

    private static final int NOT_PRESENT = Integer.MIN_VALUE;
    private int arrowHeight;
    private int arrowWidth;
    private int cornerRadius;
    private @IdRes int anchoredViewId;
    private @ColorRes int tooltipColor;
    private Paint paint;
    private Path tooltipPath;

    public TooltipView(Context context) {
        super(context);
        init(null, 0);
    }

    public TooltipView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public TooltipView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        Resources res = getResources();
        TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.TooltipView, defStyle, 0);
        try {
            anchoredViewId = a.getResourceId(R.styleable.TooltipView_anchoredView, View.NO_ID);
            tooltipColor = a.getColor(R.styleable.TooltipView_tooltipColor, Color.TRANSPARENT);
            cornerRadius = getDimension(a, R.styleable.TooltipView_cornerRadius,
                    R.dimen.tooltip_default_corner_radius);
            arrowHeight = getDimension(a, R.styleable.TooltipView_arrowHeight,
                    R.dimen.tooltip_default_arrow_height);
            arrowWidth = getDimension(a, R.styleable.TooltipView_arrowWidth,
                    R.dimen.tooltip_default_arrow_width);
        } finally {
            a.recycle();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(getMeasuredWidth(), getMeasuredHeight() + arrowHeight);
    }

    @Override
    public void invalidate() {
        super.invalidate();
        tooltipPath = null;
        paint = null;
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        if (tooltipPath == null || paint == null) {
            initTooltip(canvas);
        }
        canvas.drawPath(tooltipPath, paint);
        super.onDraw(canvas);
    }

    private void initTooltip(Canvas canvas) {
        tooltipPath = new Path();
        RectF rectF = new RectF(canvas.getClipBounds());
        rectF.bottom -= arrowHeight;
        tooltipPath.addRoundRect(rectF, cornerRadius, cornerRadius, Direction.CW);

        float middle = rectF.width() / 2;
        if (anchoredViewId != View.NO_ID) {
            View anchoredView = ((View) getParent()).findViewById(anchoredViewId);
            middle += anchoredView.getX() + anchoredView.getWidth() / 2 - getX() - getWidth() / 2;
        }
        tooltipPath.moveTo(middle, getHeight());
        int arrowDx = arrowWidth / 2;
        tooltipPath.lineTo(middle - arrowDx, rectF.bottom);
        tooltipPath.lineTo(middle + arrowDx, rectF.bottom);
        tooltipPath.close();

        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(tooltipColor);
    }

    private int getDimension(TypedArray a, @StyleableRes int styleableId,
            @DimenRes int defaultDimension) {
        int result = a.getDimensionPixelSize(styleableId, NOT_PRESENT);
        if (result == NOT_PRESENT) {
            result = getResources().getDimensionPixelSize(defaultDimension);
        }
        return result;
    }
}
