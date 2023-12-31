package com.common.share;

import java.io.Serializable;

import com.vaadin.server.Resource;
import com.vaadin.server.ThemeResource;

/**
 * Manages the loading of icon resources. You can override the method <code>loadResource</code> to use customized icons.
 * The new <code>ResourceFactory</code> must be set to the static member <code>RESOURCE_FACTORY</code> of the class <code>MessageBox</code>.
 * 
 * @author Dieter Steinwedel
 */
public class ResourceFactory implements Serializable {
	
	private static final long serialVersionUID = 1L;

	/**
	 * A enumeration with named resources.
	 * 
	 * @author Dieter Steinwedel
	 */
	public static enum IconResource {
		QUESTION,
		INFO,
		WARN,
		ERROR,
		OK,
		ABORT,
		CANCEL,
		YES,
		NO,
		CLOSE,
		SAVE,
		RETRY,
		IGNORE,
		HELP,
		CUSTOM1,
		CUSTOM2,
		CUSTOM3,
		CUSTOM4,
		CUSTOM5
	}
	
	/**
	 * Loads the specified icon for the application.
	 * @param icon				The requested icon
	 * @param application		The application, where the icon is used
	 * @return					The loaded icon
	 */
	public Resource loadResource(IconResource icon) {
		switch (icon) {
		case QUESTION:
			return new ThemeResource("../dashboard/images/question.png");
		case INFO:
			return new ThemeResource("../dashboard/images/info.png");
		case WARN:
			return new ThemeResource("../dashboard/images/warn.png");
		case ERROR:
			return new ThemeResource("../dashboard/images/error.png");
		case OK:
			return new ThemeResource("../dashboard/images/btnico/tick.png");
		case ABORT:
			return new ThemeResource("../dashboard/images/btnico/cross.png");
		case CANCEL:
			return new ThemeResource("../dashboard/images/btnico/cross.png");
		case YES:
			return new ThemeResource("../dashboard/images/btnico/tick.png");
		case NO:
			return new ThemeResource("../dashboard/images/btnico/cross.png");
		case CLOSE:
			return new ThemeResource("../dashboard/images/btnico/door.png");
		case SAVE:
			return new ThemeResource("../dashboard/images/btnico/disk.png");
		case RETRY:
			return new ThemeResource("../dashboard/images/btnico/arrow_refresh.png");
		case IGNORE:
			return new ThemeResource("../dashboard/images/btnico/lightning_go.png");
		case HELP:
			return new ThemeResource("../dashboard/images/btnico/lightbulb.png");
		default:
			return null;
		}
	}

}
