package com.opticstore.Analytics.dto;

import com.opticstore.Analytics.model.TimePeriod;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsRequestDTO {
    private TimePeriod period = TimePeriod.THIS_MONTH;
    private LocalDate startDate;
    private LocalDate endDate;
    private String category;
    private String brand;
    private boolean includeComparisons = false;
    private String groupBy = "day";

    //  getters and setters
    public TimePeriod getPeriod() { return period; }
    public void setPeriod(TimePeriod period) { this.period = period; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public boolean isIncludeComparisons() { return includeComparisons; }
    public void setIncludeComparisons(boolean includeComparisons) { this.includeComparisons = includeComparisons; }

    public String getGroupBy() { return groupBy; }
    public void setGroupBy(String groupBy) { this.groupBy = groupBy; }
}