package com.common.share;

import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.ui.TextField;
import com.vaadin.ui.themes.ValoTheme;

@SuppressWarnings("serial")
public class AmountField extends TextField implements TextChangeListener
{
	public AmountField()
	{
		setImmediate(true);
		setTextChangeEventMode(TextChangeEventMode.EAGER);
		addTextChangeListener(this);
		setStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
	}

	public void textChange(TextChangeEvent event)
	{
		String text = event.getText();
		try
		{ new Double(text); }
		catch (NumberFormatException e)
		{ setValue(""); }
	}
}
