package com.devmarvel.creditcardentry.library;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.devmarvel.creditcardentry.R;
import com.devmarvel.creditcardentry.internal.CreditCardEntry;

public class CreditCardForm extends RelativeLayout {
	
	private CreditCardEntry entry;

	public CreditCardForm(Context context) {
		super(context);
		init(context);
	}

	public CreditCardForm(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public CreditCardForm(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	public void init(Context context) {
        View.inflate(context, R.layout.credit_card_form, this);

		entry = (CreditCardEntry) findViewById(R.id.credit_card_entry);
		entry.setCardImageView((ImageView) findViewById(R.id.credit_card_icon));
		entry.setBackCardImage((ImageView) findViewById(R.id.credit_card_back_icon));
		entry.setTextHelper((TextView) findViewById(R.id.credit_card_hint));
	}
	
	public boolean isCreditCardValid()
	{
		return entry.isCreditCardValid();
	}
	
	public CreditCard getCreditCard()
	{
		return entry.getCreditCard();
	}

}
