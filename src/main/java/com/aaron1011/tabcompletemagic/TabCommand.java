package com.aaron1011.tabcompletemagic;

import com.google.common.collect.Lists;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TabCommand implements CommandCallable {

    private TabCompleteMagic tabCompleteMagic;
    private final Text help;
    private final Text usage;

    public TabCommand(TabCompleteMagic tabCompleteMagic) {
        this.tabCompleteMagic = tabCompleteMagic;
        this.help = Text.of("Allows executing a command via <tab>");
        this.usage = Text.of("<command> EXECUTE :tab:");
    }

    @Override
    public CommandResult process(CommandSource source, String arguments) throws CommandException {
        return tabCompleteMagic.handleSuggestions(source, new ArrayList<>(), arguments).orElse(CommandResult.empty());
    }

    @Override
    public List<String> getSuggestions(CommandSource source, String arguments) throws CommandException {
        tabCompleteMagic.ignoreEvent = true;
        List<String> suggestions = Lists.newArrayList(Sponge.getCommandManager().getSuggestions(source, arguments));
        tabCompleteMagic.ignoreEvent = false;

        tabCompleteMagic.handleSuggestions(source, suggestions, arguments);
        return suggestions;
    }

    @Override
    public boolean testPermission(CommandSource source) {
        return false;
    }

    @Override
    public Optional<? extends Text> getShortDescription(CommandSource source) {
        return Optional.of(this.help);
    }

    @Override
    public Optional<? extends Text> getHelp(CommandSource source) {
        return Optional.of(this.help);
    }

    @Override
    public Text getUsage(CommandSource source) {
        return this.usage;
    }
}
