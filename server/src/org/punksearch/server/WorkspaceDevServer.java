package org.punksearch.server;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.webapp.*;
import sun.misc.JarFilter;

import java.io.File;
import java.io.IOException;

/**
 * User: gubarkov
 * Date: 22.05.12
 * Time: 15:49
 */
public class WorkspaceDevServer {
    static String[] modules = {
            "Web", "Core",
    };

    public static void main(String[] args) throws Exception {
        String rootFolder = ".";
        String webModule = join(rootFolder, "Web");

        Server server = new Server();

        Connector connector = new SelectChannelConnector();
        connector.setPort(8080);
        server.addConnector(connector);

        WebAppContext root = new WebAppContext(join(webModule, "WebContent"), "/");

        WebAppClassLoader rootClassLoader = new WebAppClassLoader(root);

        for (String moduleName : modules) {
            rootClassLoader.addClassPath(ideOutputClasspathForModule(rootFolder, moduleName));
        }

        addAllDependentJars(rootClassLoader, webModule);

        root.setClassLoader(rootClassLoader);

        HandlerList handlerList = new HandlerList();
        handlerList.addHandler(root);

        server.setHandler(handlerList);

        server.start();
    }

    private static String ideOutputClasspathForModule(String rootFolder, String moduleName) {
        // for IDEA
        return join(join(rootFolder, "out/production"), moduleName);
    }

    private static void addAllDependentJars(WebAppClassLoader rootClassLoader, String webModule) throws IOException {
        final File libDir = new File(join(webModule, "build/war_lib"));

        final File[] jars = libDir.listFiles(new JarFilter());

        jar_loop:
        for (File jar : jars) {
            for (String moduleName : modules) {
                if (jar.getName().startsWith(moduleName.toLowerCase())) {
                    System.out.println("Not loading: " + jar.getName());
                    // don't add this
                    continue jar_loop;
                }
            }

            System.out.println("Loading: " + jar.getName());
            rootClassLoader.addClassPath(jar.getAbsolutePath());
        }
    }

    private static String join(String rootFolder, final String child) {
        return new File(rootFolder, child).getAbsolutePath();
    }

}
