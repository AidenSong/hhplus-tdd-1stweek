package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.impl.PointServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class PointServiceTest {

    @Mock
    UserPointTable upt;

    @Mock
    PointHistoryTable pht;


    @DisplayName("유저 포인트 확인")
    @Test
    void point() {

        // Given
        UserPoint user1 = new UserPoint(1L, 1000, 1000);
        UserPoint user2 = new UserPoint(2L, 2000, 1000);

        when(upt.selectById(1L)).thenReturn(user1);
        when(upt.selectById(2L)).thenReturn(user2);

        // When
        UserPoint point1 = upt.selectById(1L);
        UserPoint point2 = upt.selectById(2L);

        // Then
        assertThat(point1.point()).isEqualTo(1000);
        assertThat(point2.point()).isEqualTo(2000);
    }

    @DisplayName("유저 충전/사용 내역 확인")
    @Test
    void history() {
        // Given

        List<PointHistory> his1 = List.of(
            new PointHistory(1L, 1L, 1000, TransactionType.CHARGE, 1000),
            new PointHistory(1L, 1L, 2000, TransactionType.CHARGE, 1000)
        );
        List<PointHistory> his2 = List.of(
            new PointHistory(2L, 2L, 2000, TransactionType.CHARGE, 1000),
            new PointHistory(2L, 2L, 4000, TransactionType.CHARGE, 1000)
        );

        when(pht.selectAllByUserId(1L)).thenReturn(his1);
        when(pht.selectAllByUserId(2L)).thenReturn(his2);

        // When
        List<PointHistory> result1 = pht.selectAllByUserId(1L);
        List<PointHistory> result2 = pht.selectAllByUserId(2L);

        // Then
        assertThat(result1.get(0).amount()).isEqualTo(1000);
        assertThat(result1.get(1).amount()).isEqualTo(2000);

        assertThat(result2.get(0).amount()).isEqualTo(2000);
        assertThat(result2.get(1).amount()).isEqualTo(4000);
    }

    @DisplayName("포인트 충전")
    @Test
    void charge(long id, long amount) throws Exception {


        // Given
        amount = 2000L;
        UserPoint user1 = new UserPoint(1L, 5000, 1000);
        UserPoint user2 = new UserPoint(2L, 8000, 1000);



        when(upt.selectById(1L)).thenReturn(user1);
        when(upt.selectById(2L)).thenReturn(user2);

        // When
        if (amount <= 0) {
            throw new Exception("충전 금액응 1원 이상이여야 합니다.");
        }
        if (upt.selectById(id).point() + amount < 10000) {
            throw new Exception("충전 가능 금액 초과");

        }

        UserPoint userPoint1 = upt.selectById(1L);
        UserPoint userPoint2 = upt.selectById(2L);

        UserPoint point1 = new UserPoint(1L, userPoint1.point() + amount, 100);
        UserPoint point2 = new UserPoint(2L, userPoint2.point() + amount, 100);


        // Then
        assertThat(point1.point()).isEqualTo(7000);
        assertThat(point1.point()).isEqualTo(10000);

    }
    @DisplayName("포인트 사용")
    @Test
    void use(long id, long amount) throws Exception {
        // Given
        amount = 2000;
        UserPoint user1 = new UserPoint(1L, 5000, 1000);
        UserPoint user2 = new UserPoint(2L, 9000, 1000);

        when(upt.selectById(1L)).thenReturn(user1);
        when(upt.selectById(2L)).thenReturn(user2);

        // When
        if (amount <= 0) {
            throw new Exception("사용 금액은 1원 이상이여야 합니다.");
        } else if (upt.selectById(id).point() - amount < 0L) {
            throw new Exception("잔고가 부족합니다.");
        }

        UserPoint userPoint1 = upt.selectById(1L);
        UserPoint userPoint2 = upt.selectById(2L);

        UserPoint point1 = new UserPoint(1L, userPoint1.point() - amount, 100);
        UserPoint point2 = new UserPoint(2L, userPoint2.point() - amount, 100);

        // Then
        assertThat(point1.point()).isEqualTo(3000);
        assertThat(point1.point()).isEqualTo(7000);
    }
}