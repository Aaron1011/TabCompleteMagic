package com.aaron1011.tabcompletemagic;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public class Config {

    @Setting(value = "invasive-complete", comment = "Enable invasible tab complete execution. This allows tab-execution for all commands, not just /tab")
    protected boolean invasiveComplete = true;

    @Setting(value = "execute", comment = "The 'EXECUTE' pseudo-suggestion, which must be present at the end of a command for tab-execution to run")
    protected String execute = "EXECUTE";

    @Setting(value = "deny", comment = "The 'DENY' psuedo-suggestion, which is selected by default when the original command has no more tab completions."
            + "The user must explicitly select 'EXECUTE' instead of this, to prevent accidental execution of a command.")
    protected String deny = "DENY";

}
