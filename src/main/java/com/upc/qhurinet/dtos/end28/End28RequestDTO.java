package com.upc.qhurinet.dtos.end28;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;

/** TODO: sustituir payload por los campos documentados de END-28. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class End28RequestDTO {
    private Map<String, Object> payload;
}
