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
package org.punksearch.server;

import org.mortbay.jetty.Server;
import org.mortbay.jetty.nio.SelectChannelConnector;
import org.mortbay.jetty.webapp.WebAppContext;

public class EmbededServer
{
	private Server	server;

	private String	port;
	private String	warFileName;

	public EmbededServer(String port, String warFileName)
	{
		this.port = port;
		this.warFileName = warFileName;
	}

	public void start() throws Exception
	{
		SelectChannelConnector connector = new SelectChannelConnector();
		connector.setPort(Integer.valueOf(port));

		WebAppContext wac = new WebAppContext();
		wac.setContextPath("/punksearch");
		wac.setWar(warFileName);

		server = new Server();
		server.addConnector(connector);
		server.setHandler(wac);

		server.start();
		server.join();
	}

	public void stop() throws Exception
	{
		if (server != null)
			server.stop();
	}

}
