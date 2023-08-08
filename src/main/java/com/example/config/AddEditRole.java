package com.example.config;

import java.util.ArrayList;
import java.util.Iterator;

import com.common.share.CommonButton;
import com.common.share.CommonMethod;
import com.common.share.MessageBox;
import com.common.share.SessionBean;
import com.common.share.TablePaged;
import com.common.share.MessageBox.ButtonType;
import com.common.share.MessageBox.EventListener;
import com.common.share.MultiComboBox;
import com.example.gateway.RoleInfoGateway;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Table.Align;
import com.vaadin.ui.themes.ValoTheme;

@SuppressWarnings("serial")
public class AddEditRole extends Window
{
	private SessionBean sessionBean;
	private String flag, roleId;
	private TextField txtRoleName;
	private MultiComboBox cmbModuleName;

	private boolean tick = false, ticki = true, tickd = true, ticku = true;

	private CommonButton cBtn = new CommonButton("", "Save", "", "", "", "", "", "View", "Exit");
	private TablePaged tblMenuList;
	private ArrayList<CheckBox> tbChkSelect = new ArrayList<CheckBox>();
	private ArrayList<Label> tbLblMenuId = new ArrayList<Label>();
	private ArrayList<Label> tbLblModuleName = new ArrayList<Label>();
	private ArrayList<Label> tbLblMenuName = new ArrayList<Label>();
	private ArrayList<CheckBox> tbChkInsert = new ArrayList<CheckBox>();
	private ArrayList<CheckBox> tbChkUpdate = new ArrayList<CheckBox>();
	private ArrayList<CheckBox> tbChkDelete = new ArrayList<CheckBox>();

	private CommonMethod cm;
	private RoleInfoGateway rig = new RoleInfoGateway();

	public AddEditRole(SessionBean sessionBean, String flag, String roleId)
	{
		this.sessionBean = sessionBean;
		this.flag = flag;
		this.roleId = roleId;
		this.setCaption(flag+" Role Information :: "+this.sessionBean.getCompanyName()+
				" ("+this.sessionBean.getBranchName()+")");
		setWidth("950px");
		setHeight("600px");

		cm = new CommonMethod(sessionBean);

		setContent(buildLayout());
		roleDataLoad();
		loadData();

		addActions();
	}

	private void loadData()
	{
		loadMenuList();
		if (flag.equals("Edit"))
		{ loadEditData(); }
	}

	private void addActions()
	{
		cBtn.btnSave.addClickListener(event -> addValidation());

		cBtn.btnExit.addClickListener(event -> close());

		cmbModuleName.addValueChangeListener(event -> loadMenuList());

		tblMenuList.addHeaderClickListener(event ->
		{
			if (event.getPropertyId().toString().equalsIgnoreCase("Select"))
			{ selectAll(); }
			if (event.getPropertyId().toString().equalsIgnoreCase("Insert"))
			{ selectOther(1); }
			if (event.getPropertyId().toString().equalsIgnoreCase("Delete"))
			{ selectOther(2); }
			if (event.getPropertyId().toString().equalsIgnoreCase("Update"))
			{ selectOther(3); }
		});
	}

	private void addValidation()
	{
		if (!txtRoleName.getValue().toString().trim().isEmpty())
		{
			if (checkTable())
			{
				if (!rig.checkExist(txtRoleName.getValue().toString().trim(), roleId))
				{
					cBtn.btnSave.setEnabled(false);
					insertEditData();
				}
				else
				{
					txtRoleName.focus();
					cm.showNotification("warning", "Warning!", "Role name already exist.");
				}
			}
			else
			{
				tbChkSelect.get(0).focus();
				cm.showNotification("warning", "Warning!", "Select menu name from table.");
			}
		}
		else
		{
			txtRoleName.focus();
			cm.showNotification("warning", "Warning!", "Provide role name.");
		}
	}

	private boolean checkTable()
	{
		boolean ret = false;
		for (int i=0; i<tbLblMenuId.size(); i++)
		{
			if (tbChkSelect.get(i).getValue().booleanValue())
			{ ret = true; break; }
		}
		return ret;
	}

	private void selectAll()
	{
		if (tick)
		{tick = false;}
		else
		{tick = true;}
		for (int i = 0; i < tbLblMenuId.size(); i++)
		{
			if (!tbLblMenuId.get(i).getValue().toString().isEmpty())
			{ tbChkSelect.get(i).setValue(tick); }
		}
	}

