package com.github.rvesse.airline.help;

import javax.inject.Inject;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Predicate;

import com.github.rvesse.airline.annotations.Arguments;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;
import com.github.rvesse.airline.help.cli.CliCommandGroupUsageGenerator;
import com.github.rvesse.airline.help.cli.CliCommandUsageGenerator;
import com.github.rvesse.airline.help.cli.CliGlobalUsageGenerator;
import com.github.rvesse.airline.help.cli.CliGlobalUsageSummaryGenerator;
import com.github.rvesse.airline.model.CommandGroupMetadata;
import com.github.rvesse.airline.model.CommandMetadata;
import com.github.rvesse.airline.model.GlobalMetadata;
import com.github.rvesse.airline.utils.predicates.parser.AbbreviatedCommandFinder;
import com.github.rvesse.airline.utils.predicates.parser.AbbreviatedGroupFinder;
import com.github.rvesse.airline.utils.predicates.parser.CommandFinder;
import com.github.rvesse.airline.utils.predicates.parser.GroupFinder;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

@Command(name = "help", description = "Display help information")
public class Help<T> implements Runnable, Callable<Void> {   
    @Inject
    public GlobalMetadata<T> global;

    @Arguments
    public List<String> command = new ArrayList<>();

    @Option(name = "--include-hidden", description = "When set the help output will include hidden commands and options", hidden = true)
    public boolean includeHidden = false;

    @Override
    public void run() {
        try {
            help(global, command, this.includeHidden);
        } catch (IOException e) {
            throw new RuntimeException("Error generating usage documentation", e);
        }
    }

    @Override
    public Void call() {
        run();
        return null;
    }

    /**
     * Displays plain text format help for the given command to standard out
     * 
     * @param command
     *            Command
     * @throws IOException
     */
    public static void help(CommandMetadata command) throws IOException {
        help(command, System.out);
    }

    /**
     * Displays plain text format help for the given command to standard out
     * 
     * @param command
     *            Command
     * @throws IOException
     */
    public static void help(CommandMetadata command, boolean includeHidden) throws IOException {
        help(command, includeHidden, System.out);
    }

    /**
     * Displays plain text format help or the given command to the given output
     * stream
     * 
     * @param command
     *            Command
     * @param out
     *            Output stream
     * @throws IOException
     */
    public static void help(CommandMetadata command, OutputStream out) throws IOException {
        help(command, false, out);
    }

    /**
     * Displays plain text format help or the given command to the given output
     * stream
     * 
     * @param command
     *            Command
     * @param out
     *            Output stream
     * @throws IOException
     */
    public static void help(CommandMetadata command, boolean includeHidden, OutputStream out) throws IOException {
        new CliCommandUsageGenerator(includeHidden).usage(null, null, command.getName(), command, out);
    }

    /**
     * Displays plain text format program help to standard out
     * 
     * @param global
     *            Program metadata
     * @param commandNames
     *            Command Names
     * @throws IOException
     */
    public static <T> void help(GlobalMetadata<T> global, List<String> commandNames) throws IOException {
        help(global, commandNames, false, System.out);
    }

    /**
     * Displays plain text format program help to standard out
     * 
     * @param global
     *            Program metadata
     * @param commandNames
     *            Command Names
     * @param includeHidden
     *            Whether to include hidden commands and options in the output
     * @throws IOException
     */
    public static <T> void help(GlobalMetadata<T> global, List<String> commandNames, boolean includeHidden) throws IOException {
        help(global, commandNames, includeHidden, System.out);
    }
    
    /**
     * Displays plain text format program help to the given output stream
     * 
     * @param global
     *            Program meta-data
     * @param commandNames
     *            Command Names
     * @param out
     *            Output Stream
     * @throws IOException
     */
    public static <T> void help(GlobalMetadata<T> global, List<String> commandNames, OutputStream out) throws IOException {
        help(global, commandNames, false, out);
    }

    /**
     * Displays plain text format program help to the given output stream
     * 
     * @param global
     *            Program meta-data
     * @param commandNames
     *            Command Names
     * @param out
     *            Output Stream
     * @throws IOException
     */
    public static <T> void help(GlobalMetadata<T> global, List<String> commandNames, boolean includeHidden, OutputStream out) throws IOException {
        if (commandNames.isEmpty()) {
            new CliGlobalUsageSummaryGenerator<T>(includeHidden).usage(global, out);
            return;
        }

        String name = commandNames.get(0);

        // Main program?
        if (name.equals(global.getName())) {
            // Main program help
            new CliGlobalUsageGenerator<T>(includeHidden).usage(global, out);
            return;
        }

        // Predicates we may need
        Predicate<CommandGroupMetadata> findGroupPredicate;
        Predicate<CommandMetadata> findCommandPredicate;
        //@formatter:off
        findGroupPredicate = global.getParserConfiguration().allowsAbbreviatedCommands() 
                             ? new AbbreviatedGroupFinder(name, global.getCommandGroups()) 
                             : new GroupFinder(name);
        //@formatter:on

        // A command in the default group?
        //@formatter:off
        findCommandPredicate = global.getParserConfiguration().allowsAbbreviatedCommands() 
                               ? new AbbreviatedCommandFinder(name, global.getDefaultGroupCommands())
                               : new CommandFinder(name);
        //@formatter:on
        CommandMetadata command = CollectionUtils.find(global.getDefaultGroupCommands(), findCommandPredicate);
        if (command != null) {
            // Command in default group help
            new CliCommandUsageGenerator(includeHidden).usage(global.getName(), null, command.getName(), command, out);
            return;
        }

        // A command in a group?
        CommandGroupMetadata group = CollectionUtils.find(global.getCommandGroups(), findGroupPredicate);
        if (group != null) {
            // General group help or specific group command help?
            if (commandNames.size() == 1) {
                // General group help
                new CliCommandGroupUsageGenerator<T>(includeHidden).usage(global, group, out);
                return;
            } else {
                // Group command help
                String commandName = commandNames.get(1);
                //@formatter:off
                findCommandPredicate = global.getParserConfiguration().allowsAbbreviatedCommands() 
                                       ? new AbbreviatedCommandFinder(commandName, group.getCommands())
                                       : new CommandFinder(commandName);
                //@formatter:on
                command = CollectionUtils.find(group.getCommands(), findCommandPredicate);
                if (command != null) {
                    new CliCommandUsageGenerator().usage(global.getName(), group.getName(), command.getName(), command,
                            out);

                    return;
                }

                // Didn't find an appropriate command
                if (global.getParserConfiguration().allowsAbbreviatedCommands()) {
                    System.out.println("Unknown command " + name + " " + commandName + " or an ambiguous abbreviation");
                } else {
                    System.out.println("Unknown command " + name + " " + commandName);
                }
            }
        }

        // Didn't find an appropriate group
        if (global.getParserConfiguration().allowsAbbreviatedCommands()) {
            System.out.println("Unknown command " + name + " or an ambiguous abbreviation");
        } else {
            System.out.println("Unknown command " + name);
        }
    }
}
