package com.common.dashboard;

import java.util.Iterator;

import com.common.share.CommonMethod;
import com.common.share.SessionBean;
import com.example.gateway.RoleInfoGateway;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.event.LayoutEvents.LayoutClickEvent;
import com.vaadin.event.LayoutEvents.LayoutClickListener;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Responsive;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.Command;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;

@SuppressWarnings("serial")
public class UserHome extends Panel implements View
{
	private SessionBean sessionBean;
	private Label titleLabel;
	private NotificationsButton notificationsButton;
	private CssLayout dashboardPanels;
	private VerticalLayout root;
	private Window notificationsWindow;
	private TextArea notes;
	//private CommonMethod cm;
	private Button btnBack;

	private static RoleInfoGateway rig = new RoleInfoGateway();

	private VerticalLayout parentLayout;

	public UserHome(SessionBean sessionBean)
	{
		this.sessionBean = sessionBean;
		//cm = new CommonMethod(this.sessionBean);

		addStyleName(ValoTheme.PANEL_BORDERLESS);
		setSizeFull();

		root = new VerticalLayout();
		root.setSizeFull();
		root.setSpacing(false);
		root.addStyleName("dashboard-view");
		setContent(root);
		Responsive.makeResponsive(root);

		root.addComponent(buildHeader());

		buildNotes(root);

		addActions();
	}

	private void addActions()
	{
		// All the open sub-windows should be closed whenever the root layout
		// gets clicked.
		root.addLayoutClickListener(new LayoutClickListener()
		{
			public void layoutClick(LayoutClickEvent event)
			{
				for (Window window : getUI().getWindows())
				{ window.close(); }
				rig.updateNote(notes.getValue().toString(), sessionBean.getUserId());
			}
		});

		notes.addTextChangeListener(new TextChangeListener()
		{
			public void textChange(TextChangeEvent event)
			{ rig.updateNote(notes.getValue().toString(), sessionBean.getUserId()); }
		});

		setValueNote();
	}

	private void buildNotes(VerticalLayout root)
	{
		Component content = buildContent();
		root.addComponent(content);
		root.setExpandRatio(content, 1);
	}

	private Component buildHeader()
	{
		HorizontalLayout header = new HorizontalLayout();
		header.setSpacing(true);
		header.addStyleName("viewheader");

		titleLabel = new Label("User Home");
		titleLabel.setId("dashboard-title");
		titleLabel.setSizeUndefined();
		titleLabel.addStyleName(ValoTheme.LABEL_H1);
		titleLabel.addStyleName(ValoTheme.LABEL_NO_MARGIN);
		header.addComponent(titleLabel);

		notificationsButton = buildNotificationsButton();

		btnBack = new Button();
		btnBack.setIcon(FontAwesome.BACKWARD);
		btnBack.addStyleName("icon-edit");
		btnBack.setStyleName(ValoTheme.BUTTON_ICON_ONLY);
		btnBack.setDescription("Back to main panel");
		if (sessionBean.war.equals("ATC"))
		{btnBack.setVisible(true);}
		else
		{btnBack.setVisible(false);}

		HorizontalLayout tools = new HorizontalLayout(notificationsButton, btnBack);
		tools.addStyleName("toolbar");
		header.addComponents(tools);
		header.setResponsive(true);

		return header;
	}

	private NotificationsButton buildNotificationsButton()
	{
		NotificationsButton btnNotification = new NotificationsButton();
		btnNotification.setDescription("Notifications");
		btnNotification.addClickListener(new ClickListener()
		{
			public void buttonClick(ClickEvent event)
			{
				openNotificationsPopup(event);
				addNotification();
				btnNotification.removeStyleName("unread");
			}
		});
		return btnNotification;
	}

	private Component buildContent()
	{
		dashboardPanels = new CssLayout();
		dashboardPanels.addStyleName("dashboard-panels");
		dashboardPanels.setWidth("100%");
		Responsive.makeResponsive(dashboardPanels);

		dashboardPanels.addComponent(buildNotes());

		return dashboardPanels;
	}

	private Component buildNotes()
	{
		notes = new TextArea("Notes");
		notes.setValue("Remember to:");
		notes.setSizeFull();
		notes.addStyleName(ValoTheme.TEXTAREA_BORDERLESS);
		Component panel = createContentWrapper(notes);
		panel.addStyleName("notes");
		return panel;
	}

	private Component createContentWrapper(Component content)
	{
		CssLayout slot = new CssLayout();
		slot.setWidth("100%");
		slot.setHeight("350px");
		slot.addStyleName("dashboard-panel-slot");

		CssLayout card = new CssLayout();
		card.setWidth("100%");
		card.addStyleName(ValoTheme.LAYOUT_CARD);

		HorizontalLayout toolbar = new HorizontalLayout();
		toolbar.addStyleName("dashboard-panel-toolbar");
		toolbar.setWidth("100%");
		toolbar.setSpacing(false);

		Label caption = new Label(content.getCaption());
		caption.addStyleName(ValoTheme.LABEL_H4);
		caption.addStyleName(ValoTheme.LABEL_COLORED);
		caption.addStyleName(ValoTheme.LABEL_NO_MARGIN);
		content.setCaption(null);

		MenuBar tools = new MenuBar();
		tools.addStyleName(ValoTheme.MENUBAR_BORDERLESS);
		MenuItem max = tools.addItem("", FontAwesome.EXPAND, new Command()
		{
			public void menuSelected(MenuItem selectedItem)
			{
				if (!slot.getStyleName().contains("max"))
				{
					selectedItem.setIcon(FontAwesome.COMPRESS);
					toggleMaximized(slot, true);
				}
				else
				{
					slot.removeStyleName("max");
					selectedItem.setIcon(FontAwesome.EXPAND);
					toggleMaximized(slot, false);
				}
			}
		});
		max.setStyleName("icon-only");
		/*MenuItem root = tools.addItem("", FontAwesome.COG, null);
		root.addItem("Configure", new Command() {
			@Override
			public void menuSelected(MenuItem selectedItem) {
				Notification.show("Not implemented in this demo");
			}
		});
		root.addSeparator();
		root.addItem("Close", new Command() {
			@Override
			public void menuSelected(MenuItem selectedItem) {
				Notification.show("Not implemented in this demo");
			}
		});*/

		toolbar.addComponents(caption, tools);
		toolbar.setExpandRatio(caption, 1);
		toolbar.setComponentAlignment(caption, Alignment.MIDDLE_LEFT);

		card.addComponents(toolbar, content);
		slot.addComponent(card);
		return slot;
	}

