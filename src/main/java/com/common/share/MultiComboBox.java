package com.common.share;

import org.vaadin.addons.comboboxmultiselect.ComboBoxMultiselect;

import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.ui.themes.ValoTheme;

@SuppressWarnings("serial")
public class MultiComboBox extends ComboBoxMultiselect
{
	public MultiComboBox()
	{
		this.setImmediate(true);
		this.setStyleName(ValoTheme.COMBOBOX_TINY);
		this.setFilteringMode(FilteringMode.CONTAINS);
		this.setClearButtonCaption("Clear All");
		this.setSelectAllButtonCaption("Select All");
		ShowButton showSelectAllButton = new ShowButton()
		{
			public boolean isShow(String filter, int page)
			{ return true; }
		};

		this.setShowSelectAllButton(showSelectAllButton);
	}
}
