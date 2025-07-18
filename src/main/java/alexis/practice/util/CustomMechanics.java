package alexis.practice.util;

import cn.nukkit.Player;
import com.denzelcode.form.FormAPI;
import com.denzelcode.form.element.Input;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class CustomMechanics {
    private String name;
    private float drag;
    private float gravity;

    public void sendForm(Player player) {
        FormAPI.customWindowForm("mechanics.values", "Mechanics " + name + " Configuration")
                .addInput("mechanics.gravity", "Gravity", "", "" + gravity)
                .addInput("mechanics.drag", "Drag", "", "" + drag)
                .addHandler(handler -> {
                    var form = handler.getForm();

                    if (!form.isValid("mechanics.values")) return;

                    Input gravityValue = form.getElement("mechanics.gravity");
                    Input dragValue = form.getElement("mechanics.drag");

                    if (!gravityValue.getValue().isEmpty()) {
                        try {
                            gravity = Float.parseFloat(gravityValue.getValue());
                        } catch (NumberFormatException ignored) {}
                    }

                    if (!dragValue.getValue().isEmpty()) {
                        try {
                            drag = Float.parseFloat(dragValue.getValue());
                        } catch (NumberFormatException ignored) {}
                    }
                })
                .sendTo(player);
    }
}
