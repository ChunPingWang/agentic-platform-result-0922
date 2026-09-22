package com.insurance.lifepremium.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class RateTableVersionRequest {
    @NotBlank
    private String version;
    @NotNull
    private List<RateEntryDto> entries;

    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }
    public List<RateEntryDto> getEntries() { return entries; }
    public void setEntries(List<RateEntryDto> entries) { this.entries = entries; }
}