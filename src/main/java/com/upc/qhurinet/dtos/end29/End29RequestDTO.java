package com.upc.qhurinet.dtos.end29;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;

/** TODO: sustituir payload por los campos documentados de END-29. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class End29RequestDTO {
    private Map<String, Object> payload;
}
