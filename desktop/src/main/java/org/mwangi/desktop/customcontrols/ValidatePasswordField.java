package org.mwangi.desktop.customcontrols;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Tooltip;
import javafx.util.Duration;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

import static atlantafx.base.theme.Styles.STATE_DANGER;

public class ValidatePasswordField extends PasswordField {
    private final BooleanProperty isValidProperty=new SimpleBooleanProperty();
    public ValidatePasswordField(Map<String, Predicate<String>> validationPredicatesWithMessages){
        textProperty().addListener((_,_,newvalue) -> {
            Optional<String> test=validationPredicatesWithMessages.entrySet()
                    .stream()
                    .map(entry-> entry.getValue().test(newvalue) ? entry.getKey():null)
                    .filter(Objects::nonNull)
                    .findFirst();
            if(test.isPresent()){
                pseudoClassStateChanged(STATE_DANGER,true);
                Tooltip tooltip = new Tooltip(test.get());
                tooltip.setShowDelay(Duration.millis(100));
                tooltipProperty().set(tooltip);
                isValidProperty.set(false);
            }else{
                pseudoClassStateChanged(STATE_DANGER,false);
                tooltipProperty().set(null);
                isValidProperty.set(true);
            }
        });
    }
    public BooleanProperty isValidPropertyProperty() {
        return isValidProperty;
    }
}