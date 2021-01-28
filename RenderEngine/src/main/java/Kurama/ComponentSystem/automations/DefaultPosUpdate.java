package Kurama.ComponentSystem.automations;

import Kurama.ComponentSystem.components.Component;
import Kurama.ComponentSystem.components.model.SceneComponent;
import Kurama.inputs.Input;

public class DefaultPosUpdate implements Automation {
    @Override
    public void run(Component current, Input input, float timeDelta) {
        SceneComponent m = (SceneComponent) current;
        m.velocity = m.velocity.add(m.acceleration.scalarMul(timeDelta));
        var detlaV = m.velocity.scalarMul(timeDelta);
        m.pos = m.pos.add(detlaV);
    }
}
