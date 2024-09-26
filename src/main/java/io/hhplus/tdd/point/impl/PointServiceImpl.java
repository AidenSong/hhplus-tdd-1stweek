package io.hhplus.tdd.point.impl;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.PointHistory;
import io.hhplus.tdd.point.PointService;
import io.hhplus.tdd.point.TransactionType;
import io.hhplus.tdd.point.UserPoint;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.locks.Lock;


@Service
public class PointServiceImpl implements PointService {
    private static final Logger log = LogManager.getLogger(PointServiceImpl.class);

    @Autowired
    private UserPointTable userPointTable;

    @Autowired
    private PointHistoryTable pointHistoryTable;

    @Autowired
    private Lock lock;


    @Override
    public UserPoint point(long id) {
        return userPointTable.selectById(id);
    }

    @Override
    public List<PointHistory> history(long id) {
        return pointHistoryTable.selectAllByUserId(id);
    }

    @Override
    public UserPoint charge(long id, long amount) throws Exception {
        UserPoint chargeResult;

        if (amount <= 0) {
            throw new Exception("충전 금액응 1원 이상이여야 합니다.");
        } else if (userPointTable.selectById(id).point() + amount < 1000) {
            throw new Exception("충전 가능 금액 초과");
        }

        lock.lock();
        try {
            chargeResult = userPointTable.insertOrUpdate(id, amount);
            pointHistoryTable.insert(id, amount, TransactionType.CHARGE, 1000);
        } finally {
            lock.unlock();
        }

        return chargeResult;
    }

    @Override
    public UserPoint use(long id, long amount) throws Exception {
        UserPoint useResult;

        if (amount <= 0) {
            throw new Exception("사용 금액은 1원 이상이여야 합니다.");
        } else if (userPointTable.selectById(id).point() - amount < 0) {
            throw new Exception("잔고가 부족합니다.");
        }

        lock.lock();
        try {
            useResult = userPointTable.insertOrUpdate(id, amount);
            pointHistoryTable.insert(id, amount, TransactionType.USE, 1000);
        } finally {
            lock.unlock();
        }

        return useResult;
    }
}
