package com.common.share;

import org.vaadin.ui.NumberField;

@SuppressWarnings("serial")
public class CommaField extends NumberField
{
	public CommaField()
	{
		this.setGroupingUsed(true);
		this.setGroupingSeparator(',');
		this.setDecimalPrecision(3);
	}

	public CommaField(Double number)
	{
		this.setGroupingUsed(true);
		this.setGroupingSeparator(',');
		this.setDecimalPrecision(3);
		this.setValue(number);
	}
}
