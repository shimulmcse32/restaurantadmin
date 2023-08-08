package com.common.share;

import com.jensjansson.pagedtable.PagedTableContainer;
import com.vaadin.data.Container;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.themes.Reindeer;
import com.vaadin.ui.themes.ValoTheme;

import java.util.ArrayList;
import java.util.List;

public class PagedTable extends Table
{
	private static final long serialVersionUID = 6881455780158545828L;
	public Label totalPagesLabel;
	public ComboBox itemsPerPageSelect;

	public interface PageChangeListener
	{ public void pageChanged(PagedTableChangeEvent event); }

	public class PagedTableChangeEvent
	{
		final PagedTable table;

		public PagedTableChangeEvent(PagedTable table)
		{ this.table = table; }

		public PagedTable getTable()
		{ return table; }

		public int getCurrentPage()
		{ return table.getCurrentPage(); }

		public int getTotalAmountOfPages()
		{ return table.getTotalAmountOfPages(); }
	}

	private List<PageChangeListener> listeners = null;

	private PagedTableContainer container;

	public PagedTable() { this(null); }

	public PagedTable(String caption)
	{
		super(caption);
		addStyleName("pagedtable");
	}

	public HorizontalLayout createControls()
	{
		Label itemsPerPageLabel = new Label("Items per page:");
		itemsPerPageLabel.setStyleName(ValoTheme.LABEL_TINY);

		itemsPerPageSelect = new ComboBox();
		itemsPerPageSelect.setStyleName(ValoTheme.COMBOBOX_TINY);
		itemsPerPageSelect.addItem("10");
		itemsPerPageSelect.addItem("15");
		itemsPerPageSelect.addItem("25");
		itemsPerPageSelect.addItem("35");
		itemsPerPageSelect.addItem("50");
		itemsPerPageSelect.addItem("100");
		itemsPerPageSelect.setImmediate(true);
		itemsPerPageSelect.setNullSelectionAllowed(false);
		itemsPerPageSelect.setWidth("70px");
		itemsPerPageSelect.addValueChangeListener(new ValueChangeListener()
		{
			private static final long serialVersionUID = -2255853716069800092L;
			public void valueChange(com.vaadin.data.Property.ValueChangeEvent event)
			{ setPageLength(Integer.valueOf(String.valueOf(event.getProperty().getValue()))); }
		});
		//itemsPerPageSelect.select("10");

		Label pageLabel = new Label("Page:&nbsp;", ContentMode.HTML);
		pageLabel.setStyleName(ValoTheme.LABEL_TINY);

		final TextField currentPageTextField = new TextField();
		currentPageTextField.setEnabled(false);
		//currentPageTextField.setDecimalPrecision(0);
		currentPageTextField.setValue(String.valueOf(getCurrentPage()));
		currentPageTextField.setConverter(Integer.class);
		//currentPageTextField.addValidator(new IntegerRangeValidator("Wrong page number", 1, getTotalAmountOfPages()));
		Label separatorLabel = new Label("&nbsp;/&nbsp;", ContentMode.HTML);
		separatorLabel.setStyleName(ValoTheme.LABEL_TINY);

		totalPagesLabel = new Label(String.valueOf(getTotalAmountOfPages()), ContentMode.HTML);
		totalPagesLabel.setStyleName(ValoTheme.LABEL_TINY);
		currentPageTextField.setStyleName(Reindeer.TEXTFIELD_SMALL);
		currentPageTextField.setImmediate(true);
		currentPageTextField.setWidth("100px");
		currentPageTextField.setStyleName(ValoTheme.TEXTFIELD_TINY);
		currentPageTextField.addValueChangeListener(new ValueChangeListener()
		{
			private static final long serialVersionUID = -2255853716069800092L;
			public void valueChange(com.vaadin.data.Property.ValueChangeEvent event)
			{
				if (currentPageTextField.isValid() && currentPageTextField.getValue() != null)
				{
					int page = Integer.valueOf(String.valueOf(currentPageTextField.getValue()));
					setCurrentPage(page);
				}
			}
		});
		pageLabel.setWidth(null);
		currentPageTextField.setWidth("30px");
		separatorLabel.setWidth(null);
		totalPagesLabel.setWidth(null);

		HorizontalLayout controlBar = new HorizontalLayout();
		HorizontalLayout pageSize = new HorizontalLayout();
		HorizontalLayout pageManagement = new HorizontalLayout();
		final Button first = new Button("<<", new ClickListener()
		{
			private static final long serialVersionUID = -355520120491283992L;
			public void buttonClick(ClickEvent event)
			{ setCurrentPage(0); }
		});

		final Button previous = new Button("<", new ClickListener()
		{
			private static final long serialVersionUID = -355520120491283992L;
			public void buttonClick(ClickEvent event)
			{ previousPage(); }
		});

		final Button next = new Button(">", new ClickListener()
		{
			private static final long serialVersionUID = -1927138212640638452L;
			public void buttonClick(ClickEvent event)
			{ nextPage(); }
		});

		final Button last = new Button(">>", new ClickListener()
		{
			private static final long serialVersionUID = -355520120491283992L;
			public void buttonClick(ClickEvent event)
			{ setCurrentPage(getTotalAmountOfPages()); }
		});

		first.setStyleName(ValoTheme.BUTTON_TINY);
		first.setStyleName(Reindeer.BUTTON_LINK);
		previous.setStyleName(ValoTheme.BUTTON_TINY);
		previous.setStyleName(Reindeer.BUTTON_LINK);
		next.setStyleName(ValoTheme.BUTTON_TINY);
		next.setStyleName(Reindeer.BUTTON_LINK);
		last.setStyleName(ValoTheme.BUTTON_TINY);
		last.setStyleName(Reindeer.BUTTON_LINK);

		itemsPerPageLabel.addStyleName("pagedtable-itemsperpagecaption");
		itemsPerPageSelect.addStyleName("pagedtable-itemsperpagecombobox");
		pageLabel.addStyleName("pagedtable-pagecaption");
		currentPageTextField.addStyleName("pagedtable-pagefield");
		separatorLabel.addStyleName("pagedtable-separator");
		totalPagesLabel.addStyleName("pagedtable-total");
		first.addStyleName("pagedtable-first");
		previous.addStyleName("pagedtable-previous");
		next.addStyleName("pagedtable-next");
		last.addStyleName("pagedtable-last");

		itemsPerPageLabel.addStyleName("pagedtable-label");
		itemsPerPageSelect.addStyleName("pagedtable-combobox");
		pageLabel.addStyleName("pagedtable-label");
		currentPageTextField.addStyleName("pagedtable-label");
		separatorLabel.addStyleName("pagedtable-label");
		totalPagesLabel.addStyleName("pagedtable-label");
		first.addStyleName("pagedtable-button");
		previous.addStyleName("pagedtable-button");
		next.addStyleName("pagedtable-button");
		last.addStyleName("pagedtable-button");

		pageSize.addComponent(itemsPerPageLabel);
		pageSize.addComponent(itemsPerPageSelect);
		pageSize.setComponentAlignment(itemsPerPageLabel, Alignment.MIDDLE_LEFT);
		pageSize.setComponentAlignment(itemsPerPageSelect, Alignment.MIDDLE_LEFT);
		pageSize.setSpacing(true);
		pageManagement.addComponent(first);
		pageManagement.addComponent(previous);
		pageManagement.addComponent(pageLabel);
		pageManagement.addComponent(currentPageTextField);
		pageManagement.addComponent(separatorLabel);
		pageManagement.addComponent(totalPagesLabel);
		pageManagement.addComponent(next);
		pageManagement.addComponent(last);
		pageManagement.setComponentAlignment(first, Alignment.MIDDLE_LEFT);
		pageManagement.setComponentAlignment(previous, Alignment.MIDDLE_LEFT);
		pageManagement.setComponentAlignment(pageLabel, Alignment.MIDDLE_LEFT);
		pageManagement.setComponentAlignment(currentPageTextField, Alignment.MIDDLE_LEFT);
		pageManagement.setComponentAlignment(separatorLabel, Alignment.MIDDLE_LEFT);
		pageManagement.setComponentAlignment(totalPagesLabel, Alignment.MIDDLE_LEFT);
		pageManagement.setComponentAlignment(next, Alignment.MIDDLE_LEFT);
		pageManagement.setComponentAlignment(last, Alignment.MIDDLE_LEFT);
		pageManagement.setWidth(null);
		pageManagement.setSpacing(true);
		controlBar.addComponent(pageSize);
		controlBar.addComponent(pageManagement);
		controlBar.setComponentAlignment(pageManagement, Alignment.MIDDLE_CENTER);
		controlBar.setWidth("100%");
		controlBar.setExpandRatio(pageSize, 1);

		addListener(new PageChangeListener()
		{
			public void pageChanged(PagedTableChangeEvent event)
			{
				first.setEnabled(container.getStartIndex() > 0);
				previous.setEnabled(container.getStartIndex() > 0);
				next.setEnabled(container.getStartIndex() < container.getRealSize() - getPageLength());
				last.setEnabled(container.getStartIndex() < container.getRealSize() - getPageLength());
				currentPageTextField.setValue(String.valueOf(getCurrentPage()));
				totalPagesLabel.setValue(String.valueOf(getTotalAmountOfPages()));
				itemsPerPageSelect.setValue(String.valueOf(getPageLength()));
			}
		});
		return controlBar;
	}

