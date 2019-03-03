package io.triada.commands.invoice;

import com.google.common.base.Predicates;
import lombok.AllArgsConstructor;

import java.util.List;

@AllArgsConstructor
final class InvoiceParams {

    private final List<String> params;

    public String receiverId() {
        return this.params.stream()
                .filter(p -> p.contains("receiver"))
                .map(p -> p.substring(p.indexOf("=") + 1))
                .findFirst()
                .filter(Predicates.not(String::isEmpty))
                .orElseThrow(() -> new IllegalStateException("Receiver wallet ID is required"));
    }
}
