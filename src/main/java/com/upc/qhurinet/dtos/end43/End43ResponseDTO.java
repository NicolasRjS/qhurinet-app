package com.upc.qhurinet.dtos.end43;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;

/** TODO: sustituir payload por los campos documentados de END-43. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class End43ResponseDTO {
    private Map<String, Object> payload;
}
