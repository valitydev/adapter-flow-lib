package dev.vality.adapter.flow.lib.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.Instant;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PollingInfo {

    @JsonProperty(value = "start_date_time_polling")
    private Instant startDateTimePolling;

    @JsonProperty(value = "max_date_time_polling")
    private Instant maxDateTimePolling;

}