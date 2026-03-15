package org.example.deliver_system.dto;

import org.example.deliver_system.entity.MerchantDetails;
import org.example.deliver_system.entity.User;

public class UserProfileDTO extends User {
    private MerchantDetails merchantDetails;

    public MerchantDetails getMerchantDetails() {
        return merchantDetails;
    }

    public void setMerchantDetails(MerchantDetails merchantDetails) {
        this.merchantDetails = merchantDetails;
    }
}
