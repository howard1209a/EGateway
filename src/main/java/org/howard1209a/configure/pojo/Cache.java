package org.howard1209a.configure.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Cache {
    private String path;
    private String level;
    private boolean metadata;
    private Integer expireTime;
    private boolean statusHeader;
    private Integer minuse;
}
