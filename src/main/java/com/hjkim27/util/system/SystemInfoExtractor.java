package com.hjkim27.util.system;

import com.google.gson.Gson;
import com.hjkim27.util.process.ProcessExecutor;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.*;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SystemInfoExtractor {

    private static String uptimeProcessLine = "";
    private static int uptimeUpIdx = -2;
    private static int uptimeUserIdx = -2;
    private static int uptimeLoadAvgIdx = -2;
    private static long uptimeCheckTime = -2;

    private static final long uptimeCheckInterval = 2000;

    public static String getUptime() {
        String uptimeLine = uptimeProcessResult();
        String result = "";
        try {
            String[] arr = uptimeLine.split(" ");

            log.debug("uptime exec stdout => parse => " + new Gson().toJson(arr));

            final int uptimeInterval = uptimeUserIdx - uptimeUpIdx;

            if (uptimeInterval > 3) {

                for (int i = 0; i < (uptimeInterval - 2); i++) {
                    result += (arr[uptimeUpIdx + 1 + i] + " ");
                }

            } else {
                result += (arr[uptimeUpIdx + 1]);
            }
            result = result.trim();

            if (result.endsWith(",")) {
                result = result.substring(0, result.length() - 1);
            }

        } catch (RuntimeException e) {
            resetUptimeCheck();
            log.warn(e + " | " + e.getMessage());
        } catch (Exception e) {
            resetUptimeCheck();
            log.warn(e + " | " + e.getMessage());
        }
        return result;
    }


    public static int getUsers() {
        String uptimeLine = uptimeProcessResult();
        int user = -1;
        try {
            String[] arr = uptimeLine.split(" ");

            log.debug("uptime exec stdout => parse => " + new Gson().toJson(arr));

            return Integer.parseInt(arr[uptimeUserIdx - 1]);

        } catch (RuntimeException e) {
            resetUptimeCheck();
            log.warn(e + " | " + e.getMessage());
        } catch (Exception e) {
            resetUptimeCheck();
            log.warn(e + " | " + e.getMessage());
        }
        return user;
    }

    public static String getLoadAverage() {
        String uptimeLine = uptimeProcessResult();
        String result = "";
        try {
            String[] arr = uptimeLine.split(" ");

            log.debug("uptime exec stdout => parse => " + new Gson().toJson(arr));

            int idx = uptimeLoadAvgIdx;

            String str = "";

            for (int i = 0; i < 3; i++) {
                idx++;
                str += arr[idx];
            }
            return str;

        } catch (RuntimeException e) {
            resetUptimeCheck();
            log.warn(e + " | " + e.getMessage());
        } catch (Exception e) {
            resetUptimeCheck();
            log.warn(e + " | " + e.getMessage());
        }
        return result;
    }

    private static final void resetUptimeCheck() {
        uptimeProcessLine = "";
        uptimeUpIdx = -2;
        uptimeUserIdx = -2;
        uptimeLoadAvgIdx = -2;
        uptimeCheckTime = -2;
    }


    private static String uptimeProcessResult() {
        final long now = System.currentTimeMillis();
        final boolean old = (now - uptimeCheckTime) > uptimeCheckInterval;

        log.debug("------------------------------------------------------------------------------");
        log.debug("uptimeProcessLine = {}", uptimeProcessLine);
        log.debug("now = {}  uptimeCheckTime = {}  old = {}", now, uptimeCheckTime, old);
        log.debug("uptimeUpIdx = {}  uptimeUserIdx = {}  uptimeLoadAvgIdx = {}", uptimeUpIdx, uptimeUserIdx, uptimeLoadAvgIdx);

        if (old) {
            uptimeCheckTime = now;
            String line = "";
            try {
                line = ProcessExecutor.runSimpleCommand("uptime");

                log.debug("new uptimeProcessLine = {}", uptimeProcessLine);

            } catch (IOException | InterruptedException e) {
                log.warn(e.getMessage(), e);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }

            if (line.length() > 4) {
                line = line.replace("   ", " ");
                line = line.replace("  ", " ");
                line = line.replace("  ", " ");
                line = line.replace("load average", "loadaverage");
            }
            uptimeProcessLine = line;
        }

        uptimeProcessLine = uptimeProcessLine.trim();

        String[] arr = uptimeProcessLine.split(" ");

        for (int i = 0; i < arr.length; i++) {
            if (arr[i].toLowerCase().contains("up")) {
                uptimeUpIdx = i;
                continue;
            }
            if (arr[i].toLowerCase().contains("users")) {
                uptimeUserIdx = i;
                continue;
            }
            if (arr[i].toLowerCase().contains("loadaverage")) {
                uptimeLoadAvgIdx = i;
                continue;
            }
        }

        return uptimeProcessLine;
    }


    public static String getLocalIpAddress() {
        String ip = null;
        try {
            ip = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            log.warn(e.getMessage(), e);
            ip = getIpAddress();
        }
        if (ip == null || ip.trim().equals("127.0.0.1")) {
            return getIpAddress();
        }

        return ip;
    }

    public static String getIpAddress() {
        String ip = null;
        try {
            boolean isLB = true;
            Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();

            while (en.hasMoreElements()) {
                NetworkInterface ni = en.nextElement();
                if (ni.isLoopback()) {
                    continue;
                }

                Enumeration<InetAddress> ia = ni.getInetAddresses();
                while (ia.hasMoreElements()) {
                    InetAddress tmp = ia.nextElement();

                    if (tmp.getHostAddress() != null && tmp.getHostAddress().indexOf(".") > -1) {
                        ip = tmp.getHostAddress();
                        isLB = false;
                        break;
                    }
                }
                if (!isLB) {
                    break;
                }
            }
        } catch (SocketException e) {
            log.warn(e.getMessage(), e);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return ip;
    }


    /**
     * 파일시스템 정보를 얻는다
     *
     * @return <PRE>
     * List&lt;FileSystemInfo&gt;
     * {@link FileSystemInfo}
     * </PRE>
     */
    public static List<FileSystemInfo> getFileSystemInfo() {
        List<FileSystemInfo> fileSystemInfoList = new ArrayList<FileSystemInfo>();
        String lineAdd = "";

        StringBuilder sb = new StringBuilder();

        try {
            List<String> dfh = ProcessExecutor.runCommand("df -h");

            if (dfh != null) {

                for (String line : dfh) {
                    if (!line.startsWith("Filesystem")) {
                        StringTokenizer token = new StringTokenizer(line);
                        if (token.countTokens() == 6) {
                            lineAdd = "";
                            fileSystemInfoList.add(new FileSystemInfo(line));
                        } else {
                            sb.append(line);
                            StringTokenizer tokenAdd = new StringTokenizer(lineAdd);
                            if (tokenAdd.countTokens() == 6) {
                                fileSystemInfoList.add(new FileSystemInfo(lineAdd));
                            }
                        }
                    }
                }

            }
        } catch (IOException | InterruptedException e) {
            log.warn(e.getMessage(), e);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        return fileSystemInfoList;
    }

    public static synchronized List<CpuInfo> getcpuinfo() {

        String line = "";
        CpuInfo cpuinfo = null;

        List<CpuInfo> cpuList = new ArrayList<>();

        try (FileInputStream file = new FileInputStream("/proc/cpuinfo"); InputStreamReader is = new InputStreamReader(file); BufferedReader in = new BufferedReader(is);) {

            try {
                while ((line = in.readLine()) != null) {
                    if (line.startsWith("model name")) {
                        cpuinfo = new CpuInfo();
                        cpuinfo.setCpuName(getValue(line));
                    }
                    if (line.startsWith("cpu MHz")) {
                        cpuinfo.setCpuMhz(getValue(line));
                    }
                    if (line.startsWith("cpu cores")) {
                        cpuinfo.setCore(Integer.parseInt(getValue(line)));
                    }
                    if (line.startsWith("cache size")) {
                        cpuinfo.setCpuCache(getValue(line));
                        cpuList.add(cpuinfo);
                    }
                }

                Map<Integer, CpuInfo> cpuSet = new HashMap<>();

                for (CpuInfo cpuInfo : cpuList) {

                    final int hashKey = cpuInfo.hashCode();
                    if (cpuSet.containsKey(hashKey)) {
                        cpuSet.get(hashKey).setThread(cpuSet.get(hashKey).getThread() + 1);
                    } else {
                        cpuSet.put(hashKey, cpuInfo);
                    }
                }
                cpuList.clear();

                cpuList = new ArrayList<>(cpuSet.values());

            } catch (NullPointerException e) {
                log.warn(e.getMessage(), e);
            } catch (Exception e) {
                if (log.isDebugEnabled()) {
                    log.error(e.getMessage(), e);
                } else {
                    log.warn(e.getMessage(), e);
                }
            }
        } catch (IOException e) {
            log.warn(e.getMessage(), e);
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.error(e.getMessage(), e);
            } else {
                log.warn(e.getMessage(), e);
            }
        }
        return cpuList;
    }


    private static String getValue(String line) {
        int index = line.indexOf(":");
        if (index != -1) {
            return line.substring(index + 1).trim();
        }
        return "";
    }


}
