package com.group_finity.mascot.x11;

import com.sun.jna.Library;
import com.sun.jna.platform.unix.X11;
import com.sun.jna.ptr.PointerByReference;

interface X11Ext extends Library {
	public int XC_crosshair = 34;

	public void XMoveWindow(final X11.Display disp, final X11.Window win, final long x,
					 final long y);

	public void XResizeWindow(final X11.Display disp, final X11.Window win,
					   final long width, final long height);

	public void XMoveResizeWindow(final X11.Display disp, final X11.Window win,
						   final long x, final long y, final long width, final long height);

	public X11.Cursor XCreateFontCursor(final X11.Display disp, final long shape);

/*	int XGrabPointer(final X11.Display disp, final X11.Window grab_window,
					 final int owner_events, final NativeLong event_mask,
					 final int pointer_mode, final int keyboard_mode,
					 final X11.Window confine_to, final X11.Cursor cursor, final int time);*/

	public int XAllowEvents(final X11.Display disp, final int event_mode,
					 final int time);

	public int XUngrabPointer(final X11.Display disp, final int time);

	public X11.Window XmuClientWindow(final X11.Display disp, final X11.Window win);

	public int XGetIconName(final X11.Display disp, final X11.Window win,
					 final PointerByReference icon_name_return);

	public int XIconifyWindow(final X11.Display disp, final X11.Window win,
					   final int screen);
	}
