package pl.mgr.hs.manager.entity;

import lombok.Data;

import javax.persistence.*;

/** Created by dominik on 19.10.18. */
@Data
@Entity
public class Slice {
  @Id @GeneratedValue private Integer id;

  private String name;

  @Column(length = 1024)
  private String description;

  private String managerHostName;

  private int externalPort;

  @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
  private Application clientApplication;

  @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
  private Application serverApplication;
}
