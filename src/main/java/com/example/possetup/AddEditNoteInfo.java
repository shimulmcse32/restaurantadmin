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
import com.vaadin.ui.Component;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.Window;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.themes.ValoTheme;

@SuppressWarnings("serial")
public class AddEditNoteInfo extends Window
{
	private SessionBean sessionBean;
	private String flag, noteId = "";

	private OptionGroup ogNoteType;
	private TextArea txtNoteDetails;

	private CommonButton cBtn = new CommonButton("", "Save", "", "", "", "", "", "View", "Exit");
	private ItemUnitGateway iug = new ItemUnitGateway();
	private ArrayList<Component> allComp = new ArrayList<Component>();
	private CommonMethod cm;

	public AddEditNoteInfo(SessionBean sessionBean, String flag, String noteId)
	{
		this.sessionBean = sessionBean;
		this.flag = flag;
		this.noteId = noteId;
		this.setCaption(flag+" Note Information :: "+this.sessionBean.getCompanyName());
		cm = new CommonMethod(sessionBean);
		setWidth("500px");
		setHeight("250px");

		setContent(buildLayout());

		addActions();
	}

	private void addActions()
	{
		cBtn.btnSave.addClickListener(event ->
		{ addValidation(); });

		cBtn.btnExit.addClickListener(event ->
		{ close(); });

		if (flag.equals("Edit"))
		{ setEditData(); }
		focusEnter();
	}

	private void addValidation()
	{
		if (!txtNoteDetails.getValue().toString().trim().isEmpty())
		{
			if (!iug.checkExistNote(txtNoteDetails.getValue().toString().trim(), noteId))
			{
				cBtn.btnSave.setEnabled(false);
				insertEditData();
			}
			else
			{
				txtNoteDetails.focus();
				cm.showNotification("warning", "Warning!", "Note already exist.");
			}
		}
		else
		{
			txtNoteDetails.focus();
			cm.showNotification("warning", "Warning!", "Provide note details.");
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
						String noteIdN = flag.equals("Add")?iug.getNoteId():noteId;
						ium.setUnitId(noteIdN);
						ium.setBranchId(sessionBean.getBranchId());
						ium.setType(ogNoteType.getValue().toString());
						ium.setUnitName(txtNoteDetails.getValue().toString().trim());
						ium.setCreatedBy(sessionBean.getUserId());

						if (iug.insertEditDataNote(ium, flag))
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
			if (iug.selectEditDataNote(ium, noteId))
			{
				ogNoteType.setValue(ium.getType());
				txtNoteDetails.setValue(ium.getUnitName());
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

		ogNoteType = new OptionGroup();
		ogNoteType.addItem("Kitchen");
		ogNoteType.addItem("Invoice");
		ogNoteType.select("Kitchen");
		ogNoteType.addStyleName(ValoTheme.OPTIONGROUP_HORIZONTAL);
		ogNoteType.addStyleName(ValoTheme.OPTIONGROUP_SMALL);
		Label lbl = new Label("Note Type: ");
		lbl.setWidth("-1px");
		grid.addComponent(lbl, 0, 0);
		grid.addComponent(ogNoteType, 1, 0, 3, 0);
		grid.setComponentAlignment(ogNoteType, Alignment.MIDDLE_LEFT);

		txtNoteDetails = new TextArea();
		txtNoteDetails.setImmediate(true);
		txtNoteDetails.addStyleName(ValoTheme.TEXTFIELD_SMALL);
		txtNoteDetails.setWidth("100%");
		txtNoteDetails.setHeight("60px");
		txtNoteDetails.setInputPrompt("Note Details(50 Character)");
		txtNoteDetails.setRequired(true);
		txtNoteDetails.setRequiredError("This field is required");
		txtNoteDetails.setMaxLength(50);
		grid.addComponent(new Label("Note Details: "), 0, 1);
		grid.addComponent(txtNoteDetails, 1, 1, 3, 1);

		grid.addComponent(cBtn, 0, 2, 3, 2);
		grid.setComponentAlignment(cBtn, Alignment.BOTTOM_CENTER);

		return grid;
	}

	private void focusEnter()
	{
		allComp.add(ogNoteType);
		allComp.add(txtNoteDetails);
		allComp.add(cBtn.btnSave);

		new FocusMoveByEnter(this, allComp);
	}

	private void txtClear()
	{ txtNoteDetails.setValue(""); }
}
