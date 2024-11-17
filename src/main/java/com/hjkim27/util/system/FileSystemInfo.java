package com.hjkim27.util.system;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.NoSuchElementException;
import java.util.StringTokenizer;

@Slf4j
@Getter
@Setter
public class FileSystemInfo {
    private String fileSystem;

    private String size;

    private String used;

    private String percent;

    private String avail;

    private String mounted;

    public FileSystemInfo(String line) {
        try {
            StringTokenizer token = new StringTokenizer(line);
            fileSystem = token.nextToken();
            size = token.nextToken();
            used = token.nextToken();
            avail = token.nextToken();
            percent = token.nextToken();
            mounted = token.nextToken();
        } catch (NoSuchElementException e) {
            log.warn("NoSuchElementException | {}", e.getMessage());
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.error(e.getMessage(), e);
            } else {
                log.warn(e.getMessage(), e);
            }
        }
    }
}
