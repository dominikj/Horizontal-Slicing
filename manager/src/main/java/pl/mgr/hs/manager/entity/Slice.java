package pl.mgr.hs.manager.entity;

import lombok.Data;

import javax.persistence.*;

/** Created by dominik on 19.10.18. */
@Data
@Entity
public class Slice {
  @Id @GeneratedValue private Integer id;
  private String name;
  private String managerHostName;

  @OneToOne(cascade = CascadeType.ALL)
  private Application clientApplication;

  @OneToOne(cascade = CascadeType.ALL)
  private Application serverApplication;
}
