package github.hjkim27.util.system;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@Getter
public class SystemInfo {
    private final String uptime;
    private final int users;
    private final String loadAverage;
    private final String localIpAddress;
    private final List<FileSystemInfo> fileSystemInfos;
    private final List<NetworkInterfaceInfo> networkInterfaceInfos;
    private final List<CpuInfo> cpuInfos;
    private final FileSystemInfo rootFileSystemInfo;

    public SystemInfo() {
        this.uptime = SystemInfoExtractor.getUptime();
        this.users = SystemInfoExtractor.getUsers();
        this.loadAverage = SystemInfoExtractor.getLoadAverage();
        this.localIpAddress = SystemInfoExtractor.getLocalIpAddress();
        this.fileSystemInfos = SystemInfoExtractor.getFileSystemInfo();
        this.networkInterfaceInfos = NetworkInterfaceInfo.getNetworkInterfaces();
        this.cpuInfos = SystemInfoExtractor.getcpuinfo();

        FileSystemInfo rootFileSystemInfo = null;

        for (FileSystemInfo fileSystemInfo : fileSystemInfos) {
            if (fileSystemInfo.getMounted().equals("/")) {
                rootFileSystemInfo = fileSystemInfo;
                break;
            }
        }
        this.rootFileSystemInfo = rootFileSystemInfo;
    }

}
