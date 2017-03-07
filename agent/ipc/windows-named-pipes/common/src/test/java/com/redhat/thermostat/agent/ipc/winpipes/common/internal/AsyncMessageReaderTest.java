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

package com.redhat.thermostat.agent.ipc.winpipes.common.internal;

import org.junit.Before;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

public class AsyncMessageReaderTest {

    private MessageListener messageListener;
    private MessageLimits messageLimits;

    @Before
    public void setup() {
        messageListener = mock(MessageListener.class);
        messageLimits = mock(MessageLimits.class);
    }

    @Test
    public void canConstruct() {
        new AsyncMessageReader(messageListener);
        new AsyncMessageReader(messageListener, messageLimits);
    }

    @Test
    public void callsListener() {
        final AsyncMessageReader rdr = new AsyncMessageReader(messageListener);
        rdr.readFullMessage(ByteBuffer.wrap(new byte[]{0,1,2}));
        verify(messageListener).messageRead(any(ByteBuffer.class));
    }

    @Test
    public void testReadDataSingle() throws Exception {
        final byte[] messageBytes = "Hello".getBytes(Charset.forName("UTF-8"));
        final byte[] headerBytes = ChannelTestUtils.createHeader(messageBytes.length, false);

        final AsyncMessageReader rdr = new AsyncMessageReader(messageListener);

        rdr.process(ByteBuffer.wrap(headerBytes));
        verify(messageListener, never()).messageRead(any(ByteBuffer.class));

        rdr.process(ByteBuffer.wrap(messageBytes));
        final ByteBuffer expected = ByteBuffer.wrap(messageBytes);
        verify(messageListener).messageRead(expected);
    }

    @Test
    public void testReadDataSingleJoined() throws Exception {
        final byte[] messageBytes = "Hello".getBytes(Charset.forName("UTF-8"));
        final byte[] headerBytes = ChannelTestUtils.createHeader(messageBytes.length, false);

        final AsyncMessageReader rdr = new AsyncMessageReader(messageListener);

        final ByteBuffer allBytes = ByteBuffer.allocate(headerBytes.length + messageBytes.length);
        allBytes.put(headerBytes).put(messageBytes);
        allBytes.flip();
        rdr.process(allBytes);
        final ByteBuffer expected = ByteBuffer.wrap(messageBytes);
        verify(messageListener).messageRead(expected);
    }

    @Test
    public void testReadDataMulti() throws Exception {
        final byte[] fullMessageBytes = "Hello World.".getBytes(Charset.forName("UTF-8"));
        final byte[] message1Bytes = Arrays.copyOfRange(fullMessageBytes, 0, 5);
        final byte[] header1Bytes = ChannelTestUtils.createHeader(message1Bytes.length, true);
        final byte[] message2Bytes = Arrays.copyOfRange(fullMessageBytes, 5, fullMessageBytes.length);
        final byte[] header2Bytes = ChannelTestUtils.createHeader(message2Bytes.length, false);

        // First first header, then all remaining data
        final byte[] joined = ChannelTestUtils.joinByteArrays(message1Bytes, header2Bytes, message2Bytes);

        final AsyncMessageReader rdr = new AsyncMessageReader(messageListener);

        rdr.process(ByteBuffer.wrap(header1Bytes));
        verify(messageListener, never()).messageRead(any(ByteBuffer.class));
        rdr.process(ByteBuffer.wrap(joined));
        ByteBuffer expected = ByteBuffer.wrap(fullMessageBytes);
        verify(messageListener).messageRead(expected);
    }
    /**/
}
