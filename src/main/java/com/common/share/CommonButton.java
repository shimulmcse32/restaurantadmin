package com.common.share;

import java.io.Serializable;

import com.vaadin.server.FontAwesome;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.themes.ValoTheme;

@SuppressWarnings("serial")
public class CommonButton extends HorizontalLayout implements Serializable
{	
	public Button btnNew, btnEdit, btnSave, btnRefresh, btnDelete,
	btnSearch, btnCancel, btnPreview, btnExit;

	public CommonButton(String New, String Save, String Edit, String Delete, String Refresh,
			String Search, String Cancel, String Preview, String Exit)
	{
		setSpacing(true);	   
		if (New.equals("New"))
		{
			btnNew = new Button("New");
			buttonSize(btnNew);
			btnNew.addStyleName(ValoTheme.BUTTON_PRIMARY);
			btnNew.setIcon(FontAwesome.PLUS);
			addComponent(btnNew);			
		}

		if (Save.equals("Save"))
		{
			btnSave = new Button("Save");
			buttonSize(btnSave);
			btnSave.addStyleName(ValoTheme.BUTTON_FRIENDLY);
			btnSave.setIcon(FontAwesome.SAVE);
			addComponent(btnSave);
		}

		if (Edit.equals("Edit"))
		{
			btnEdit = new Button("Edit");
			buttonSize(btnEdit);
			btnEdit.setIcon(FontAwesome.EDIT);
			addComponent(btnEdit);
		}

		if (Delete.equals("Delete"))
		{
			btnDelete = new Button("Delete");
			buttonSize(btnDelete);
			//btnSave.setIcon(FontAwesome.DEL);
			addComponent(btnDelete);
		}

		if (Refresh.equals("Refresh"))
		{
			btnRefresh = new Button("Refresh");
			buttonSize(btnRefresh);
			btnRefresh.setIcon(FontAwesome.REFRESH);
			addComponent(btnRefresh);
		}

		if (Search.equals("Search"))
		{
			btnSearch = new Button("Search");
			buttonSize(btnSearch);
			btnSearch.setIcon(FontAwesome.SEARCH);
			addComponent(btnSearch);
		}

		if (Cancel.equals("Cancel"))
		{
			btnCancel = new Button("Cancel");
			buttonSize(btnCancel);
			addComponent(btnCancel);
		}

		if (Preview.equals("View"))
		{
			btnPreview = new Button("View");
			buttonSize(btnPreview);
			btnPreview.setIcon(FontAwesome.EYE);
			addComponent(btnPreview);
		}

		if (Exit.equals("Exit"))
		{
			btnExit = new Button("Exit");
			buttonSize(btnExit);
			btnExit.addStyleName(ValoTheme.BUTTON_DANGER);
			btnExit.setIcon(FontAwesome.CLOSE);
			addComponent(btnExit);
		}
	}

	private void buttonSize(Button btn)
	{
		btn.setStyleName(ValoTheme.BUTTON_SMALL);
	}
}
