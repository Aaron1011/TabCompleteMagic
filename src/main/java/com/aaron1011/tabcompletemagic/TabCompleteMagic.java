package com.aaron1011.tabcompletemagic;

import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMapper;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.bstats.sponge.Metrics;
import org.spongepowered.api.Sponge;
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
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

@Plugin(id = "tabcompletemagic", name = "Tab Complete Magic", version = "2.0.0",
        description = "A plugin to add extra magic to tab completion", url = "https://github.com/Aaron1011/TabCompleteMagic", authors = "Aaron1011")
public class TabCompleteMagic {

    @Inject @DefaultConfig(sharedRoot = false)
    private Path defaultConfig;

    @Inject
    private Metrics metrics;

    private CommentedConfigurationNode rootNode;
    private HoconConfigurationLoader loader;

    private ObjectMapper.BoundInstance mapper;
    private Config config = new Config();

    protected boolean ignoreEvent = false;


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

            Sponge.getCommandManager().register(this, new TabCommand(this), "tab");
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
            this.handleSuggestions(src, event.getTabCompletions(), event.getRawMessage());
        }
    }

    protected Optional<CommandResult> handleSuggestions(CommandSource src, List<String> suggestions, String raw) {

        boolean hasExecute = raw.trim().endsWith(config.execute);
        boolean hasDeny = raw.trim().endsWith(config.deny);

        if (hasExecute) {
            suggestions.clear();
            String real = raw.substring(0, raw.lastIndexOf(config.execute));
            return Optional.of(Sponge.getCommandManager().process(src, real.trim())); /// Actual commands always have whitespace stripped *client-side*
        } else if (!hasExecute && !hasDeny && suggestions.isEmpty()) {
            char last = raw.charAt(raw.length() - 1);
            if (Character.isWhitespace(last)) { // There's nothing we can do otherwise - the client is dumb
                suggestions.add(config.deny);
                suggestions.add(config.execute);
            }
        }
        return Optional.empty();
    }

}
