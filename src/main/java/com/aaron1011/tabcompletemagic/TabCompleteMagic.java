package com.aaron1011.tabcompletemagic;

import com.google.common.collect.Lists;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMapper;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.command.TabCompleteEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

@Plugin(id = "com.aaron1011.tabcompletemagic", name = "Tab Complete Magic")
public class TabCompleteMagic {

    @Inject @DefaultConfig(sharedRoot = false)
    private Path defaultConfig;

    private CommentedConfigurationNode rootNode;
    private HoconConfigurationLoader loader;

    private ObjectMapper.BoundInstance mapper;
    private Config config = new Config();

    private boolean ignoreEvent = false;


    @Listener
    public void onPre(GamePreInitializationEvent event) {
        try {
            this.mapper = ObjectMapper.forClass(Config.class).bind(config);
            this.loader = HoconConfigurationLoader.builder().setPath(this.defaultConfig).build();

            CommandSpec reload = CommandSpec.builder()
                    .permission("tabcompletemagic.command.reload")
                    .executor((src, args) -> {
                        try {
                            this.reload();
                            src.sendMessage(Text.of(TextColors.GREEN, "Successfully reloaded TabCompleteMagic config!"));

                            return CommandResult.success();

                        } catch (ObjectMappingException | IOException e) {
                            Text message = Text.of(TextColors.RED, "Failed to reload TabCompleteMagic config!");

                            src.sendMessage(message);
                            throw new CommandException(message, e);
                        }
                    }).build();

            Sponge.getCommandManager().register(this, new MyCommand(), "tab");
            Sponge.getCommandManager().register(this, reload, "tabreload");

            this.reload();
            this.loader.save(rootNode);

        } catch (ObjectMappingException | IOException e) {
            throw new RuntimeException("Failed to use ObjectMapper!", e);
        }
    }

    private void reload() throws ObjectMappingException, IOException {
        this.rootNode = this.loader.load();
        this.mapper.populate(this.rootNode);
    }


    @Listener(order = Order.POST)
    public void onTab(TabCompleteEvent.Command event, @First CommandSource src) {
        if (!ignoreEvent && this.config.invasiveComplete) {
            this.handleSuggestions(src, event.getTabCompletions(), event.getRawMessage().trim());
        }
        this.ignoreEvent = false;
    }

    private Optional<CommandResult> handleSuggestions(CommandSource src, List<String> suggestions, String raw) {

        if (suggestions.isEmpty()) {
            boolean hasExecute = raw.endsWith("EXECUTE");
            boolean hasDeny = raw.endsWith("DENY");

            if (!hasExecute && !hasDeny) {
                suggestions.add("DENY");
                suggestions.add("EXECUTE");
            } else if (hasExecute) {
                String real = raw.substring(0, raw.lastIndexOf("EXECUTE")).trim();
                return Optional.of(Sponge.getCommandManager().process(src, real));
            }
        }

        return Optional.empty();
    }

    public class MyCommand implements CommandCallable {

        private final Text help;
        private final Text usage;

        public MyCommand() {
            this.help = Text.of("Allows executing a command via <tab>");
            this.usage = Text.of("<command> EXECUTE :tab:");
        }

        @Override
        public CommandResult process(CommandSource source, String arguments) throws CommandException {
            return TabCompleteMagic.this.handleSuggestions(source, new ArrayList<>(), arguments).orElse(CommandResult.empty());
        }

        @Override
        public List<String> getSuggestions(CommandSource source, String arguments) throws CommandException {
            TabCompleteMagic.this.ignoreEvent = true;

            arguments = arguments.trim();
            if (arguments.length() == 0) {
                return Sponge.getCommandManager().getSuggestions(source, arguments);
            }

            List<String> suggestions = Lists.newArrayList(Sponge.getCommandManager().getSuggestions(source, arguments));
            TabCompleteMagic.this.handleSuggestions(source, suggestions, arguments);
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

}
