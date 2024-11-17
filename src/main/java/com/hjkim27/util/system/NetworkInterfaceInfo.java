package com.hjkim27.util.system;

import com.hjkim27.exception.NetworkInterfaceParseException;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.*;

@Getter
@Setter
@Slf4j
public class NetworkInterfaceInfo {


    private String name;
    private String displayName;
    private int idx;
    private Set<InterfaceAddressInfo> interfaceAddressInfos = new HashSet<>();
    private boolean virtual = false;

    @Getter
    @Setter
    public static class InterfaceAddressInfo {
        private String address;
        private String broadcast = "";
        private int maskLength;

        public InterfaceAddressInfo(InterfaceAddress interfaceAddress) throws NetworkInterfaceParseException {
            try {
                if (interfaceAddress == null) {
                    throw new IllegalArgumentException("networkInterface name is null");
                }

                final InetAddress var1 = interfaceAddress.getAddress();
                final int var2 = interfaceAddress.getNetworkPrefixLength();
                final InetAddress var3 = interfaceAddress.getBroadcast();

                this.address = var1.getHostAddress();
                this.maskLength = var2;
                if (var3 != null) {
                    this.broadcast = var3.getHostAddress();
                }
            } catch (IllegalArgumentException e) {
                throw e;
            } catch (Exception e) {
                throw new NetworkInterfaceParseException(e);
            }
        }
    }

    public NetworkInterfaceInfo(NetworkInterface networkInterface) throws NetworkInterfaceParseException {

        try {
            if (networkInterface == null) {
                throw new IllegalArgumentException("networkInterface is null");
            }

            final String name = networkInterface.getName();
            final String displayName = networkInterface.getDisplayName();

            if (name == null) {
                throw new IllegalArgumentException("networkInterface name is null");
            }
            if (displayName == null) {
                throw new IllegalArgumentException("networkInterface displayName is null");
            }

            if (networkInterface.isLoopback()) {
                throw new IllegalArgumentException("networkInterface is loopback :: " + name);
            }

            if (!networkInterface.isUp()) {
                throw new IllegalArgumentException("networkInterface is down :: " + name);
            }

            this.name = name;
            this.displayName = displayName;
            this.idx = networkInterface.getIndex();
            this.virtual = networkInterface.isVirtual();

            List<InterfaceAddress> var11 = networkInterface.getInterfaceAddresses();

            for (InterfaceAddress interfaceAddress : var11) {
                InterfaceAddressInfo var33 = new InterfaceAddressInfo(interfaceAddress);
                interfaceAddressInfos.add(var33);
            }

        } catch (IllegalArgumentException e) {
            throw e;
        } catch (SocketException e) {
            throw new NetworkInterfaceParseException(e);
        } catch (Exception e) {
            throw new NetworkInterfaceParseException(e);
        }

    }

    public static List<NetworkInterfaceInfo> getNetworkInterfaces() {
        List<NetworkInterfaceInfo> networkInterfaceInfoList = new ArrayList<>();
        try {

            boolean isLB = true;
            Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();

            while (en.hasMoreElements()) {
                NetworkInterface ni = en.nextElement();
                if (ni.isLoopback()) {
                    continue;
                }

                try {
                    NetworkInterfaceInfo networkInterfaceInfo = new NetworkInterfaceInfo(ni);
                    networkInterfaceInfoList.add(networkInterfaceInfo);
                } catch (NetworkInterfaceParseException e) {
                    log.warn("NetworkInterfaceParseException | {}", e.getMessage());
                } catch (Exception e) {
                    log.warn(e + "  " + e.getMessage());
                }

            }
        } catch (SocketException e1) {
            log.warn(e1.getMessage());
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.error(e.getMessage(), e);
            } else {
                log.warn(e.getMessage(), e);
            }
        }
        return networkInterfaceInfoList;
    }

}

