package com.example.possetup;

import java.util.ArrayList;

import com.common.share.CommonButton;
import com.common.share.CommonMethod;
import com.common.share.FocusMoveByEnter;
import com.common.share.MessageBox;
import com.common.share.SessionBean;
import com.common.share.MessageBox.ButtonType;
import com.common.share.MessageBox.EventListener;
import com.example.gateway.ItemUnitGateway;
import com.example.model.ItemUnitModel;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Window;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.themes.ValoTheme;

@SuppressWarnings("serial")
public class AddEditItemUnit extends Window
{
	private SessionBean sessionBean;
	private String flag, unitId = "", unitType = "";

	private TextField txtUnitName;
	private TextArea txtDescription;

	private CommonButton cBtn = new CommonButton("", "Save", "", "", "", "", "", "View", "Exit");
	private ItemUnitGateway iug = new ItemUnitGateway();
	private ArrayList<Component> allComp = new ArrayList<Component>();
	private CommonMethod cm;

	public AddEditItemUnit(SessionBean sessionBean, String flag, String unitId, String unitType)
	{
		this.sessionBean = sessionBean;
		this.flag = flag;
		this.unitId = unitId;
		this.unitType = unitType;
		this.setCaption(flag+" Item Packages Information :: "+this.sessionBean.getCompanyName());
		cm = new CommonMethod(sessionBean);
		setWidth("550px");
		setHeight("250px");

		setContent(buildLayout());

		addActions();
	}

	private void addActions()
	{
		cBtn.btnSave.addClickListener(new ClickListener()
		{
			public void buttonClick(ClickEvent event)
			{ addValidation(); }
		});

		cBtn.btnExit.addClickListener(new ClickListener()
		{
			public void buttonClick(ClickEvent event)
			{ close(); }
		});

		if (flag.equals("Edit"))
		{ setEditData(); }
		focusEnter();
	}

	private void addValidation()
	{
		if (!txtUnitName.getValue().toString().trim().isEmpty())
		{
			if (!iug.checkExist(txtUnitName.getValue().toString().trim(), unitId))
			{
				cBtn.btnSave.setEnabled(false);
				insertEditData();
			}
			else
			{
				txtUnitName.focus();
				cm.showNotification("warning", "Warning!", "Unit name already exist.");
			}
		}
		else
		{
			txtUnitName.focus();
			cm.showNotification("warning", "Warning!", "Provide unit name.");
		}
	}

	private void insertEditData()
	{
		MessageBox mb = new MessageBox(getUI(), "Are you sure?",
				MessageBox.Icon.QUESTION, "Do you want to save information?",
				new MessageBox.ButtonConfig(MessageBox.ButtonType.YES, "Yes"),
				new MessageBox.ButtonConfig(MessageBox.ButtonType.NO, "No"));
		mb.show(new EventListener()
		{
			public void buttonClicked(ButtonType buttonType)
			{
				if (buttonType == ButtonType.YES)
				{
					try
					{
						ItemUnitModel ium = new ItemUnitModel();
						String unitIdN = flag.equals("Add")?iug.getUnitId():unitId;
						ium.setUnitId(unitIdN);
						ium.setBranchId(sessionBean.getBranchId());
						ium.setUnitName(txtUnitName.getValue().toString().trim());
						ium.setType(unitType);
						ium.setUnitDesc(txtDescription.getValue().toString().trim());
						ium.setCreatedBy(sessionBean.getUserId());

						if (iug.insertEditData(ium, flag))
						{
							txtClear();
							cm.showNotification("success", "Successfull!", "All information saved successfully.");
							cBtn.btnSave.setEnabled(true);

							if (flag.equals("Edit"))
							{ close(); }
						}
						else
						{ cm.showNotification("failure", "Error!", "Couldn't save information."); }
					}
					catch(Exception ex)
					{ System.out.println(ex); }
				}
				else if (buttonType == ButtonType.NO)
				{ cBtn.btnSave.setEnabled(true); }
			}
		});
	}

	private void setEditData()
	{
		ItemUnitModel ium = new ItemUnitModel();
		try
		{
			if (iug.selectEditData(ium, unitId))
			{
				txtUnitName.setValue(ium.getUnitName());
				txtDescription.setValue(ium.getUnitDesc());
			}
			else
			{ cm.showNotification("warning", "Error!", "Couldn't find information."); }
		}
		catch(Exception ex)
		{ System.out.println(ex); }
	}

	private GridLayout buildLayout()
	{
		GridLayout grid = new GridLayout(4, 3);
		grid.setMargin(true);
		grid.setSpacing(true);
		grid.setSizeFull();

		txtUnitName = new TextField();
		txtUnitName.setImmediate(true);
		txtUnitName.addStyleName(ValoTheme.TEXTFIELD_SMALL);
		txtUnitName.setWidth("100%");
		txtUnitName.setInputPrompt("Unit Name");
		txtUnitName.setRequired(true);
		txtUnitName.setRequiredError("This field is required.");
		Label lbl = new Label("Unit Name: ");
		lbl.setWidth("-1px");
		grid.addComponent(lbl, 0, 0);
		grid.addComponent(txtUnitName, 1, 0, 3, 0);

		txtDescription = new TextArea();
		txtDescription.setImmediate(true);
		txtDescription.addStyleName(ValoTheme.TEXTFIELD_SMALL);
		txtDescription.setWidth("100%");
		txtDescription.setHeight("60px");
		txtDescription.setInputPrompt("Description");
		grid.addComponent(new Label("Description: "), 0, 1);
		grid.addComponent(txtDescription, 1, 1, 3, 1);

		grid.addComponent(cBtn, 0, 2, 3, 2);
		grid.setComponentAlignment(cBtn, Alignment.BOTTOM_CENTER);

		return grid;
	}

	private void focusEnter()
	{
		allComp.add(txtUnitName);
		allComp.add(txtDescription);
		allComp.add(cBtn.btnSave);

		new FocusMoveByEnter(this, allComp);
	}

	private void txtClear()
	{
		txtUnitName.setValue("");
		txtDescription.setValue("");
	}
}
