package kr.co.wikibook.logbatch;

import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.function.SupplierItemReader;

import java.util.Iterator;
import java.util.List;

class FunctionInterfaceComponents {
    ItemReader<String> stringReader1(String ... str) {
        Iterator<String> itr = List.of(str).iterator();
        return () -> {
            if (itr.hasNext())
                return itr.next();
            else
                return null;
        };
    }

    ItemReader<String> stringReader2(String ... str) {
        Iterator<String> itr = List.of(str).iterator();
        return new SupplierItemReader<>(() -> {
            if (itr.hasNext())
                return itr.next();
            else
                return null;
        });
    }
}
