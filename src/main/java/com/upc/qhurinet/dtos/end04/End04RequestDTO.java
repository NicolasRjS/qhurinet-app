package com.upc.qhurinet.dtos.end04;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;

/** TODO: sustituir payload por los campos documentados de END-04. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class End04RequestDTO {
    private Map<String, Object> payload;
}
