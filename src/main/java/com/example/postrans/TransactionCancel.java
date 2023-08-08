package com.example.postrans;

import java.util.ArrayList;

import com.common.share.CommonButton;
import com.common.share.CommonMethod;
import com.common.share.FocusMoveByEnter;
import com.common.share.MessageBox;
import com.common.share.SessionBean;
import com.common.share.MessageBox.ButtonType;
import com.common.share.MessageBox.EventListener;
import com.example.gateway.TransAppCanGateway;
import com.example.model.TransactionApprovedCancelModel;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.Window;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.themes.ValoTheme;

@SuppressWarnings("serial")
public class TransactionCancel extends Window
{
	private SessionBean sessionBean;
	private String primaryId, flag;

	private TextArea txtReason;

	private CommonButton cBtn = new CommonButton("", "Save", "", "", "", "", "", "View", "Exit");
	private TransAppCanGateway tacg = new TransAppCanGateway();
	private ArrayList<Component> allComp = new ArrayList<Component>();
	private CommonMethod cm;

	public TransactionCancel(SessionBean sessionBean, String primaryId, String flag)
	{
		this.sessionBean = sessionBean;
		this.primaryId = primaryId;
		this.flag = flag;
		this.setCaption(flag+" Cancel :: "+this.sessionBean.getCompanyName()+
				" ("+this.sessionBean.getBranchName()+")");
		cm = new CommonMethod(sessionBean);
		setWidth("450px");
		setHeight("200px");

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
		focusEnter();
	}

	private void addValidation()
	{
		if (!txtReason.getValue().toString().trim().isEmpty())
		{
			cBtn.btnSave.setEnabled(false);
			insertEditData();
		}
		else
		{
			txtReason.focus();
			cm.showNotification("warning", "Warning!", "Provide reason for cancellation.");
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
					{ saveInformation(); }
					catch(Exception ex)
					{ System.out.println(ex); }
				}
				else if (buttonType == ButtonType.NO)
				{ cBtn.btnSave.setEnabled(true); }
			}
		});
	}

	private void saveInformation()
	{
		TransactionApprovedCancelModel tacm = new TransactionApprovedCancelModel();
		tacm.setTransactionId(primaryId);
		tacm.setCancelReason(txtReason.getValue().toString().trim());
		tacm.setCancelBy(sessionBean.getUserId());
		tacm.setModifyBy(sessionBean.getUserId());
		tacm.setStatusId("S7");
		if (tacg.TransactionCancel(tacm, flag))
		{
			cm.showNotification("success", "Successfull!", "All information saved successfully.");
			cBtn.btnSave.setEnabled(true);
			close();
		}
		else
		{ cm.showNotification("failure", "Error!", "Couldn't save information."); }
	}

	private GridLayout buildLayout()
	{
		GridLayout grid = new GridLayout(5, 2);
		grid.setMargin(true);
		grid.setSpacing(true);
		grid.setSizeFull();

		txtReason = new TextArea();
		txtReason.setImmediate(true);
		txtReason.addStyleName(ValoTheme.TEXTFIELD_SMALL);
		txtReason.setWidth("100%");
		txtReason.setHeight("56px");
		txtReason.setInputPrompt("Reason for cancellation");
		txtReason.setRequired(true);
		txtReason.setRequiredError("This field is required");
		grid.addComponent(new Label("Reason: "), 0, 0);
		grid.addComponent(txtReason, 1, 0, 4, 0);

		grid.addComponent(cBtn, 0, 1, 4, 1);
		grid.setComponentAlignment(cBtn, Alignment.BOTTOM_CENTER);
		return grid;
	}

	private void focusEnter()
	{
		allComp.add(txtReason);
		allComp.add(cBtn.btnSave);
		new FocusMoveByEnter(this, allComp);
	}
}
