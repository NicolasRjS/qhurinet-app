package com.upc.qhurinet.dtos.end46;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;

/** TODO: sustituir payload por los campos documentados de END-46. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class End46ResponseDTO {
    private Map<String, Object> payload;
}
