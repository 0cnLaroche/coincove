package webserver.service;

import webserver.model.Sale;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import webserver.repository.SaleRepository;

@Service
public class SaleService {

    private SaleRepository saleRepository;

    private MailService mailService;

    @Autowired
    public SaleService(MailService mailService, SaleRepository saleRepository) {
        this.mailService = mailService;
        this.saleRepository = saleRepository;
    }

    public void process(Sale sale) {
        String id = saleRepository.save(sale).getId();
        sale.setId(id);
        mailService.sendMessage("sales", sale.getClient().getEmail(), "Sale confirmation" + sale.getId(), "Test" );
        // TODO: send email to vendor
        // TODO: Decrement stocks
    }

    public Sale findSaleById(String id) {
        return saleRepository.findById(id).orElse(null);
    }
}
