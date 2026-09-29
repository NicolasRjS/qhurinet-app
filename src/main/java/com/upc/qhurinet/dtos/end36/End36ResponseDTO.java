package com.upc.qhurinet.dtos.end36;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;

/** TODO: sustituir payload por los campos documentados de END-36. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class End36ResponseDTO {
    private Map<String, Object> payload;
}
