package org.example.deliver_system.entity;

public class MerchantDetails {
    private Long userId;
    private Integer status;
    private String licenseUrl;
    private String idCardFrontUrl;
    private String idCardBackUrl;
    private String rejectReason;
    private String category;
    private java.time.LocalDateTime submitTime;

    // Getters and Setters
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public String getLicenseUrl() { return licenseUrl; }
    public void setLicenseUrl(String licenseUrl) { this.licenseUrl = licenseUrl; }
    public String getIdCardFrontUrl() { return idCardFrontUrl; }
    public void setIdCardFrontUrl(String idCardFrontUrl) { this.idCardFrontUrl = idCardFrontUrl; }
    public String getIdCardBackUrl() { return idCardBackUrl; }
    public void setIdCardBackUrl(String idCardBackUrl) { this.idCardBackUrl = idCardBackUrl; }
    public String getRejectReason() { return rejectReason; }
    public void setRejectReason(String rejectReason) { this.rejectReason = rejectReason; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public java.time.LocalDateTime getSubmitTime() { return submitTime; }
    public void setSubmitTime(java.time.LocalDateTime submitTime) { this.submitTime = submitTime; }
}
