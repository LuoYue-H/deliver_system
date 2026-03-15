package org.example.deliver_system.service;

import org.example.deliver_system.entity.ReviewHistory;
import org.example.deliver_system.mapper.ReviewHistoryMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ReviewService {

    @Autowired
    private ReviewHistoryMapper reviewHistoryMapper;

    public void addHistory(Long merchantId, Long operatorId, String action, String comment) {
        ReviewHistory history = new ReviewHistory();
        history.setMerchantId(merchantId);
        history.setOperatorId(operatorId);
        history.setAction(action);
        history.setComment(comment);
        reviewHistoryMapper.insert(history);
    }
}
