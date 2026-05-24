package com.example.testapi.domain.clothesorder.dto.order.requestDto;

import com.example.testapi.domain.clothesorder.Enum.ItemType;
import com.example.testapi.domain.clothesorder.entity.Orders;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter

@NoArgsConstructor
@AllArgsConstructor
public class OrderRequestDto {

    @NotNull(message = "구매자 정보가 있어야합니다.")
    private ContactInfo contactInfo;

    @NotNull(message = "주문 타입 및 상품이 있어야합니다")
    private Items items;

    @Getter
    @NoArgsConstructor
    public static class ContactInfo{
        @NotBlank(message = "구매자 이메일이 없습니다.")
        private String contactEmail;
        @NotBlank(message = "구매자 이름이 없습니다")
        private String contactName;
        @NotBlank(message = "구매자 Mobile번호가 없습니다")
        private String mobile;

        public String getContactEmail() {
            return contactEmail == null ? null : contactEmail.trim();
        }

        public String getContactName() {
            return contactName == null ? null : contactName.trim();
        }

        public String getMobile() {
            return mobile == null ? null : mobile.trim();
        }
    }

    @Getter
    @NoArgsConstructor
    public static class Items{
        @NotBlank(message = "주문 타입이 없습니다.")
        private String itemType;

        @NotNull(message = "주문 상품이 없습니다.")
        private Long itemId;

        public String getItemType() {
            return itemType == null ? null : itemType.trim().toLowerCase();
        }
        public ItemType getItemTypeEnum() {
            return itemType == null ? null : ItemType.valueOf(itemType.trim().toUpperCase());
        }
    }

    public Orders ordertoEntity(){
        return Orders.builder()
                .contactEmail(this.getContactInfo().getContactEmail())
                .contactName(this.getContactInfo().getContactName())
                .mobile(this.getContactInfo().getMobile())
                .itemType(this.getItems().getItemType())
                .itemId(this.getItems().getItemId())
                .build();
    }
}
