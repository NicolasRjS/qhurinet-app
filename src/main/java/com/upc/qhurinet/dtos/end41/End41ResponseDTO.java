package com.upc.qhurinet.dtos.end41;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;

/** TODO: sustituir payload por los campos documentados de END-41. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class End41ResponseDTO {
    private Map<String, Object> payload;
}
