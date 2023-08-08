package com.common.share;

import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.ui.TextField;
import com.vaadin.ui.themes.ValoTheme;

@SuppressWarnings("serial")
public class IntegerField extends TextField implements TextChangeListener
{
	public IntegerField()
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
		{
			new Integer(text);
		}
		catch (NumberFormatException e)
		{
			setValue("");
		}
	}
}