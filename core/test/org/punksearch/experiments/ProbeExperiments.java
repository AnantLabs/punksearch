package org.punksearch.experiments;

import org.junit.Test;
import org.punksearch.logic.online.Probe;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * User: gubarkov
 * Date: 06.06.12
 * Time: 17:01
 */
public class ProbeExperiments {
    @Test
    public void probeTest1() {
        final Probe probe = new Probe();
        final boolean res = probe.probe("smb://194.85.80.61");
        System.out.println(res);
    }

    @Test
    public void probeTest2() {
        final Probe probe = new Probe();
        for (int i = 0; i < 10; i++) {
            System.out.println("ftp: " + probe.probe("ftp://194.85.80.11"));
            System.out.println("smb: " + probe.probe("smb://194.85.80.11"));
        }
    }

    static class IpProber extends Thread {
        final Probe probe = new Probe();

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
                System.out.println("ftp: " + probe.probe("ftp://" + host));
                final boolean smbOk = probe.probe("smb://" + host);
                if (smbOk) {
                    smbSuccess++;
                }
                System.out.println("smb: " + smbOk);
            }
        }
    }

    @Test
    public void probeTest3() throws InterruptedException {
        List<IpProber> threads = new ArrayList<IpProber>();

        final int times = 5;

        threads.add(new IpProber("194.85.80.8", times));
        threads.add(new IpProber("194.85.80.9", times));
        threads.add(new IpProber("194.85.80.10", times));
        threads.add(new IpProber("194.85.80.11", times));
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

    @Test
    public void probeTest4() throws InterruptedException {
        final Probe probe = new Probe();

        for (int i = 0; i <= 12; i++) {
            System.out.println(probe.probe("ftp://194.85.80." + i));
            System.out.println(probe.probe("smb://194.85.80." + i));
        }
    }

}
