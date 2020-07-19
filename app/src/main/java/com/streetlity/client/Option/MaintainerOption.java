package com.streetlity.client.Option;

public class MaintainerOption implements OptionInterface {
    private boolean acceptEmergency;

    public MaintainerOption(boolean acceptEmergency) {
        this.acceptEmergency = acceptEmergency;
    }

    public MaintainerOption() {
    }


    public void setAcceptEmergency(boolean acceptEmergency) {
        this.acceptEmergency = acceptEmergency;
    }

    public boolean isAcceptEmergency() {
        return acceptEmergency;
    }
}
