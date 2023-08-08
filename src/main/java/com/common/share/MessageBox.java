package com.common.share;

import java.io.Serializable;

import com.vaadin.server.Resource;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.UI;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;
import com.common.share.ResourceFactory.IconResource;

public class MessageBox extends Window
{
	private static final long serialVersionUID = 1L;

	public static String DIALOG_DEFAULT_WIDTH = "420px";
	public static String DIALOG_DEFAULT_HEIGHT = "200px";
	public static String BUTTON_DEFAULT_WIDTH = "100px";
	public static Alignment BUTTON_DEFAULT_ALIGNMENT = Alignment.MIDDLE_RIGHT;
	public static String ICON_DEFAULT_SIZE = "48px";
	public static ResourceFactory RESOURCE_FACTORY = new ResourceFactory();

	public static VisibilityInterceptor VISIBILITY_INTERCEPTOR;

	private UI parentWindow;
	private EventListener listener;

	/**
	 * Defines the possible icon types for the message box.
	 * 
	 * @author Dieter Steinwedel
	 */
	public static enum Icon {
		NONE,
		QUESTION,
		INFO,
		WARN,
		ERROR
	}

	/**
	 * Defines the possible buttons type for the message box. The button type
	 * defines which icon is displayed on the button. The button type is also 
	 * used for the event listener (see {@link EventListener}) to determine which button is pressed.
	 * 
	 * @author ds
	 */
	public static enum ButtonType {
		OK,
		ABORT,
		CANCEL,
		YES,
		NO,
		CLOSE,
		SAVE,
		RETRY,
		IGNORE,
		/**
		 * The message box is not closed on using this value. You may have to explicitly close the message box.
		 */
		HELP,
		NONE,
		CUSTOM1,
		CUSTOM2,
		CUSTOM3,
		CUSTOM4,
		CUSTOM5
	}

	/**
	 * This class defines the appearance and caption of a button displayed inside 
	 * the message box.
	 * 
	 * @author Dieter Steinwedel
	 */
	public static class ButtonConfig implements Serializable {

		private static final long serialVersionUID = 1L;

		private Resource optionalResource;
		private ButtonType buttonType;
		private String caption;
		private String width;

		/**
		 * Equals to {@link ButtonConfig(ButtonType, String, String)}, but the button 
		 * icon resource is set explicitly.
		 * 
		 * @param optionalResource		an none default resource, that should be displayed as icon
		 */
		public ButtonConfig(ButtonType buttonType,
				String caption, 
				String width,
				Resource optionalResource) {
			super();
			if (buttonType == null) {
				throw new IllegalArgumentException("The field buttonType cannot be null");
			}
			this.optionalResource = optionalResource;
			this.buttonType = buttonType;
			this.caption = caption;
			this.width = width;
		}

		/**
		 * Equals to {@link ButtonConfig(ButtonType, String)}, but the button 
		 * width is set explicitly instead of using {@code BUTTON_DEFAULT_WIDTH}.
		 * 
		 * @param width button width
		 */
		public ButtonConfig(ButtonType buttonType,
				String caption, 
				String width) {
			this(buttonType, caption, width, null);
		}

		/**
		 * Creates an instance of this class for defining the appearance and 
		 * caption of a button displayed inside the message box.
		 * 
		 * @param buttonType 			which button type (see {@link ButtonType})
		 * @param caption				caption of the button
		 */
		public ButtonConfig(ButtonType buttonType,
				String caption) {
			this(buttonType, caption, BUTTON_DEFAULT_WIDTH);
		}

		/**
		 * Returns the optional resource, if set.
		 * 
		 * @return optional resource.
		 */
		public Resource getOptionalResource() {
			return optionalResource;
		}

		/**
		 * Returns the button type.
		 * 
		 * @return button type
		 */
		public ButtonType getButtonType() {
			return buttonType;
		}

		/**
		 * Returns the button caption.
		 * 
		 * @return button caption
		 */
		public String getCaption() {
			return caption;
		}

		/**
		 * Returns the button width.
		 * 
		 * @return button width
		 */
		public String getWidth() {
			return width;
		}

	}

	/**
	 * Intercepts the displaying and closing of the dialog.
	 * 
	 * @author ds
	 */
	public interface VisibilityInterceptor extends Serializable {

		/**
		 * Intercepts the displaying of the dialog.
		 * 
		 * @param parentWindow		The parent Window for the <code>MessageBox</code> 
		 * @param displayedDialog	The <code>MessageBox</code> instance to be displayed
		 * @return					Returns <code>false</code>, if the method implementation opens the <code>MessageBox</code> window itself. Otherwise returns <code>true</code>.
		 */
		public boolean show(UI parentWindow, MessageBox displayedDialog);

