/*******************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.equinox.internal.frameworkadmin.equinox;

import java.util.Dictionary;
import java.util.Hashtable;
import org.eclipse.equinox.internal.provisional.frameworkadmin.FrameworkAdmin;
import org.eclipse.equinox.internal.provisional.frameworkadmin.Manipulator;
import org.osgi.framework.*;

/**
 * This bundle provides the {@link FrameworkAdmin} implementation for Felix.
 * 
 * This bundle registers {@link Manipulator} object with these service property values.
 *  
 *  FW_NAME = "Equinox";
 * 	FW_VERSION = "3.3";
 *	LAUCNHER_NAME = "Eclipse.exe";
 *  LAUNCHER_VERSION = "3.2";
 * 
 * The launching by the eclipse launcher is supported.
 * 
 * Handling plugins in non Jar format is not supported.
 * 
 * FwBundleState supports retrieving fw persistent data
 *  and  resolving bundles if running on equinox.
 * FwBundleState Does NOT support retrieving start Levels from fw persistent data location/
 *   
 */
public class Activator implements BundleActivator {
	private static BundleContext context;
	private ServiceRegistration registrationFA;
	EquinoxFwAdminImpl fwAdmin = null;

	private void registerFwAdmin() {
		Dictionary props = new Hashtable();
		props.put(Constants.SERVICE_VENDOR, "Eclipse.org");

		props.put(FrameworkAdmin.SERVICE_PROP_KEY_FW_NAME, EquinoxConstants.FW_NAME);
		props.put(FrameworkAdmin.SERVICE_PROP_KEY_FW_VERSION, EquinoxConstants.FW_VERSION);
		props.put(FrameworkAdmin.SERVICE_PROP_KEY_LAUNCHER_NAME, EquinoxConstants.LAUNCHER_NAME);
		props.put(FrameworkAdmin.SERVICE_PROP_KEY_LAUNCHER_VERSION, EquinoxConstants.LAUNCHER_VERSION);

		if (EquinoxFwAdminImpl.isRunningFw(context)) {
			props.put(FrameworkAdmin.SERVICE_PROP_KEY_RUNNING_SYSTEM_FLAG, "true");
			fwAdmin = new EquinoxFwAdminImpl(context, true);
		} else
			fwAdmin = new EquinoxFwAdminImpl(context);

		registrationFA = context.registerService(FrameworkAdmin.class.getName(), fwAdmin, props);
	}

	/**
	 * TODO: These services are never disposed.
	 */
	public static Object acquireService(String serviceName) {
		//be tolerant of concurrent shutdown
		BundleContext theContext = context;
		if (theContext == null)
			return null;
		ServiceReference reference = theContext.getServiceReference(serviceName);
		if (reference == null)
			return null;
		return theContext.getService(reference);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext bundleContext) throws Exception {
		Activator.context = bundleContext;
		Log.init(bundleContext);
		registerFwAdmin();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bundleContext) throws Exception {
		Activator.context = null;
		if (registrationFA != null)
			registrationFA.unregister();
		if (fwAdmin != null)
			fwAdmin.deactivate();
		Log.dispose();
	}
}