	private void selectOther(int a)
	{
		if (a == 1)
		{
			if (ticki)
			{ticki = false;}
			else
			{ticki = true;}
		}
		if (a == 2)
		{
			if (tickd)
			{tickd = false;}
			else
			{tickd = true;}
		}
		if (a == 3)
		{
			if (ticku)
			{ticku = false;}
			else
			{ticku = true;}
		}		

		for (int i = 0; i < tbLblMenuId.size(); i++)
		{
			if (!tbLblMenuId.get(i).getValue().toString().isEmpty() &&
					tbChkSelect.get(i).getValue().booleanValue())
			{
				if (a == 1)
				{ tbChkInsert.get(i).setValue(ticki); }
				if (a == 2)
				{ tbChkDelete.get(i).setValue(tickd); }
				if (a == 3)
				{ tbChkUpdate.get(i).setValue(ticku); }
			}
		}
	}

	private void insertEditData()
	{
		MessageBox mb = new MessageBox(getUI(), "Are you sure?", MessageBox.Icon.QUESTION, "Do you want to save information?",
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
						String roleIdN = flag.equals("Add")?rig.getRoleId():roleId;
						String sqlInfo = "", sqlDetails = "", sqlTables = "", menusId = "", parentSql = "";

						if (flag.equals("Add"))
						{
							sqlInfo = "insert into master.tbUserRoleInfo(vRoleId, vRoleName, iActive, vCreatedBy, dCreatedDate,"+
									" vModifiedBy, dModifiedDate, iSynced, vSyncedMacId) values ( '"+roleIdN+"',"+
									" '"+txtRoleName.getValue().toString().trim()+"', '1', '"+sessionBean.getUserId()+"',"+
									" getdate(), '"+sessionBean.getUserId()+"', getdate(), 0, '')";
						}
						else
						{
							sqlInfo = "update master.tbUserRoleInfo set vRoleName = '"+txtRoleName.getValue().toString().trim()+"',"+
									" vModifiedBy = '"+sessionBean.getUserId()+"', dModifiedDate = getdate(), iSynced = 0,"+
									" vSyncedMacId = '' where vRoleId = '"+roleId +"' ";
							sqlInfo = sqlInfo+" delete from master.tbUserRoleDetails where vRoleId = '"+roleId +"' ";
						}

						sqlDetails = " insert into master.tbUserRoleDetails(vRoleId, vMenuId, iInsert, iUpdate, iDelete) values";
						for (int i=0; i<tbLblMenuId.size(); i++)
						{
							if (tbChkSelect.get(i).getValue().booleanValue())
							{
								sqlTables = sqlTables+" ('"+roleIdN+"', '"+tbLblMenuId.get(i).getValue().toString()+"',"+
										" '"+(tbChkInsert.get(i).getValue().booleanValue()?"1":"0")+"'," +
										" '"+(tbChkUpdate.get(i).getValue().booleanValue()?"1":"0")+"'," +
										" '"+(tbChkDelete.get(i).getValue().booleanValue()?"1":"0")+"'),";
								menusId = menusId+"'"+tbLblMenuId.get(i).getValue().toString()+"',";
							}
						}

						if (!menusId.isEmpty())
						{
							parentSql = " insert into master.tbUserRoleDetails(vRoleId, vMenuId, iInsert, iUpdate, iDelete)"+
									" select '"+roleIdN+"', vMenuId, 1, 1, 1 from master.tbMenuList where vMenuId in"+
									" (select distinct vParentId from master.tbMenuList where vMenuId in"+
									" ("+menusId.substring(0, menusId.length()-1)+")) and iActive = 1 union all"+
									" select '"+roleIdN+"', vMenuId, 1, 1, 1 from master.tbMenuList where vMenuId in"+
									" (select vParentId from master.tbMenuList where vMenuId in"+
									" (select distinct vParentId from master.tbMenuList where vMenuId in"+
									" ("+menusId.substring(0, menusId.length()-1)+")) and iActive = 1)";
						}

						String mergeSql = sqlInfo+ sqlDetails+ sqlTables.substring(0, sqlTables.length()-1)+" "+parentSql;
						if (rig.insertData(mergeSql))
						{
							txtClear();
							cm.showNotification("success", "Successfull!", "All information saved successfully.");
							if (flag.equals("Edit"))
							{ close(); }
							tableClear();
							cBtn.btnSave.setEnabled(true);
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

	private VerticalLayout buildLayout()
	{
		VerticalLayout layout = new VerticalLayout();
		layout.setMargin(true);
		layout.setSpacing(true);

		GridLayout grid = new GridLayout(4, 1);
		grid.setSpacing(true);

		txtRoleName = new TextField();
		txtRoleName.setImmediate(true);
		txtRoleName.setStyleName(ValoTheme.TEXTFIELD_TINY);
		txtRoleName.setRequired(true);
		txtRoleName.setRequiredError("This field is required");
		grid.addComponent(new Label("Role Name: "), 0, 0);
		grid.addComponent(txtRoleName, 1, 0);

		cmbModuleName = new MultiComboBox();
		cmbModuleName.setInputPrompt("Select module name");
		cmbModuleName.setDescription("Select to filter menu");
		grid.addComponent(new Label("Module Name: "), 2, 0);
		grid.addComponent(cmbModuleName, 3, 0);

		buildTable();

		layout.addComponents(grid, tblMenuList, tblMenuList.createControls(), cBtn);
		layout.setComponentAlignment(cBtn, Alignment.BOTTOM_CENTER);

		return layout;
	}

	private void buildTable()
	{
		tblMenuList = new TablePaged();

		tblMenuList.addContainerProperty("Select", CheckBox.class, new CheckBox(), null, null, Align.CENTER);

		tblMenuList.addContainerProperty("Menu Id", Label.class, new Label(), null, null, Align.CENTER);
		tblMenuList.setColumnCollapsed("Menu Id", true);

		tblMenuList.addContainerProperty("Module Name", Label.class, new Label(), null, null, Align.CENTER);

		tblMenuList.addContainerProperty("Menu Name", Label.class, new Label(), null, null, Align.CENTER);

		tblMenuList.addContainerProperty("Insert", CheckBox.class, new CheckBox(), null, null, Align.CENTER);

		tblMenuList.addContainerProperty("Update", CheckBox.class, new CheckBox(), null, null, Align.CENTER);

		tblMenuList.addContainerProperty("Delete", CheckBox.class, new CheckBox(), null, null, Align.CENTER);
	}

	private void tableRowAdd(final int ar)
	{
		try
		{
			tbChkSelect.add(ar, new CheckBox());
			tbChkSelect.get(ar).setWidth("100%");
			tbChkSelect.get(ar).setImmediate(true);
			tbChkSelect.get(ar).setStyleName(ValoTheme.CHECKBOX_SMALL);
			tbChkSelect.get(ar).setDescription("Click table header '"+"Select"+"' to select all.");
			tbChkSelect.get(ar).addValueChangeListener(event -> checkAllOptions(ar));

			tbLblMenuId.add(ar, new Label());
			tbLblMenuId.get(ar).setWidth("100%");
			tbLblMenuId.get(ar).setImmediate(true);

			tbLblModuleName.add(ar, new Label());
			tbLblModuleName.get(ar).setWidth("100%");
			tbLblModuleName.get(ar).setImmediate(true);

			tbLblMenuName.add(ar, new Label());
			tbLblMenuName.get(ar).setWidth("100%");
			tbLblMenuName.get(ar).setImmediate(true);

			tbChkInsert.add(ar, new CheckBox());
			tbChkInsert.get(ar).setWidth("100%");
			tbChkInsert.get(ar).setImmediate(true);
			tbChkInsert.get(ar).setStyleName(ValoTheme.CHECKBOX_SMALL);
			tbChkInsert.get(ar).setEnabled(false);

			tbChkUpdate.add(ar, new CheckBox());
			tbChkUpdate.get(ar).setWidth("100%");
			tbChkUpdate.get(ar).setImmediate(true);
			tbChkUpdate.get(ar).setStyleName(ValoTheme.CHECKBOX_SMALL);
			tbChkUpdate.get(ar).setEnabled(false);

			tbChkDelete.add(ar, new CheckBox());
			tbChkDelete.get(ar).setWidth("100%");
			tbChkDelete.get(ar).setImmediate(true);
			tbChkDelete.get(ar).setStyleName(ValoTheme.CHECKBOX_SMALL);
			tbChkDelete.get(ar).setEnabled(false);

			tblMenuList.addItem(new Object[]{tbChkSelect.get(ar), tbLblMenuId.get(ar), tbLblModuleName.get(ar),
					tbLblMenuName.get(ar), tbChkInsert.get(ar), tbChkUpdate.get(ar), tbChkDelete.get(ar)},ar);
		}
		catch(Exception exp)
		{ cm.showNotification("failure", "Error!", "Can't add rows to table"); }
	}

	private void checkAllOptions(int ar)
	{
		if (!tbLblMenuId.get(ar).getValue().toString().isEmpty())
		{
			tbChkInsert.get(ar).setEnabled(tbChkSelect.get(ar).getValue().booleanValue());
			tbChkUpdate.get(ar).setEnabled(tbChkSelect.get(ar).getValue().booleanValue());
			tbChkDelete.get(ar).setEnabled(tbChkSelect.get(ar).getValue().booleanValue());

			tbChkInsert.get(ar).setValue(tbChkSelect.get(ar).getValue().booleanValue());
			tbChkUpdate.get(ar).setValue(tbChkSelect.get(ar).getValue().booleanValue());
			tbChkDelete.get(ar).setValue(tbChkSelect.get(ar).getValue().booleanValue());
		}
		else
		{ cm.showNotification("warning", "Warning!", "Menu not found."); }
	}

	private void roleDataLoad()
	{
		String sql = "select vModuleId, vModuleName from master.tbModuleMaster"+
				" where iActive = 1 order by vModuleName";
		//cmbModuleName.addItem(cm.selectSql(sql));
		for (Iterator<?> iter = cm.selectSql(sql).iterator(); iter.hasNext();)
		{
			Object[] element = (Object[]) iter.next();
			cmbModuleName.addItem(element[0].toString());
			cmbModuleName.setItemCaption(element[0].toString(), element[1].toString());
		}
	}

	private void loadMenuList()
	{
		String mod = cmbModuleName.getValue().toString().replace("[", "").replace("]", "").trim();
		String moduleId = mod.isEmpty()? moduleId = "and iModuleId like '%'":"and iModuleId in ("+mod+")";
		//if (flag.equals("Add"))
		{ tableClear(); }
		int i = 0;
		String sql = "select ml.vMenuId, ml.vMenuName, mm.vModuleName from master.tbMenuList ml,"+
				" master.tbModuleMaster mm where ml.iModuleId = mm.vModuleId "+moduleId+"" +
				" and ml.iActive = 1 and ml.vURL != '#' order by mm.vModuleName, ml.vMenuName";
		//System.out.println(sql);
		for (Iterator<?> iter = cm.selectSql(sql).iterator(); iter.hasNext();)
		{
			Object[] element = (Object[]) iter.next();
			if (tbLblMenuId.size() <= i)
			{ tableRowAdd(i); }

			tbLblMenuId.get(i).setValue(element[0].toString());
			tbLblMenuName.get(i).setValue(element[1].toString());
			tbLblModuleName.get(i).setValue(element[2].toString());
			i++;
		}
		tblMenuList.nextPage();
		tblMenuList.previousPage();
	}

	private void loadEditData()
	{
		//tableClear();
		//String moduleId = cmbModuleName.getValue()!=null?cmbModuleName.getValue().toString():"%";
		int i = 0;
		String sql = ""; 
		/*if (flag.equals("Edit"))
		{
			sql = "select uri.vRoleName, urd.vMenuId, ml.vMenuName, iInsert, iUpdate, iDelete, iModuleId, 1 sel" +
					" from master.tbUserRoleInfo uri, master.tbUserRoleDetails urd, master.tbMenuList ml" +
					" where uri.vRoleId = urd.vRoleId and urd.vMenuId = ml.vMenuId and uri.vRoleId = '"+roleId+"'" +
					" union all"+
					" select '', vMenuId, vMenuName, 0, 0, 0, iModuleId, 0 sel from master.tbMenuList ml where"+
					" iModuleId like '"+moduleId+"' and vMenuId not in (select urd.vMenuId from master.tbUserRoleInfo"+
					" uri, master.tbUserRoleDetails urd, master.tbMenuList ml where uri.vRoleId = urd.vRoleId"+
					" and uri.vRoleId = '"+roleId+"') order by iModuleId, ml.vMenuName";
		}
		else*/
		{
			sql = "select uri.vRoleName, urd.vMenuId, ml.vMenuName, iInsert, iUpdate, iDelete, iModuleId, 1 sel" +
					" from master.tbUserRoleInfo uri, master.tbUserRoleDetails urd, master.tbMenuList ml" +
					" where uri.vRoleId = urd.vRoleId and urd.vMenuId = ml.vMenuId and uri.vRoleId = '"+roleId+"' " +
					" order by iModuleId, ml.vMenuName";
		}
		//System.out.println(sql);
		for (Iterator<?> iter = cm.selectSql(sql).iterator(); iter.hasNext();)
		{
			Object[] element = (Object[]) iter.next();

			if (i == 0)
			{ txtRoleName.setValue(element[0].toString()); }
			selecTableDateEdit(element[1].toString(), (element[3].toString().equals("1")?true:false),
					(element[4].toString().equals("1")?true:false), (element[5].toString().equals("1")?true:false),
					Integer.parseInt(element[7].toString()));
			i++;
		}
	}

	private void selecTableDateEdit(String menuId, boolean insert,
			boolean update, boolean delete, int sel)
	{
		for (int i=0; i<tbLblMenuId.size(); i++)
		{
			if (tbLblMenuId.get(i).getValue().toString().equals(menuId))
			{
				if (sel == 1) { tbChkSelect.get(i).setValue(true); }
				tbChkInsert.get(i).setValue(insert);
				tbChkUpdate.get(i).setValue(update);
				tbChkDelete.get(i).setValue(delete);
			}
		}
	}

	private void txtClear()
	{ txtRoleName.setValue(""); }

	private void tableClear()
	{ cm.tableClear(tblMenuList, tbLblMenuId); }
}