		/**
		 * Intercepts the closing of the dialog.
		 * 
		 * @param parentWindow		The parent Window for the <code>MessageBox</code> 
		 * @param displayedDialog	The <code>MessageBox</code> instance to be displayed
		 * @return					Returns <code>false</code>, if the method implementation closes the <code>MessageBox</code> window itself. Otherwise returns <code>true</code>.
		 */
		public boolean close(UI parentWindow, MessageBox displayedDialog);

	}

	/**
	 * This event listener is triggered when a button of the message box is pressed.
	 * 
	 * @author ds
	 */
	public interface EventListener extends Serializable {

		/**
		 * The method is triggered when a button of the message box is pressed. The
		 * parameter describes, which configured button was pressed.
		 * 
		 * @param buttonType pressed button
		 */
		public void buttonClicked(ButtonType buttonType);

	}

	/**
	 * Private implementation for handling the button events.
	 * 
	 * @author ds
	 */
	private class ButtonClickListener implements ClickListener {

		private static final long serialVersionUID = 1L;

		private ButtonType buttonType;

		/**
		 * The constructor.
		 */
		public ButtonClickListener(ButtonType buttonType) {
			this.buttonType = buttonType;
		}

		/**
		 * @see com.vaadin.ui.Button.ClickListener#buttonClick(com.vaadin.ui.Button.ClickEvent)
		 */
		public void buttonClick(ClickEvent event) {
			if (listener != null) {
				listener.buttonClicked(buttonType);
			}
			if (!buttonType.equals(ButtonType.HELP)) {
				close();
			}
		}
	}

	/**
	 * Creates an MessageBox instance.
	 * 
	 * @param parentWindow the parent window
	 * @param dialogCaption the caption of the dialog
	 * @param dialogIcon the displayed icon
	 * @param message the displayed message
	 * @param buttonConfigs the displayed buttons (see {@link ButtonConfig})
	 */
	public MessageBox(UI parentWindow, 
			String dialogCaption, 
			Icon dialogIcon, 
			String message, 
			ButtonConfig... buttonConfigs) {
		this(parentWindow, 
				DIALOG_DEFAULT_WIDTH, 
				DIALOG_DEFAULT_HEIGHT,
				dialogCaption, 
				dialogIcon, 
				message,
				BUTTON_DEFAULT_ALIGNMENT, 
				buttonConfigs);
		this.setStyleName("cwindow");
	}

	/**
	 * Similar to {@link #MessageBox(Window, String, Icon, String, ButtonConfig...)},
	 * but the alignment of the buttons is an additional button.
	 * 
	 * @param buttonsAlignment alignment of the button.
	 */
	/**
	 * Similar to {@link #MessageBox(Window, String, Icon, String, Alignment, ButtonConfig...)},
	 * but the window size is defined explicitly.
	 * 
	 * @param dialogWidth  width of the dialog (e.g. absolute "100px" or relative "50%")
	 * @param dialogHeight height of the dialog (e.g. absolute "100px" or relative "50%")
	 */
	public MessageBox(UI parentWindow, 
			String dialogWidth, 
			String dialogHeight,
			String dialogCaption, 
			Icon dialogIcon, 
			String message,
			Alignment buttonsAlignment, 
			ButtonConfig... buttonConfigs) {
		this(parentWindow, 
				dialogWidth, 
				dialogHeight,
				dialogCaption, 
				dialogIcon, 
				new Label(message, ContentMode.HTML),
				buttonsAlignment, 
				buttonConfigs);
	}

