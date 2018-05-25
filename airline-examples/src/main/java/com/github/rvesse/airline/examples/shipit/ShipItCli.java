/**
 * Copyright (C) 2010-16 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.rvesse.airline.examples.shipit;

import java.util.Arrays;

import com.github.rvesse.airline.annotations.Cli;
import com.github.rvesse.airline.annotations.Parser;
import com.github.rvesse.airline.examples.ExampleRunnable;
import com.github.rvesse.airline.examples.cli.commands.Help;
import com.github.rvesse.airline.parser.ParseResult;
import com.github.rvesse.airline.parser.errors.ParseException;
import com.github.rvesse.airline.parser.errors.handlers.CollectAll;
import com.github.rvesse.airline.parser.options.ListValueOptionParser;

//@formatter:off
@Cli(name = "ship-it", 
     defaultCommand = Help.class, 
     commands = {
             CheckAddress.class,
             Send.class,
             Price.class,
             Help.class
     },
     description = "A demonstration CLI around shipping",
     parserConfiguration = @Parser(
       useDefaultOptionParsers = true,
       defaultParsersFirst = false,
       optionParsers = { ListValueOptionParser.class },
       errorHandler = CollectAll.class
     )
)
//@formatter:on
public class ShipItCli {

    public static void main(String[] args) {
        com.github.rvesse.airline.Cli<ExampleRunnable> cli = new com.github.rvesse.airline.Cli<ExampleRunnable>(ShipItCli.class);
        try {
            // Parse with a result to allow us to inspect the results of parsing
            ParseResult<ExampleRunnable> result = cli.parseWithResult(args);
            if (result.wasSuccessful()) {
                // Parsed successfully, so just run the command and exit
                System.exit(result.getCommand().run());
            } else {
                // Parsing failed
                // Display errors and then the help information
                System.err.println(String.format("%d errors encountered:", result.getErrors().size()));
                int i = 1;
                for (ParseException e : result.getErrors()) {
                    System.err.println(String.format("Error %d: %s", i, e.getMessage()));
                    i++;
                }
                
                System.err.println();
                
                com.github.rvesse.airline.help.Help.<ExampleRunnable>help(cli.getMetadata(), Arrays.asList(args), System.err);
            }
        } catch (Exception e) {
            // Errors should be being collected so if anything is thrown it is unexpected
            System.err.println(String.format("Unexpected error: %s", e.getMessage()));
            e.printStackTrace(System.err);
        }
        
        // If we got here we are exiting abnormally
        System.exit(1);
    }
}
