package net.rusnet.sb.fingerdrawapp;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import java.util.HashMap;
import java.util.Map;

import yuku.ambilwarna.AmbilWarnaDialog;

public class MainActivity extends AppCompatActivity {

    private MultiDrawView mDrawView;
    private Map<Brush, ImageView> mButtons = new HashMap<>();
    private Brush mSelectedBrush;

    private Button mColorPickerButton;
    private int mSelectedColor;

    private SwitchCompat mSwitch;
    private View mScrollSwitchButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
    }

    private void initViews() {
        Button mResetButton = findViewById(R.id.button_reset);
        mDrawView = findViewById(R.id.multi_draw_view);

        mResetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawView.clear();
            }
        });

        mColorPickerButton = findViewById(R.id.color_picker_button);
        mSelectedColor = getResources().getColor(R.color.defaultColor);
        mColorPickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openColorPicker();
            }
        });

        mButtons.put(Brush.PATH, (ImageView) findViewById(R.id.path_button));
        mButtons.put(Brush.LINE, (ImageView) findViewById(R.id.line_button));
        mButtons.put(Brush.RECTANGLE, (ImageView) findViewById(R.id.rectangle_button));
        mButtons.put(Brush.FIGURE, (ImageView) findViewById(R.id.figure_button));


        for (final Brush brush : mButtons.keySet()) {
            mButtons.get(brush).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mSelectedBrush = brush;
                    updateDrawViewParams();
                    updateButtonDisplay();
                }
            });
        }

        mButtons.get(Brush.LINE).callOnClick();

        mSwitch = findViewById(R.id.scroll_switch);

        mScrollSwitchButton = findViewById(R.id.scroll_switch_button);
        mScrollSwitchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSwitch.setChecked(!mSwitch.isChecked());
                mDrawView.setScrollMode(mSwitch.isChecked());
            }
        });

    }

    private void openColorPicker() {
        AmbilWarnaDialog colorPicker = new AmbilWarnaDialog(this,
                mSelectedColor,
                new AmbilWarnaDialog.OnAmbilWarnaListener() {
                    @Override
                    public void onCancel(AmbilWarnaDialog dialog) {}

                    @Override
                    public void onOk(AmbilWarnaDialog dialog, int color) {
                        mSelectedColor = color;
                        mColorPickerButton.setBackgroundColor(mSelectedColor);
                        updateDrawViewParams();
                    }
                });
        colorPicker.show();
    }

    private void updateDrawViewParams() {
        mDrawView.setParams(mSelectedBrush, mSelectedColor);
    }

    private void updateButtonDisplay() {
        for (Brush brush : mButtons.keySet()) {
            int resId;
            if (brush == mSelectedBrush) {
                resId = R.color.activeBrush;
            } else {
                resId = R.color.inactiveBrush;
            }
            mButtons.get(brush).setBackgroundColor(getResources().getColor(resId));
        }
    }

}