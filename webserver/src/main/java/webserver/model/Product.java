package webserver.model;

import org.javamoney.moneta.FastMoney;
import org.springframework.data.annotation.Id;

public class Product {

    @Id
    private int id;
    private String name;
    private Brand brand;
    private Category category;
    private String description;
    private FastMoney listPrice;
    private String currency;
    private Vendor vendor;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public FastMoney getListPrice() {
        return listPrice;
    }

    public void setListPrice(FastMoney listPrice) {
        this.listPrice = listPrice;
    }

    public Vendor getVendor() {
        return vendor;
    }

    public void setVendor(Vendor vendor) {
        this.vendor = vendor;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Brand getBrand() {
        return brand;
    }

    public void setBrand(Brand brand) {
        this.brand = brand;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }
}
