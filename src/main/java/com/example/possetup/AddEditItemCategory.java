package com.example.possetup;

import java.util.ArrayList;

import com.common.share.CommonButton;
import com.common.share.CommonMethod;
import com.common.share.FocusMoveByEnter;
import com.common.share.MessageBox;
import com.common.share.SessionBean;
import com.common.share.MessageBox.ButtonType;
import com.common.share.MessageBox.EventListener;
import com.example.gateway.ItemCategoryGateway;
import com.example.model.ItemCategoryModel;
import com.vaadin.ui.Component;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Window;
import com.vaadin.server.ThemeResource;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.themes.ValoTheme;

@SuppressWarnings("serial")
public class AddEditItemCategory extends Window
{
	private SessionBean sessionBean;
	private String flag, catId = "", catType = "";

	private TextField txtCategoryName;
	private TextArea txtDescription;
	//private OptionGroup ogCategoryType;
	private ComboBox cmbColor;
	public CheckBox chkShowOnline;

	private CommonButton cBtn = new CommonButton("", "Save", "", "", "", "", "", "View", "Exit");
	private ItemCategoryGateway cig = new ItemCategoryGateway();
	private ArrayList<Component> allComp = new ArrayList<Component>();
	private CommonMethod cm;

	public AddEditItemCategory(SessionBean sessionBean, String flag, String catId, String catType)
	{
		this.sessionBean = sessionBean;
		this.flag = flag;
		this.catId = catId;
		this.catType = catType;
		this.setCaption(flag+" "+catType+" Category Information :: "+this.sessionBean.getCompanyName()+
				" ("+this.sessionBean.getBranchName()+")");
		cm = new CommonMethod(sessionBean);
		setWidth("550px");
		setHeight("310px");

		setContent(buildLayout());

		addActions();
		//ogCategoryType.select(catType);
	}

	private void addActions()
	{
		cBtn.btnSave.addClickListener(event -> addValidation());

		cBtn.btnExit.addClickListener(event -> close());

		/*ogCategoryType.addValueChangeListener(new ValueChangeListener()
		{
			public void valueChange(ValueChangeEvent event)
			{ setCategoryColor(); }
		});*/

		if (flag.equals("Edit"))
		{ setEditData(); }
		focusEnter();
	}

	private void addValidation()
	{
		/*if (ogCategoryType.getValue() != null)
		{
			String catType = ogCategoryType.getValue().toString();*/
		if (!txtCategoryName.getValue().toString().trim().isEmpty())
		{
			if (!cig.checkExist(txtCategoryName.getValue().toString().trim(), catId, catType))
			{
				cBtn.btnSave.setEnabled(false);
				insertEditData();
			}
			else
			{
				txtCategoryName.focus();
				cm.showNotification("warning", "Warning!", "Category name already exist.");
			}
		}
		else
		{
			txtCategoryName.focus();
			cm.showNotification("warning", "Warning!", "Provide category name.");
		}
		/*}
		else
		{
			ogCategoryType.focus();
			cm.showNotification("warning", "Warning!", "Select category name.");
		}*/
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
						ItemCategoryModel cim = new ItemCategoryModel();
						String catIdN = flag.equals("Add")?cig.getCategoryId():catId;
						cim.setCategoryId(catIdN);
						cim.setBranchId(sessionBean.getBranchId());
						cim.setCategoryType(catType);
						cim.setCategoryName(txtCategoryName.getValue().toString().trim());
						cim.setCatDesc(txtDescription.getValue().toString().trim());
						cim.setCatColor(cmbColor.getValue() != null? cmbColor.getValue().toString() : "");
						cim.setCreatedBy(sessionBean.getUserId());
						cim.setShowOnline(chkShowOnline.getValue().booleanValue()? 1:0);

						if (cig.insertEditData(cim, flag))
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
		ItemCategoryModel cim = new ItemCategoryModel();
		try
		{
			if (cig.selectEditData(cim, catId))
			{
				//ogCategoryType.setValue(cim.getCategoryType());
				txtCategoryName.setValue(cim.getCategoryName());
				txtDescription.setValue(cim.getCatDesc());
				cmbColor.setValue(cim.getCatColor());
				chkShowOnline.setValue(cim.getShowOnline()==1? true:false);
				/*cpColor.setCaption(cim.getCatColor());
				cpColor.setColor(new Color(Integer.parseInt(cim.getCatColor().substring(1, 3), 16),
						Integer.parseInt(cim.getCatColor().substring(3, 5), 16),
						Integer.parseInt(cim.getCatColor().substring(5, 7), 16)));*/
			}
			else
			{ cm.showNotification("warning", "Error!", "Couldn't find information."); }
		}
		catch(Exception ex)
		{ System.out.println(ex); }
	}

