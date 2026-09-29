package com.upc.qhurinet.dtos.end03;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;

/** TODO: sustituir payload por los campos documentados de END-03. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class End03RequestDTO {
    private Map<String, Object> payload;
}
