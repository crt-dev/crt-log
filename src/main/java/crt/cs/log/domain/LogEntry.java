package domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LogEntry {
    private String id;
    private String state;
    private String type;
    private String host;
    private long timestamp;
}