	/**
	 * Similar to {@link #MessageBox(Window, String, Icon, String, Alignment, ButtonConfig...)},
	 * but the message component is defined explicitly. The component can be even a composite
	 * of a layout manager and manager further Vaadin components.
	 * 
	 * @param messageComponent a Vaadin component
	 */
	@SuppressWarnings("incomplete-switch")
	public MessageBox(UI parentWindow, 
			String dialogWidth, 
			String dialogHeight,
			String dialogCaption, 
			Icon dialogIcon, 
			Component messageComponent,
			Alignment buttonsAlignment, 
			ButtonConfig... buttonConfigs) {
		super();
		this.parentWindow = parentWindow;
		setResizable(false);
		setClosable(false);
		setWidth(dialogWidth);
		setHeight(dialogHeight);
		setCaption(dialogCaption);

		GridLayout mainLayout = new GridLayout(2, 2);
		mainLayout.setMargin(true);
		mainLayout.setSpacing(true);
		mainLayout.setSizeFull();

		// Add Content
		messageComponent.setSizeFull();
		if (dialogIcon == null || Icon.NONE.equals(dialogIcon)) {
			mainLayout.addComponent(messageComponent, 0, 0, 1, 0);
			mainLayout.setRowExpandRatio(0, 1.0f);
			mainLayout.setColumnExpandRatio(0, 1.0f);
		} else {
			mainLayout.addComponent(messageComponent, 1, 0);
			messageComponent.setSizeFull();
			mainLayout.setRowExpandRatio(0, 1.0f);
			mainLayout.setColumnExpandRatio(1, 1.0f);
			mainLayout.setComponentAlignment(messageComponent, Alignment.MIDDLE_RIGHT);
			Embedded icon = null;
			switch (dialogIcon) {
			case QUESTION:
				icon = new Embedded(null, RESOURCE_FACTORY.loadResource(IconResource.QUESTION));
				break;
			case INFO:
				icon = new Embedded(null, RESOURCE_FACTORY.loadResource(IconResource.INFO));
				break;
			case WARN:
				icon = new Embedded(null, RESOURCE_FACTORY.loadResource(IconResource.WARN));
				break;
			case ERROR:
				icon = new Embedded(null, RESOURCE_FACTORY.loadResource(IconResource.ERROR));
				break;
			}
			mainLayout.addComponent(icon, 0, 0);
			icon.setWidth(ICON_DEFAULT_SIZE);
			icon.setHeight(ICON_DEFAULT_SIZE);
		}

		// Add Buttons
		HorizontalLayout buttonLayout = new HorizontalLayout();
		buttonLayout.setSpacing(true);
		mainLayout.addComponent(buttonLayout,0, 1, 1, 1);
		mainLayout.setComponentAlignment(buttonLayout, buttonsAlignment);
		for (ButtonConfig buttonConfig : buttonConfigs) {
			Button button = new Button(buttonConfig.getCaption(), new ButtonClickListener(buttonConfig.getButtonType()));
			button.setStyleName(ValoTheme.BUTTON_SMALL);
			if (buttonConfig.getWidth() != null) {
				button.setWidth(buttonConfig.getWidth());
			}
			if (buttonConfig.getOptionalResource() != null) {
				button.setIcon(buttonConfig.getOptionalResource());
			} else {
				Resource icon = null;
				switch (buttonConfig.getButtonType()) {
				case ABORT:
					icon = RESOURCE_FACTORY.loadResource(IconResource.ABORT);
					break;
				case CANCEL:
					icon = RESOURCE_FACTORY.loadResource(IconResource.CANCEL);
					break;
				case CLOSE:
					icon = RESOURCE_FACTORY.loadResource(IconResource.CLOSE);
					break;
				case HELP:
					icon = RESOURCE_FACTORY.loadResource(IconResource.HELP);
					break;
				case OK:
					icon = RESOURCE_FACTORY.loadResource(IconResource.OK);
					break;
				case YES:
					icon = RESOURCE_FACTORY.loadResource(IconResource.YES);
					button.focus();
					break;
				case NO:
					icon = RESOURCE_FACTORY.loadResource(IconResource.NO);
					break;
				case SAVE:
					icon = RESOURCE_FACTORY.loadResource(IconResource.SAVE);
					break;
				case RETRY:
					icon = RESOURCE_FACTORY.loadResource(IconResource.RETRY);
					break;
				case IGNORE:
					icon = RESOURCE_FACTORY.loadResource(IconResource.IGNORE);
					break;
				}
				button.setIcon(icon);
				button.setHeight("28px");
			}
			buttonLayout.addComponent(button);
		}

		setContent(mainLayout);
	}

	/**
	 * Displays the message box in modal style. No listener is used.
	 */
	public void show() {
		show(true, null);
	}

	/**
	 * Displays the message box. No listener is used.
	 * 
	 * @param modal switches the message box modal or none-modal
	 */
	public void show(boolean modal) {
		show(modal, null);
	}

	/**
	 * Displays the message box in modal style with triggering the event listener on pressing a button.
	 * 
	 * @param listener trigger the parameterized listener on pressing a button
	 */
	public void show(EventListener listener) {
		show(true, listener);
	}

	/**
	 * Displays the message box with triggering the event listener on pressing a button.
	 * 
	 * @param modal 	switches the message box modal or none-modal
	 * @param listener trigger the parameterized listener on pressing a button
	 */
	public void show(boolean modal, EventListener listener) {
		this.listener = listener;
		setModal(modal);
		if (VISIBILITY_INTERCEPTOR == null || (VISIBILITY_INTERCEPTOR != null && VISIBILITY_INTERCEPTOR.show(parentWindow, this))) {
			parentWindow.addWindow(this);
		}
	}

	/**
	 * Closes the message box. Call this method, if the dialog should be closed without a button event, e.g. by a timeout. 
	 * The buttons inside the dialog close automatically the message box. 
	 */
	public void close() {
		if (VISIBILITY_INTERCEPTOR == null || (VISIBILITY_INTERCEPTOR != null && VISIBILITY_INTERCEPTOR.close(parentWindow, this))) {
			parentWindow.removeWindow(this);
		}
	}

}
