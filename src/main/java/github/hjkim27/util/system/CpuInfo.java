package github.hjkim27.util.system;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CpuInfo {
    private String cpuName;
    private String cpuMhz;
    private String cpuCache;
    private int core = 1;
    private int thread = 1;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CpuInfo)) return false;

        CpuInfo cpuInfo = (CpuInfo) o;

        return cpuName.equals(cpuInfo.cpuName);
    }

    @Override
    public int hashCode() {
        return cpuName.hashCode();
    }
}
