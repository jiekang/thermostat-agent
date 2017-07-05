/*
 * Copyright 2012-2017 Red Hat, Inc.
 *
 * This file is part of Thermostat.
 *
 * Thermostat is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation; either version 2, or (at your
 * option) any later version.
 *
 * Thermostat is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Thermostat; see the file COPYING.  If not see
 * <http://www.gnu.org/licenses/>.
 *
 * Linking this code with other modules is making a combined work
 * based on this code.  Thus, the terms and conditions of the GNU
 * General Public License cover the whole combination.
 *
 * As a special exception, the copyright holders of this code give
 * you permission to link this code with independent modules to
 * produce an executable, regardless of the license terms of these
 * independent modules, and to copy and distribute the resulting
 * executable under terms of your choice, provided that you also
 * meet, for each linked independent module, the terms and conditions
 * of the license of that module.  An independent module is a module
 * which is not derived from or based on this code.  If you modify
 * this code, you may extend this exception to your version of the
 * library, but you are not obligated to do so.  If you do not wish
 * to do so, delete this exception statement from your version.
 */

package com.redhat.thermostat.vm.gc.agent.params;

import com.redhat.thermostat.shared.locale.Translate;
import com.redhat.thermostat.vm.gc.agent.internal.LocaleResources;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Formats XML validation issues to strings.
 *
 * @see GcParamsMappingValidatorException
 */
public class ValidationErrorsFormatter {

    private enum ErrorType {
        WARNING,
        ERROR,
        FATALERROR;
    }

    private Map<ErrorType, LocaleResources> translateKeys;
    private static final Translate<LocaleResources> translator = LocaleResources.createLocalizer();

    public ValidationErrorsFormatter() {

        translateKeys = new HashMap<>();
        translateKeys.put(ErrorType.ERROR, LocaleResources.VALIDATION_ERROR);
        translateKeys.put(ErrorType.WARNING, LocaleResources.VALIDATION_WARNING);
        translateKeys.put(ErrorType.FATALERROR, LocaleResources.VALIDATION_FATAL_ERROR);

    }

    public String format(List<ValidationIssue> list) {
        StringBuilder outputBuilder = new StringBuilder();
        for (ValidationIssue ave : list) {
            outputBuilder.append(formatError(ave));
        }
        return outputBuilder.toString();
    }

    private StringBuilder formatError(ValidationIssue ave) {
        StringBuilder builder = new StringBuilder();

        String LS = System.lineSeparator();
        String firstLine = null;
        String secondLine = null;
        String thirdLine = null;
        String errorLine = null;
        String pointer = "";
        String absolutePath = ave.getXmlFilePath();

        try {
            BufferedReader br = new BufferedReader(new FileReader(absolutePath));
            for (int i = 1; i < ave.getLineNumber()-3; i++) {
                br.readLine();
            }
            firstLine = br.readLine();
            secondLine = br.readLine();
            thirdLine = br.readLine();
            errorLine = br.readLine();

            for (int j = 1; j < ave.getColumnNumber()-1; j++) {
                pointer = pointer.concat(" ");
            }
            pointer = pointer.concat("^");
            br.close();
        } catch (IOException exception) {
            // if br fails to close
        }

        builder.append(translator.localize(
                translateKeys.get(ErrorType.valueOf(ave.getClass().getSimpleName().toUpperCase())),
                absolutePath,
                Integer.toString(ave.getLineNumber()),
                Integer.toString(ave.getColumnNumber())).getContents());

        builder.append(formatMessage(ave.getMessage())).append(LS).append(LS);
        builder.append(firstLine).append(LS);
        builder.append(secondLine).append(LS);
        builder.append(thirdLine).append(LS);
        builder.append(errorLine).append(LS);
        builder.append(pointer).append(LS);

        return builder;
    }

    private String formatMessage(String message) {
        String[] arguments = message.split("\"http://icedtea.classpath.org/thermostat/gc-params-mapping/v1.0\":");
        StringBuilder sb = new StringBuilder();
        for (String argument : arguments) {
            sb.append(argument);
        }
        return sb.toString();
    }

}