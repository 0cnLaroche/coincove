package webserver.model;

import org.javamoney.moneta.FastMoney;
import org.springframework.data.mongodb.core.mapping.DBRef;

public class Item {

    @DBRef
    private Product product;
    private int quantity;
    private FastMoney salePrice;
    private FastMoney discount;

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public FastMoney getSalePrice() {
        return salePrice;
    }

    public void setSalePrice(FastMoney salePrice) {
        this.salePrice = salePrice;
    }

    public FastMoney getDiscount() {
        return discount;
    }

    public void setDiscount(FastMoney discount) {
        this.discount = discount;
    }
}
