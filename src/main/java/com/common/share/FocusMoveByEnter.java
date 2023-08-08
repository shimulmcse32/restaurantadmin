package com.common.share;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import com.vaadin.event.ShortcutAction.ModifierKey;
import com.vaadin.event.ShortcutListener;
import com.vaadin.server.VariableOwner;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.Component.Focusable;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.Window;

@SuppressWarnings("deprecation")
public class FocusMoveByEnter
{
	public FocusMoveByEnter(Window win, final ComponentContainer cparent[])
	{
		ArrayList<Component> c = new ArrayList<Component>();
		for (int i=0; i<cparent.length; i++)
		{
			for (Iterator<Component> it = ((ComponentContainer) cparent[i]).getComponentIterator(); it.hasNext();)
			{
				Object next = it.next();
				if (next instanceof Focusable)
				{ c.add((Component)next); }
			}
		}
		Component op[] = new Component[c.size()];
		c.toArray(op);
		new FocusMoveByEnter(win,op);
	}

	public FocusMoveByEnter(Window win,ArrayList<Component> ob)
	{
		Component[] c = new Component[ob.size()];
		ob.toArray(c);
		new FocusMoveByEnter(win,c);
	}

	@SuppressWarnings("serial")
	public FocusMoveByEnter(Window win,final Component oparent[])
	{
		win.addAction(new ShortcutListener("Next field", KeyCode.ENTER, null) 
		{
			public void handleAction(Object sender, Object target)
			{
				for (int i=0; i<oparent.length; i++)
				{
					// target is the field we're currently in, focus the next
					if (target instanceof Button)
					{
						Button b = (Button)target;
						((VariableOwner) b).changeVariables(b,Collections.singletonMap("state", (Object)true));
						break;
					}
					if (oparent[i] == target && i<(oparent.length-1))
					{
						Component next = oparent[i+1];
						if (next instanceof Focusable && next.isEnabled())
						{ ((Focusable) next).focus(); }
						else
						{
							for (;i<(oparent.length-1);i++)
							{
								next = oparent[i+1];
								if (next instanceof Focusable && oparent[i+1].isEnabled())
								{ ((Focusable) next).focus(); break; }
							}
						}
					}
				}
			}
		});
		int m[] = {ModifierKey.CTRL};

		win.addAction(new ShortcutListener("Next field", KeyCode.ARROW_UP, m)
		{
			public void handleAction(Object sender, Object target)
			{
				boolean tf = true;
				for (int i=0; i<oparent.length && tf; i++)
				{
					if (oparent[i] == target && i>0)
					{
						Component next = oparent[i-1];
						if (next instanceof Focusable && next.isEnabled())
						{ ((Focusable) next).focus(); }
						else
						{
							i--;
							if (i == 0) { break; }
							for (; i>0; i--)
							{
								next = oparent[i];
								if (next instanceof Focusable && next.isEnabled())
								{ ((Focusable) next).focus(); tf = false; break; }
							}
						}
					}
				}
			}
		});

		win.addAction(new ShortcutListener("Next field", KeyCode.ARROW_DOWN, m)
		{
			public void handleAction(Object sender, Object target)
			{
				for (int i=0; i<oparent.length; i++)
				{
					if (oparent[i] == target && i<(oparent.length-1))
					{
						Component next = oparent[i+1];
						if (next instanceof Focusable && next.isEnabled())
						{ ((Focusable) next).focus(); }
						else
						{
							for (; i<(oparent.length-1); i++)
							{
								next = oparent[i+1];
								if (next instanceof Focusable && oparent[i+1].isEnabled())
								{ ((Focusable) next).focus(); break; }
							}
						}
					}
				}
			}
		});
	}
}
