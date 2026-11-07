package com.splitwise.modules.expense.strategy;

import com.splitwise.common.exception.BadRequestException;
import com.splitwise.modules.expense.model.SplitType;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class ExpenseSplitStrategyFactory {

    private final Map<SplitType, ExpenseSplitStrategy> strategyMap = new EnumMap<>(SplitType.class);

    public ExpenseSplitStrategyFactory(List<ExpenseSplitStrategy> strategies) {
        for (ExpenseSplitStrategy strategy : strategies) {
            strategyMap.put(strategy.getSplitType(), strategy);
        }
    }

    public ExpenseSplitStrategy getStrategy(SplitType splitType) {
        ExpenseSplitStrategy strategy = strategyMap.get(splitType);
        if (strategy == null) {
            throw new BadRequestException("Unsupported split type: " + splitType);
        }
        return strategy;
    }
}

