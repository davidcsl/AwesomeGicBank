package com.david.model;

public class InterestRateDto {

  private Double currentInterestRate;
  private Double annualizedInterest;
  private Integer dateFrom;

  public Double getCurrentInterestRate() {
    return currentInterestRate;
  }

  public void setCurrentInterestRate(Double currentInterestRate) {
    this.currentInterestRate = currentInterestRate;
  }

  public Double getAnnualizedInterest() {
    return annualizedInterest;
  }

  public void setAnnualizedInterest(Double annualizedInterest) {
    this.annualizedInterest = annualizedInterest;
  }

  public Integer getDateFrom() {
    return dateFrom;
  }

  public void setDateFrom(Integer dateFrom) {
    this.dateFrom = dateFrom;
  }


}
