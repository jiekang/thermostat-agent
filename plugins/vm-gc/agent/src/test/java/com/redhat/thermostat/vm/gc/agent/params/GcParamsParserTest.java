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

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class GcParamsParserTest {

    private static final String SINGLE_COLLECTOR_CONFIG = "<gc-params-mapping xmlns=\"http://icedtea.classpath.org/thermostat/gc-params-mapping/v1.0\"\n" +
            "        xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
            "        xsi:schemaLocation=\"http://icedtea.classpath.org/thermostat/plugins/v1.0 gc-params-mapping.xsd\">\n" +
            "    <common>\n" +
            "      <gc-params>\n" +
            "        <gc-param>\n" +
            "          <flag>PrintGC</flag>\n" +
            "          <description>Print messages when garbage collection takes place</description>\n" +
            "        </gc-param>\n" +
            "        <gc-param>\n" +
            "          <flag>PrintGCDetails</flag>\n" +
            "          <description>Print more details in garbage collection messages</description>\n" +
            "        </gc-param>\n" +
            "      </gc-params>\n" +
            "    </common>" +
            "    <collector>\n" +
            "      <collector-info>\n" +
            "        <version>[1.0.0.0,1.8.0.45]</version>\n" +
            "        <common-name>Garbage-First Collector (G1)</common-name>\n" +
            "        <collector-distinct-names>\n" +
            "          <collector-name>G1 incremental collections</collector-name>\n" +
            "        </collector-distinct-names>\n" +
            "        <url>http://www.oracle.com/technetwork/java/javase/gc-tuning-6-140523.html#available_collectors</url>\n" +
            "      </collector-info>\n" +
            "      <gc-params>\n" +
            "        <gc-param>\n" +
            "          <flag>G1ConcMarkStepDurationMillis</flag>\n" +
            "          <description>Description Text</description>\n" +
            "          <version>[1.5.0.31,1.8.0.45]</version>" +
            "        </gc-param>\n" +
            "        <gc-param>\n" +
            "          <flag>G1ConcRSHotCardLimit</flag>\n" +
            "          <description></description>\n" +
            "        </gc-param>\n" +
            "        <gc-param>\n" +
            "          <flag>G1ConcRSLogCacheSize</flag>\n" +
            "          <description></description>\n" +
            "        </gc-param>\n" +
            "        <gc-param>\n" +
            "          <flag>G1ConcRefinementGreenZone</flag>\n" +
            "          <description></description>\n" +
            "        </gc-param>\n" +
            "        <gc-param>\n" +
            "          <flag>G1ConcRefinementRedZone</flag>\n" +
            "          <description></description>\n" +
            "        </gc-param>\n" +
            "        <gc-param>\n" +
            "          <flag>G1ConcRefinementServiceIntervalMillis</flag>\n" +
            "          <description></description>\n" +
            "        </gc-param>\n" +
            "        <gc-param>\n" +
            "          <flag>G1ConcRefinementThreads</flag>\n" +
            "          <description></description>\n" +
            "        </gc-param>\n" +
            "        <gc-param>\n" +
            "          <flag>G1ConcRefinementThresholdStep</flag>\n" +
            "          <description></description>\n" +
            "        </gc-param>\n" +
            "        <gc-param>\n" +
            "          <flag>G1ConcRefinementYellowZone</flag>\n" +
            "          <description></description>\n" +
            "        </gc-param>\n" +
            "        <gc-param>\n" +
            "          <flag>G1ConfidencePercent</flag>\n" +
            "          <description></description>\n" +
            "        </gc-param>\n" +
            "        <gc-param>\n" +
            "          <flag>G1HeapRegionSize</flag>\n" +
            "          <description></description>\n" +
            "        </gc-param>\n" +
            "        <gc-param>\n" +
            "          <flag>G1HeapWastePercent</flag>\n" +
            "          <description></description>\n" +
            "        </gc-param>\n" +
            "        <gc-param>\n" +
            "          <flag>G1MixedGCCountTarget</flag>\n" +
            "          <description></description>\n" +
            "        </gc-param>\n" +
            "        <gc-param>\n" +
            "          <flag>G1RSetRegionEntries</flag>\n" +
            "          <description></description>\n" +
            "        </gc-param>\n" +
            "        <gc-param>\n" +
            "          <flag>G1RSetScanBlockSize</flag>\n" +
            "          <description></description>\n" +
            "        </gc-param>\n" +
            "        <gc-param>\n" +
            "          <flag>G1RSetSparseRegionEntries</flag>\n" +
            "          <description></description>\n" +
            "        </gc-param>\n" +
            "        <gc-param>\n" +
            "          <flag>G1RSetUpdatingPauseTimePercent</flag>\n" +
            "          <description></description>\n" +
            "        </gc-param>\n" +
            "        <gc-param>\n" +
            "          <flag>G1RefProcDrainInterval</flag>\n" +
            "          <description></description>\n" +
            "        </gc-param>\n" +
            "        <gc-param>\n" +
            "          <flag>G1ReservePercent</flag>\n" +
            "          <description></description>\n" +
            "        </gc-param>\n" +
            "        <gc-param>\n" +
            "          <flag>G1SATBBufferEnqueueingThresholdPercent</flag>\n" +
            "          <description></description>\n" +
            "        </gc-param>\n" +
            "        <gc-param>\n" +
            "          <flag>G1SATBBufferSize</flag>\n" +
            "          <description></description>\n" +
            "        </gc-param>\n" +
            "        <gc-param>\n" +
            "          <flag>G1UpdateBufferSize</flag>\n" +
            "          <description></description>\n" +
            "        </gc-param>\n" +
            "        <gc-param>\n" +
            "          <flag>G1UseAdaptiveConcRefinement</flag>\n" +
            "          <description></description>\n" +
            "        </gc-param>\n" +
            "        <gc-param>\n" +
            "          <flag>UseG1GC</flag>\n" +
            "          <description></description>\n" +
            "        </gc-param>\n" +
            "      </gc-params>\n" +
            "    </collector>\n" +
            "</gc-params-mapping>";

    private static final String TWO_COLLECTOR_CONFIG = "<gc-params-mapping xmlns=\"http://icedtea.classpath.org/thermostat/gc-params-mapping/v1.0\"\n" +
            "        xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
            "        xsi:schemaLocation=\"http://icedtea.classpath.org/thermostat/plugins/v1.0 gc-params-mapping.xsd\">\n" +
            "    <common>\n" +
            "      <gc-params>\n" +
            "        <gc-param>\n" +
            "          <flag>PrintGC</flag>\n" +
            "          <description>Print messages when garbage collection takes place</description>\n" +
            "        </gc-param>\n" +
            "        <gc-param>\n" +
            "          <flag>PrintGCDetails</flag>\n" +
            "          <description>Print more details in garbage collection messages</description>\n" +
            "        </gc-param>\n" +
            "      </gc-params>\n" +
            "    </common>" +
            "    <collector>\n" +
            "      <collector-info>\n" +
            "        <version>[1.0.0.0,1.8.0.45]</version>\n" +
            "        <common-name>Garbage-First Collector (G1)</common-name>\n" +
            "        <collector-distinct-names>\n" +
            "          <collector-name>G1 incremental collections</collector-name>\n" +
            "        </collector-distinct-names>\n" +
            "        <url>http://www.oracle.com/technetwork/java/javase/gc-tuning-6-140523.html#available_collectors</url>\n" +
            "      </collector-info>\n" +
            "      <gc-params>\n" +
            "        <gc-param>\n" +
            "          <flag>G1ConcMarkStepDurationMillis</flag>\n" +
            "          <description>Description Text</description>\n" +
            "          <version>[1.5.0.31,1.8.0.45]</version>" +
            "        </gc-param>\n" +
            "      </gc-params>\n" +
            "    </collector>\n" +
            "    <collector>\n" +
            "      <collector-info>\n" +
            "        <version>[1.5.0.31,1.8.0.45]</version>\n" +
            "        <common-name>Parallel</common-name>\n" +
            "        <collector-distinct-names>\n" +
            "          <collector-name>PSParallelCompact</collector-name>\n" +
            "          <collector-name>PSScavenge</collector-name>\n" +
            "        </collector-distinct-names>\n" +
            "        <url>http://www.oracle.com/technetwork/java/javase/gc-tuning-6-140523.html#available_collectors</url>\n" +
            "      </collector-info>\n" +
            "      <gc-params>\n" +
            "        <gc-param>\n" +
            "          <flag>UseParallelGC</flag>\n" +
            "          <description>Parallel Description Text</description>\n" +
            "          <version>[1.7.0.41,1.8.0.45]</version>" +
            "        </gc-param>\n" +
            "      </gc-params>\n" +
            "    </collector>\n" +
            "</gc-params-mapping>";

    private static final JavaVersionRange G1_VERSION = new JavaVersionRange(new JavaVersionRange.VersionPoints(1, 0, 0, 0), true, new JavaVersionRange.VersionPoints(1, 8, 0, 45), true);
    private static final String G1_COMMON_NAME = "Garbage-First Collector (G1)";
    private static final Set<String> G1_DISTINCT_NAMES = Collections.singleton("G1 incremental collections");
    private static final String G1_REFERENCE_URL = "http://www.oracle.com/technetwork/java/javase/gc-tuning-6-140523.html#available_collectors";

    private static final String PARALLEL_COMMON_NAME = "Parallel";

    private static final String MARK_STEP_DURATION_MILLIS_FLAG = "G1ConcMarkStepDurationMillis";
    private static final String MARK_STEP_DURATION_MILLIS_DESCRIPTION = "Description Text";
    private static final JavaVersionRange MARK_STEP_DURATION_MILLIS_VERSION = new JavaVersionRange(new JavaVersionRange.VersionPoints(1, 5, 0, 31), true, new JavaVersionRange.VersionPoints(1, 8, 0, 45), true);
    private static final Set<String> PARALLEL_DISTINCT_NAMES = new HashSet<>(Arrays.asList("PSParallelCompact", "PSScavenge"));
    private static final JavaVersionRange ALL_JAVA_VERSIONS_RANGE = new JavaVersionRange(JavaVersionRange.VersionPoints.MINIMUM_VERSION, true, JavaVersionRange.VersionPoints.MAXIMUM_VERSION, true);
    private static final GcParam PRINT_GC_COMMON_PARAM = new GcParam("PrintGC", "Print messages when garbage collection takes place", ALL_JAVA_VERSIONS_RANGE);
    private static final GcParam PRINT_GC_DETAILS_COMMON_PARAM = new GcParam("PrintGCDetails", "Print more details in garbage collection messages", ALL_JAVA_VERSIONS_RANGE);
    private static final Set<GcParam> COMMON_PARAMS = new HashSet<>(Arrays.asList(PRINT_GC_COMMON_PARAM, PRINT_GC_DETAILS_COMMON_PARAM));

    @Test(expected = GcParamsParser.GcParamsParseException.class)
    public void testEmptyConfigurationThrowsException() throws UnsupportedEncodingException {
        String config = "<?xml version=\"1.0\"?>\n";
        GcParamsParser.parse(new ByteArrayInputStream(config.getBytes("UTF-8")));
        fail("should not reach here");
    }

    @Test
    public void testMinimalConfigurationParsesOneCollector() throws UnsupportedEncodingException {
        GcParamsParser.ParseResult result = getSingleCollectorResult();
        assertEquals(1, result.getCollectors().size());
    }

    @Test
    public void testMinimalConfigurationParsesCorrectCommonParams() throws UnsupportedEncodingException {
        GcParamsParser.ParseResult result = getSingleCollectorResult();
        assertNonEmptyResult(result.getGcCommonParams());
        assertEquals(result.getGcCommonParams(), COMMON_PARAMS);
    }

    @Test
    public void testMinimalConfigurationParsesCorrectCollectorName() throws UnsupportedEncodingException {
        GcParamsParser.ParseResult result = getSingleCollectorResult();
        assertNonEmptyResult(result.getCollectors());
        assertEquals(result.getCollectors().get(0).getCollectorInfo().getCommonName(), G1_COMMON_NAME);
    }

    @Test
    public void testMinimalConfigurationParsesCorrectCollectorDistinctNames() throws UnsupportedEncodingException {
        GcParamsParser.ParseResult result = getSingleCollectorResult();
        assertNonEmptyResult(result.getCollectors());
        assertEquals(result.getCollectors().get(0).getCollectorInfo().getCollectorDistinctNames(), G1_DISTINCT_NAMES);
    }

    @Test
    public void testMinimalConfigurationParsesCorrectCollectorVersion() throws UnsupportedEncodingException {
        GcParamsParser.ParseResult result = getSingleCollectorResult();
        assertNonEmptyResult(result.getCollectors());
        assertEquals(result.getCollectors().get(0).getCollectorInfo().getJavaVersionRange(), G1_VERSION);
    }

    @Test
    public void testMinimalConfigurationParsesCorrectReferenceUrl() throws UnsupportedEncodingException {
        GcParamsParser.ParseResult result = getSingleCollectorResult();
        assertNonEmptyResult(result.getCollectors());
        assertEquals(result.getCollectors().get(0).getCollectorInfo().getReferenceUrl(), G1_REFERENCE_URL);
    }

    @Test
    public void testMinimalConfigurationParsesGcParams() throws UnsupportedEncodingException {
        GcParamsParser.ParseResult result = getSingleCollectorResult();
        assertNonEmptyResult(result.getCollectors());
        assertEquals(result.getCollectors().get(0).getGcParams().size(), 24);
    }

    @Test
    public void testMinimalConfigurationParsesGcParamsVersions() throws UnsupportedEncodingException {
        GcParamsParser.ParseResult result = getSingleCollectorResult();
        assertNonEmptyResult(result.getCollectors());
        for (GcParam param : result.getCollectors().get(0).getGcParams()) {
            if (param.getFlag().equals(MARK_STEP_DURATION_MILLIS_FLAG)) {
                assertEquals(param.getJavaVersionRange(), MARK_STEP_DURATION_MILLIS_VERSION);
            } else {
                assertEquals(param.getJavaVersionRange(), G1_VERSION);
            }
        }
    }

    @Test
    public void testMinimalConfigurationParsesGcParamsDescriptions() throws UnsupportedEncodingException {
        GcParamsParser.ParseResult result = getSingleCollectorResult();
        assertNonEmptyResult(result.getCollectors());
        for (GcParam param : result.getCollectors().get(0).getGcParams()) {
            if (param.getFlag().equals(MARK_STEP_DURATION_MILLIS_FLAG)) {
                assertEquals(param.getDescription(), MARK_STEP_DURATION_MILLIS_DESCRIPTION);
            } else {
                assertTrue(param.getDescription(), param.getDescription().isEmpty());
            }
        }
    }

    @Test
    public void testTwoCollectorConfigurationParsesCommonParams() throws UnsupportedEncodingException {
        GcParamsParser.ParseResult parseResult = getTwoCollectorResult();
        assertNonEmptyResult(parseResult.getGcCommonParams());
        assertEquals(parseResult.getGcCommonParams(), COMMON_PARAMS);
    }

    @Test
    public void testTwoCollectorConfigurationParsesCollectors() throws UnsupportedEncodingException {
        GcParamsParser.ParseResult result = getTwoCollectorResult();
        assertNonEmptyResult(result.getCollectors());
        assertEquals(result.getCollectors().size(), 2);
    }

    @Test
    public void testTwoCollectorConfigurationParsesCollectorCommonNames() throws UnsupportedEncodingException {
        GcParamsParser.ParseResult result = getTwoCollectorResult();
        assertNonEmptyResult(result.getCollectors());
        assertEquals(result.getCollectors().size(), 2);
        Set<String> commonNames = new HashSet<>(Arrays.asList(result.getCollectors().get(0).getCollectorInfo().getCommonName(),
                result.getCollectors().get(1).getCollectorInfo().getCommonName()));
        assertEquals(commonNames, new HashSet<>(Arrays.asList(G1_COMMON_NAME, PARALLEL_COMMON_NAME)));
    }

    @Test
    public void testTwoCollectorConfigurationParsesCollectorDistinctNames() throws UnsupportedEncodingException {
        GcParamsParser.ParseResult result = getTwoCollectorResult();
        assertNonEmptyResult(result.getCollectors());
        assertEquals(result.getCollectors().size(), 2);
        for (Collector collector : result.getCollectors()) {
            if (collector.getCollectorInfo().getCommonName().equals(G1_COMMON_NAME)) {
                assertEquals(G1_DISTINCT_NAMES, collector.getCollectorInfo().getCollectorDistinctNames());
            } else if (collector.getCollectorInfo().getCommonName().equals(PARALLEL_COMMON_NAME)) {
                assertEquals(PARALLEL_DISTINCT_NAMES, collector.getCollectorInfo().getCollectorDistinctNames());
            } else {
                fail("no other collector should have been parsed");
            }
        }
    }

    private void assertNonEmptyResult(List<Collector> result) {
        failIfEmpty(result, "collectors");
    }

    private void assertNonEmptyResult(Set<GcParam> result) {
        failIfEmpty(result, "common params");
    }

    private void failIfEmpty(Collection<?> result, String part) {
        if (result.isEmpty()) {
            fail(part + " should not be empty");
        }
    }

    private GcParamsParser.ParseResult getSingleCollectorResult() throws UnsupportedEncodingException {
        return parseHelper(SINGLE_COLLECTOR_CONFIG);
    }

    private GcParamsParser.ParseResult getTwoCollectorResult() throws UnsupportedEncodingException {
        return parseHelper(TWO_COLLECTOR_CONFIG);
    }

    private GcParamsParser.ParseResult parseHelper(String config) throws UnsupportedEncodingException {
        return GcParamsParser.parse(new ByteArrayInputStream(config.getBytes("UTF-8")));
    }

}