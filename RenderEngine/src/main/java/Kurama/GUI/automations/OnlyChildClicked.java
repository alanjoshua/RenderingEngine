package Kurama.GUI.automations;

import Kurama.GUI.Component;
import Kurama.inputs.Input;

public class OnlyChildClicked implements Automation {

    @Override
    public void run(Component current, Input input) {
        if(current.isClicked)  {
            current.parent.isClicked = false;
        }
    }

}
