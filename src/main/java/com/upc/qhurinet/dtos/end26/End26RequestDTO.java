package com.upc.qhurinet.dtos.end26;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;

/** TODO: sustituir payload por los campos documentados de END-26. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class End26RequestDTO {
    private Map<String, Object> payload;
}
