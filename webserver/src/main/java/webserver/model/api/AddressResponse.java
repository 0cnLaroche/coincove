package webserver.model.api;

import java.time.LocalDateTime;

public class AddressResponse {
    private String address;
    private LocalDateTime expiry;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public LocalDateTime getExpiry() {
        return expiry;
    }

    public void setExpiry(LocalDateTime expiry) {
        this.expiry = expiry;
    }
}
