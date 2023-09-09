package nl.rubium.efteling.stands.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Product {
    private String name;
    private float price;
    private ProductType type;

    public boolean isMeal() {
        return type.equals(ProductType.MEAL);
    }

    public boolean isDrink() {
        return type.equals(ProductType.DRINK);
    }
}
