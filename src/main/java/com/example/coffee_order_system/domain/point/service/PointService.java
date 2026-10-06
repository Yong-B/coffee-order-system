package com.example.coffee_order_system.domain.point.service;

import com.example.coffee_order_system.domain.member.repository.MemberRepository;
import com.example.coffee_order_system.domain.point.dto.PointChargeResponse;
import com.example.coffee_order_system.domain.point.entity.PointAccount;
import com.example.coffee_order_system.domain.point.entity.PointHistory;
import com.example.coffee_order_system.domain.point.repository.PointAccountRepository;
import com.example.coffee_order_system.domain.point.repository.PointHistoryRepository;
import com.example.coffee_order_system.global.error.BusinessException;
import com.example.coffee_order_system.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PointService {

    private final MemberRepository memberRepository;
    private final PointAccountRepository pointAccountRepository;
    private final PointHistoryRepository pointHistoryRepository;

    @Transactional
    public PointChargeResponse charge(Long memberId, Long amount) {
        if (amount == null || amount <= 0) {
            throw new BusinessException(ErrorCode.INVALID_CHARGE_AMOUNT);
        }

        if (!memberRepository.existsById(memberId)) {
            throw new BusinessException(ErrorCode.MEMBER_NOT_FOUND);
        }

        PointAccount account =
                pointAccountRepository.findByMemberIdForUpdate(memberId)
                        .orElseThrow(() ->
                                new BusinessException(
                                        ErrorCode.POINT_ACCOUNT_NOT_FOUND
                                )
                        );

        account.charge(amount);

        pointHistoryRepository.save(
                PointHistory.charge(account, amount)
        );

        return new PointChargeResponse(
                memberId,
                amount,
                account.getBalance()
        );
    }
}