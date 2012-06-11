package org.punksearch.experiments;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;

/**
 * User: gubarkov
 * Date: 06.06.12
 * Time: 17:01
 */
public class ProbeExperimentsMain {
    static class IpProber extends Thread {
        String host;
        private int times;

        int smbSuccess = 0;

        IpProber(String host, int times) {
            this.host = host;
            this.times = times;
        }

        @Override
        public void run() {
            for (int i = 0; i < times; i++) {
                System.out.println("ftp: " + probe(host, 21));
                final boolean smbOk = probe(host, 443);
                if (smbOk) {
                    smbSuccess++;
                }
                System.out.println("smb: " + smbOk);
            }
        }

        private boolean probe(final String ip, int port) {
            boolean result;
            try {
                SocketAddress sockaddr = new InetSocketAddress(ip, port);
                Socket s = new Socket();
                s.connect(sockaddr, 1000);
                s.close();
                result = true;
            } catch (IOException e) {
                System.out.println(ip + ":" + port + " -> " + e);
                result = false;
            }
            System.out.println("Probe (connect): " + ip + ":" + port + " = " + result);
            return result;
        }
    }

    public static void main(String[] args) throws InterruptedException, FileNotFoundException {
        List<IpProber> threads = new ArrayList<IpProber>();

        final int times = 20;

        threads.add(new IpProber("194.85.80.8", times));
        threads.add(new IpProber("194.85.80.9", times));
        threads.add(new IpProber("194.85.80.10", times));
//        threads.add(new IpProber("194.85.80.11", times));
        threads.add(new IpProber("194.85.80.61", times));
//        threads.add(new Thread(new IpProber("194.85.80.12", times)));

        for (Thread thread : threads) {
            thread.start();
        }


        for (Thread thread : threads) {
            thread.join();
        }

        for (IpProber thread : threads) {
            System.out.println(thread.host + " : " + thread.smbSuccess + " of " + thread.times);
        }
    }
}
