package pl.mgr.hs.manager.form;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/** Created by dominik on 27.10.18. */
@Data
public class NewSliceForm {
  @NotBlank(message = "Name cannot be blank")
  String name;

  @NotBlank(message = "Client application image id cannot be blank")
  String clientAppImageId;

  @NotNull(message = "Port cannot be empty")
  @Min(value = 1, message = "Port cannot be smaller than 1")
  Integer clientAppPublishedPort;

  @NotBlank(message = "Server application image id cannot be blank")
  String serverAppImageId;

  @NotNull(message = "Port cannot be empty")
  @Min(value = 1, message = "Port cannot be smaller than 1")
  Integer serverAppPublishedPort;
}
