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

public class Main {

	public static void main(String[] args) throws Exception {
		String port = args[0];
		String war = args[1];
		EmbededServer server = new EmbededServer(port, war);
		server.start();
	}

}
