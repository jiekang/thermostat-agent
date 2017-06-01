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

package com.redhat.thermostat.annotations.internal;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

import org.junit.Before;
import org.junit.Test;

import com.redhat.thermostat.annotations.ExtensionPoint;
import com.redhat.thermostat.annotations.Service;

public class AnnotationProcessorTest {

	private ProcessingEnvironment processingEnv;
	private RoundEnvironment roundEnv;
	private FileObject procesorOutputFile;
	private Messager messager;
	private Elements elementUtils;

	private static final String AUTO_GENERATED_COMMENT = "<!-- autogenerated by " + AnnotationProcessor.class.getName() + " -->";

	@Before
	public void setUp() throws IOException {
		procesorOutputFile = mock(FileObject.class);
		Filer filer = mock(Filer.class);
		when(filer.createResource(
				eq(StandardLocation.CLASS_OUTPUT),
				eq(""),
				eq("META-INF/thermostat/plugin-docs.xml"),
				any(Element.class)))
			.thenReturn(procesorOutputFile);

		messager = mock(Messager.class);

		elementUtils = mock(Elements.class);

		processingEnv = mock(ProcessingEnvironment.class);
		when(processingEnv.getFiler()).thenReturn(filer);
		when(processingEnv.getMessager()).thenReturn(messager);
        when(processingEnv.getElementUtils()).thenReturn(elementUtils);

        roundEnv = mock(RoundEnvironment.class);
	}

	@Test
	public void testNoAnnotationProducesNoFile() {
		Set<? extends TypeElement> annotations = new TreeSet<>();

		AnnotationProcessor processor = new AnnotationProcessor();
		processor.init(processingEnv);
		processor.process(annotations, roundEnv);

		verifyZeroInteractions(procesorOutputFile);
	}

	@Test
    public void testProcessOnServiceClass() throws IOException {
		final String CLASS_NAME = "c.r.t.annotations.Test";
		final String JAVADOC = "some javadoc";
		final String ANNOTATION_CLASS_NAME = Service.class.getName();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        when(procesorOutputFile.openOutputStream()).thenReturn(out);

        TypeElement serviceAnnotation = mock(TypeElement.class);
        Name serviceAnnotationName = mock(Name.class);
        when(serviceAnnotationName.toString()).thenReturn(ANNOTATION_CLASS_NAME);
        when(serviceAnnotation.getSimpleName()).thenReturn(serviceAnnotationName);

        TypeElement annotatedClass = mock(TypeElement.class);
        Name annotatedClassName = mock(Name.class);
        when(annotatedClass.getQualifiedName()).thenReturn(annotatedClassName);
        when(annotatedClassName.toString()).thenReturn(CLASS_NAME);

        Set<TypeElement> annotations = new HashSet();
        annotations.add(serviceAnnotation);

        Set annotatedClasses = new HashSet();
        annotatedClasses.add(annotatedClass);

        when(roundEnv.getElementsAnnotatedWith(serviceAnnotation)).thenReturn(annotatedClasses);

        when(elementUtils.getDocComment(annotatedClass)).thenReturn(JAVADOC);

        AnnotationProcessor processor = new AnnotationProcessor();
        processor.init(processingEnv);
        processor.process(annotations, roundEnv);

        String actualFileContents = out.toString("UTF-8");
        String expectedFileContents = "<?xml version=\"1.0\"?>\n"
        		+ AUTO_GENERATED_COMMENT + "\n"
        		+ "  <plugin-docs>\n"
                + "    <service>\n"
                + "      <name>" + CLASS_NAME + "</name>\n"
                + "      <doc>\n"
                + JAVADOC + "\n"
                + "      </doc>\n"
                + "    </service>\n"
                + "  </plugin-docs>\n"
                + "";
        assertEqualsNoCR(expectedFileContents, actualFileContents);
    }

	@Test
    public void testProcessOnExtensionPointClass() throws IOException {
		final String CLASS_NAME = "c.r.t.annotations.Test";
		final String JAVADOC = "some javadoc";
		final String ANNOTATION_CLASS_NAME = ExtensionPoint.class.getName();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        when(procesorOutputFile.openOutputStream()).thenReturn(out);

        TypeElement serviceAnnotation = mock(TypeElement.class);
        Name serviceAnnotationName = mock(Name.class);
        when(serviceAnnotationName.toString()).thenReturn(ANNOTATION_CLASS_NAME);
        when(serviceAnnotation.getSimpleName()).thenReturn(serviceAnnotationName);

        TypeElement annotatedClass = mock(TypeElement.class);
        Name annotatedClassName = mock(Name.class);
        when(annotatedClass.getQualifiedName()).thenReturn(annotatedClassName);
        when(annotatedClassName.toString()).thenReturn(CLASS_NAME);

        Set<TypeElement> annotations = new HashSet();
        annotations.add(serviceAnnotation);

        Set annotatedClasses = new HashSet();
        annotatedClasses.add(annotatedClass);

        when(roundEnv.getElementsAnnotatedWith(serviceAnnotation)).thenReturn(annotatedClasses);

        when(elementUtils.getDocComment(annotatedClass)).thenReturn(JAVADOC);

        AnnotationProcessor processor = new AnnotationProcessor();
        processor.init(processingEnv);
        processor.process(annotations, roundEnv);

        String actualFileContents = out.toString("UTF-8");
        String expectedFileContents = "<?xml version=\"1.0\"?>\n"
        		+ AUTO_GENERATED_COMMENT + "\n"
        		+ "  <plugin-docs>\n"
                + "    <extension-point>\n"
                + "      <name>" + CLASS_NAME + "</name>\n"
                + "      <doc>\n"
                + JAVADOC + "\n"
                + "      </doc>\n"
                + "    </extension-point>\n"
                + "  </plugin-docs>\n"
                + "";
        assertEqualsNoCR(expectedFileContents, actualFileContents);
    }

    // copied from Asserts to avoid a circular dependency
    private static void assertEqualsNoCR(final String expected, final String actual) {
        assertEquals(expected, actual.replace("\r", ""));
    }
}

