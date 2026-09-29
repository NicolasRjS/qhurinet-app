package com.upc.qhurinet.dtos.end45;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;

/** TODO: sustituir payload por los campos documentados de END-45. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class End45RequestDTO {
    private Map<String, Object> payload;
}
