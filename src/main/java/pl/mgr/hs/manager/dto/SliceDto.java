package pl.mgr.hs.manager.dto;

import lombok.Data;

/**
 * Created by dominik on 20.10.18.
 */
@Data
public class SliceDto {
    private Integer id;
    private String name;
    private Boolean working;
    private int activeHosts;
    private String managerHostName;
}
