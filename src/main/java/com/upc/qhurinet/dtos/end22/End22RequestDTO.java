package com.upc.qhurinet.dtos.end22;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;

/** TODO: sustituir payload por los campos documentados de END-22. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class End22RequestDTO {
    private Map<String, Object> payload;
}
