/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.thinkaurelius.thrift.util;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TTransport;

import com.thinkaurelius.thrift.util.mem.TMemoryInputTransport;

/**
 * Thrift original TBinaryProtocol implementation relies on the fact that all
 * TTransport instances are byte array backed, so for all it's operations they can just
 * use that array, requesting it using TTransport.getBuffer().
 *
 * Disruptor implementation follows different approach, it uses direct byte buffers
 * (backed by native memory, allocated by Unsafe) which allows it to free/reallocate memory
 * when needed and doesn't bloat JVM Heap, as there is no guarantee
 * that all of the data would "die young" and put no pressure on Garbage Collector.
 */
public class TBinaryProtocol extends org.apache.thrift.protocol.TBinaryProtocol
{
    private final TMemoryInputTransport inMemoryTransport;

    @SuppressWarnings("unused")
    public TBinaryProtocol(TMemoryInputTransport trans)
    {
        this(trans, false, true);
    }

    public TBinaryProtocol(TMemoryInputTransport trans, boolean strictRead, boolean strictWrite)
    {
        super(trans, strictRead, strictWrite);
        inMemoryTransport = trans;
    }

    @SuppressWarnings("unused")
    public static class Factory extends org.apache.thrift.protocol.TBinaryProtocol.Factory
    {
        public Factory()
        {
            super(false, true);
        }

        public Factory(boolean strictRead, boolean strictWrite)
        {
            super(strictRead, strictWrite);
        }

        public TProtocol getProtocol(TTransport transport)
        {
            return (transport instanceof TMemoryInputTransport)
                     ? new TBinaryProtocol((TMemoryInputTransport) transport, strictRead_, strictWrite_)
                     : new ExtendedTBinaryProtocol(transport);
        }
    }

    public byte readByte() throws TException
    {
        return inMemoryTransport.readByte();
    }

    public short readI16() throws TException
    {
        return inMemoryTransport.readShort();
    }

    public int readI32() throws TException
    {
        return inMemoryTransport.readInt();
    }

    public long readI64() throws TException
    {
        return inMemoryTransport.readLong();
    }

    public double readDouble() throws TException
    {
        return inMemoryTransport.readDouble();
    }

    public String readString() throws TException
    {
        return readStringBody(readI32());
    }

    public String readStringBody(int size) throws TException
    {
        byte[] str = new byte[size];
        inMemoryTransport.readFully(str);

        try
        {
            return new String(str, "UTF-8");
        }
        catch (UnsupportedEncodingException uex)
        {
            throw new TException("JVM DOES NOT SUPPORT UTF-8");
        }
    }

    public ByteBuffer readBinary() throws TException
    {
        return inMemoryTransport.readBytes(readI32());
    }

    private static class ExtendedTBinaryProtocol extends org.apache.thrift.protocol.TBinaryProtocol {
        public ExtendedTBinaryProtocol(TTransport trans) {
            super(trans);
        }

        @Override
        public void writeBinary(ByteBuffer buffer) throws TException
        {
            writeI32(buffer.remaining());

            if (buffer.hasArray())
            {
                trans_.write(buffer.array(), buffer.position() + buffer.arrayOffset(), buffer.remaining());
            }
            else
            {
                byte[] bytes = new byte[buffer.remaining()];

                int j = 0;
                for (int i = buffer.position(); i < buffer.limit(); i++)
                {
                    bytes[j++] = buffer.get(i);
                }

                trans_.write(bytes);
            }
        }
    }
}
