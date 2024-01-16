package org.howard1209a.monitor;

public interface FrequencyMonitor {
    Integer getFrequencyPerSecond(String key);

    void record(String key);
}
