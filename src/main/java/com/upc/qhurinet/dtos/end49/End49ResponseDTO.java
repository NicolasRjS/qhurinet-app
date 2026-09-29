package com.upc.qhurinet.dtos.end49;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;

/** TODO: sustituir payload por los campos documentados de END-49. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class End49ResponseDTO {
    private Map<String, Object> payload;
}
