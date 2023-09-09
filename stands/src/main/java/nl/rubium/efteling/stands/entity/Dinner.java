package nl.rubium.efteling.stands.entity;

import java.util.Set;
import lombok.Getter;
import org.openapitools.client.model.DinnerDto;

@Getter
public class Dinner {
    private final Set<Product> meals;
    private final Set<Product> drinks;

    public Dinner(Set<Product> meals, Set<Product> drinks) {
        if (containsOtherType(meals, ProductType.MEAL)
                || containsOtherType(drinks, ProductType.DRINK)) {
            throw new IllegalArgumentException("Meals or drinks contains wrong types");
        }

        this.meals = meals;
        this.drinks = drinks;
    }

    private boolean containsOtherType(Set<Product> products, ProductType expectedType) {
        return products.stream().anyMatch(product -> !product.getType().equals(expectedType));
    }

    public boolean isValid() {
        return !meals.isEmpty() || !drinks.isEmpty();
    }

    public DinnerDto toDto() {
        return DinnerDto.builder()
                .drinks(drinks.stream().map(Product::getName).toList())
                .meals(meals.stream().map(Product::getName).toList())
                .build();
    }
}
