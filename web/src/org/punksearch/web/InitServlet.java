/***************************************************************************
 *                                                                         *
 *   PunkSearch - Searching over LAN                                       *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package org.punksearch.web;

import java.io.FileNotFoundException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.punksearch.common.PunksearchProperties;

public class InitServlet extends HttpServlet {

	public void init(ServletConfig config) throws ServletException {
		super.init(config);

		try {
			String home = getServletContext().getInitParameter("punksearch_home");
			if (home != null) {
				System.setProperty("org.punksearch.home", home);
			}
			PunksearchProperties.loadDefault();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		// System.setProperty("jcifs.smb.client.responseTimeout", "5000");
		System.setProperty("jcifs.util.loglevel", "0");

		SearcherWrapper.init();
	}
}
