package io.airlift.airline.help;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;

import io.airlift.airline.model.CommandMetadata;
import io.airlift.airline.model.OptionMetadata;

import javax.annotation.Nullable;

/**
 * Abstract command usage generator
 * 
 */
public abstract class AbstractCommandUsageGenerator extends AbstractUsageGenerator implements CommandUsageGenerator {

    private final Comparator<? super Entry<Integer, String>> exitCodeComparator;

    public AbstractCommandUsageGenerator() {
        this(UsageHelper.DEFAULT_OPTION_COMPARATOR);
    }

    public AbstractCommandUsageGenerator(Comparator<? super OptionMetadata> optionComparator) {
        this(optionComparator, UsageHelper.DEFAULT_EXIT_CODE_COMPARATOR);
    }

    public AbstractCommandUsageGenerator(Comparator<? super OptionMetadata> optionComparator,
            Comparator<? super Entry<Integer, String>> exitCodeComparator) {
        super(optionComparator, UsageHelper.DEFAULT_COMMAND_COMPARATOR);
        this.exitCodeComparator = exitCodeComparator;
    }

    @Override
    public void usage(@Nullable String programName, @Nullable String groupName, String commandName,
            CommandMetadata command) throws IOException {
        usage(programName, groupName, commandName, command, System.out);
    }

    /**
     * Sorts the exit codes assuming a non-null comparator was provided at
     * instantiation time
     * 
     * @param exitCodes
     *            Exit codes
     * @return Sorted exit codes
     */
    protected List<Entry<Integer, String>> sortExitCodes(List<Entry<Integer, String>> exitCodes) {
        if (exitCodeComparator != null) {
            exitCodes = new ArrayList<>(exitCodes);
            Collections.sort(exitCodes, exitCodeComparator);
        }
        return exitCodes;
    }
}