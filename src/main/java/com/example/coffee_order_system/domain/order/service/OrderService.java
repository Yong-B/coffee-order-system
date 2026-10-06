package com.example.coffee_order_system.domain.order.service;

import com.example.coffee_order_system.domain.member.entity.Member;
import com.example.coffee_order_system.domain.member.repository.MemberRepository;
import com.example.coffee_order_system.domain.menu.entity.Menu;
import com.example.coffee_order_system.domain.menu.repository.MenuRepository;
import com.example.coffee_order_system.domain.order.dto.OrderResponse;
import com.example.coffee_order_system.domain.order.entity.Order;
import com.example.coffee_order_system.domain.order.repository.OrderRepository;
import com.example.coffee_order_system.domain.payment.entity.Payment;
import com.example.coffee_order_system.domain.payment.event.PaymentCompletedEvent;
import com.example.coffee_order_system.domain.payment.repository.PaymentRepository;
import com.example.coffee_order_system.domain.point.entity.PointAccount;
import com.example.coffee_order_system.domain.point.entity.PointHistory;
import com.example.coffee_order_system.domain.point.repository.PointAccountRepository;
import com.example.coffee_order_system.domain.point.repository.PointHistoryRepository;
import com.example.coffee_order_system.global.error.BusinessException;
import com.example.coffee_order_system.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneOffset;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final MemberRepository memberRepository;
    private final MenuRepository menuRepository;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final PointAccountRepository pointAccountRepository;
    private final PointHistoryRepository pointHistoryRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public OrderResponse order(Long memberId, Long menuId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() ->
                        new BusinessException(ErrorCode.MEMBER_NOT_FOUND)
                );

        PointAccount account =
                pointAccountRepository.findByMemberIdForUpdate(memberId)
                        .orElseThrow(() ->
                                new BusinessException(
                                        ErrorCode.POINT_ACCOUNT_NOT_FOUND
                                )
                        );

        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() ->
                        new BusinessException(ErrorCode.MENU_NOT_FOUND)
                );

        Order order = orderRepository.save(
                Order.create(member, menu, menu.getPrice())
        );

        Payment payment = Payment.create(order);

        // 잔액 부족 시 여기서 예외가 발생하고 주문 저장도 롤백됩니다.
        account.deduct(payment.getAmount());

        payment.complete();
        order.markPaid(payment.getPaidAt());

        Payment savedPayment = paymentRepository.save(payment);

        pointHistoryRepository.save(
                PointHistory.payment(account, order)
        );

        Instant paidAt =
                savedPayment.getPaidAt().toInstant(ZoneOffset.UTC);

        // 여기서는 Spring 이벤트만 발행합니다.
        // Kafka 및 외부 HTTP는 AFTER_COMMIT 리스너에서 실행합니다.
        eventPublisher.publishEvent(
                new PaymentCompletedEvent(
                        savedPayment.getId(),
                        order.getId(),
                        member.getId(),
                        menu.getId(),
                        savedPayment.getAmount(),
                        paidAt
                )
        );

        return new OrderResponse(
                order.getId(),
                savedPayment.getId(),
                member.getId(),
                menu.getId(),
                savedPayment.getAmount(),
                account.getBalance(),
                order.getStatus(),
                savedPayment.getStatus(),
                paidAt
        );
    }
}