	public Container.Indexed getContainerDataSource()
	{ return container; }

	public void setContainerDataSource(Container newDataSource)
	{
		if (!(newDataSource instanceof Container.Indexed))
		{
			throw new IllegalArgumentException("PagedTable can only use containers that implement Container.Indexed");
		}
		PagedTableContainer pagedTableContainer = new PagedTableContainer((Container.Indexed) newDataSource);
		pagedTableContainer.setPageLength(getPageLength());
		super.setContainerDataSource(pagedTableContainer);
		this.container = pagedTableContainer;
		firePagedChangedEvent();
	}

	private void setPageFirstIndex(int firstIndex)
	{
		if (container != null)
		{
			if (firstIndex <= 0)
			{ firstIndex = 0; }
			if (firstIndex > container.getRealSize() - 1)
			{
				int size = container.getRealSize() - 1;
				int pages = 0;
				if (getPageLength() != 0)
				{
					pages = (int) Math.floor(0.0 + size / getPageLength());
				}
				firstIndex = pages * getPageLength();
			}
			container.setStartIndex(firstIndex);
			setCurrentPageFirstItemIndex(firstIndex);
			containerItemSetChange(new Container.ItemSetChangeEvent()
			{
				private static final long serialVersionUID = -5083660879306951876L;
				public Container getContainer()
				{ return container; }
			});
			if (alwaysRecalculateColumnWidths)
			{
				for (Object columnId : container.getContainerPropertyIds())
				{ setColumnWidth(columnId, -1); }
			}
			firePagedChangedEvent();
		}
	}

