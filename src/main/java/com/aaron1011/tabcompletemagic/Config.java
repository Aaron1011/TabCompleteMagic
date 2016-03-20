package com.aaron1011.tabcompletemagic;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public class Config {

    @Setting(value = "invasive-complete", comment = "Enable invasible tab complete execution. This allows tab-execution for all commands, not just /tab")
    protected boolean invasiveComplete = true;

}