	private GridLayout buildLayout()
	{
		GridLayout grid = new GridLayout(9, 6);
		grid.setMargin(true);
		grid.setSpacing(true);
		grid.setSizeFull();

		/*ogCategoryType = new OptionGroup();
		ogCategoryType.addItem("Raw");
		ogCategoryType.addItem("Menu");
		ogCategoryType.addItem("Modifier");
		ogCategoryType.select("Raw");
		ogCategoryType.setDescription("Item Category");
		ogCategoryType.addStyleName(ValoTheme.OPTIONGROUP_HORIZONTAL);
		ogCategoryType.addStyleName(ValoTheme.OPTIONGROUP_SMALL);
		//ogCategoryType.setEnabled(catType.isEmpty());
		if (!catType.equals("Raw"))
		{
			ogCategoryType.removeItem("Raw");
			ogCategoryType.select("Menu");
			grid.addComponent(new Label("Category Type: "), 0, 0);
			grid.addComponent(ogCategoryType, 1, 0, 3, 0);
		}*/

		txtCategoryName = new TextField();
		txtCategoryName.setImmediate(true);
		txtCategoryName.addStyleName(ValoTheme.TEXTFIELD_TINY);
		txtCategoryName.setWidth("100%");
		txtCategoryName.setInputPrompt("Category Name");
		txtCategoryName.setRequired(true);
		txtCategoryName.setRequiredError("This field is required.");
		Label lblCa = new Label("Category Name: ");
		lblCa.setWidth("-1px");
		grid.addComponent(lblCa, 0, 1);
		grid.addComponent(txtCategoryName, 1, 1, 8, 1);

		txtDescription = new TextArea();
		txtDescription.setImmediate(true);
		txtDescription.addStyleName(ValoTheme.TEXTFIELD_TINY);
		txtDescription.setWidth("100%");
		txtDescription.setHeight("60px");
		txtDescription.setInputPrompt("Description");
		grid.addComponent(new Label("Description: "), 0, 2);
		grid.addComponent(txtDescription, 1, 2, 8, 2);

		cmbColor = new ComboBox();
		cmbColor.setImmediate(true);
		cmbColor.setNullSelectionAllowed(false);
		cmbColor.setFilteringMode(FilteringMode.CONTAINS);
		cmbColor.addStyleName(ValoTheme.COMBOBOX_TINY);
		cmbColor.setInputPrompt("Select category color");
		cmbColor.setWidth("50%");
		if (!catType.equals("Raw"))
		{
			Label lbl = new Label("Font Color: ");
			lbl.setWidth("-1px");
			grid.addComponent(lbl, 0, 3);
			grid.addComponent(cmbColor, 1, 3, 8, 3);
			loadColors();
		}

		chkShowOnline = new CheckBox("Show Online");
		chkShowOnline.setImmediate(true);
		chkShowOnline.setValue(true);
		chkShowOnline.addStyleName(ValoTheme.CHECKBOX_SMALL);
		chkShowOnline.setDescription("Save as for show online.");
		grid.addComponent(chkShowOnline, 1, 4);

		grid.addComponent(cBtn, 0, 5, 8, 5);
		grid.setComponentAlignment(cBtn, Alignment.BOTTOM_CENTER);

		return grid;
	}

	private void loadColors()
	{
		cmbColor.addItem("#0049b6");
		cmbColor.setItemIcon("#0049b6", new ThemeResource("../dashboard/colors/0049b6.gif"));
		cmbColor.setItemCaption("#0049b6", "Deep Blue");

		cmbColor.addItem("#6da8ff");
		cmbColor.setItemIcon("#6da8ff", new ThemeResource("../dashboard/colors/6da8ff.gif"));
		cmbColor.setItemCaption("#6da8ff", "Blue");
		
		cmbColor.addItem("#1a1a1a");
		cmbColor.setItemIcon("#1a1a1a", new ThemeResource("../dashboard/colors/1a1a1a.gif"));
		cmbColor.setItemCaption("#1a1a1a", "Black");
		
		cmbColor.addItem("#24b600");
		cmbColor.setItemIcon("#24b600", new ThemeResource("../dashboard/colors/24b600.gif"));
		cmbColor.setItemCaption("#24b600", "Green");
		
		cmbColor.addItem("#9200b6");
		cmbColor.setItemIcon("#9200b6", new ThemeResource("../dashboard/colors/9200b6.gif"));
		cmbColor.setItemCaption("#9200b6", "Purple");
		
		cmbColor.addItem("#d324ff");
		cmbColor.setItemIcon("#d324ff", new ThemeResource("../dashboard/colors/d324ff.gif"));
		cmbColor.setItemCaption("#d324ff", "Pink");
		
		cmbColor.addItem("#db0083");
		cmbColor.setItemIcon("#db0083", new ThemeResource("../dashboard/colors/db0083.gif"));
		cmbColor.setItemCaption("#db0083", "Deep Pink");
		
		cmbColor.addItem("#ff0000");
		cmbColor.setItemIcon("#ff0000", new ThemeResource("../dashboard/colors/ff0000.gif"));
		cmbColor.setItemCaption("#ff0000", "Red");
		
		cmbColor.addItem("#ff9900");
		cmbColor.setItemIcon("#ff9900", new ThemeResource("../dashboard/colors/ff9900.gif"));
		cmbColor.setItemCaption("#ff9900", "Orange");
		
		cmbColor.select("#1a1a1a");
	}

	/*private void setCategoryColor()
	{
		String type = "Menu";
		if (type.equals("Raw"))
		{ cmbColor.setEnabled(false); }
		else
		{ cmbColor.setEnabled(true); }
	}*/

	private void focusEnter()
	{
		allComp.add(txtCategoryName);
		allComp.add(txtDescription);
		allComp.add(cBtn.btnSave);

		new FocusMoveByEnter(this, allComp);
	}

	private void txtClear()
	{
		txtCategoryName.setValue("");
		txtDescription.setValue("");
	}
}
