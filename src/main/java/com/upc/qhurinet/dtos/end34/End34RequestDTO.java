package com.upc.qhurinet.dtos.end34;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;

/** TODO: sustituir payload por los campos documentados de END-34. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class End34RequestDTO {
    private Map<String, Object> payload;
}
