package com.upc.qhurinet.dtos.end32;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;

/** TODO: sustituir payload por los campos documentados de END-32. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class End32RequestDTO {
    private Map<String, Object> payload;
}
