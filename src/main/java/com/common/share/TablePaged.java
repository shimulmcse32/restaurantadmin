package com.common.share;

import com.vaadin.ui.themes.ValoTheme;

@SuppressWarnings("serial")
public class TablePaged extends PagedTable
{
	public TablePaged()
	{
		this.setFooterVisible(true);
		this.setSelectable(true);
		this.setImmediate(true);
		this.setColumnCollapsingAllowed(true);
		this.addStyleName(ValoTheme.TABLE_SMALL);
		this.setRowHeaderMode(RowHeaderMode.INDEX);
		this.setPageLength(10);
		this.setWidth("100%");
	}
}
