package kr.co.wikibook.logbatch;

import org.junit.jupiter.api.Test;
import org.springframework.batch.item.function.PredicateFilteringItemProcessor;
import org.springframework.batch.item.validator.ValidatingItemProcessor;
import org.springframework.batch.item.validator.ValidationException;

import static org.assertj.core.api.Assertions.assertThat;

class FilteringTest {
    @Test
    void filterByPredicate() throws Exception {
        var processor = new PredicateFilteringItemProcessor<Integer>(num -> num < 10);
        assertThat(processor.process(9)).isNull();
        assertThat(processor.process(10)).isEqualTo(10);
    }

    @Test
    void filterByValidate() {
        var processor = new ValidatingItemProcessor<Integer>(
                num -> {
                    if (num < 10) {
                        throw new ValidationException(num + " is less than 10");
                    }
                }
        );
        processor.setFilter(true);
        assertThat(processor.process(9)).isNull();
        assertThat(processor.process(10)).isEqualTo(10);
    }
}
