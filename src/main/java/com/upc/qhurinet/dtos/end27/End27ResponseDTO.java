package com.upc.qhurinet.dtos.end27;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;

/** TODO: sustituir payload por los campos documentados de END-27. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class End27ResponseDTO {
    private Map<String, Object> payload;
}
