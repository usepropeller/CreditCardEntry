package com.devmarvel.creditcardentry.internal;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.devmarvel.creditcardentry.R;
import com.devmarvel.creditcardentry.fields.CreditCardText;
import com.devmarvel.creditcardentry.fields.CreditEntryFieldBase;
import com.devmarvel.creditcardentry.fields.ExpDateText;
import com.devmarvel.creditcardentry.fields.SecurityCodeText;
import com.devmarvel.creditcardentry.fields.ZipCodeText;
import com.devmarvel.creditcardentry.internal.CreditCardUtil.CardType;
import com.devmarvel.creditcardentry.internal.CreditCardUtil.CreditCardFieldDelegate;
import com.devmarvel.creditcardentry.library.CreditCard;

public class CreditCardEntry extends HorizontalScrollView implements
		OnTouchListener, OnGestureListener, CreditCardFieldDelegate {

	private Context context;

	private RelativeLayout container;

	private ImageView cardImage;
	private ImageView backCardImage;
	private CreditCardText creditCardText;
	private ExpDateText expDateText;
	private SecurityCodeText securityCodeText;
	private ZipCodeText zipCodeText;

	private TextView textHelper;

    private Paint measurementPaint = new Paint();

    public enum State {
        STARTING, CREDIT_CARD, INFO
    }
    private State state = State.STARTING;
    private CreditEntryFieldBase focusedField;
	private boolean showingBack;

    public CreditCardEntry(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CreditCardEntry(Context context) {
        super(context);
        init(context);
    }

	private void init(Context context) {
        View.inflate(context, R.layout.credit_card_entry, this);
		this.context = context;

		this.setHorizontalScrollBarEnabled(false);
		this.setOnTouchListener(this);
		this.setSmoothScrollingEnabled(true);

		container = (RelativeLayout) findViewById(R.id.credit_card_entry_container);

        creditCardText = (CreditCardText) findViewById(R.id.credit_card_text_field);
        measurementPaint.setTextSize(creditCardText.getTextSize());

		expDateText = (ExpDateText) findViewById(R.id.credit_card_exp_date_field);

		securityCodeText = (SecurityCodeText) findViewById(R.id.credit_card_security_code_field);

		zipCodeText = (ZipCodeText) findViewById(R.id.credit_card_zip_code_field);

        creditCardText.setDelegate(this);
        expDateText.setDelegate(this);
        securityCodeText.setDelegate(this);
        zipCodeText.setDelegate(this);
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		return true;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		return true;
	}

	@Override
	public boolean onDown(MotionEvent e) {
		return false;
	}

	@Override
	public void onLongPress(MotionEvent e) {
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		return false;
	}

	@Override
	public void onShowPress(MotionEvent e) {
	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		return false;
	}

	@Override
	public void onCardTypeChange(CardType type) {
		cardImage.setImageResource(CreditCardUtil.cardImageForCardType(type,
				false));
		backCardImage.setImageResource(CreditCardUtil.cardImageForCardType(
				type, true));
		updateCardImage(false);
	}

	@Override
	public void onCreditCardNumberValid() {
		focusOnField(expDateText);
	}

	@Override
	public void onExpirationDateValid() {
		focusOnField(securityCodeText);
	}

	@Override
	public void onSecurityCodeValid() {
		focusOnField(zipCodeText);
	}

	@Override
	public void onZipCodeValid() {

	}

	@Override
	public void onBadInput(final EditText field) {
		Animation shake = AnimationUtils.loadAnimation(context, R.anim.shake);
		field.startAnimation(shake);
		field.setTextColor(Color.RED);

		final Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				field.setTextColor(Color.BLACK);
			}
		}, 1000);
	}

	public void setCardImageView(ImageView image) {
		cardImage = image;
	}

	public void updateCardImage(boolean back) {
		if (showingBack != back) {
			flipCardImage();
		}

		showingBack = back;
	}

	public void flipCardImage() {
		FlipAnimator animator = new FlipAnimator(cardImage, backCardImage,
				backCardImage.getWidth() / 2, backCardImage.getHeight() / 2);
		if (cardImage.getVisibility() == View.GONE) {
			animator.reverse();
		}
		cardImage.startAnimation(animator);
	}

	public void focusOnField(final CreditEntryFieldBase field) {
        if (field == this.focusedField) {
            return;
        }

        field.setFocusable(false);

		if (this.textHelper != null) {
			this.textHelper.setText(field.helperText());
		}

        final double transitionTime = 300;
        final double tickInterval = 5;

		if (field == creditCardText && this.state != State.CREDIT_CARD) {
            CreditCardEntry.this.setState(State.CREDIT_CARD);

            final long startOffset = this.computeHorizontalScrollOffset();
            new CountDownTimer((long)transitionTime, (long)tickInterval) {
                public void onTick(long millisUntilFinished) {
                    double percentageComplete = (transitionTime - millisUntilFinished) / transitionTime;
                    int scrollX = (int) (startOffset - (startOffset * percentageComplete));
                    CreditCardEntry.this.scrollTo(scrollX, 0);
                }

                public void onFinish() {
                    CreditCardEntry.this.scrollTo(0, 0);

                    field.setFocusable(true);
                    field.requestFocusFromTouch();
                }
            }.start();
		} else if (field != creditCardText && this.state != State.INFO) {
            this.setState(State.INFO);

            String text = creditCardText.getText().toString();
            String digits = text.substring(text.length() - 4);
            int sizeDPI = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 15, getContext().getResources().getDisplayMetrics());
            final int sizeOfLastFour = (int)measurementPaint.measureText(digits) + sizeDPI;

            container.setPadding(0,0,sizeOfLastFour,0);

            final  int endPosition = creditCardText.getWidth() - sizeOfLastFour;

            new CountDownTimer((long)transitionTime, (long)tickInterval) {
				public void onTick(long millisUntilFinished) {
                    double percentageComplete = (transitionTime - millisUntilFinished) / transitionTime;
                    int scrollX = (int) (endPosition * percentageComplete);

                    CreditCardEntry.this.scrollTo(scrollX, 0);
				}

				public void onFinish() {
                    CreditCardEntry.this.scrollTo(endPosition, 0);

                    field.setFocusable(true);
                    field.requestFocusFromTouch();
                }
			}.start();
		}
        else {
            field.setFocusable(true);
            field.requestFocusFromTouch();
        }
        this.focusedField = field;

		if (field.getClass().equals(SecurityCodeText.class)) {
			((SecurityCodeText) field).setType(creditCardText.getType());
			updateCardImage(true);
		} else {
			updateCardImage(false);
		}
	}

    private void setState(State state) {
        this.state = state;
    }

	@Override
	public void focusOnPreviousField(CreditEntryFieldBase field) {
		if (field.getClass().equals(ExpDateText.class)) {
			focusOnField(creditCardText);
		} else if (field.getClass().equals(SecurityCodeText.class)) {
			focusOnField(expDateText);
		} else if (field.getClass().equals(ZipCodeText.class)) {
			focusOnField(securityCodeText);
		}
	}

	public ImageView getBackCardImage() {
		return backCardImage;
	}

	public void setBackCardImage(ImageView backCardImage) {
		this.backCardImage = backCardImage;
	}

	public TextView getTextHelper() {
		return textHelper;
	}

	public void setTextHelper(TextView textHelper) {
		this.textHelper = textHelper;
	}

	public boolean isCreditCardValid() {
		return creditCardText.isValid() && expDateText.isValid()
				&& securityCodeText.isValid() && zipCodeText.isValid();
	}

	public CreditCard getCreditCard() {
		if (isCreditCardValid()) {
			return new CreditCard(creditCardText.getText().toString(),
					expDateText.getText().toString(), securityCodeText
							.getText().toString(), zipCodeText.getText()
							.toString());
		} else {
			return null;
		}
	}
}