	private void openNotificationsPopup(ClickEvent event)
	{
		parentLayout = new VerticalLayout();
		parentLayout.setSpacing(true);
		parentLayout.setMargin(true);

		Label title = new Label("Notifications");
		title.addStyleName(ValoTheme.LABEL_H3);
		title.addStyleName(ValoTheme.LABEL_NO_MARGIN);
		parentLayout.addComponent(title);
		parentLayout.setComponentAlignment(title, Alignment.TOP_CENTER);

		if (notificationsWindow == null)
		{
			notificationsWindow = new Window();
			notificationsWindow.setWidth(300.0f, Unit.PIXELS);
			notificationsWindow.setHeight("83%");
			notificationsWindow.addStyleName("notifications");
			notificationsWindow.setResizable(false);
			notificationsWindow.setDraggable(false);
			notificationsWindow.addCloseShortcut(KeyCode.ESCAPE, null);
			notificationsWindow.setContent(parentLayout);
		}

		if (!notificationsWindow.isAttached())
		{
			notificationsWindow.setPositionY(event.getClientY() - event.getRelativeY() + 40);
			getUI().addWindow(notificationsWindow);
			notificationsWindow.focus();
		}
		else
		{ notificationsWindow.close(); }
	}

	private void addNotification()
	{
		parentLayout.removeAllComponents();
		/*NotificationsLayout noti = new NotificationsLayout(sessionBean);
		parentLayout.addComponent(noti);*/

		HorizontalLayout footer = new HorizontalLayout();
		footer.addStyleName(ValoTheme.WINDOW_BOTTOM_TOOLBAR);
		footer.setWidth("100%");
		footer.setSpacing(false);
		Button showAll = new Button("View All Notifications", new ClickListener()
		{
			public void buttonClick(ClickEvent event)
			{ getNotificationWindow(); }
		});
		showAll.addStyleName(ValoTheme.BUTTON_BORDERLESS_COLORED);
		showAll.addStyleName(ValoTheme.BUTTON_SMALL);
		footer.addComponent(showAll);
		footer.setComponentAlignment(showAll, Alignment.TOP_CENTER);
		parentLayout.addComponent(footer);
	}

	private void getNotificationWindow()
	{
		/*NotificationsWindow win = new NotificationsWindow(sessionBean);
		getUI().addWindow(win);
		win.center();
		win.setModal(true);
		win.addCloseShortcut(KeyCode.ESCAPE, null);
		win.focus();*/
	}

	private void toggleMaximized(Component panel, boolean maximized)
	{
		for (Iterator<Component> it = root.iterator(); it.hasNext();)
		{ it.next().setVisible(!maximized); }
		dashboardPanels.setVisible(true);

		for (Iterator<Component> it = dashboardPanels.iterator(); it.hasNext();)
		{
			Component c = it.next();
			c.setVisible(!maximized);
		}

		if (maximized)
		{
			panel.setVisible(true);
			panel.addStyleName("max");
		}
		else
		{ panel.removeStyleName("max"); }
	}

	public static class NotificationsButton extends Button
	{
		private static String STYLE_UNREAD = "unread";
		public static String ID = "dashboard-notifications";

		public NotificationsButton()
		{
			setIcon(FontAwesome.BELL);
			setId(ID);
			addStyleName("notifications");
			addStyleName(ValoTheme.BUTTON_ICON_ONLY);

			//setUnreadCount(rig.getNotifCount(userId, branchId));
		}

		public void setUnreadCount(int count)
		{
			setCaption(String.valueOf(count));
			String description = "Notifications";
			if (count > 0)
			{
				addStyleName(STYLE_UNREAD);
				description += " (" + count + " unread)";
			}
			else
			{
				removeStyleName(STYLE_UNREAD);
			}
			setDescription(description);
		}
	}

	private void setValueNote()
	{
		RoleInfoGateway rig = new RoleInfoGateway();
		CommonMethod cm = new CommonMethod(sessionBean);

		String sqlSel = "select vUserId, vNoteDetails from tbUserNote where"+
				" vUserId = '"+sessionBean.getUserId()+"'";
		//System.out.println(sqlSel);
		for(Iterator<?> iter = cm.selectSql(sqlSel).iterator(); iter.hasNext();)
		{
			Object[] element = (Object[]) iter.next();
			notes.setValue(element[1].toString().replaceAll("~", "\n"));
		}

		if (notes.getValue().toString().replaceAll("\n", "~").isEmpty())
		{
			notes.setValue("Remember to:\n");
			String insert = "insert into tbUserNote(vUserId, vNoteDetails, dModifiedDate)"+
					" values ('"+sessionBean.getUserId()+"', 'Remember to:~', getdate())";
			rig.insertData(insert);
		}
	}

	public void enter(ViewChangeEvent event)
	{
		//buildCharts(root);
	}
}
