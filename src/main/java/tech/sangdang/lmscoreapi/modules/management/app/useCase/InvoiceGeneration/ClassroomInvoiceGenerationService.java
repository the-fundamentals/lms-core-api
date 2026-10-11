package tech.sangdang.lmscoreapi.modules.management.app.useCase.InvoiceGeneration;

import java.time.Month;

public interface ClassroomInvoiceGenerationService {

    void generateMonthlyInvoices(Month month);

}
