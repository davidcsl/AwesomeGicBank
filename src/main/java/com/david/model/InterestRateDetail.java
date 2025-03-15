package com.david.model;

public class InterestRateDetail {
  private String date;
  private String ruleId;
  private String rate;

  public String getDate() {
    return date;
  }

  public void setDate(String date) {
    this.date = date;
  }

  public String getRuleId() {
    return ruleId;
  }

  public void setRuleId(String ruleId) {
    this.ruleId = ruleId;
  }

  public String getRate() {
    return rate;
  }

  public void setRate(String rate) {
    this.rate = rate;
  }

  @Override
  public String toString() {
    return "InterestRateDetail{" +
            "date='" + date + '\'' +
            ", ruleId='" + ruleId + '\'' +
            ", rate='" + rate + '\'' +
            '}';
  }
}
