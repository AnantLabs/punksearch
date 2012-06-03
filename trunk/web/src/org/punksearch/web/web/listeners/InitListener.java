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
package org.punksearch.web.web.listeners;

import java.io.FileNotFoundException;

import javax.servlet.*;

import org.punksearch.common.PunksearchProperties;
import org.punksearch.web.SearcherWrapper;

public class InitListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {

        if (System.getProperty("org.punksearch.home") == null) {
            String home = servletContextEvent.getServletContext().getInitParameter("punksearch_home");
            if (home != null) {
                System.setProperty("org.punksearch.home", home);
            }
        }

        try {
            PunksearchProperties.loadDefault();
        } catch (FileNotFoundException e) {
            e.printStackTrace();// TODO: ?
        }

        // System.setProperty("jcifs.smb.client.responseTimeout", "5000");
        System.setProperty("jcifs.util.loglevel", "0");

        SearcherWrapper.init();
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
    }
}
