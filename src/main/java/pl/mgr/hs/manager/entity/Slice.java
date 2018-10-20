package pl.mgr.hs.manager.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/**
 * Created by dominik on 19.10.18.
 */
@Data
@Entity
public class Slice {
    @Id
    @GeneratedValue
    private Integer id;
    private String name;
    private Boolean working;
    private int activeHosts;
}
