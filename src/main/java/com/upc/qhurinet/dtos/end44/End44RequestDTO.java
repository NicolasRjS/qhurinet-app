package com.upc.qhurinet.dtos.end44;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;

/** TODO: sustituir payload por los campos documentados de END-44. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class End44RequestDTO {
    private Map<String, Object> payload;
}