	private void firePagedChangedEvent()
	{
		if (listeners != null)
		{
			PagedTableChangeEvent event = new PagedTableChangeEvent(this);
			for (PageChangeListener listener : listeners)
			{ listener.pageChanged(event); }
		}
	}

	public void setPageLength(int pageLength)
	{
		if (pageLength >= 0 && getPageLength() != pageLength)
		{
			container.setPageLength(pageLength);
			super.setPageLength(pageLength);
			firePagedChangedEvent();
		}
	}

	public void nextPage()
	{ setPageFirstIndex(container.getStartIndex() + getPageLength()); }

	public void previousPage()
	{ setPageFirstIndex(container.getStartIndex() - getPageLength()); }

	public int getCurrentPage()
	{
		double pageLength = getPageLength();
		int page = (int) Math.floor((double) container.getStartIndex() / pageLength) + 1;
		if (page < 1)
		{ page = 1; }
		return page;
	}

	public void setCurrentPage(int page)
	{
		int newIndex = (page - 1) * getPageLength();
		if (newIndex < 0)
		{ newIndex = 0; }
		if (newIndex >= 0 && newIndex != container.getStartIndex())
		{ setPageFirstIndex(newIndex); }
	}

	public int getTotalAmountOfPages()
	{
		int size = container.getContainer().size();
		double pageLength = getPageLength();
		int pageCount = (int) Math.ceil(size / pageLength);
		if (pageCount < 1)
		{ pageCount = 1; }
		return pageCount;
	}

	public void addListener(PageChangeListener listener)
	{
		if (listeners == null)
		{ listeners = new ArrayList<PageChangeListener>(); }
		listeners.add(listener);
	}

	public void removeListener(PageChangeListener listener)
	{
		if (listeners == null)
		{ listeners = new ArrayList<PageChangeListener>(); }
		listeners.remove(listener);
	}

	public void setAlwaysRecalculateColumnWidths(boolean alwaysRecalculateColumnWidths)
	{ this.alwaysRecalculateColumnWidths = alwaysRecalculateColumnWidths; }
}