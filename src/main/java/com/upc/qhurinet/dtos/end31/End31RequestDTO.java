package com.upc.qhurinet.dtos.end31;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;

/** TODO: sustituir payload por los campos documentados de END-31. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class End31RequestDTO {
    private Map<String, Object> payload;
}
