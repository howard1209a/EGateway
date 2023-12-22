package org.howard1209a.configure.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Address {
    private String name;
    private String ip;
    private String port;
    private Integer weight = 1;
}
